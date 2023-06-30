package com.example.prototype

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.AsyncQueryHandler
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Camera
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import coil.imageLoader
import coil.load
import coil.transform.CircleCropTransformation
import com.bumptech.glide.Glide
import com.example.prototype.databinding.ActivityItemAddBinding
import com.example.prototype.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

class ItemAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemAddBinding
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private lateinit var dbRef: DatabaseReference
    private lateinit var etItemName: EditText
    private lateinit var etDate: EditText
    private lateinit var etItemDescription: EditText
    private lateinit var btnSaveData: Button
    private lateinit var spinnerNames: Spinner
    private lateinit var namesListener: ValueEventListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // creating a storage reference
        storageRef = FirebaseStorage.getInstance().getReference("Images")

        firebaseAuth = FirebaseAuth.getInstance()

        etItemName = findViewById(R.id.etItemName)
        etDate = findViewById(R.id.etDate)
        etItemDescription = findViewById(R.id.etItemDescription)
        btnSaveData = findViewById(R.id.btnSave)

        dbRef = FirebaseDatabase.getInstance().getReference("Item")

        btnSaveData.setOnClickListener {
            saveItemData()
        }


        binding.addItemBtn.setOnClickListener {
                // PICK INTENT picks item from data
                // and returned selected item
                val galleryIntent = Intent(Intent.ACTION_PICK)
                // here item is type of image
                galleryIntent.type = "image/*"
                // ActivityResultLauncher callback

                imagePickerActivityResult.launch(galleryIntent)
            }


        }

    private fun saveItemData() {

        //getting values
        val itemName = etItemName.text.toString()
        val itemDate = etDate.text.toString()
        val itemDescription = etItemDescription.text.toString()


        if (itemName.isEmpty()) {
            etItemName.error = "Please enter item name"
        }
        if (itemDate.isEmpty()) {
            etDate.error = "Please enter date"
        }
        if (itemDescription.isEmpty()) {
            etItemDescription.error = "Please enter description"
        }

        val itemId = dbRef.push().key!!

        val item = ItemModel(itemId, itemName, itemDate, itemDescription)

        dbRef.child(itemId).setValue(item)
            .addOnCompleteListener {
                Toast.makeText(this, "Data inserted successfully", Toast.LENGTH_LONG).show()

                etItemName.text.clear()
                etDate.text.clear()
                etItemDescription.text.clear()


            }.addOnFailureListener { err ->
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
            }
    }

    private var imagePickerActivityResult: ActivityResultLauncher<Intent> =
    // lambda expression to receive a result back, here we
        // receive single item(photo) on selection
        registerForActivityResult( ActivityResultContracts.StartActivityForResult()) { result ->
            if (result != null) {
                // getting URI of selected Image
                val imageUri: Uri? = result.data?.data

                // val fileName = imageUri?.pathSegments?.last()

                // extract the file name with extension
                val sd = getFileName(applicationContext, imageUri!!)

                // Upload Task with upload to directory 'file'
                // and name of the file remains same
                val uploadTask = storageRef.child("file/$sd").putFile(imageUri)

                // On success, download the file URL and display it
                uploadTask.addOnSuccessListener {
                    // using glide library to display the image
                    storageRef.child("upload/$sd").downloadUrl.addOnSuccessListener {
                        Glide.with(this@ItemAddActivity)
                            .load(it)
                            .into(binding.iconIv)

                        Log.e("Firebase", "download passed")
                    }.addOnFailureListener {
                        Log.e("Firebase", "Failed in downloading")
                    }
                }.addOnFailureListener {
                    Log.e("Firebase", "Image Upload fail")
                }
            }
        }

    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String? {
        val test=20;
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (cursor != null) {
                    if(cursor.moveToFirst()) {
                        return@use cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    } else {
                        return uri.path?.lastIndexOf('/')?.let { uri.path?.substring(it) }
                    }
                } else {
                    return uri.path?.lastIndexOf('/')?.let { uri.path?.substring(it) }
                }
            }
        }
        return uri.path?.lastIndexOf('/')?.let { uri.path?.substring(it) }
    }

}








