package org.nehuatl.sample

import android.content.Context
import android.content.Intent
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
        uri?.let { modelUri ->
            try {
                contentResolver.takePersistableUriPermission(
                    modelUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                Log.d("MainActivity", "Вечные права на GGUF-модель зафиксированы!")
            } catch (e: Exception) {
                Log.e("MainActivity", "Ошибка фиксации прав на GGUF", e)
            }
            modelPath = modelUri.toString()
        }
    }

    private val mmprojPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { mmprojUri ->
            try {
                contentResolver.takePersistableUriPermission(
                    mmprojUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                Log.d("MainActivity", "Вечные права на mmproj зафиксированы!")
            } catch (e: Exception) {
                Log.e("MainActivity", "Ошибка фиксации прав на mmproj", e)
            }
            mmprojPath = mmprojUri.toString()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                Log.d("MainActivity", "Вечные права на изображение зафиксированы!")
            } catch (e: Exception) {
                Log.e("MainActivity", "Ошибка фиксации прав на изображение", e)
            }
            imagePath = it.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // 🔔 ЗАПРОС РАЗРЕШЕНИЯ ДЛЯ ТОЧНОГО БУДИЛЬНИКА (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
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
}
