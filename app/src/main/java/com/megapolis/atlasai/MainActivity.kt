package com.megapolis.atlasai

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var status: TextView
    private lateinit var commandInput: EditText
    private lateinit var tts: TextToSpeech
    private val audio by lazy { getSystemService(AUDIO_SERVICE) as AudioManager }
    private var language = "uz"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        buildScreen()
    }

    private fun buildScreen() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 30, 24, 24)
        }
        page.addView(TextView(this).apply {
            text = "ATLAS AI"
            textSize = 32f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 4)
        })
        page.addView(TextView(this).apply {
            text = "Шахсий овозли ёрдамчи"
            textSize = 14f
            gravity = Gravity.CENTER
        })
        status = TextView(this).apply {
            text = "Тайёр. Микрофон тугмасини босинг."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 18)
        }
        page.addView(status)
        commandInput = EditText(this).apply {
            hint = "Буйруқни шу ерга ёзинг..."
            minLines = 2
        }
        page.addView(commandInput, LinearLayout.LayoutParams(-1, -2))
        addButton(page, "🎙  ГАПИРИШ") { startListening() }
        addButton(page, "БУЙРУҚНИ БАЖАРИШ") { runCommand(commandInput.text.toString()) }
        addButton(page, "ЎЗБЕКЧА / РУСЧА") {
            language = if (language == "uz") "ru" else "uz"
            tts.language = if (language == "ru") Locale("ru", "RU") else Locale("uz", "UZ")
            speak(if (language == "ru") "Русский язык выбран" else "Ўзбек тили танланди")
        }
        addButton(page, "ОВОЗНИ КЎТАРИШ") { adjustVolume(true) }
        addButton(page, "ОВОЗНИ ПАСАЙТИРИШ") { adjustVolume(false) }
        addButton(page, "ПАУЗА / ТЎХТАТИШ") {
            tts.stop()
            status.text = "ATLAS вақтинча тўхтатилди."
        }
        page.addView(TextView(this).apply {
            text = "Ҳозирги версия: овозли буйруқлар илова очиқ турганида ишлайди. Экран ўчганда «ATLAS» деб уйғотиш ва AI интернет хизмати кейинги босқичда қўшилади."
            textSize = 12f
            setPadding(0, 22, 0, 0)
        })
        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun addButton(parent: LinearLayout, label: String, action: () -> Unit) {
        parent.addView(Button(this).apply {
            text = label
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 8 })
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (language == "ru") "ru-RU" else "uz-UZ")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "ATLAS тинглаяпти...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try {
            startActivityForResult(intent, 101)
        } catch (_: Exception) {
            respond("Овозни таниш хизмати қурилмада мавжуд эмас.")
        }
    }

    @Deprecated("Kept for Android compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            commandInput.setText(spoken)
            runCommand(spoken)
        }
    }

    private fun runCommand(raw: String) {
        val text = raw.trim().lowercase(Locale.ROOT)
        if (text.isBlank()) {
            respond("Буйруқ киритилмаган.")
            return
        }
        when {
            text.contains("telegram") || text.contains("телеграм") -> openApp("org.telegram.messenger", "Telegram")
            text.contains("youtube") || text.contains("ютуб") -> openApp("com.google.android.youtube", "YouTube")
            text.contains("браузер") || text.contains("интернет") || text.contains("browser") -> {
                startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com")))
                respond("Браузер очилди.")
            }
            text.contains("смс") || text.contains("хабар") || text.contains("sms") -> {
                startActivity(Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:")))
                respond("SMS ёзиш ойнаси очилди.")
            }
            text.contains("қўнғироқ") || text.contains("телефон") || text.contains("звон") || text.contains("call") -> {
                startActivity(Intent(Intent.ACTION_DIAL))
                respond("Қўнғироқ қилиш ойнаси очилди.")
            }
            text.contains("будильник") || text.contains("алярм") || text.contains("alarm") -> {
                startActivity(Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS))
                respond("Будильник очилди.")
            }
            text.contains("овозни кўтар") || text.contains("громче") -> adjustVolume(true)
            text.contains("овозни пасайт") || text.contains("тише") -> adjustVolume(false)
            text.contains("тўхта") || text.contains("стоп") || text.contains("stop") -> {
                tts.stop()
                status.text = "ATLAS тўхтатилди."
            }
            else -> respond("Бу буйруқ ҳозирча қўллаб-қувватланмайди. Telegram, YouTube, браузер, SMS, қўнғироқ, будильник ёки овоз буйруғини айтинг.")
        }
    }

    private fun openApp(packageName: String, appName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
            respond("$appName очилди.")
        } else respond("$appName иловаси топилмади.")
    }

    private fun adjustVolume(up: Boolean) {
        audio.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (up) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
        respond(if (up) "Овоз кўтарилди." else "Овоз пасайтирилди.")
    }

    private fun respond(message: String) {
        status.text = message
        speak(message)
    }

    private fun speak(message: String) {
        if (::tts.isInitialized) tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "atlas-response")
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            tts.language = Locale("uz", "UZ")
            if (::status.isInitialized) status.text = "ATLAS AI тайёр."
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
