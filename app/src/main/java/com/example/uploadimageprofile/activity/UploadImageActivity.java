package com.example.uploadimageprofile.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.uploadimageprofile.R;
import com.example.uploadimageprofile.api.ApiClient;
import com.example.uploadimageprofile.api.ApiService;
import com.example.uploadimageprofile.model.ApiMessage;
import com.example.uploadimageprofile.util.RealPathUtil;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadImageActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int REQUEST_CODE_PERMISSION = 200;

    private CircleImageView imgPreview;
    private Button btnChooseFile, btnUpload;
    private Uri selectedImageUri;
    private File imageFile;
    private int userId = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        imgPreview   = findViewById(R.id.imgPreview);
        btnChooseFile = findViewById(R.id.btnChoose);
        btnUpload     = findViewById(R.id.btnUpload);

        // Nếu bạn truyền userId từ ProfileActivity
        int idFromIntent = getIntent().getIntExtra("user_id", -1);
        if (idFromIntent != -1) {
            userId = idFromIntent;
        }

        btnChooseFile.setOnClickListener(v -> checkPermissionAndOpenGallery());
        btnUpload.setOnClickListener(v -> {
            if (imageFile == null) {
                Toast.makeText(this, "Bạn chưa chọn ảnh", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        });
    }

    /* ------------------ 1. Xin quyền & mở thư viện ------------------ */

    private void checkPermissionAndOpenGallery() {
        String permission;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            // Android 12 trở xuống
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{permission},
                    REQUEST_CODE_PERMISSION
            );
        } else {
            openGallery();
        }
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    /* ------------------ 2. Nhận Uri ảnh chọn xong ------------------ */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Hiển thị preview
                imgPreview.setImageURI(selectedImageUri);

                // Dùng RealPathUtil lấy đường dẫn thật
                String realPath = RealPathUtil.getRealPath(this, selectedImageUri);
                if (realPath != null) {
                    imageFile = new File(realPath);
                } else {
                    Toast.makeText(this, "Không lấy được đường dẫn file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /* ------------------ 3. Kết quả xin quyền ------------------ */

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* ------------------ 4. Gửi file lên server bằng Retrofit ------------------ */

    private void uploadImage() {
        if (imageFile == null) {
            Toast.makeText(this, "Bạn chưa chọn ảnh hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestFile =
                RequestBody.create(MediaType.parse("images/*"), imageFile);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("images", imageFile.getName(), requestFile);

        RequestBody idBody =
                RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));

        ApiService apiService = ApiClient.getApiService();
        Call<ResponseBody> call = apiService.uploadProfileImage(body, idBody);

        btnUpload.setEnabled(false);
        btnUpload.setText("Đang upload...");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnUpload.setEnabled(true);
                btnUpload.setText("UPLOAD IMAGES");

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Lấy raw text server trả về (có thể là "success" hoặc Notice...)
                        String result = response.body().string();
                        Toast.makeText(UploadImageActivity.this,
                                "Server trả về: " + result, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(UploadImageActivity.this,
                                "Lỗi đọc phản hồi server", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UploadImageActivity.this,
                            "Upload thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnUpload.setEnabled(true);
                btnUpload.setText("UPLOAD IMAGES");
                Toast.makeText(UploadImageActivity.this,
                        "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
