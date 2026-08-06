package org.nehuatl.sample

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
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

    // ========== ВСПОМОГАТЕЛЬНАЯ ФУНКЦИЯ ДЛЯ ВИБРАЦИИ ==========
    private fun vibrateButton() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibration failed", e)
        }
    }

    // ========== ФУНКЦИИ РАБОТЫ С ПАРОЛЕМ ==========

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun isPasswordSet(): Boolean {
        return prefs.contains(KEY_PASSWORD_HASH)
    }

    private fun checkPassword(input: String): Boolean {
        val storedHash = prefs.getString(KEY_PASSWORD_HASH, null) ?: return false
        return hashPassword(input) == storedHash
    }

    private fun setPassword(password: String) {
        prefs.edit().putString(KEY_PASSWORD_HASH, hashPassword(password)).apply()
    }

    private fun createStyledEditText(hintText: String): android.widget.EditText {
        return android.widget.EditText(this).apply {
            hint = hintText
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            gravity = android.view.Gravity.CENTER
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    setHint("")
                } else {
                    if (text.isNullOrEmpty()) {
                        setHint(hintText)
                    }
                }
            }
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#FFFFF9DB"))
                cornerRadius = 12.dpToPx().toFloat()
                setStroke(1.dpToPx(), android.graphics.Color.parseColor("#FFCED4DA"))
            }
            background = drawable
            setPadding(16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 12.dpToPx())
        }
    }

    // ========== ДИАЛОГ УСТАНОВКИ ПАРОЛЯ ==========
    private fun showSetPasswordDialog() {
        val dialogView = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        // 1. Заголовок с роботом (центрируем)
        val titleView = android.widget.TextView(this).apply {
            text = "🤖 Установка пароля"
            textSize = 18f
            setTextColor(android.graphics.Color.parseColor("#FF212529"))
            gravity = android.view.Gravity.CENTER
            textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            setPadding(0, 0, 0, 16)
        }
        dialogView.addView(titleView)

        // 2. Сообщение (центрируем)
        val messageView = android.widget.TextView(this).apply {
            text = "Приложение будет защищено паролем. Введите пароль дважды для подтверждения."
            textSize = 13f
            setTextColor(android.graphics.Color.parseColor("#FF212529"))
            gravity = android.view.Gravity.CENTER
            textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            setPadding(0, 0, 0, 16)
        }
        dialogView.addView(messageView)

        // 3. Поля ввода
        val passwordInput = createStyledEditText("Введите пароль")
        val confirmInput = createStyledEditText("Подтвердите пароль")
        dialogView.addView(passwordInput)
        dialogView.addView(confirmInput)

        // 4. Контейнер для кнопок (горизонтальный, центрированный)
        val buttonContainer = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(0, 16, 0, 0)
        }

        // Создаём диалог ДО кнопок, чтобы иметь ссылку
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Кнопка "Установить" с тактильной обратной связью
        val positiveButton = android.widget.Button(this).apply {
            text = "Установить"
            setBackgroundColor(android.graphics.Color.parseColor("#FF74C0FC"))
            setTextColor(android.graphics.Color.parseColor("#FF212529"))
            setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
            textSize = 12f
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#FF74C0FC"))
                cornerRadius = 10.dpToPx().toFloat()
            }
            background = drawable
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(10.dpToPx(), 0, 10.dpToPx(), 0)
            }
            // Добавляем тактильную обратную связь при нажатии
            setOnTouchListener { _, _ ->
                vibrateButton()
                false
            }
            setOnClickListener {
                val password = passwordInput.text.toString()
                val confirm = confirmInput.text.toString()
                if (password.isNotEmpty() && password == confirm) {
                    setPassword(password)
                    Log.d(TAG, "Пароль установлен успешно")
                    dialog.dismiss()
                    showPasswordDialog()
                } else {
                    Log.w(TAG, "Пароль не совпадает или пустой")
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "Пароли не совпадают или пустые",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        buttonContainer.addView(positiveButton)

        // Кнопка "Выйти" с тактильной обратной связью
        val negativeButton = android.widget.Button(this).apply {
            text = "Выйти"
            setBackgroundColor(android.graphics.Color.parseColor("#FF74C0FC"))
            setTextColor(android.graphics.Color.parseColor("#FF212529"))
            setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
            textSize = 12f
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#FF74C0FC"))
                cornerRadius = 10.dpToPx().toFloat()
            }
            background = drawable
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(10.dpToPx(), 0, 10.dpToPx(), 0)
            }
            setOnTouchListener { _, _ ->
                vibrateButton()
                false
            }
            setOnClickListener {
                dialog.dismiss()
                finishAffinity()
            }
        }
        buttonContainer.addView(negativeButton)

        dialogView.addView(buttonContainer)

        // Настраиваем и показываем диалог
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        // Устанавливаем фон и закругления
        dialog.window?.decorView?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        val rootView = dialog.window?.decorView?.findViewById<android.widget.FrameLayout>(android.R.id.content)

        if (rootView != null) {
            val background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#FFF1F3F5"))
                cornerRadius = 16.dpToPx().toFloat()
            }
            rootView.background = background

            val params = rootView.layoutParams as? android.widget.FrameLayout.LayoutParams
            params?.let {
                val marginPx = 20.dpToPx()
                it.setMargins(marginPx, marginPx, marginPx, marginPx)
                rootView.layoutParams = it
            }
        }
    }

    // ========== ДИАЛОГ ВВОДА ПАРОЛЯ ==========
    private fun showPasswordDialog() {
        val dialogView = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        // 1. Заголовок с роботом (центрируем)
        val titleView = android.widget.TextView(this).apply {
            text = "🤖 Введите пароль"
            textSize = 18f
            setTextColor(android.graphics.Color.parseColor("#FF212529"))
            gravity = android.view.Gravity.CENTER
            textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            setPadding(0, 0, 0, 16)
        }
        dialogView.addView(titleView)

        // 2. Сообщение (центрируем)
        val messageView = android.widget.TextView(this).apply {
            text = "Для доступа к приложению требуется пароль."
            textSize = 13f
            setTextColor(android.graphics.Color.parseColor("#FF212529"))
            gravity = android.view.Gravity.CENTER
            textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            setPadding(0, 0, 0, 16)
        }
        dialogView.addView(messageView)

        // 3. Поле ввода
        val passwordInput = createStyledEditText("Введите пароль")
        dialogView.addView(passwordInput)

        // 4. Контейнер для кнопок (горизонтальный, центрированный)
        val buttonContainer = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(0, 16, 0, 0)
        }

        // Создаём диалог ДО кнопок, чтобы иметь ссылку
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Кнопка "Войти" с тактильной обратной связью
        val positiveButton = android.widget.Button(this).apply {
            text = "Войти"
            setBackgroundColor(android.graphics.Color.parseColor("#FF74C0FC"))
            setTextColor(android.graphics.Color.parseColor("#FF212529"))
            setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
            textSize = 12f
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#FF74C0FC"))
                cornerRadius = 10.dpToPx().toFloat()
            }
            background = drawable
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(10.dpToPx(), 0, 10.dpToPx(), 0)
            }
            setOnTouchListener { _, _ ->
                vibrateButton()
                false
            }
            setOnClickListener {
                val input = passwordInput.text.toString()
                if (checkPassword(input)) {
                    Log.d(TAG, "Пароль верный, вход разрешён")
                    dialog.dismiss()
                    showMainContent()
                } else {
                    Log.w(TAG, "Неверный пароль")
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "Неверный пароль",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        buttonContainer.addView(positiveButton)

        // Кнопка "Выйти" с тактильной обратной связью
        val negativeButton = android.widget.Button(this).apply {
            text = "Выйти"
            setBackgroundColor(android.graphics.Color.parseColor("#FF74C0FC"))
            setTextColor(android.graphics.Color.parseColor("#FF212529"))
            setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
            textSize = 12f
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#FF74C0FC"))
                cornerRadius = 10.dpToPx().toFloat()
            }
            background = drawable
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(10.dpToPx(), 0, 10.dpToPx(), 0)
            }
            setOnTouchListener { _, _ ->
                vibrateButton()
                false
            }
            setOnClickListener {
                dialog.dismiss()
                finishAffinity()
            }
        }
        buttonContainer.addView(negativeButton)

        dialogView.addView(buttonContainer)

        // Настраиваем и показываем диалог
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        // Устанавливаем фон и закругления
        dialog.window?.decorView?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        val rootView = dialog.window?.decorView?.findViewById<android.widget.FrameLayout>(android.R.id.content)

        if (rootView != null) {
            val background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#FFF1F3F5"))
                cornerRadius = 16.dpToPx().toFloat()
            }
            rootView.background = background

            val params = rootView.layoutParams as? android.widget.FrameLayout.LayoutParams
            params?.let {
                val marginPx = 20.dpToPx()
                it.setMargins(marginPx, marginPx, marginPx, marginPx)
                rootView.layoutParams = it
            }
        }
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ==========

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

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

        checkAndRequestAllPermissions()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("MainActivity", "Разрешение на точный будильник не предоставлено.")
            }
        }

        checkPasswordAndProceed()
    }

    override fun onResume() {
        super.onResume()
        // УБРАНО: пароль больше не запрашивается при возврате из фона
    }

    private fun checkPasswordAndProceed() {
        if (!isPasswordSet()) {
            showSetPasswordDialog()
        } else {
            showPasswordDialog()
        }
    }

    // ========== РАЗРЕШЕНИЯ ==========

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
