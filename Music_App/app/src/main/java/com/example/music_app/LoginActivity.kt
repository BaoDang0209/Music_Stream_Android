package com.example.music_app

import com.example.music_app.LoginActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.music_app.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

/*
* Author: Ngô Phạm Quang Vinh
* Main fuction: Login with Firebase*/

class LoginActivity : AppCompatActivity() {
    lateinit var  binding: ActivityLoginBinding
    private var isPasswordVisible: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEdittext.text.toString()
            val password = binding.passwordEdittext.text.toString()
//Kiem tra email hop le
            if(!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(),email)){
                binding.emailEdittext.setError("Invalid email")
                return@setOnClickListener
            }
//Kiem tra do dai mat khau
            if(password.length < 6){
                binding.passwordEdittext.setError("Length should be 6 char")
                return@setOnClickListener
            }
//Kiem tra mat khau co chu cai viet Hoa
            if (!password.matches(Regex(".*[A-Z].*"))) {
                binding.passwordEdittext.error = "Must contain at least one uppercase letter"
                return@setOnClickListener
            }
//Kiem tra mat khau co chu cai viet thuong
            if (!password.matches(Regex(".*[a-z].*"))) {
                binding.passwordEdittext.error = "Must contain at least one lowercase letter"
                return@setOnClickListener
            }

            loginWithFirebase(email,password)
        }

        binding.gotoSignupBtn.setOnClickListener {
            startActivity(Intent(this,SignupActivity::class.java))
        }
        binding.passwordToggle.setOnClickListener {
            togglePasswordVisibility()
        }
    }
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.passwordEdittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.passwordToggle.setImageResource(R.drawable.ic_eye_open)
        } else {
            binding.passwordEdittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.passwordToggle.setImageResource(R.drawable.ic_eye_closed)
        }
        binding.passwordEdittext.setSelection(binding.passwordEdittext.text.length)
        isPasswordVisible = !isPasswordVisible
    }

    fun loginWithFirebase(email : String,password: String){
        setInProgress(true)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                setInProgress(false)
                startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                finish()
            }.addOnFailureListener {
                setInProgress(false)
                Toast.makeText(applicationContext,"Login account failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        FirebaseAuth.getInstance().currentUser?.apply {
            startActivity(Intent(this@LoginActivity,MainActivity::class.java))
            finish()
        }
    }

    fun setInProgress(inProgress : Boolean){
        if(inProgress){
            binding.loginBtn.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.loginBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}
