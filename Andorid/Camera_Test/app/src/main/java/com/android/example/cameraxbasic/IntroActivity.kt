package com.android.example.cameraxbasic

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.HandlerCompat.postDelayed
import com.android.example.cameraxbasic.utils.FLAGS_FULLSCREEN

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)



        var handler = Handler()
        handler . postDelayed ({
            var intent = Intent(this, MainActivity::class.java)
            startActivity (intent)
        }, 4000)
    }

    override fun onPause() {
        super.onPause()
        finish ()
    }

}
