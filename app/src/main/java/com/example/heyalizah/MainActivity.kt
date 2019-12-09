package com.example.heyalizah

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mAssistant: Button = findViewById (R.id.driver);
        val mCustomer : Button = findViewById (R.id.customer);

        mAssistant.setOnClickListener {
                val intent = Intent(this, AssistantLoginActivity::class.java)
                startActivity(intent)
                finish()

            }
        mCustomer.setOnClickListener {
            val intent = Intent(this, CustomerLoginActivity::class.java)
            startActivity(intent)
            finish()

        }

    }

}
