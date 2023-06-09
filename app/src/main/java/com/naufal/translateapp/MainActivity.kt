package com.naufal.translateapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.naufal.translateapp.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var items = arrayOf("English", "Indonesia", "Italy", "France", "Japanese")
    private var conditions = DownloadConditions.Builder().requireWifi().build()
    private val speechRec = 102
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val itemsAdapter: ArrayAdapter<String> = ArrayAdapter(
            this, android.R.layout.simple_dropdown_item_1line, items
        )

        binding.languageFrom.setAdapter(itemsAdapter)

        binding.languageTo.setAdapter(itemsAdapter)

        binding.translate.setOnClickListener {
            val options = TranslatorOptions.Builder().setSourceLanguage(selectFrom())
                .setTargetLanguage(selectTo()).build()

            val translator = Translation.getClient(options)

            translator.downloadModelIfNeeded(conditions).addOnSuccessListener {

                translator.translate(binding.tvInput.text.toString())
                    .addOnSuccessListener { translatedText ->
                        binding.tvOutput.text = translatedText
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.voiceSpeechButton.setOnClickListener {
            speechInput()
        }

        binding.ibInput.setOnClickListener {
            inputToSpeech()
        }

        binding.ibOutput.setOnClickListener {
            outputToSpeech()
        }
    }

    private fun selectFrom(): String {
        return when (binding.languageFrom.text.toString()) {
            "" -> TranslateLanguage.ENGLISH
            "English" -> TranslateLanguage.ENGLISH
            "Indonesia" -> TranslateLanguage.INDONESIAN
            "Italy" -> TranslateLanguage.ITALIAN
            "France" -> TranslateLanguage.FRENCH
            "Japanese" -> TranslateLanguage.JAPANESE
            else -> TranslateLanguage.ENGLISH
        }
    }

    private fun selectTo(): String {
        return when (binding.languageTo.text.toString()) {
            "" -> TranslateLanguage.ENGLISH
            "English" -> TranslateLanguage.ENGLISH
            "Indonesia" -> TranslateLanguage.INDONESIAN
            "Italy" -> TranslateLanguage.ITALIAN
            "France" -> TranslateLanguage.FRENCH
            "Japanese" -> TranslateLanguage.JAPANESE
            else -> TranslateLanguage.INDONESIAN
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == speechRec && resultCode == Activity.RESULT_OK){
            val result:ArrayList<String>? = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            binding.tvInput.text = Editable.Factory.getInstance().newEditable(result?.get(0).toString())

        }
    }
    private fun speechInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech Failed", Toast.LENGTH_SHORT).show()
        } else {
            val voiceSpeech = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            voiceSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            voiceSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            voiceSpeech.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak")
            startActivityForResult(voiceSpeech,speechRec)
        }
    }

    private fun inputToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(getLocaleFromLanguage(binding.languageFrom.text.toString()))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show()
                } else {
                    val text = binding.tvInput.text.toString()
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            } else {
                Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun outputToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(getLocaleFromLanguage(binding.languageFrom.text.toString()))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show()
                } else {
                    val text = binding.tvOutput.text.toString()
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            } else {
                Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLocaleFromLanguage(language: String): Locale {
        return when (language) {
            "English" -> Locale.ENGLISH
            "Indonesia" -> Locale("id", "ID")
            "Italy" -> Locale.ITALIAN
            "France" -> Locale.FRENCH
            "Japanese" -> Locale.JAPANESE
            else -> Locale.ENGLISH
        }
    }
}