package com.example.uploadimageprofile.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.uploadimageprofile.R
import com.example.uploadimageprofile.api.ApiClient
import com.example.uploadimageprofile.util.RealPathUtil
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException

class UploadImageActivity : AppCompatActivity() {
    private var imgPreview: CircleImageView? = null
    private var btnChooseFile: Button? = null
    private var btnUpload: Button? = null
    private var selectedImageUri: Uri? = null
    private var imageFile: File? = null
    private var userId = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_image)

        imgPreview = findViewById(R.id.imgPreview)
        btnChooseFile = findViewById(R.id.btnChoose)
        btnUpload = findViewById(R.id.btnUpload)

        // Nếu bạn truyền userId từ ProfileActivity
        val idFromIntent = intent.getIntExtra("user_id", -1)
        if (idFromIntent != -1) {
            userId = idFromIntent
        }

        btnChooseFile?.setOnClickListener { checkPermissionAndOpenGallery() }
        btnUpload?.setOnClickListener {
            if (imageFile == null) {
                Toast.makeText(this, "Bạn chưa chọn ảnh", Toast.LENGTH_SHORT).show()
            } else {
                uploadImage()
            }
        }
    }

    /* ------------------ 1. Xin quyền & mở thư viện ------------------ */
    private fun checkPermissionAndOpenGallery() {
        val permission: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            // Android 12 trở xuống
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                REQUEST_CODE_PERMISSION
            )
        } else {
            openGallery()
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    /* ------------------ 2. Nhận Uri ảnh chọn xong ------------------ */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let { uri ->
                // Hiển thị preview
                imgPreview?.setImageURI(uri)

                // Dùng RealPathUtil lấy đường dẫn thật
                val realPath = RealPathUtil.getRealPath(this, uri)
                if (realPath != null) {
                    imageFile = File(realPath)
                } else {
                    Toast.makeText(this, "Không lấy được đường dẫn file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /* ------------------ 3. Kết quả xin quyền ------------------ */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để chọn ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /* ------------------ 4. Gửi file lên server bằng Retrofit ------------------ */
    private fun uploadImage() {
        imageFile?.let { file ->
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("images", file.name, requestFile)

            val idBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val apiService = ApiClient.apiService
            val call = apiService.uploadProfileImage(body, idBody)

            btnUpload?.isEnabled = false
            btnUpload?.text = "Đang upload..."

            call.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                    btnUpload?.isEnabled = true
                    btnUpload?.text = "UPLOAD IMAGES"

                    if (response.isSuccessful && response.body() != null) {
                        try {
                            // Lấy raw text server trả về (có thể là "success" hoặc Notice...)
                            val result = response.body()!!.string()
                            Toast.makeText(
                                this@UploadImageActivity,
                                "Server trả về: $result", Toast.LENGTH_LONG
                            ).show()
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@UploadImageActivity,
                                "Lỗi đọc phản hồi server", Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@UploadImageActivity,
                            "Upload thất bại: ${response.code()}", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    btnUpload?.isEnabled = true
                    btnUpload?.text = "UPLOAD IMAGES"
                    Toast.makeText(
                        this@UploadImageActivity,
                        "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } ?: Toast.makeText(this, "Bạn chưa chọn ảnh hợp lệ", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 100
        private const val REQUEST_CODE_PERMISSION = 200
    }
}