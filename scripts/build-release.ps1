# ==============================================================================
# openStream — Local Release Build & Signing Script
# ==============================================================================
# Automates the clean compilation, release signing, signature verification,
# cryptographic SHA-256 hashing, and manifest preparation for OTA updates.
# ==============================================================================

param(
    [int]$TargetVersionCode = 3,
    [string]$TargetVersionName = "1.2.0",
    [switch]$SkipTests = $false
)

$ErrorActionPreference = "Stop"
$root = (Get-Item $PSScriptRoot).Parent.FullName

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " openStream — Release Build & Signing Pipeline" -ForegroundColor Cyan
Write-Host " Target Version: $TargetVersionName (versionCode: $TargetVersionCode)" -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Environment & Java Check
if (-not $env:JAVA_HOME) {
    if (Test-Path "C:\Android\jdk-17.0.21+9") {
        $env:JAVA_HOME = "C:\Android\jdk-17.0.21+9"
    } else {
        Write-Error "JAVA_HOME environment variable is not defined!"
    }
}
if (-not $env:ANDROID_HOME) {
    if (Test-Path "C:\Android\sdk") {
        $env:ANDROID_HOME = "C:\Android\sdk"
    } else {
        Write-Error "ANDROID_HOME environment variable is not defined!"
    }
}

# 2. Check Keystore Configuration
$keystoreFile = Join-Path $root "openstream-release.jks"
$keystoreProps = Join-Path $root "keystore.properties"

if (-not (Test-Path $keystoreFile) -and -not (Test-Path $keystoreProps) -and -not $env:OPENSTREAM_KEYSTORE_FILE) {
    Write-Error "Release keystore not found! Ensure openstream-release.jks or keystore.properties exists."
}

# 3. Execute Quality Gate / Tests
if (-not $SkipTests) {
    Write-Host "`n[1/5] Executing Complete Test Suite (Quality Gate)..." -ForegroundColor Cyan
    & "$root\gradlew.bat" test
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Unit tests failed! Aborting release packaging."
    }
    Write-Host "Unit tests passed cleanly." -ForegroundColor Green
} else {
    Write-Host "`n[1/5] Skipping Unit Tests (SkipTests flag specified)..." -ForegroundColor DarkGray
}

# 4. Assemble Signed Release APK
Write-Host "`n[2/5] Compiling and Packaging Signed Release APK..." -ForegroundColor Cyan
& "$root\gradlew.bat" assembleRelease
if ($LASTEXITCODE -ne 0) {
    Write-Error "assembleRelease failed! Aborting release."
}

$apkPath = Join-Path $root "app\build\outputs\apk\release\app-release.apk"
if (-not (Test-Path $apkPath)) {
    Write-Error "Signed release APK was not found at $apkPath"
}

# 5. Verify Signature with apksigner
Write-Host "`n[3/5] Verifying Signature Integrity with apksigner..." -ForegroundColor Cyan
$apksigner = Join-Path $env:ANDROID_HOME "build-tools\34.0.0\apksigner.bat"
& $apksigner verify --verbose --print-certs $apkPath
if ($LASTEXITCODE -ne 0) {
    Write-Error "APK signature verification failed!"
}
Write-Host "Signature verified successfully." -ForegroundColor Green

# 6. Compute Cryptographic SHA-256
Write-Host "`n[4/5] Computing SHA-256 Digest..." -ForegroundColor Cyan
$hashResult = Get-FileHash -Algorithm SHA256 $apkPath
$sha256 = $hashResult.Hash.ToLower()
Write-Host "SHA-256: $sha256" -ForegroundColor Green

# 7. Update update-server Artifacts
Write-Host "`n[5/5] Updating Local Manifest (update-server/update.json)..." -ForegroundColor Cyan
$serverDir = Join-Path $root "update-server"
if (-not (Test-Path $serverDir)) {
    New-Item -ItemType Directory -Path $serverDir | Out-Null
}

$destApk = Join-Path $serverDir "openStream.apk"
Copy-Item -Path $apkPath -Destination $destApk -Force
Write-Host "Copied release APK to $destApk" -ForegroundColor Green

$manifestPath = Join-Path $serverDir "update.json"
$manifestJson = @"
{
  "versionCode": $TargetVersionCode,
  "versionName": "$TargetVersionName",
  "apkUrl": "https://github.com/mahmudjanovr/openStream/releases/download/v$TargetVersionName/openStream.apk",
  "releaseNotes": [
    "Improved app performance and ultra-fast touch response",
    "Fixed movie poster accuracy with high-resolution archival artwork",
    "Optimized bottom navigation with dedicated ergonomics and touch targets",
    "Enhanced ExoPlayer buffer management and PiP transitions",
    "Over-The-Air (OTA) in-app updates directly via GitHub Releases"
  ],
  "forceUpdate": false,
  "minimumSupportedVersion": 2,
  "sha256": "$sha256"
}
"@
Set-Content -Path $manifestPath -Value $manifestJson -Encoding UTF8
Write-Host "Updated manifest at $manifestPath" -ForegroundColor Green

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host " RELEASE ARTIFACT READY FOR PUBLISHING" -ForegroundColor Green
Write-Host " Version: $TargetVersionName (Build $TargetVersionCode)"
Write-Host " APK Path: $destApk"
Write-Host " File Size: $((Get-Item $destApk).Length) bytes"
Write-Host " SHA-256: $sha256"
Write-Host " Manifest: $manifestPath"
Write-Host "==========================================================" -ForegroundColor Green
