# Push to GitHub & Build APK

## Step-by-Step Instructions

### 1. Create GitHub Repository

Go to https://github.com/new

Fill in:
- **Repository name:** `motion-photo-extractor` (or any name you want)
- **Description:** `Extract embedded MP4 videos from Samsung motion photos`
- **Visibility:** Choose **Private** (recommended, but either works)
- **DO NOT** check "Initialize with README" (we already have files)
- Click **Create repository**

### 2. Copy the Commands from GitHub

GitHub will show you commands. **Use the "push an existing repository" option**:

It should look like:
```bash
git remote add origin https://github.com/YOUR_USERNAME/motion-photo-extractor.git
git branch -M main
git push -u origin main
```

### 3. Run the Commands

Open Terminal and run:

```bash
cd "/Users/nick/Desktop/motion photo project/MotionPhotoExtractor"
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
git push -u origin main
```

**Replace:** `YOUR_USERNAME` with your GitHub username and `YOUR_REPO_NAME` with the repo name you created.

Example:
```bash
git remote add origin https://github.com/nick123/motion-photo-extractor.git
git push -u origin main
```

You'll be prompted for your GitHub username and password (use a Personal Access Token as password if 2FA is enabled).

### 4. Check the Actions Tab

1. Go to your repository on GitHub
2. Click the **"Actions"** tab (top menu)
3. You should see a workflow running: "Build APK"
4. Click on it to watch the progress
5. Wait 2-3 minutes for it to complete

### 5. Download the APK

1. When the workflow shows a ✅ green checkmark
2. Click on the workflow run
3. Scroll down to **"Artifacts"** section
4. Click **"app-debug"**
5. A ZIP file will download
6. Extract the ZIP → Inside is `app-debug.apk`

### 6. Install on Your Phone

1. Transfer `app-debug.apk` to your S24 Ultra:
   - Email it to yourself
   - Upload to Google Drive/Dropbox
   - Use USB
2. Open the APK on your phone
3. If prompted about "Install from unknown sources" → Allow
4. Tap Install
5. Done!

## Troubleshooting

**"Authentication failed" when pushing:**
- Use a Personal Access Token instead of password
- Go to: GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
- Generate new token with `repo` scope
- Use that as your password

**Actions not showing:**
- Make sure you pushed to the `main` branch
- The `.github/workflows/build-apk.yml` file should be in your repo

**Build fails:**
- Check the Actions log for error messages
- Most common: Missing dependencies or invalid characters in files

## Future Updates

After making changes to the code:

```bash
cd "/Users/nick/Desktop/motion photo project/MotionPhotoExtractor"
git add .
git commit -m "Description of your changes"
git push
```

The workflow will automatically build a new APK!

