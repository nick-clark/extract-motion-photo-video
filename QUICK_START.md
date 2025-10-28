# Quick Start Guide

## Easiest Way to Build APK Without Android Studio Setup

### Option 1: Use Android Studio (Recommended for Testing)

1. Download [Android Studio](https://developer.android.com/studio) (if not installed)
2. Open Android Studio
3. Click "Open an Existing Project"
4. Navigate to and select the `MotionPhotoExtractor` folder
5. Wait for "Gradle Sync" to complete
6. Connect your Samsung phone via USB
7. Enable USB debugging on your phone (Settings > Developer Options)
8. Click the green Play button ▶️ or press `Ctrl+R`
9. Select your phone and install!

### Option 2: Build APK with Command Line (If you have Android SDK)

```bash
cd MotionPhotoExtractor
./gradlew assembleDebug
```

Then find the APK at: `app/build/outputs/apk/debug/app-debug.apk`

Transfer to your phone and install.

### Option 3: Online Build via GitHub

1. Create a new GitHub repository
2. Push this entire `MotionPhotoExtractor` folder to GitHub
3. See the README.md for GitHub Actions workflow setup
4. Download the built APK from GitHub Actions artifacts

## How to Use the App

1. Open your Gallery app
2. Select a motion photo (or multiple)
3. Tap the Share button
4. Select "Motion Photo Extractor" from the list
5. Wait a moment for extraction
6. When share dialog appears, choose CapCut, TikTok, or any video app
7. Done!

## Troubleshooting

**App doesn't appear in share menu:**
- Make sure the APK installed successfully
- Try restarting your phone
- Some older Android versions have limited share options

**"Permission needed" error:**
- Go to Settings > Apps > Motion Photo Extractor
- Grant "Photos and media" permission manually

**Extraction fails:**
- Make sure you selected a true motion photo (taken with motion mode enabled)
- Check that the photo was taken on a Samsung device with motion support
- Try with a different motion photo

## What Gets Extracted?

Only the embedded video stream from motion photos. Regular photos, portrait mode photos, and videos without motion data will be skipped.

## Storage

Temporary files are stored in app cache and automatically deleted after 5 seconds. Your extracted videos are sent to CapCut/TikTok and copied there, so you don't need to worry about storage.

