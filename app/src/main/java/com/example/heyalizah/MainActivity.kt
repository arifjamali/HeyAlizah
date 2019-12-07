package com.example.heyalizah

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import kotlinx.android.synthetic.main.activity_main.*





class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mAssistant: Button = findViewById (R.id.driver);
        val mCustomer : Button = findViewById (R.id.customer);

        mAssistant.setOnClickListener {
                val intent = Intent(this, Main2Activity::class.java)
                startActivity(intent)
                finish()

            }
        mCustomer.setOnClickListener {
            startActivity(Intent(this,CustomerLoginActivity::class.java))
            finish()

        }

    }

}
