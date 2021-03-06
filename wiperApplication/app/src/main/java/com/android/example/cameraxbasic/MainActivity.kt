/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.cameraxbasic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ActionProvider
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.example.cameraxbasic.utils.FLAGS_FULLSCREEN
import java.io.File

const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"
private const val IMMERSIVE_FLAG_TIMEOUT = 500L

/**
 * Main entry point into our app. This app follows the single-activity pattern, and all
 * functionality is implemented in the form of fragments.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var container: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fullScreen()    // 전체 화면 설정
        container = findViewById(R.id.fragment_container)
    }

    override fun onResume() {
        super.onResume()
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick

        container.postDelayed({
            container.systemUiVisibility = FLAGS_FULLSCREEN
        }, IMMERSIVE_FLAG_TIMEOUT)

    }


    /** When key down event is triggered, relay it via local broadcast so fragments can handle it */
    //키 다운 이벤트가 트리거되면 조각이 처리할 수 있도록 로컬 브로드캐스트를 통해 키 다운 이벤트를 중계합니다.
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val intent = Intent(KEY_EVENT_ACTION).apply { putExtra(KEY_EVENT_EXTRA, keyCode) }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop(){
        super.onStop()
        finish()
    }

    companion object {

        /** Use external media if it is available, our app's file directory otherwise */
        /** 사용 가능한 외부 미디어, 그렇지 않은 경우 앱의 파일 디렉토리 사용 */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext


            //firstOrNull() 은 첫번째 아이템을 가져오되, 첫번째 아이템이 없으면 null을 반환합니다.
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }


            //Log.d("Output_mediaDir","${mediaDir}")
            if (mediaDir != null && mediaDir.exists()){
                Log.d("Output_mediaDir","${mediaDir}")
                return mediaDir//사용 가능한 외부 미디어


            }else{
                Log.d("Output_filesDir","${appContext.filesDir}")
                return appContext.filesDir//앱의 파일 디렉토리
            }
        }
    }

    // 전체 화면 설정하는 함수
    fun fullScreen() {
        window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }
}

