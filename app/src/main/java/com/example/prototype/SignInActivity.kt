package com.example.prototype

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.prototype.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
private lateinit var binding: ActivitySignInBinding
private lateinit var actionBar: ActionBar
private lateinit var progressDialog: ProgressDialog
private lateinit var firebaseAuth: FirebaseAuth
private var email = ""
private var password = ""

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivitySignInBinding.inflate(layoutInflater)
    setContentView(binding.root)

    actionBar = supportActionBar!!
    actionBar.title="Login"

    progressDialog = ProgressDialog(this)
    progressDialog.setTitle("Please wait")
    progressDialog.setMessage("Logging In...")
    progressDialog.setCanceledOnTouchOutside(false)

    firebaseAuth = FirebaseAuth.getInstance()
    checkUser()

    binding.noAccount.setOnClickListener {
        startActivity(Intent(this, SignUpActivity::class.java))
    }
    binding.loginBtn.setOnClickListener{

        validateData()
    }
}

private fun validateData() {
    email = binding.emailEt.text.toString().trim()
    password = binding.passwordET.text.toString().trim()

    if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
        binding.emailEt.error = "Invalid Email Format"
    }
    else if (TextUtils.isEmpty(password)){
        binding.passwordET.error = "Please Enter Password"
    }
    else{
        firebaseLogin()
    }
}

private fun firebaseLogin() {
    progressDialog.show()
    firebaseAuth.signInWithEmailAndPassword(email,password)
        .addOnSuccessListener {

            progressDialog.dismiss()
            val firebaseUser = firebaseAuth.currentUser
            val email = firebaseUser!!.email
            Toast.makeText(this, "Logged In as $email", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()

        }
        .addOnFailureListener { e->

            progressDialog.dismiss()
            Toast.makeText(this, "Login Failed due to ${e.message} ", Toast.LENGTH_SHORT).show()
        }
}

private fun checkUser() {
    val firebaseUser = firebaseAuth.currentUser
    if(firebaseUser != null ){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

}
