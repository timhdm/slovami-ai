# Installs the signed debug APK built for the current HEAD of the requested branch.
# Requirements: GitHub CLI (gh), Android platform-tools (adb), one authorized device.
param(
    [ValidateNotNullOrEmpty()]
    [string]$Repository = 'timhdm/slovami-ai',

    [ValidateNotNullOrEmpty()]
    [string]$Branch = 'main',

    [ValidateRange(0, 600)]
    [int]$RunDiscoveryTimeoutSeconds = 120
)

$ErrorActionPreference = 'Stop'

function Get-BranchSha {
    $response = & gh api "repos/$Repository/git/ref/heads/$Branch" --jq '.object.sha'
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot read HEAD for branch '$Branch' in $Repository."
    }

    $sha = ($response | Out-String).Trim()
    if ($sha -notmatch '^[0-9a-fA-F]{40}$') {
        throw "GitHub returned an invalid HEAD SHA for branch '$Branch': $sha"
    }
    return $sha
}

function Get-BranchRuns {
    $response = & gh run list --repo $Repository --workflow android.yml --branch $Branch --limit 100 --json databaseId,createdAt,status,conclusion,event,headSha
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot list Android checks for branch '$Branch'."
    }
    return ConvertFrom-Json -InputObject ($response | Out-String)
}

function Get-RunDetails {
    param([long]$RunId)

    $response = & gh run view $RunId --repo $Repository --json databaseId,status,conclusion,headSha,event
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot inspect workflow run $RunId."
    }
    return ConvertFrom-Json -InputObject ($response | Out-String)
}

foreach ($tool in @('gh', 'adb')) {
    if (-not (Get-Command $tool -ErrorAction SilentlyContinue)) {
        throw "Missing command: $tool. Install it and restart PowerShell."
    }
}

& gh auth status 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw 'GitHub CLI is not authenticated. Run: gh auth login'
}

$deadline = (Get-Date).AddSeconds($RunDiscoveryTimeoutSeconds)
$run = $null
do {
    $expectedSha = Get-BranchSha
    $runs = @(Get-BranchRuns)
    $run = $runs |
        Where-Object {
            $_.headSha -eq $expectedSha -and
            $_.event -in @('push', 'workflow_dispatch', 'pull_request')
        } |
        Sort-Object createdAt -Descending |
        Select-Object -First 1

    if ($null -ne $run) { break }

    if ((Get-Date) -ge $deadline) {
        throw "No Android checks found for HEAD $expectedSha on branch '$Branch'. Open a PR or dispatch android.yml, then retry."
    }

    Write-Host "Waiting for Android checks to start for $expectedSha..."
    Start-Sleep -Seconds 5
} while ($true)

$runId = [long]$run.databaseId
$runUrl = "https://github.com/$Repository/actions/runs/$runId"
Write-Host "Branch: $Branch"
Write-Host "Commit: $expectedSha"
Write-Host "Workflow: $runUrl"

if ($run.status -ne 'completed') {
    Write-Host 'Waiting for GitHub Actions...'
    & gh run watch $runId --repo $Repository --exit-status
    if ($LASTEXITCODE -ne 0) {
        throw "Android checks failed. See: $runUrl"
    }
}

$verifiedRun = Get-RunDetails -RunId $runId
if ($verifiedRun.headSha -ne $expectedSha) {
    throw "Workflow SHA mismatch. Expected $expectedSha, got $($verifiedRun.headSha)."
}
if ($verifiedRun.status -ne 'completed' -or $verifiedRun.conclusion -ne 'success') {
    throw "Android checks are not successful ($($verifiedRun.status)/$($verifiedRun.conclusion)). See: $runUrl"
}

# The branch may have advanced while the workflow was running.
if ((Get-BranchSha) -ne $expectedSha) {
    throw "Branch '$Branch' advanced during the build. Rerun the installer for the new HEAD."
}

$artifactResponse = & gh api "repos/$Repository/actions/runs/$runId/artifacts?per_page=100"
if ($LASTEXITCODE -ne 0) {
    throw "Cannot inspect artifacts for $runUrl."
}
$artifactData = ConvertFrom-Json -InputObject ($artifactResponse | Out-String)
$artifacts = @($artifactData.artifacts | Where-Object {
    $_.name -eq 'slovami-ai-debug-apk' -and
    $_.expired -eq $false -and
    $_.size_in_bytes -gt 0
})
if ($artifacts.Count -ne 1) {
    throw "Expected one available slovami-ai-debug-apk artifact for $runUrl, found $($artifacts.Count)."
}

$deviceOutput = & adb devices
if ($LASTEXITCODE -ne 0) { throw 'Could not list ADB devices.' }
$devices = @($deviceOutput | Select-Object -Skip 1 | Where-Object { $_ -match '^\S+\s+device\s*$' })
if ($devices.Count -ne 1) {
    throw "Expected exactly one authorized ADB device, found $($devices.Count). Check: adb devices"
}

$dir = Join-Path ([System.IO.Path]::GetTempPath()) ("slovami-ai-" + $runId + "-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Path $dir -Force | Out-Null

try {
    Write-Host 'Downloading verified debug APK...'
    & gh run download $runId --repo $Repository --name slovami-ai-debug-apk --dir $dir
    if ($LASTEXITCODE -ne 0) { throw "Could not download APK from $runUrl." }

    $apks = @(Get-ChildItem -LiteralPath $dir -Filter '*.apk' -File -Recurse)
    if ($apks.Count -ne 1) {
        throw "Expected exactly one APK in the downloaded artifact, found $($apks.Count)."
    }

    # Do not install if another commit was pushed while downloading.
    if ((Get-BranchSha) -ne $expectedSha) {
        throw "Branch '$Branch' advanced during the download. Rerun the installer."
    }

    Write-Host "Installing $($apks[0].Name) (commit $expectedSha)..."
    & adb install -r $apks[0].FullName
    if ($LASTEXITCODE -ne 0) {
        throw 'ADB installation failed. Check app signing key and device storage.'
    }

    Write-Host "APK installed successfully from $runUrl"
}
finally {
    Remove-Item -LiteralPath $dir -Recurse -Force -ErrorAction SilentlyContinue
}
