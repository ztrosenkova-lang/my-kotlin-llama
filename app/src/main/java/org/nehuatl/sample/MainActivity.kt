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

    // Универсальный лаунчер для множественных разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (!audioGranted) {
            Log.w("MainActivity", "Разрешение на запись audio не получено")
        }

        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        }
        if (!storageGranted) {
            Log.w("MainActivity", "Разрешение на чтение хранилища не получено")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Проверка и запрос всех необходимых разрешений
        checkAndRequestAllPermissions()

        // Логирование состояния точного будильника без принудительного открытия настроек
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("MainActivity", "Разрешение на точный будильник не предоставлено. Пользователь сможет установить его при создании первого будильника.")
                // Запрос разрешения будет выполнен при установке будильника через AlarmReceiver
            }
        }

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

    /**
     * Запрашивает все необходимые разрешения в зависимости от версии Android:
     * - RECORD_AUDIO всегда
     * - Для Android 13+ (Tiramisu): READ_MEDIA_IMAGES и READ_MEDIA_VIDEO
     * - Для Android 12 и ниже: READ_EXTERNAL_STORAGE
     */
    private fun checkAndRequestAllPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}
