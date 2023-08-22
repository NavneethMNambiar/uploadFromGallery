package com.example.imgstut

import ApiService
import ApiService2
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.imgstut.databinding.ActivityMainBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var button: Button
    private lateinit var button2: Button
    private lateinit var imageView: ImageView
    private lateinit var descriptionEditText: EditText

    companion object {
        const val VIDEO_REQUEST_CODE = 1_001
        const val IMAGE_REQUEST_CODE = 1_000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        button = findViewById(R.id.button)
//        button2 = findViewById(R.id.button2)
        binding.pickImageButton.setOnClickListener {
            pickImageFromGallery()
        }
        binding.pickvideobutton.setOnClickListener {
            pickVideoFromGallery()
        }
        imageView = findViewById(R.id.imageView)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        button.setOnClickListener {
            sendFormData()
        }
//        button2.setOnClickListener{
//            sendVideoData(videoUri)
//        }
    }
    private fun sendFormData() {
        val description = descriptionEditText.text.toString().trim()

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a description.", Toast.LENGTH_SHORT).show()
            return
        }

        val imageFile = createImageFile()
        val imageByteArray = getImageByteArrayFromImageView(imageView)
        writeByteArrayToFile(imageFile, imageByteArray)
        val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, imageRequestBody)

        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

        val retrofit = Retrofit.Builder()
            .baseUrl("https://8992-103-160-233-145.ngrok-free.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.uploadImageAndDescription(imagePart, descriptionRequestBody)


        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    if (!responseBody.isNullOrEmpty()) {
                        // Handle the string response
                        Log.d("Response", responseBody)
                        Toast.makeText(this@MainActivity, "UPLOAD SUCCESSFUL", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Response not successful.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Error occurred
                Toast.makeText(this@MainActivity, "Error occurred: ${t.message} ", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(cacheDir, "Image_$timeStamp.jpg")
    }

    private fun getImageByteArrayFromImageView(imageView: ImageView): ByteArray {
        val drawable = imageView.drawable
        val bitmap: Bitmap = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> {
                val width = drawable.intrinsicWidth
                val height = drawable.intrinsicHeight
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return outputStream.toByteArray()
    }

    private fun writeByteArrayToFile(file: File, byteArray: ByteArray) {
        file.outputStream().use {
            it.write(byteArray)
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            binding.imageView.setImageURI(data?.data)
        }
        else if (requestCode == VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {
                // Handle the video URI, you may want to set it to a VideoView or handle it accordingly
                val videoUri = data?.data
                if (videoUri != null) {
                    // Set the video URI to your video view or perform other operations
                    sendVideoData(videoUri)
                }
            }
    }

//    private fun sendVideoData(videoUri: Uri) {
//        val description = descriptionEditText.text.toString().trim()
//
//        if (description.isEmpty()) {
//            Toast.makeText(this, "Please enter a description.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val videoFile = createVideoFile()
//        // Assuming you have a method to get the video file data as a ByteArray, similar to the image case
////        val videoByteArray = getVideoByteArrayFromFile(videoFile)
////        writeVideoByteArrayToFile(videoFile, videoByteArray)
//        val videoRequestBody = videoFile.asRequestBody("video/*".toMediaTypeOrNull())
//        val videoPart = MultipartBody.Part.createFormData("file", videoFile.name, videoRequestBody)
//
//        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://8992-103-160-233-145.ngrok-free.app/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        val apiService = retrofit.create(ApiService2::class.java)
//        val call = apiService.uploadVideoAndDescription(videoPart, descriptionRequestBody)
//
//        call.enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.isSuccessful) {
//                    val responseBody = response.body()?.string()
//                    if (!responseBody.isNullOrEmpty()) {
//                        // Handle the string response
//                        Log.d("Response", responseBody)
//                        Toast.makeText(this@MainActivity, "UPLOAD SUCCESSFUL", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Toast.makeText(this@MainActivity, "Response not successful.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                // Error occurred
//                Toast.makeText(this@MainActivity, "Error occurred: ${t.message} ", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
    private fun createVideoFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return File(cacheDir, "Video_$timeStamp.mp4")
    }
//
//    // Replace this with your method to get the video file data as a ByteArray
//    private fun getVideoByteArrayFromFile(videoFile: File): ByteArray {
//        val inputStream: FileInputStream
//        try {
//            // Open the video file input stream
//            inputStream = FileInputStream(videoFile)
//
//            // Create a buffer to read the data in chunks
//            val bufferSize = 1024
//            val buffer = ByteArray(bufferSize)
//            val byteArrayOutputStream = ByteArrayOutputStream()
//
//            // Read video data into the buffer and write it to the ByteArrayOutputStream
//            var bytesRead: Int
//            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
//                byteArrayOutputStream.write(buffer, 0, bytesRead)
//            }
//
//            // Close the input stream and return the ByteArray
//            inputStream.close()
//            return byteArrayOutputStream.toByteArray()
//        } catch (e: IOException) {
//            // Handle any exceptions, e.g., file not found, permissions, etc.
//            e.printStackTrace()
//            return ByteArray(0)
//        }
//    }
//
private fun sendVideoData(videoUri: Uri) {
    val description = descriptionEditText.text.toString().trim()

    if (description.isEmpty()) {
        Toast.makeText(this, "Please enter a description.", Toast.LENGTH_SHORT).show()
        return
    }

    val videoFile = createVideoFile()
    val videoByteArray = getVideoByteArrayFromUri(videoUri)
    writeVideoByteArrayToFile(videoFile, videoByteArray)
    val videoRequestBody = videoFile.asRequestBody("video/*".toMediaTypeOrNull())
    val videoPart = MultipartBody.Part.createFormData("file", videoFile.name, videoRequestBody)

    val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

    val retrofit = Retrofit.Builder()
        .baseUrl("https://8992-103-160-233-145.ngrok-free.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(ApiService2::class.java)
    val call = apiService.uploadVideoAndDescription(videoPart, descriptionRequestBody)

    call.enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                if (!responseBody.isNullOrEmpty()) {
                    // Handle the string response
                    Log.d("Response", responseBody)
                    Toast.makeText(this@MainActivity, "UPLOAD SUCCESSFUL", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Response not successful.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            // Error occurred
            Toast.makeText(this@MainActivity, "Error occurred: ${t.message} ", Toast.LENGTH_SHORT).show()
        }
    })
}

    // ... (other methods)

    private fun getVideoByteArrayFromUri(videoUri: Uri): ByteArray {
        val inputStream: InputStream
        try {
            // Open the video URI input stream
            inputStream = contentResolver.openInputStream(videoUri)!!

            // Create a buffer to read the data in chunks
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            val byteArrayOutputStream = ByteArrayOutputStream()

            // Read video data into the buffer and write it to the ByteArrayOutputStream
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead)
            }

            // Close the input stream and return the ByteArray
            inputStream.close()
            return byteArrayOutputStream.toByteArray()
        } catch (e: IOException) {
            // Handle any exceptions, e.g., file not found, permissions, etc.
            e.printStackTrace()
            return ByteArray(0)
        }
    }
    private fun writeVideoByteArrayToFile(file: File, byteArray: ByteArray) {
        file.outputStream().use {
            it.write(byteArray)
        }
    }

    private fun pickVideoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startActivityForResult(intent, VIDEO_REQUEST_CODE)
    }

}
