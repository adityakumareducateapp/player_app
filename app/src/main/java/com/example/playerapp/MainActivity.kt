package com.example.playerapp



import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editText = findViewById<EditText>(R.id.editText)
        val playButton = findViewById<Button>(R.id.playButton)

        playButton.setOnClickListener {
            val url = editText.text.toString().trim()

            if (url.isNotEmpty()) {
                // URL se URI banao
                val uri = Uri.parse(url)

                // PlayerActivity ko start karne ke liye Intent banao
                val intent = Intent(this@MainActivity, PlayerActivity::class.java)
                intent.data = uri

                // PlayerActivity ko start karo
                startActivity(intent)
            } else {
                // Agar URL khali hai toh appropriate message display karo
                // Jaise ki toast ya koi error message
            }
        }
    }
}
