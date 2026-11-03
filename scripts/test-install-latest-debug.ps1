# Mocked smoke tests for the installer. No GitHub requests or ADB installation occur.
$ErrorActionPreference = 'Stop'
$installer = Join-Path $PSScriptRoot 'install-latest-debug.ps1'
$global:CurrentSha = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'
$global:OtherSha = 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb'
$global:RunSha = $global:CurrentSha
$global:RunConclusion = 'success'
$global:RunStatus = 'completed'
$global:ArtifactAvailable = $true
$global:IncludeCurrentRun = $true
$global:AdvanceBranchOnView = $false
$global:Installed = $false
$global:Watched = $false

function global:gh {
    $global:LASTEXITCODE = 0
    if ($args[0] -eq 'auth') { return }
    if ($args[0] -eq 'api') {
        if ($args[1] -like '*git/ref/heads/*') { return $global:CurrentSha }
        if ($args[1] -like '*actions/runs/*/artifacts*') {
            if ($global:ArtifactAvailable) {
                return '{"artifacts":[{"name":"slovami-ai-debug-apk","expired":false,"size_in_bytes":1234}]}'
            }
            return '{"artifacts":[]}'
        }
    }
    if ($args[0] -eq 'run') {
        switch ($args[1]) {
            'list' {
                $runs = @(
                    @{ databaseId = 100; headSha = $global:OtherSha; status = 'completed'; conclusion = 'success'; event = 'pull_request'; createdAt = '2026-11-03T10:00:00Z' }
                )
                if ($global:IncludeCurrentRun) {
                    $runs += @{
                        databaseId = 101; headSha = $global:RunSha
                        status = $global:RunStatus; conclusion = $global:RunConclusion
                        event = 'pull_request'; createdAt = '2026-11-03T11:00:00Z'
                    }
                }
                return ConvertTo-Json -InputObject $runs -Compress -Depth 5
            }
            'watch' { $global:Watched = $true; return }
            'view' {
                if ($global:AdvanceBranchOnView) { $global:CurrentSha = $global:OtherSha }
                return (ConvertTo-Json -InputObject @{
                    databaseId = 101; headSha = $global:RunSha
                    status = 'completed'; conclusion = $global:RunConclusion; event = 'pull_request'
                } -Compress)
            }
            'download' {
                $index = [array]::IndexOf([object[]]$args, '--dir')
                if ($index -lt 0) { throw 'Missing download directory' }
                Set-Content -LiteralPath (Join-Path $args[$index + 1] 'mock.apk') -Value 'mock'
                return
            }
        }
    }
    throw "Unexpected gh command: $($args -join ' ')"
}

function global:adb {
    $global:LASTEXITCODE = 0
    if ($args[0] -eq 'devices') {
        return @('List of devices attached', 'mock-device    device')
    }
    if ($args[0] -eq 'install' -and $args[1] -eq '-r') {
        $global:Installed = $true
        return 'Success'
    }
    throw "Unexpected adb command: $($args -join ' ')"
}

function Reset-Mocks {
    $global:CurrentSha = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'
    $global:RunSha = $global:CurrentSha
    $global:RunConclusion = 'success'
    $global:RunStatus = 'completed'
    $global:ArtifactAvailable = $true
    $global:IncludeCurrentRun = $true
    $global:AdvanceBranchOnView = $false
    $global:Installed = $false
    $global:Watched = $false
}

function Assert-Throws {
    param([string]$ExpectedMessage)
    $caught = $null
    try {
        & $installer -Branch 'feat/test' -RunDiscoveryTimeoutSeconds 0 *> $null
    }
    catch { $caught = $_ }
    if ($null -eq $caught) { throw "Expected error containing: $ExpectedMessage" }
    if ($caught.Exception.Message -notlike "*$ExpectedMessage*") {
        throw "Expected '$ExpectedMessage', got: $($caught.Exception.Message)"
    }
    if ($global:Installed) { throw 'Installer reached ADB despite failed validation.' }
}

try {
    Reset-Mocks
    & $installer -Branch 'feat/test' -RunDiscoveryTimeoutSeconds 0 | Out-Null
    if (-not $global:Installed) { throw 'Current successful run was not installed.' }

    Reset-Mocks
    $global:RunStatus = 'in_progress'
    & $installer -Branch 'feat/test' -RunDiscoveryTimeoutSeconds 0 | Out-Null
    if (-not $global:Watched -or -not $global:Installed) { throw 'Running workflow was not watched and installed.' }

    Reset-Mocks
    $global:IncludeCurrentRun = $false
    Assert-Throws -ExpectedMessage 'No Android checks found for HEAD'

    Reset-Mocks
    $global:RunConclusion = 'failure'
    Assert-Throws -ExpectedMessage 'Android checks are not successful'

    Reset-Mocks
    $global:ArtifactAvailable = $false
    Assert-Throws -ExpectedMessage 'Expected one available'

    Reset-Mocks
    $global:AdvanceBranchOnView = $true
    Assert-Throws -ExpectedMessage 'advanced during the build'

    Write-Host 'Installer mock tests passed.'
}
finally {
    Remove-Item Function:\gh -ErrorAction SilentlyContinue
    Remove-Item Function:\adb -ErrorAction SilentlyContinue
}
