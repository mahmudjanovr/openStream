# openStream — Over-The-Air (OTA) In-App Update Infrastructure

This directory documents the release manifest and distribution infrastructure for the **openStream** Over-The-Air (OTA) in-app update system backed by **GitHub Releases**.

---

## 1. Distribution Endpoints & URLs

- **Target Repository**: `https://github.com/mahmudjanovr/openStream`
- **Update Manifest (Raw GitHub HTTPS)**:
  `https://raw.githubusercontent.com/mahmudjanovr/openStream/master/update-server/update.json`
- **Target Release Tag Format**:
  `v1.2.0` (Semver prefixed with `v`)
- **Direct APK Download URL**:
  `https://github.com/mahmudjanovr/openStream/releases/download/v1.2.0/openStream.apk`
- **Latest Release Asset Redirect**:
  `https://github.com/mahmudjanovr/openStream/releases/latest/download/openStream.apk`

> [!IMPORTANT]
> **Repository Visibility Notice**:
> For the Android application to query `update.json` and download `openStream.apk` from GitHub Releases without requiring GitHub authentication tokens, the repository `mahmudjanovr/openStream` must be **PUBLIC**, or the manifest must be hosted on GitHub Pages with public access.

---

## 2. Release Keystore & Certificate Integrity

Android security mandates that all update APKs **MUST** be signed with the exact same certificate as the installed version. Upgrades signed with a different key will fail with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`.

### Generated Production Keystore Specification:
- **Filename**: `openstream-release.jks` *(Ignored by Git)*
- **Alias**: `openstream_release_key`
- **Validity**: Until **March 25, 2054** (10,000 days)
- **Distinguished Name (DN)**: `CN=openStream, OU=Engineering, O=openStream, L=Tashkent, ST=Tashkent, C=UZ`
- **Certificate SHA-256 Fingerprint**:
  `60:E1:0A:AA:67:E6:90:8D:D6:8D:2B:3B:19:7F:79:F9:8A:20:44:44:A8:B4:FC:0B:6C:8D:33:BB:03:33:E1:6C`

### Keystore Backup Policy:
1. Store a copy of `openstream-release.jks` in a secure password manager or offline encrypted storage (e.g., 1Password, Bitwarden, or encrypted USB).
2. If this keystore is permanently lost, **future OTA updates cannot be installed** over existing installations.

---

## 3. GitHub Actions CI/CD Secrets Setup

To enable automated release builds via `.github/workflows/release.yml`, configure the following GitHub Repository Secrets under **Settings > Secrets and variables > Actions**:

| Secret Name | Value Description |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | Base64-encoded binary string of `openstream-release.jks`. |
| `RELEASE_STORE_PASSWORD` | Keystore password. |
| `RELEASE_KEY_ALIAS` | Key alias (`openstream_release_key`). |
| `RELEASE_KEY_PASSWORD` | Key password. |

To generate the base64 string for `RELEASE_KEYSTORE_BASE64` on Windows PowerShell:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("openstream-release.jks")) | Set-Clipboard
```

---

## 4. Manifest Specification (`update.json`)

```json
{
  "versionCode": 3,
  "versionName": "1.2.0",
  "apkUrl": "https://github.com/mahmudjanovr/openStream/releases/download/v1.2.0/openStream.apk",
  "releaseNotes": [
    "Improved app performance and ultra-fast touch response",
    "Fixed movie poster accuracy with high-resolution archival artwork",
    "Optimized bottom navigation with dedicated ergonomics and touch targets",
    "Enhanced ExoPlayer buffer management and PiP transitions",
    "Over-The-Air (OTA) in-app updates directly via GitHub Releases"
  ],
  "forceUpdate": false,
  "minimumSupportedVersion": 2,
  "sha256": "58c2e09daf79a2a821fd9e85f0df4cbd1c86038919ae10d55efae3246671b575"
}
```

---

## 5. Local Release Build Pipeline

To build and sign a release locally without committing keys:

```powershell
.\scripts\build-release.ps1 -TargetVersionCode 3 -TargetVersionName "1.2.0"
```

This script:
1. Executes the complete unit test suite (`.\gradlew test`).
2. Builds the release APK (`.\gradlew assembleRelease`).
3. Validates the signature with Android SDK `apksigner`.
4. Computes the cryptographic SHA-256 digest.
5. Copies `openStream.apk` and updates `update-server/update.json`.
