package org.nehuatl.sample

import android.Manifest
import android.app.AlertDialog
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
import java.security.MessageDigest

class MainActivity : ComponentActivity() {

    private var modelPath by mutableStateOf<String?>(null)
    private var mmprojPath by mutableStateOf<String?>(null)
    private var imagePath by mutableStateOf<String?>(null)

    // Константы для работы с паролем
    companion object {
        private const val PREFS_NAME = "app_security"
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val TAG = "MainActivity"
    }

    private val prefs by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

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

    // ========== ФУНКЦИИ РАБОТЫ С ПАРОЛЕМ ==========

    /**
     * Хеширует пароль с помощью SHA-256
     */
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Проверяет, установлен ли пароль
     */
    private fun isPasswordSet(): Boolean {
        return prefs.contains(KEY_PASSWORD_HASH)
    }

    /**
     * Проверяет введённый пароль
     */
    private fun checkPassword(input: String): Boolean {
        val storedHash = prefs.getString(KEY_PASSWORD_HASH, null) ?: return false
        return hashPassword(input) == storedHash
    }

    /**
     * Сохраняет хеш пароля
     */
    private fun setPassword(password: String) {
        prefs.edit().putString(KEY_PASSWORD_HASH, hashPassword(password)).apply()
    }

    /**
     * Показывает диалог установки пароля (при первом запуске)
     * Исправлено: убран вызов R.layout.dialog_set_password, используется программное создание полей
     */
    private fun showSetPasswordDialog() {
        val passwordInput = android.widget.EditText(this).apply {
            hint = "Введите пароль"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val confirmInput = android.widget.EditText(this).apply {
            hint = "Подтвердите пароль"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val linearLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            addView(passwordInput)
            addView(confirmInput)
        }

        AlertDialog.Builder(this)
            .setTitle("🔒 Установка пароля")
            .setMessage("Приложение будет защищено паролем. Введите пароль дважды для подтверждения.")
            .setView(linearLayout)
            .setPositiveButton("Установить") { _, _ ->
                val password = passwordInput.text.toString()
                val confirm = confirmInput.text.toString()
                if (password.isNotEmpty() && password == confirm) {
                    setPassword(password)
                    Log.d(TAG, "Пароль установлен успешно")
                    // После установки пароля показываем экран входа
                    showPasswordDialog()
                } else {
                    Log.w(TAG, "Пароль не совпадает или пустой")
                    showSetPasswordDialog() // Повторяем попытку
                }
            }
            .setNegativeButton("Выйти") { _, _ ->
                finishAffinity() // Закрываем приложение
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Показывает диалог ввода пароля
     */
    private fun showPasswordDialog() {
        val passwordInput = android.widget.EditText(this).apply {
            hint = "Введите пароль"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        AlertDialog.Builder(this)
            .setTitle("🔒 Введите пароль")
            .setMessage("Для доступа к приложению требуется пароль.")
            .setView(passwordInput)
            .setPositiveButton("Войти") { _, _ ->
                val input = passwordInput.text.toString()
                if (checkPassword(input)) {
                    Log.d(TAG, "Пароль верный, вход разрешён")
                    // Показываем основной интерфейс
                    showMainContent()
                } else {
                    Log.w(TAG, "Неверный пароль")
                    // Показываем сообщение об ошибке и закрываем приложение
                    AlertDialog.Builder(this)
                        .setTitle("Ошибка")
                        .setMessage("Неверный пароль. Приложение будет закрыто.")
                        .setPositiveButton("OK") { _, _ ->
                            finishAffinity()
                        }
                        .setCancelable(false)
                        .show()
                }
            }
            .setNegativeButton("Выйти") { _, _ ->
                finishAffinity()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Показывает основной интерфейс (ChatScreen)
     */
    private fun showMainContent() {
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

    // ========== ЖИЗНЕННЫЙ ЦИКЛ ==========

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
            }
        }

        // Проверяем пароль при запуске
        checkPasswordAndProceed()
    }

    override fun onResume() {
        super.onResume()
        // При возврате из фона также проверяем пароль
        if (isPasswordSet()) {
            showPasswordDialog()
        }
    }

    /**
     * Проверяет, установлен ли пароль, и показывает соответствующий диалог
     */
    private fun checkPasswordAndProceed() {
        if (!isPasswordSet()) {
            // Первый запуск — предлагаем установить пароль
            showSetPasswordDialog()
        } else {
            // Пароль уже установлен — запрашиваем ввод
            showPasswordDialog()
        }
    }

    // ========== РАЗРЕШЕНИЯ ==========

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
