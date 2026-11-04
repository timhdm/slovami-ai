# Build and install the CURRENT LOCAL branch. No GitHub Actions artifact is used.
# The existing filename is retained for compatibility with project instructions.
param(
    [Parameter(Mandatory = $true)]
    [string]$Branch,
    [switch]$RunTests,
    [switch]$BuildOnly
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$sdk = 'E:\Projects\AI\slovami-ai\android-sdk'
$adb = Join-Path $sdk 'platform-tools\adb.exe'
$aar = Join-Path $root 'app\libs\sherpa-onnx.aar'
$aarSha256 = '1cae99f3330e8da180cbf9d2e20c5257d4444d8713c4df4094a9471a2c11300b'
$aarUrl = 'https://huggingface.co/csukuangfj2/sherpa-onnx-libs/resolve/main/android/aar/1.13.1/sherpa-onnx-static-link-onnxruntime-1.13.1.aar'

function Invoke-Checked([string]$Executable, [string[]]$Arguments) {
    & $Executable @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Command failed ($LASTEXITCODE): $Executable $($Arguments -join ' ')" }
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) { throw 'git is not installed.' }
if (-not (Test-Path $adb)) { throw "ADB not found: $adb" }
if (-not (Test-Path (Join-Path $root 'gradlew.bat'))) { throw 'Gradle wrapper not found.' }
if (-not (Test-Path $sdk)) { throw "Android SDK not found: $sdk" }

Push-Location $root
try {
    $current = (& git branch --show-current).Trim()
    if ($LASTEXITCODE -ne 0 -or $current -ne $Branch) {
        throw "Local branch '$current' does not match requested '$Branch'. Switch branches manually before running."
    }
    $changes = @(& git status --porcelain)
    if ($LASTEXITCODE -ne 0) { throw 'Cannot read git status.' }
    if ($changes.Count -gt 0) { throw 'Working tree is not clean. Commit or stash changes before building.' }
    $sha = (& git rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0) { throw 'Cannot determine local commit SHA.' }
    Write-Host "Building local branch $Branch at $sha"

    if (-not (Test-Path $aar) -or (Get-FileHash $aar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $aarSha256) {
        New-Item -ItemType Directory -Force -Path (Split-Path $aar) | Out-Null
        Write-Host 'Downloading verified sherpa-onnx dependency...'
        $tmp = "$aar.download"
        try {
            Invoke-WebRequest -Uri $aarUrl -OutFile $tmp -MaximumRedirection 10
            if ((Get-FileHash $tmp -Algorithm SHA256).Hash.ToLowerInvariant() -ne $aarSha256) {
                throw 'sherpa-onnx AAR checksum mismatch.'
            }
            Move-Item -Force $tmp $aar
        } finally {
            Remove-Item $tmp -Force -ErrorAction SilentlyContinue
        }
    }
    if ((Get-FileHash $aar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $aarSha256) {
        throw 'sherpa-onnx AAR checksum mismatch.'
    }

    foreach ($name in @('DEBUG_KEYSTORE_PATH','DEBUG_KEYSTORE_PASSWORD','DEBUG_KEY_PASSWORD')) {
        if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) {
            throw "Missing $name. Configure the SAME debug signing key as CI to preserve installed app data."
        }
    }
    if (-not (Test-Path $env:DEBUG_KEYSTORE_PATH)) { throw 'DEBUG_KEYSTORE_PATH does not exist.' }

    $env:ANDROID_HOME = $sdk
    $env:ANDROID_SDK_ROOT = $sdk
    $tasks = if ($RunTests) { @('assembleDebug','testDebugUnitTest') } else { @('assembleDebug') }
    Invoke-Checked (Join-Path $root 'gradlew.bat') $tasks
    $apk = Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
    if (-not (Test-Path $apk)) { throw "APK not found: $apk" }
    Write-Host "Local APK ready: $apk"
    if (-not $BuildOnly) {
        $lines = @(& $adb devices)
        if ($LASTEXITCODE -ne 0) { throw 'ADB devices failed.' }
        $devices = @($lines | Where-Object { $_ -match '^\S+\s+device\s*$' })
        if ($devices.Count -ne 1) { throw "Expected exactly one authorized Android device; found $($devices.Count)." }
        Invoke-Checked $adb @('install','-r',$apk)
        Write-Host "Installed $Branch ($sha) without clearing application data."
    }
} finally {
    Pop-Location
}
