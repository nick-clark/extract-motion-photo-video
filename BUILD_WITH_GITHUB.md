# Build APK Using GitHub Actions (No Android Studio Needed!)

This is the easiest way if you don't have Android Studio.

## Steps:

1. **Create a GitHub account** (if you don't have one): https://github.com

2. **Create a new repository:**
   - Go to github.com → New Repository
   - Name it something like `motion-photo-extractor`
   - Make it **Private** (unless you want it public)
   - Don't initialize with README

3. **Push this code to GitHub:**
```bash
cd MotionPhotoExtractor
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
git push -u origin main
```

4. **Wait for build to complete:**
   - Go to your repo on GitHub
   - Click the "Actions" tab
   - You'll see a workflow running
   - Wait 2-3 minutes for it to finish

5. **Download the APK:**
   - After the workflow completes (green checkmark)
   - Click on the workflow run
   - Scroll down to "Artifacts"
   - Click "app-debug" to download a zip file
   - Extract the APK from the zip

6. **Install on your phone:**
   - Transfer the APK to your S24 Ultra (email, cloud drive, USB, etc.)
   - Open the APK on your phone
   - You may need to allow "Install from Unknown Sources"
   - Tap Install
   - Done!

## Rebuild when you make changes:

Just push your changes and GitHub will automatically rebuild:
```bash
git add .
git commit -m "Your changes"
git push
```

Then download the new APK from Actions.

## Pros:
- ✅ No software installation needed
- ✅ Works on any computer with internet
- ✅ Free for private repos
- ✅ Automatic builds

## Cons:
- ⚠️ Need a GitHub account
- ⚠️ Need internet connection

