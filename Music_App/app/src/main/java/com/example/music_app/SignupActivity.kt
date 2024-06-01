package com.example.music_app

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.music_app.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

/*
* Author: Ngô Phạm Quang Vinh
* Main fuction: Signup with Firebase*/
class SignupActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupBinding
    private var isPasswordVisible: Boolean = false // Khai báo biến isPasswordVisible

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createAccountBtn.setOnClickListener {
            val email = binding.emailEdittext.text.toString()
            val password = binding.passwordEdittext.text.toString()
            val confirmPassword = binding.confirmPasswordEdittext.text.toString()
            val phoneNumber = binding.phoneEdittext.text.toString()

            // Kiểm tra email hợp lệ
            if (!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(), email)) {
                binding.emailEdittext.error = "Invalid email"
                return@setOnClickListener
            }

            // Kiểm tra độ dài mật khẩu
            if (password.length < 6) {
                binding.passwordEdittext.error = "Length should be 6 char"
                return@setOnClickListener
            }

            // Kiểm tra mật khẩu có chữ cái viết hoa
            if (!password.matches(Regex(".*[A-Z].*"))) {
                binding.passwordEdittext.error = "Must contain at least one uppercase letter"
                return@setOnClickListener
            }

            // Kiểm tra mật khẩu có chữ cái viết thường
            if (!password.matches(Regex(".*[a-z].*"))) {
                binding.passwordEdittext.error = "Must contain at least one lowercase letter"
                return@setOnClickListener
            }

            // Kiểm tra mật khẩu xác nhận có khớp với mật khẩu không
            if (password != confirmPassword) {
                binding.confirmPasswordEdittext.error = "Mật khẩu không khớp"
                return@setOnClickListener
            }

            // Kiểm tra email hoặc số điện thoại hợp lệ
            if (email.isNotEmpty()) {
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.emailEdittext.error = "Email không hợp lệ"
                    return@setOnClickListener
                }
            } else if (phoneNumber.isNotEmpty()) {
                if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
                    binding.phoneEdittext.error = "Số điện thoại không hợp lệ"
                    return@setOnClickListener
                }
            }

            createAccountWithFirebase(email, password)
        }

        binding.gotoLoginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.passwordToggle.setOnClickListener {
            togglePasswordVisibility()
        }
        binding.confirmPasswordToggle.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.confirmPasswordEdittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.confirmPasswordToggle.setImageResource(R.drawable.ic_eye_open)
        } else {
            binding.confirmPasswordEdittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.confirmPasswordToggle.setImageResource(R.drawable.ic_eye_closed)
        }
        if (isPasswordVisible) {
            binding.passwordEdittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.passwordToggle.setImageResource(R.drawable.ic_eye_open)
        } else {
            binding.passwordEdittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.passwordToggle.setImageResource(R.drawable.ic_eye_closed)
        }
        binding.passwordEdittext.setSelection(binding.passwordEdittext.text.length)
        binding.confirmPasswordEdittext.setSelection(binding.confirmPasswordEdittext.text.length)
        isPasswordVisible = !isPasswordVisible
    }

    private fun createAccountWithFirebase(email: String, password: String) {
        setInProgress(true)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password) // Sửa đổi ở đây
            .addOnSuccessListener {
                setInProgress(false)
                startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                finish()
            }.addOnFailureListener {
                setInProgress(false)
                Toast.makeText(applicationContext, "Create account failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        FirebaseAuth.getInstance().currentUser?.apply {
            startActivity(Intent(this@SignupActivity, MainActivity::class.java))
            finish()
        }
    }

    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.createAccountBtn.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.createAccountBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}
