package com.example.music_app;

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.Toast
//import com.example.music_app.OTPActivity
import com.example.music_app.databinding.ActivitySignupBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class SignupActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var verificationId: String? = null
    private var isPasswordVisible: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant validation
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                setInProgress(false)
                Toast.makeText(applicationContext, "Xác thực thất bại: ${e.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                setInProgress(false)
                this@SignupActivity.verificationId = verificationId
                navigateToOTPActivity(verificationId)
            }
        }

        binding.createAccountBtn.setOnClickListener {
            val email = binding.emailEdittext.text.toString()
            val phoneNumber = binding.phoneEdittext.text.toString()
            val password = binding.passwordEdittext.text.toString()
            val confirmPassword = binding.confirmPasswordEdittext.text.toString()

            if (password.length < 6) {
                binding.passwordEdittext.error = "Độ dài phải là 6 ký tự"
                return@setOnClickListener
            }
            if (!password.matches(Regex(".*[A-Z].*"))) {
                binding.passwordEdittext.error = "Phải chứa ít nhất một chữ cái in hoa"
                return@setOnClickListener
            }
            if (!password.matches(Regex(".*[a-z].*"))) {
                binding.passwordEdittext.error = "Phải chứa ít nhất một chữ cái thường"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                binding.confirmPasswordEdittext.error = "Mật khẩu không khớp"
                return@setOnClickListener
            }

            if (email.isNotEmpty()) {
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.emailEdittext.error = "Email không hợp lệ"
                    return@setOnClickListener
                }
                // Lưu thông tin tài khoản để sau khi OTP hợp lệ mới tạo tài khoản
                navigateToOTPActivityForEmail(email, password)
            } else if (phoneNumber.isNotEmpty()) {
                if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
                    binding.phoneEdittext.error = "Số điện thoại không hợp lệ"
                    return@setOnClickListener
                }
                setInProgress(true)
                sendVerificationCode(phoneNumber)
            } else {
                Toast.makeText(this, "Vui lòng nhập email hoặc số điện thoại", Toast.LENGTH_SHORT).show()
            }
        }

        binding.gotoLoginBtn.setOnClickListener {
            finish()
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

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                setInProgress(false)
                if (task.isSuccessful) {
                    // Đăng ký thành công
                    Toast.makeText(applicationContext, "Xác thực thành công", Toast.LENGTH_SHORT).show()
                    // Chuyển hướng đến màn hình tiếp theo hoặc hoàn thành quá trình đăng ký
                } else {
                    // Nếu xác thực thất bại
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(applicationContext, "Mã OTP không hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun navigateToOTPActivity(verificationId: String) {
        val intent = Intent(this, OTPActivity::class.java).apply {
            putExtra("verificationId", verificationId)
        }
        startActivity(intent)
    }

    private fun navigateToOTPActivityForEmail(email: String, password: String) {
        val intent = Intent(this, OTPActivity::class.java).apply {
            putExtra("email", email)
            putExtra("password", password)
        }
        startActivity(intent)
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.createAccountBtn.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.createAccountBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}
