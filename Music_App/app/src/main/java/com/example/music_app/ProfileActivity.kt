package com.example.music_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.music_app.databinding.ActivityProfileBinding
import com.example.music_app.models.UserModel
import com.example.music_app.utils.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var imageUri: Uri? = null

    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
        binding.imgAvt.setImageURI(imageUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgAvt.setOnClickListener {
            selectImage.launch("image/*")
        }
        //Handle event node update records
        binding.saveData.setOnClickListener {
            validateData()
        }
        //Handle event node update records
        binding.changePassword.setOnClickListener{
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
        binding.backBtn.setOnClickListener{
            finish()
        }

        showUserInformation()
    }

    private fun validateData() {
        if (binding.userEmail.text.toString().isEmpty()
            || binding.userName.text.toString().isEmpty()
            || binding.userPhone.text.toString().isEmpty()
            || imageUri == null
        ) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
        } else {
            uploadImage()
        }
    }

    private fun uploadImage() {
        Config.showDialog(this)

        val storageRef = FirebaseStorage.getInstance().getReference("profile")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("profile.jpg")

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    storeData(uri.toString())
                }.addOnFailureListener {
                    Config.hideDialog()
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Config.hideDialog()
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeData(imageUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef = FirebaseDatabase.getInstance("https://music-stream-ef950-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference.child("users").child(userId)

        val data = UserModel(
            fullname = binding.userName.text.toString(),
            email = FirebaseAuth.getInstance().currentUser!!.email ?: "",
            phone = binding.userPhone.text.toString(),
            image = imageUrl
        )

        userRef.setValue(data).addOnCompleteListener {
            Config.hideDialog()
            if (it.isSuccessful) {
                Toast.makeText(this, "Update Successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, it.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUserInformation() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance("https://music-stream-ef950-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference.child("users")
            .child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    if (userModel != null) {
                        binding.userName.setText(userModel.fullname)
                        binding.userEmail.setText(userModel.email)
                        binding.userPhone.setText(userModel.phone)
                        if (userModel.image != null && !isDestroyed) {
                            Glide.with(this@ProfileActivity)
                                .load(userModel.image)
                                .apply(RequestOptions.circleCropTransform())
                                .into(binding.imgAvt)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }
}
