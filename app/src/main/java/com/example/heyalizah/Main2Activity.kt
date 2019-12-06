package com.example.heyalizah

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.lang.NullPointerException

class Main2Activity : AppCompatActivity() {
    /*Firebase Reference */
    private var mDatabaseReference: DatabaseReference? = null
    lateinit var mDatabase: DatabaseReference;
    val mAuth = FirebaseAuth.getInstance();





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)


        val mLogin: Button = findViewById (R.id.login);
        val mRegistration : Button = findViewById (R.id.registration);

        mLogin.setOnClickListener(View.OnClickListener {
            view -> login();
        })
        mRegistration.setOnClickListener(View.OnClickListener {
                view -> register();
        })




    }

    private fun login(){
        val mEmail: EditText = findViewById (R.id.email);
        val mPassword : EditText = findViewById (R.id.password);

        var email = mEmail.text.toString();
        var password = mPassword.text.toString();

        if(!email.isEmpty() && !password.isEmpty()){
            mAuth.signInWithEmailAndPassword(email,password).
                addOnCompleteListener(this, OnCompleteListener {
                    task ->
                    if(task.isSuccessful){
                        startActivity(Intent(this,AssistantMapActivity::class.java))
                        Toast.makeText(this,"Sucesslly logged in: )", Toast.LENGTH_LONG).show();
                     }else{
                        Toast.makeText(this,"Error Invalid Username or Password",Toast.LENGTH_LONG).show();

                    }
                }
            )
        }else{
            Toast.makeText(this,"Please Type Email and Password:(",Toast.LENGTH_LONG).show();
        }

    }

    private fun register(){
        val rEmail: EditText = findViewById (R.id.email);
        val rPassword : EditText = findViewById (R.id.password);

        var email = rEmail.text.toString();
        var password = rPassword.text.toString();

        if(!email.isEmpty() && !password.isEmpty()){
            mAuth.createUserWithEmailAndPassword(email,password).
                addOnCompleteListener(this, OnCompleteListener {
                        task ->
                    if(task.isSuccessful){
                        val user = mAuth.currentUser
                        val uid = user?.uid.toString()
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Assistant").child(uid);
                        mDatabase.setValue(true);
                        Toast.makeText(this,"Successfully You are Registered: )", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(this,"This Account is already Registered",Toast.LENGTH_LONG).show();

                    }
                }
                )
        }else{
            Toast.makeText(this,"Please Type Email and Password:(",Toast.LENGTH_LONG).show();
        }
    }
}
