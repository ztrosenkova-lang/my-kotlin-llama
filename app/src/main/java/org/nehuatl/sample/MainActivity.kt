package org.nehuatl.sample

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.nehuatl.sample.ui.theme.KotlinLlamaCppTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private var modelPath by mutableStateOf<String?>(null)
    private var mmprojPath by mutableStateOf<String?>(null)
    private var imagePath by mutableStateOf<String?>(null)

    private val modelPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            modelPath = it.toString()
        }
    }

    private val mmprojPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            mmprojPath = it.toString()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            imagePath = it.toString()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (!audioGranted) {
            Log.w("MainActivity", "Разрешение на запись audio не получено")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("MainActivity", "Разрешение на точный будильник не предоставлено. Открываем настройки.")
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }

        checkAndRequestAudioPermission()
        copyVoskModelIfNeeded()

        setContent {
            KotlinLlamaCppTheme {
                val viewModel: MainViewModel by viewModels {
                    object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return MainViewModel(application, contentResolver) as T
                        }
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ChatScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel,
                        currentModelPath = modelPath,
                        mmprojPath = mmprojPath,
                        onPickModel = { modelPickerLauncher.launch(arrayOf("*/*")) },
                        onPickMmproj = { mmprojPickerLauncher.launch(arrayOf("*/*")) },
                        onPickImage = { imagePickerLauncher.launch(arrayOf("image/*")) },
                        imagePath = imagePath,
                        onImageUsed = { imagePath = null }
                    )
                }
            }
        }
    }

    private fun checkAndRequestAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    private fun copyVoskModelIfNeeded() {
        val modelDir = File(filesDir, "model")
        if (!modelDir.exists()) {
            try {
                modelDir.mkdirs()
                val assetFiles = assets.list("model")
                if (assetFiles.isNullOrEmpty()) {
                    Log.e("MainActivity", "Модель Vosk не найдена в assets/model/")
                    return
                }
                assetFiles.forEach { fileName ->
                    assets.open("model/$fileName").use { input ->
                        File(modelDir, fileName).outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                Log.d("MainActivity", "Модель Vosk скопирована успешно")
            } catch (e: Exception) {
                Log.e("MainActivity", "Ошибка копирования модели Vosk: ${e.message}")
            }
        } else {
            Log.d("MainActivity", "Модель Vosk уже существует")
        }
    }
}
