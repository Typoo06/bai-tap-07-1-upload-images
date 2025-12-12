package com.example.uploadimageprofile.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.uploadimageprofile.R
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {
    private var imgAvatar: CircleImageView? = null
    private val userId = 3
    private val imageUrl = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById<View?>(R.id.header_title),
            OnApplyWindowInsetsListener { v: View?, insets: WindowInsetsCompat? ->
                val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
                v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            })
        imgAvatar = findViewById<CircleImageView>(R.id.imgAvatar)

        // Load ảnh avatar hiện tại (nếu đã có từ server)
        if (!imageUrl.isEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_users)
                .into(imgAvatar!!)
        }

        imgAvatar!!.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(this@ProfileActivity, UploadImageActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        })
    }
}
