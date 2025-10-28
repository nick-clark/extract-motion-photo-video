package com.motionphoto.extractor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    
    private lateinit var extractedVideoFiles: List<File>
    private var progressBar: ProgressBar? = null
    private var statusText: TextView? = null
    private var countText: TextView? = null
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            processSharedFiles(intent)
        } else {
            Toast.makeText(this, "Permission needed to access photos", Toast.LENGTH_SHORT).show()
            showInstructions()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if opened with a share intent
        val hasShareIntent = intent?.action == Intent.ACTION_SEND || intent?.action == Intent.ACTION_SEND_MULTIPLE
        
        if (hasShareIntent) {
            // Check permissions for Android 13+ (READ_MEDIA_IMAGES)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                processSharedFiles(intent)
            }
        } else {
            // Just opened the app - show instructions
            showInstructions()
        }
    }
    
    private fun showInstructions() {
        setContentView(R.layout.activity_main)
    }
    
    private fun processSharedFiles(intent: Intent?) {
        val imageUris = arrayListOf<Uri>()
        
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let {
                    imageUris.add(it)
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let {
                    imageUris.addAll(it)
                }
            }
        }
        
        if (imageUris.isEmpty()) {
            Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Show progress UI
        showProgressUI(imageUris.size)
        
        // Extract videos in background
        Thread {
            extractedVideoFiles = extractVideosFromImages(imageUris)
            
            runOnUiThread {
                if (extractedVideoFiles.isEmpty()) {
                    Toast.makeText(this, "No motion photos found", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    shareVideos(extractedVideoFiles)
                }
            }
        }.start()
    }
    
    private fun showProgressUI(totalFiles: Int) {
        setContentView(R.layout.activity_progress)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        countText = findViewById(R.id.countText)
        
        progressBar?.max = 100
        countText?.text = "0 / $totalFiles"
    }
    
    private fun updateProgress(currentFile: Int, totalFiles: Int, fileName: String) {
        runOnUiThread {
            val progress = ((currentFile.toFloat() / totalFiles.toFloat()) * 100).toInt()
            progressBar?.progress = progress
            statusText?.text = "Extracting: $fileName"
            countText?.text = "$currentFile / $totalFiles"
        }
    }
    
    private fun extractVideosFromImages(imageUris: ArrayList<Uri>): List<File> {
        val extractedFiles = mutableListOf<File>()
        val totalFiles = imageUris.size
        
        for ((index, uri) in imageUris.withIndex()) {
            try {
                // Get filename from URI
                val fileName = getFileNameFromUri(uri)
                
                // Update progress
                updateProgress(index, totalFiles, fileName)
                
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val tempFile = File.createTempFile("motion_", ".jpg", cacheDir)
                    FileOutputStream(tempFile).use { inputStream.copyTo(it) }
                    
                    val mp4Data = extractMp4FromJpeg(tempFile)
                    
                    if (mp4Data != null) {
                        val videoFile = File(cacheDir, "extracted_${System.currentTimeMillis()}.mp4")
                        FileOutputStream(videoFile).use {
                            it.write(mp4Data)
                        }
                        extractedFiles.add(videoFile)
                    }
                    
                    tempFile.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Final progress update
        updateProgress(totalFiles, totalFiles, "Complete")
        
        return extractedFiles
    }
    
    private fun getFileNameFromUri(uri: Uri): String {
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor?.moveToFirst()
            val fileName = cursor?.getString(nameIndex ?: 0) ?: "photo"
            cursor?.close()
            fileName
        } catch (e: Exception) {
            "photo"
        }
    }
    
    private fun extractMp4FromJpeg(jpegFile: File): ByteArray? {
        val data = jpegFile.readBytes()
        
        // Find JPEG end marker (0xFF 0xD9)
        var jpegEndOffset = -1
        for (i in 0 until data.size - 1) {
            if (data[i].toInt() == 0xFF && data[i + 1].toInt() == 0xD9) {
                jpegEndOffset = i + 2
                break
            }
        }
        
        if (jpegEndOffset == -1) {
            // No JPEG end marker found, try old method
            return extractMp4OldMethod(data)
        }
        
        // The MP4 should start after JPEG end
        // Look for 'ftyp' box after JPEG end
        var mp4Start = -1
        for (i in jpegEndOffset until data.size - 3) {
            if (data[i] == 'f'.code.toByte() && 
                data[i+1] == 't'.code.toByte() && 
                data[i+2] == 'y'.code.toByte() && 
                data[i+3] == 'p'.code.toByte()) {
                
                // Found ftyp, MP4 starts 4 bytes before (box size)
                mp4Start = if (i >= 4) i - 4 else i
                break
            }
        }
        
        if (mp4Start == -1) {
            return null
        }
        
        // Extract the MP4 data
        val mp4Data = data.sliceArray(mp4Start until data.size)
        
        // Verify it's a valid MP4
        if (isValidMp4(mp4Data)) {
            return mp4Data
        }
        
        return null
    }
    
    // Fallback to old extraction method
    private fun extractMp4OldMethod(data: ByteArray): ByteArray? {
        for (i in data.indices) {
            if (i + 20 < data.size && 
                data[i] == 'f'.code.toByte() && 
                data[i+1] == 't'.code.toByte() && 
                data[i+2] == 'y'.code.toByte() && 
                data[i+3] == 'p'.code.toByte()) {
                
                val startOffset = if (i >= 4) i - 4 else i
                val mp4Data = data.sliceArray(startOffset until data.size)
                if (isValidMp4(mp4Data)) {
                    return mp4Data
                }
            }
        }
        return null
    }
    
    private fun isValidMp4(mp4Data: ByteArray): Boolean {
        if (mp4Data.size < 100) return false
        
        val hasFtyp = mp4Data.joinToString("") { "%02x".format(it) }.contains("66747970") // 'ftyp'
        val hasMoov = mp4Data.take(5000).toByteArray()
            .joinToString("") { "%02x".format(it) }.contains("6d6f6f76") // 'moov'
        val hasMdat = mp4Data.joinToString("") { "%02x".format(it) }.contains("6d646174") // 'mdat'
        
        return hasFtyp && (hasMoov || hasMdat)
    }
    
    private fun shareVideos(videoFiles: List<File>) {
        val contentUris = ArrayList<Uri>()
        
        for (file in videoFiles) {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            contentUris.add(uri)
        }
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "video/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, contentUris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share to CapCut/TikTok"))
        
        // Clean up after 5 seconds (Option B)
        Handler(Looper.getMainLooper()).postDelayed({
            for (file in videoFiles) {
                file.delete()
            }
        }, 5000)
    }
}

