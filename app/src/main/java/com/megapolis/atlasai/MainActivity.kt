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
        val navy = Color.rgb(5, 13, 31)
        val blue = Color.rgb(0, 130, 235)
        val cyan = Color.rgb(35, 195, 255)
        val muted = Color.rgb(165, 190, 220)
        fun panel(color: Int, radius: Float, border: Int = Color.TRANSPARENT) =
            android.graphics.drawable.GradientDrawable().apply {
                setColor(color); cornerRadius = radius
                if (border != Color.TRANSPARENT) setStroke(1, border)
            }
        fun text(value: String, size: Float, color: Int, bold: Boolean = false) =
            TextView(this).apply {
                text = value; textSize = size; setTextColor(color)
                if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
        window.statusBarColor = navy
        window.navigationBarColor = navy
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 28)
            setBackgroundColor(navy)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val logo = text("T", 30f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(blue, cyan)).apply { cornerRadius = 22f }
        }
        header.addView(logo, LinearLayout.LayoutParams(58, 58))
        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 0, 0, 0)
            addView(text("TIMUR", 25f, Color.WHITE, true))
            addView(text("Шахсий AI ёрдамчи", 12f, muted))
        }
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val lang = text("UZ  ⇄  RU", 13f, cyan, true).apply {
            setPadding(12, 12, 12, 12); gravity = Gravity.CENTER
            background = panel(Color.rgb(13, 29, 55), 30f, Color.rgb(30, 90, 145))
            setOnClickListener {
                language = if (language == "uz") "ru" else "uz"
                tts.language = if (language == "ru") Locale("ru", "RU") else Locale("uz", "UZ")
                this.text = if (language == "uz") "UZ  ⇄  RU" else "RU  ⇄  UZ"
                speak(if (language == "ru") "Русский язык выбран" else "Ўзбек тили танланди")
            }
        }
        header.addView(lang)
        page.addView(header)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setPadding(18, 26, 18, 24)
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(12, 47, 91), Color.rgb(9, 21, 45))).apply {
                cornerRadius = 30f; setStroke(1, Color.rgb(25, 93, 150))
            }
        }
        val orb = text("T", 56f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(blue, cyan)).apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setStroke(4, Color.rgb(110, 225, 255))
            }
        }
        hero.addView(orb, LinearLayout.LayoutParams(132, 132))
        hero.addView(text("TIMUR СИЗНИ ТИНГЛАЯПТИ", 12f, cyan, true).apply {
            gravity = Gravity.CENTER; setPadding(0, 20, 0, 8)
        })
        status = text("Тайёрман. Менга буйруқ беринг.", 17f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER; setPadding(8, 4, 8, 8)
        }
        hero.addView(status)
        hero.addView(text("Микрофон тугмасини босиб гапиринг", 12f, muted).apply {
            gravity = Gravity.CENTER
        })
        page.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 24 })

        val mic = text("🎙   ГАПИРИШ", 17f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER; setPadding(16, 18, 16, 18)
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(blue, Color.rgb(0, 190, 235))).apply { cornerRadius = 28f }
            setOnClickListener { startListening() }
        }
        page.addView(mic, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 18 })
        val inputCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(16, 14, 16, 14)
            background = panel(Color.rgb(13, 27, 54), 22f, Color.rgb(30, 58, 93))
        }
        inputCard.addView(text("МАТНЛИ БУЙРУҚ", 11f, cyan, true))
        commandInput = EditText(this).apply {
            hint = "Масалан: Telegram’ни оч"; setHintTextColor(muted)
            setTextColor(Color.WHITE); textSize = 15f; minLines = 1; maxLines = 3
            setPadding(0, 8, 0, 8); background = null
        }
        inputCard.addView(commandInput)
        val run = text("БУЙРУҚНИ БАЖАРИШ  →", 13f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER; setPadding(12, 14, 12, 14)
            background = panel(Color.rgb(25, 75, 125), 16f)
            setOnClickListener { runCommand(commandInput.text.toString()) }
        }
        inputCard.addView(run)
        page.addView(inputCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 16 })
        page.addView(text("ТЕЗКОР БОШҚАРУ", 12f, muted, true).apply {
            setPadding(2, 24, 0, 10)
        })
        addButton(page, "✈  TELEGRAM") { openApp("org.telegram.messenger", "Telegram") }
        addButton(page, "▶  YOUTUBE") { openApp("com.google.android.youtube", "YouTube") }
        addButton(page, "☎  ҚЎНҒИРОҚ") { startActivity(Intent(Intent.ACTION_DIAL)) }
        addButton(page, "◷  БУДИЛЬНИК") { startActivity(Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)) }
        addButton(page, "ОВОЗНИ КЎТАРИШ") { adjustVolume(true) }
        addButton(page, "ОВОЗНИ ПАСАЙТИРИШ") { adjustVolume(false) }
        addButton(page, "ПАУЗА / ТЎХТАТИШ") {
            tts.stop(); status.text = "Timur вақтинча тўхтатилди."
        }
        page.addView(text("TIMUR  •  СИЗНИНГ ОВОЗЛИ ЁРДАМЧИНГИЗ", 10f, muted).apply {
            gravity = Gravity.CENTER; setPadding(0, 24, 0, 0)
        })
        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun addButton(parent: LinearLayout, label: String, action: () -> Unit) {
        parent.addView(Button(this).apply {
            text = label
            textSize = 14f
            setTextColor(Color.WHITE)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.rgb(13, 32, 61))
                cornerRadius = 22f
                setStroke(1, Color.rgb(30, 80, 130))
            }
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 9 })
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
