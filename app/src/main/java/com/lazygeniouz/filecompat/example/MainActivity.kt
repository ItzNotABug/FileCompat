package com.lazygeniouz.filecompat.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.storage.StorageManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.lazygeniouz.filecompat.example.performance.Performance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var button: Button
    private lateinit var textView: TextView
    private lateinit var progress: ProgressBar

    private val folderResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val documentUri = result.data?.data
                if (documentUri != null) {
                    lifecycleScope.launch {
                        progress.isVisible = true
                        val performanceResult = withContext(Dispatchers.IO) {
                            Performance.calculatePerformance(
                                this@MainActivity, documentUri
                            )
                        }

                        progress.isVisible = false
                        textView.text = performanceResult
                        button.isVisible = true
                    }
                }
            }
        }

    @Suppress("unchecked_cast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        button = findViewById(R.id.button)
        textView = findViewById(R.id.fileNames)
        progress = findViewById(R.id.progress)

        button.isVisible = true
        button.setOnClickListener {
            button.isVisible = false
            folderResultLauncher.launch(getStorageIntent())
        }
    }

    private fun getStorageIntent(): Intent {
        return if (SDK_INT >= 30) {
            val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
            storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
        } else Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}