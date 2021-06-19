/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.cameraxbasic


import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


/**참조 사이트
Canvas,paint 사용 : https://ywook.tistory.com/20
Paint공식문서 : https://developer.android.com/reference/android/graphics/Paint
RGB 투명도 : https://pimi.tistory.com/5
흰 도화지 : https://black-jin0427.tistory.com/144

*/
// Stroke width for the the paint.
private const val STROKE_WIDTH = 12f

/**
 * Custom view that follows touch events to draw on a canvas.
 */
class MyCanvasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, uri: Uri? =null
) : View(context, attrs, defStyleAttr) {

    private  var bitmapWidth :Float? = null
    private  var bitmapHeight :Float? = null
    private  var canvasWidth :Float? = null
    private  var canvasHeight :Float? = null
    private  var deviceWidth :Float? = null
    private  var deviceHeight :Float? = null
    private  var marginHeight :Float? = null
    private  var marginWidth :Float? = null

    private var isErasing : Boolean =false

    private var phtoUri: Uri = uri!!
    // Holds the path you are currently drawing.
    private var path = Path()
    // 드로잉 색
    val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)//
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.light_blue_A400, null)
    private lateinit var extraBitmap: Bitmap
    //extraBitmap캔버스 그리기 API를 사용하여 캐싱 비트 맵 ( )에 그리려면
    // 캐싱 비트 맵에 대한 캐싱 캔버스 ( extraCanvas)를 만듭니다 .
    private lateinit var extraCanvas: Canvas
    //private  var bitmap :Bitmap= bitmap
    //도면을 작성할 때 이전에 그린 내용을 캐시해야합니다.
    // 데이터를 캐싱하는 방법에는 여러 가지가 있으며 하나는 비트 맵 ( extraBitmap)에 있습니다.
    // 또 다른 방법은 그린 기록을 좌표와 지침으로 저장하는 것입니다.

    //https://developer.android.com/reference/android/graphics/Rect?hl=en
    // Rect 사격형 테두리 적용(굳이 사용할 필요가 없다.)
    //private lateinit var frame: Rect

    //페인트를 설정
    // Set up the paint with which to draw.



    //색칠 설정
    private val paint = Paint().apply {

        color = drawColor
        //모양에 영향을 미치지 않고 그려진 가장자리를 다듬습니다.
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        //디더링은 기기보다 정밀도가 높은 색상이 다운샘플링되는 방식에 영향을 미친다.
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }


    /**
     * Don't draw every single pixel.
     * If the finger has has moved less than this distance, don't draw. scaledTouchSlop, returns
     * the distance in pixels a touch can wander before we think the user is scrolling.
     */
    //모든 픽셀을 그리지는 마세요.
    //손가락이 이 거리보다 적게 움직였으면 크기를 조정하지 마십시오.TouchSlop, 반환
    //사용자가 스크롤한다고 생각하기 전에 터치 한 번 할 수 있는 거리(픽셀 단위)


    /** ViewConfiguration */
    //Contains methods to standard constants used in the UI for timeouts, sizes, and distances.
    //제한 시간, 크기 및 거리에 대해 UI에서 사용되는 표준 상수에 대한 메소드를 포함합니다.
    //Distance in dips a touch can wander before we think the user is scrolling
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private var currentX = 0f
    private var currentY = 0f

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f



    /**
     * Called whenever the view changes size.
     * Since the view starts out with no size, this is also called after
     * the view has been inflated and has a valid size.
     */
    // 보기 크기가 변경될 때마다 호출됩니다.
    // 뷰는 크기가 없는 상태에서 시작되므로, 이를 다음 이름으로도 부릅니다.
    // 뷰가 팽창되어 올바른 크기를 갖습니다.

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {

        super.onSizeChanged(width, height, oldWidth, oldHeight)
        Log.d("view 포함하는 레이아웃크기(dp)","가로: $width  세로: $height"  )
        Log.d("이","$oldWidth 랑 $oldHeight"  )
        /** 파일.isInitialized */
        //https://blog.jetbrains.com/kotlin/2017/09/kotlin-1-2-beta-is-out/
        //새로운 Reflection API를 추가하여 lateinit 변수가 초기화되었는지 여부를 확인할 수 있습니다.
        // Reflection API란? :구체적인 클래스 타입을 알지 못해도 그 클래스의 정보(메서드, 타입, 변수 등등)에 접근할 수 있게 해주는 자바 API다.

        /** bitmap, device Width ,Height  설정 */


        if (::extraBitmap.isInitialized) extraBitmap.recycle()

        deviceHeight=height.toFloat()
        deviceWidth=width.toFloat()
        bitmapWidth=extraBitmap.width.toFloat()
        bitmapHeight=extraBitmap.height.toFloat()

        if (bitmapWidth!! >= deviceWidth!!){
            canvasHeight = getCanvasWidth(bitmapWidth!!,bitmapHeight!!,deviceWidth!!,deviceHeight!!)
            canvasWidth = deviceWidth
            marginHeight = (deviceHeight!!- canvasHeight!!)/2
            marginWidth = 0f
            Log.d("캔버스 실행","첫번재if[너비,높이] ${canvasWidth},${canvasHeight}")

        }else if(bitmapWidth!!  < deviceWidth!!){
            canvasWidth = bitmapWidth
            //canvasHeight = getCanvasWidth(bitmapWidth!!,bitmapHeight!!,deviceWidth!!,deviceHeight!!)
            canvasHeight = bitmapHeight

            marginHeight =(deviceHeight!!- canvasHeight!!)/2
            marginWidth =(deviceWidth!!- canvasWidth!!)/2
            Log.d("캔버스 실행","두번재if[너비,높이] ${canvasWidth},${canvasHeight}")
        }

        else{
            canvasWidth = bitmapWidth
            canvasHeight = bitmapHeight
            marginHeight =0f
            Log.d("캔버스 실행","번재if[너비,높이] ${bitmapWidth},${bitmapHeight}")

        }
        extraBitmap = Bitmap.createBitmap(canvasWidth?.toInt()!!, canvasHeight?.toInt()!!, Bitmap.Config.ARGB_8888)


        extraCanvas = Canvas(extraBitmap)
        Log.d("사이즈","캔버스 비트맵 너비(onSizeChanged) :${extraBitmap.width}")
        Log.d("사이즈","캔버스 비트맵 높이(onSizeChanged)  :${extraBitmap.height}")

        //extraCanvas.drawColor(backgroundColor)
        // Calculate a rectangular frame around the picture.

        val inset = 40
        // rectangular frame(사용 안한다.)
        //frame = Rect(inset, inset, width - inset, height - inset)
    }


    //뷰의 캔버스에 그리는 가장 기본적인 방법은 onDraw()메서드 를 재정의 하고 캔버스에 그립니다.
    override fun onDraw(canvas: Canvas) {

        // 저장된 경로가 있는 비트맵을 그립니다.
        // Draw the bitmap that has the saved path.

        deviceHeight
        deviceWidth
        bitmapWidth
        bitmapHeight
        canvasHeight
        canvasWidth
        canvas.drawBitmap(extraBitmap, marginWidth!!, marginHeight!!, paint)


        // 캔버스 둘레에 프레임을 그립니다.
        // Draw a frame around the canvas.

        //rectangular frame(사용 안한다.)
        //extraCanvas.drawRect(frame, paint)
    }

    /**
     * No need to call and implement MyCanvasView#performClick, because MyCanvasView custom view
     * does not handle click actions.
     */
    //MyCanvasView 사용자 지정 보기는 클릭 동작을 처리하지 않으므로
    // MyCanvasView#performClick을 호출하여 구현할 필요가 없습니다.
    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x - marginWidth!!
        motionTouchEventY = event.y - marginHeight!!

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    // View 생명주기
    //https://charlezz.medium.com/android%EC%97%90%EC%84%9C%EB%B3%B4%EA%B8%B0%EC%9D%98-%EC%88%98%EB%AA%85%EC%A3%BC%EA%B8%B0-10b779c12261
    override fun onAttachedToWindow() {
        Log.d("실행", "MyCanvasView클래스" )
        super.onAttachedToWindow()

        if(phtoUri==null){

        } else {
            //canvas 뒤 배경에 설치할 bitmap
            uriTobitmap(phtoUri)
            Log.d("캔버스비트맵_가로:","${extraBitmap.width.toString()}")
            Log.d("캔버스비트맵_세로","${extraBitmap.height.toString()}")
        }
    }

    /**
     * The following methods factor out what happens for different touch events,
     * as determined by the onTouchEvent() when statement.
     * This keeps the when conditional block
     * concise and makes it easier to change what happens for each event.
     * No need to call invalidate because we are not drawing anything.
     */
    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY

        Log.d("터치위치","[ $currentX,$currentY ]")
    }

    private fun touchMove() {
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY+  + currentY) / 2)
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw the path in the extra bitmap to save it.
            extraCanvas.drawPath(path, paint)
        }
        // Invalidate() is inside the touchMove() under ACTION_MOVE because there are many other
        // types of motion events passed into this listener, and we don't want to invalidate the
        // view for those.

        // Invalidate()는 이 수신기로 전달된 많은 다른 유형의 모션 이벤트가 있기 때문에
        // ACTION_MOVE의 touchMove() 내부에 있으며 이러한 수신기에 대한 보기를 무효화하지 않습니다.
        invalidate()
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        //경로가 다시 그려지지 않도록 재설정합니다.
        path.reset()
    }
    //https://javaexpert.tistory.com/270
    fun inputBitmap(bitmap:Bitmap){
        extraBitmap = bitmap

         var src :Rect = Rect(0, 0, extraBitmap.width, extraBitmap.height);
         var dst :Rect = Rect(0, 0, extraBitmap.height / 2, 200 + extraBitmap.height / 2);

        extraCanvas.drawBitmap(extraBitmap,src,dst,paint)

    }

    fun initBitmap(mbitmap: Bitmap){
        extraBitmap = mbitmap
    }


    // Uri 받아서 비트맵 불러오기
    fun uriTobitmap(uri:Uri){
        try {
            uri?.let {
                if(Build.VERSION.SDK_INT < 28) {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        uri
                    )
                    extraBitmap=bitmap
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    extraBitmap=bitmap
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun eraseCanvas(){
        extraBitmap.eraseColor(Color.TRANSPARENT);
    }


    // todo 내부 저장소에 저장
    //https://boilerplate.tistory.com/43
    fun saveInternalStorage(context: Context, fileName: String) :String{
        val INTERNAL_STORAGE_IMAGE_FOLDER = "imgDIr"//app_imgDir에 저
        val storageDir = context.getDir(INTERNAL_STORAGE_IMAGE_FOLDER, Context.MODE_PRIVATE)
        val imgFile = File(storageDir, fileName + ".png")
        Log.d("사이즈","캔버스 비트맵 너비(압축전) :${extraBitmap.width}")
        Log.d("사이즈","캔버스 비트맵 높이(압축전)  :${extraBitmap.height}")
        //Log.d("사이즈","캔버스 비트맵 너비(pixTodp) :${convertPixelsToDp(extraBitmap.width.toFloat(),context)}")
        //Log.d("사이즈","캔버스 비트맵 높이(pixTodp)  :${convertPixelsToDp(extraBitmap.height.toFloat(),context)}")

        val fileOutputStream = FileOutputStream(imgFile.absoluteFile)
        extraBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)

        fileOutputStream.close()

        Log.d("사이즈","캔버스 비트맵 너비(압축후) :${extraBitmap.width}")
        Log.d("사이즈","캔버스 비트맵 높이(압축후)  :${extraBitmap.height}")
        Log.d("캐시이미지 경로","${imgFile.absoluteFile}")

        return imgFile.absoluteFile.toString()
    }



    fun getCanvasWidth(bitmapWidth: Float,bitmapHeight: Float,deviceWidth: Float,deviceHeight: Float) :Float{
        //bitmapWidth : bitmapHeight  = deviceWidth : X
            return ( (bitmapHeight*deviceWidth)/bitmapWidth)
        }

}