$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

$packageName = "com.jitou.app"

function Get-AndroidSdkPath {
    if ($env:ANDROID_HOME) {
        return $env:ANDROID_HOME
    }

    if ($env:ANDROID_SDK_ROOT) {
        return $env:ANDROID_SDK_ROOT
    }

    $localProperties = Join-Path $PSScriptRoot "local.properties"
    if (Test-Path $localProperties) {
        $sdkLine = Get-Content $localProperties |
            Where-Object { $_ -match "^sdk\.dir=" } |
            Select-Object -First 1

        if ($sdkLine) {
            return $sdkLine.Substring("sdk.dir=".Length).Replace("\:", ":").Replace("\\", "\")
        }
    }

    return $null
}

function Get-AdbPath {
    $sdkPath = Get-AndroidSdkPath
    if ($sdkPath) {
        $adbFromSdk = Join-Path $sdkPath "platform-tools\adb.exe"
        if (Test-Path $adbFromSdk) {
            return $adbFromSdk
        }
    }

    $adbCommand = Get-Command adb -ErrorAction SilentlyContinue
    if ($adbCommand) {
        return $adbCommand.Source
    }

    return $null
}

$adb = Get-AdbPath
if (-not $adb) {
    throw "adb was not found. Install Android SDK Platform Tools or set ANDROID_HOME / ANDROID_SDK_ROOT."
}

Write-Host "Checking connected Android devices..."
$devices = & $adb devices |
    Select-Object -Skip 1 |
    Where-Object { $_ -match "\sdevice$" }

if (-not $devices) {
    throw "No Android device or emulator is connected. Start an emulator or enable USB debugging on a phone, then run this again."
}

Write-Host "Building and installing debug APK..."
& (Join-Path $PSScriptRoot "gradlew.bat") :app:installDebug
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Launching $packageName..."
& $adb shell monkey -p $packageName -c android.intent.category.LAUNCHER 1 | Out-Host
if ($LASTEXITCODE -ne 0) {
    Write-Warning "The app was installed, but automatic launch failed. You can open it from the launcher."
}

Write-Host "Deploy complete."
