package com.example.chatgptapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import android.speech.tts.TextToSpeech
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private val client = OkHttpClient()
    // creating variables on below line.
    lateinit var txtResponse: TextView
    lateinit var idTVQuestion: TextView
    lateinit var etQuestion: EditText
    lateinit var btnSubmit : FloatingActionButton
//     lateinit var textt:String
    private var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etQuestion=findViewById(R.id.etQuestion)
        btnSubmit=findViewById(R.id.sub_btn)
        idTVQuestion=findViewById<TextView>(R.id.idTVQuestion)
        txtResponse=findViewById<TextView>(R.id.txtResponse)

        btnSubmit.setOnClickListener {
            txtResponse.text = "Please wait.."
        val question=etQuestion.text.toString().trim()
       // Toast.makeText(this,question, Toast.LENGTH_SHORT).show()
        if(question.isNotEmpty()){
        getResponse(question) { response ->
        runOnUiThread {
            txtResponse.text = "Open Ai: $response"
            speakOut(response)
        }
        }
        }
        }
       // idTILQuery

    /*    etQuestion.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {

                // setting response tv on below line.
                txtResponse.text = "Please wait.."

                // validating text
                val question = etQuestion.text.toString().trim()
                Toast.makeText(this,question, Toast.LENGTH_SHORT).show()
                if(question.isNotEmpty()){
                    getResponse(question) { response ->
                        runOnUiThread {
                            txtResponse.text = "Open Ai: $response"
                            Log.d("ererE",response)
                        }
                    }
                }
                return@OnEditorActionListener true
            }
            false
        })
*/
        tts = TextToSpeech(this, this)
    }
    fun getResponse(question: String, callback: (String) -> Unit){

        // setting text on for question on below line.
        idTVQuestion.text ="Subha: \n \n$question"
        etQuestion.setText("")

        val apiKey="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"
        val url="https://api.openai.com/v1/engines/text-davinci-003/completions"

        val requestBody="""
            {
            "prompt": "$question",
            "max_tokens": 500,
            "temperature": 0
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error","API failed",e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body=response.body?.string()
                if (body != null) {
                    Log.v("data",body)
                }
                else{
                    Log.v("data","empty")
                }
                val jsonObject= JSONObject(body)
                val jsonArray: JSONArray =jsonObject.getJSONArray("choices")
                val textResult=jsonArray.getJSONObject(0).getString("text")
                callback(textResult)
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.getDefault())

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            } else {
                //btnSpeak!!.isEnabled = true
                tts!!.setSpeechRate(0.75F)
               // tts!!.setPitch(0.7F)

            }
        }
    }

    private fun speakOut(  textt:String) {

        tts!!.speak(textt, TextToSpeech.QUEUE_FLUSH, null,"")
    }

    public override fun onDestroy() {
        // Shutdown TTS when
        // activity is destroyed
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
}