package com.motionphoto.extractor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            processSharedFiles(intent)
        } else {
            Toast.makeText(this, "Permission needed to access photos", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check permissions for Android 13+ (READ_MEDIA_IMAGES)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            processSharedFiles(intent)
        }
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
    
    private fun extractVideosFromImages(imageUris: ArrayList<Uri>): List<File> {
        val extractedFiles = mutableListOf<File>()
        
        for (uri in imageUris) {
            try {
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
        
        return extractedFiles
    }
    
    private fun extractMp4FromJpeg(jpegFile: File): ByteArray? {
        val data = jpegFile.readBytes()
        
        // Look for MP4 file signature (ftyp box)
        for (i in data.indices) {
            if (i + 20 < data.size && 
                data[i] == 'f'.code.toByte() && 
                data[i+1] == 't'.code.toByte() && 
                data[i+2] == 'y'.code.toByte() && 
                data[i+3] == 'p'.code.toByte()) {
                
                // Found ftyp, check if it's preceded by a size
                val startOffset = if (i >= 4) i - 4 else i
                
                // Verify this looks like a valid MP4
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

