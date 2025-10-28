# Motion Photo Extractor

Extract embedded MP4 video streams from Samsung motion photos (embedded in JPEG files).

## How it Works

1. Select motion photos from your gallery
2. Share them to this app
3. App extracts the embedded MP4 videos
4. Opens share dialog to send to CapCut/TikTok
5. Auto-cleans temporary files after 5 seconds

## Building the APK

### Option 1: Using Android Studio

1. Install [Android Studio](https://developer.android.com/studio)
2. Open the `MotionPhotoExtractor` folder in Android Studio
3. Wait for Gradle sync to complete
4. Connect your phone via USB with developer mode enabled
5. Click "Run" button or press `Shift + F10`
6. Or build APK: `Build > Build Bundle(s) / APK(s) > Build APK(s)`

### Option 2: Using Gradle Command Line

If you have Android SDK installed:

```bash
cd MotionPhotoExtractor
./gradlew assembleDebug
```

The APK will be in `app/build/outputs/apk/debug/app-debug.apk`

### Option 3: Using GitHub Actions (Cloud Build)

1. Push this project to a GitHub repository
2. Create a file `.github/workflows/build.yml` with:

```yaml
name: Build APK
on: [push, workflow_dispatch]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Setup Android SDK
        uses: android-actions/setup-android@v3
      - name: Build APK
        run: |
          cd MotionPhotoExtractor
          chmod +x gradlew
          ./gradlew assembleDebug
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug.apk
          path: MotionPhotoExtractor/app/build/outputs/apk/debug/app-debug.apk
```

3. Go to Actions tab, download the APK, install on your phone

### Option 4: Online Build Services

- **BuildBox.io** - Upload project folder, build online
- **Gitpod** - Cloud IDE with Android support

## Installation on Phone

1. Enable "Install from Unknown Sources" in developer settings
2. Transfer APK to phone (email, cloud storage, USB)
3. Open APK on phone and tap "Install"

## Usage

1. Open gallery app
2. Select one or more motion photos
3. Tap "Share" button
4. Select "Motion Photo Extractor" from apps list
5. App extracts videos and opens share dialog
6. Choose CapCut, TikTok, or any video editor

## Technical Details

- Extracts MP4 by finding `ftyp` box in JPEG binary data
- No re-encoding, pure byte copy (fast!)
- Uses app cache directory for temporary storage
- Auto-cleans cache files after 5 seconds
- Supports both single and multiple file sharing

## Tested On

- Samsung S24 Ultra (Android 14)

