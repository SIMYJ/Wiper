package com.android.example.cameraxbasic



import android.app.Activity
import android.content.Context
import android.content.Intent

import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.Build

import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.android.example.cameraxbasic.databinding.ActivityRetrofitBinding
import com.android.example.cameraxbasic.utils.DlogUtil
import com.sample.gallery_outpup.network.RetrofitAPI
import com.sample.gallery_outpup.network.RetrofitClient
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.InputStream

//동적으로 view생성하기 https://gwynn.tistory.com/58

class RetrofitActivity : AppCompatActivity(){


    lateinit private var binding : ActivityRetrofitBinding
    private val OPEN_GALLERY = 100
    var bitmap_tmp: Bitmap? = null
    var bitmap_path :String? = null
    var image_content_uri :String? =null // 이미지 절대 경로 ID
    var image_absolutely_path :String? =null // 이미지 절대 경로
    var canvas_cach_path :String? =null // paint된 canvas 경로

    var dynamicCanvasView : MyCanvasView? = null // Canvas View
    var selectedPhotoUri : Uri? =null

    lateinit var mRetrofit : Retrofit // 사용할 레트로핏 객체입니다.
    lateinit var mRetrofitAPI: RetrofitAPI // 레트로핏 api객체입니다.


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityRetrofitBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //intent 받음
        setResult(1001)


        //todo Retrofit객체 생성
        mRetrofit = RetrofitClient.mRetrofit
        mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)

        //todo갤러리 열기 버튼 클릭
        binding.ButtonGalley.setOnClickListener {
            openGallery()

            bitmap_tmp?.recycle()
            bitmap_tmp=null
        }

        //todo post01 이미지 전송
        binding.ButtonSendImage.setOnClickListener {
            // URI     : image_content_uri
            // 절대 경로 : image_absolutely_path

            //절대경로 없는 경우(실행X)
            if (image_absolutely_path == null) {
                Log.d("sendButton클릭","절대경로 없음")

            //절대 경로 있는 경우(실행O)
            } else {
                Log.d("sendButton클릭","절대경로 존재")
                sendImageRetrofit(image_absolutely_path!!)

            }
        }
        //todo post02 이미지 전송
     binding.ButtonSendImage02.setOnClickListener {
            // URI     : image_content_uri
            // 절대 경로 : image_absolutely_path

            //절대경로 없는 경우(실행X)
            if (image_absolutely_path  == null) {
                Log.d("sendButton02클릭","절대경로 없음")

                //절대 경로 있는 경우(실행O)
            } else {
                Log.d("sendButton02클릭","절대경로 존재")
                sendImageRetrofit02(image_absolutely_path!!,canvas_cach_path!!)

            }
        }


        //Request  get 전송
        binding.ButtonGet.setOnClickListener {
            Log.d("전송 버튼","클릭")

            mRetrofitAPI.request_get().enqueue(object: Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    Log.d("응답성공",response.toString())
                    Log.d("Callback<String>문자",response.toString())

                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("응답실패",t.toString())
                }
            })
        }

        // todo paint된 canvas 이미지 내부캐 저장소 저장
        binding.buttonDraw.setOnClickListener {
            // 뷰를 비트맵으로 변환(사용 안함)
            //var canvasBitmap:Bitmap=viewToBitmap(dynamicCanvasView!!)
            //binding.imageViewLoad.setImageBitmap(canvasBitmap)

            //내부캐시저장소에 canvas 이미지 저장
            canvas_cach_path=dynamicCanvasView!!.saveInternalStorage(this,"Canvas_paint_img")
            Log.d("저장","canvas_img")

        }

    }


    //todo 뷰를 비트맵으로 변환(사용 안함)
    // https://black-jin0427.tistory.com/96
    /*
    fun viewToBitmap(view: View): Bitmap {
        // View == dynamicCanvasView
        // View의 부모는 LinearLayout이여서 인지 View크기가 화면전체를 덮고 있다.

        // device 화면 너비,높이 로그 출
        val deviceWidth = resources.displayMetrics.widthPixels
        val deviceHeight = resources.displayMetrics.heightPixels
        Log.d("deviceWidth(Dp) :",deviceWidth.toString() )
        Log.d("deviceHeight(Dp) :",deviceHeight.toString() )


        var devicePxWidth = getDpToPix(this, deviceWidth.toDouble())
        var devicePxHeight = getDpToPix(this,deviceHeight.toDouble())
        Log.d("deviceWidth(DptoPx) :","${devicePxWidth}" )
        Log.d("deviceHeight(DptoPx) :","${devicePxHeight}" )

        /**이 코드는 쓸데없는코드   */
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        Log.d("canvasView(dp) :","너비:${view.width} ")
        Log.d("canvasView(dp) :","높이:${view.height} " )

        Log.d("dynamicCanvasView(dp) :","너비:${dynamicCanvasView?.width} ")
        Log.d("dynamicCanvasView(dp) :","높이:${dynamicCanvasView?.height} " )


        Log.d("viewTobitmap_(dp)","너s비 : ${bitmap.width}" )
        Log.d("viewTobitmap_(dp)","높 : ${bitmap.height}" )

        return bitmap
    }
    */
    //https://ckdgus.tistory.com/60
    // todo dp를 픽셀로 바꾸기
    /* 사용 안함
    fun  getDpToPix(con : Context, dp: Double ):Int {

        //var density :Float = 0.0f;
        // density = con.getResources().getDisplayMetrics().density;
        var displayMetrics : DisplayMetrics = DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        var dpi : Int = displayMetrics.densityDpi;
        var density :Float = displayMetrics.density; // density에는 dip/160 값이 들어 있음.

        Log.d("dpi",dpi.toString() )
        Log.d("density",density.toString() )

            var tmpInt = dp * density + 0.5
            //여기서 참고 하실점은 density의 데이터 타입은 float형 입니다.
            //DP -> PX를 하는 과정에서 좀 더 정확한 px값을 얻기 위해
            //Google Developers Site 에서는 반올림을 하여 처리하기를 권합니다
        return tmpInt.toInt()
    }
    */



    // todo 갤러리 열기
    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        startActivityForResult(intent,OPEN_GALLERY)

        DlogUtil.d(TAG, "갤러리 화면이동 $")

    }

    // todo  Uri -> 절대경로 변환
    private fun getabsolutelyPath(uri : Uri): String {

        var result = ""
        var cursor : Cursor = contentResolver?.query(uri, null, null, null, null)!!


        if(cursor == null) {
            result = uri?.path.toString()//   나중에 고쳐야됨
        }else {
            cursor.moveToFirst()
            var idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            image_absolutely_path = cursor.getString(idx).toString()
            cursor.close()
            return image_absolutely_path as String
        }
        Log.e("tag", "절대경로 "+ result)
        return result
    }



    override fun onResume() {
        super.onResume()
        //todo 동적으로 MyCanvasView 생성
        //https://gwynn.tistory.com/58
        if(bitmap_tmp==null){
        }else{
            dynamicCanvasView = MyCanvasView(this,null,0,selectedPhotoUri)

            var layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            dynamicCanvasView!!.layoutParams = layoutParams
            binding.customView.addView(dynamicCanvasView)
        }
    }

    override fun onStop(){
        super.onStop()
        // 화면이동 시 캔버스 View 지운다.
        //https://recipes4dev.tistory.com/128
        binding.customView.removeView(dynamicCanvasView)
        dynamicCanvasView=null
    }

    override fun onDestroy() {
       super.onDestroy()
    }

    // 특정 액티비티 종료시 실행되는 코드
    //https://www.masterqna.com/android/96226/%EA%B0%A4%EB%9F%AC%EB%A6%AC-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EC%A0%88%EB%8C%80%EA%B2%BD%EB%A1%9C
    // startactivityforresult  -> onactivityresul으로 override 메소드 변경
    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)



        // 요청코드= OPEN_GALLERY 이고, 응답코드 =Activity.RESULT_OK 이고, 반환 데이터가 존재할때
        if(requestCode == OPEN_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            //데이터 URI selectedPhotoUri변수에 저장후 textView에 출력
            selectedPhotoUri = data.data

            //media content URI
            image_content_uri = selectedPhotoUri.toString()


            binding.textViewContentUri.setText(image_content_uri)
            if (selectedPhotoUri != null) {
                var text:String =getabsolutelyPath(selectedPhotoUri!!)

                binding.textViewFileUri.setText(text)
            }

            // uri경로를 가지고  Bitmap불러오기
            try {
                selectedPhotoUri?.let {
                    //SDK버전이 28미만인 경우
                    if(Build.VERSION.SDK_INT < 28) {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            selectedPhotoUri
                        )
                        binding.imageView.setImageBitmap(bitmap)
                        bitmap_tmp=bitmap
                        bitmap.recycle()

                        //SDK버전이 28이상인 경우
                    } else {
                        val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri!!)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        binding.imageView.setImageBitmap(bitmap)
                        bitmap_tmp=bitmap
                    }

                 }

                Log.d("입력_비트맵_가로:", bitmap_tmp?.width.toString() )
                Log.d("입력_비트맵_세로:", bitmap_tmp?.height.toString() )
                //Log.d("imageView 가로:", binding.imageView.width.toString() )
                //Log.d("imageView 세로:", binding.imageView.height.toString() )
                //Log.d("imageViewParams 가로:", binding.imageView.layoutParams.height.toString() )
                //Log.d("imageViewParams 세로:", binding.imageView.layoutParams.height.toString() )


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // todo post01  Retrofit 전송 : https://machine-woong.tistory.com/171
    fun sendImageRetrofit(path: String) {
        //creating a file
        //path값은 절대경로
        val file = File(path)

        Log.d("file 이름", file.name)


        var fileName = "img.png"
        //fileName = fileName+".png"

        //--------------------    대안01          ----------------------------------

        //RequestBody.create(MediaType.parse(“text/plain”), data);
        //RequestBody.create(MediaType.parse(“multipart/form-data”), data);
        // 1) "image/*" 2)"multipart/form-data"
        var requestBody : RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"),file)
        //var requestBody : RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"),bitmap_tmp)
        //인자 1)file  2)image_content_uri 3)image_absolutely_path

        var body : MultipartBody.Part = MultipartBody.Part.createFormData("file",fileName,requestBody)
        Log.d("tag", "body에 무엇이 들었을까?? "+ body)
        //------------------------------------------------------------------------


        //--------------------    대안02          ----------------------------------

        //MultipartBody.Part.createFormData("file", file.getPath(), RequestBody.create(MediaType.parse("image/jpeg"), file));
        //MultipartBody.Part.createFormData("file", Urls.encode(fileName));
        //-------------------------------------------------------------------------


        mRetrofitAPI.image_Request(body).enqueue(object: Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("레트로핏 결과1","응답 실패",t)
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response?.isSuccessful) {
                    Toast.makeText(getApplicationContext(), "File Uploaded Successfully...", Toast.LENGTH_LONG).show();
                    Log.d("레트로핏 결과2","업로드 성공"+response?.body().toString())


                    /**  Call에 넘겨주는 ResponseBody는 okhttp3패키지의 ResponseBody이다.  */
                    /** https://zerodice0.tistory.com/198 참조 */
                    var inputstream : InputStream  = response.body()?.byteStream()!!;
                    var bitmap :Bitmap = BitmapFactory.decodeStream(inputstream);

                    //var file : MultipartBody? = response?.body()
                    //var inputstream : InputStream  = file.byteStream()!!;
                    //var  :Bitmap = BitmapFactory.decodeStream(inputstream);

                    binding.imageViewLoad.setImageBitmap(bitmap)
                    //binding.imageView.setImageBitmap(bitmap)


                } else {
                    Toast.makeText(getApplicationContext(), "Some error occurred...", Toast.LENGTH_LONG).show();
                    Log.d("레트로핏 결과3","업로드 실패"+response?.body().toString())
                }
            }
        })
    }

    // todo post02  Retrofit 전송 : https://machine-woong.tistory.com/171
    fun sendImageRetrofit02(originPath: String,paintPath :String) {
        //creating a file
        //path값은 절대경로
        val file01 = File(originPath)
        val file02 = File(paintPath)


        var fileName01 = "origin.png"
        var fileName02 = "paint.png"

        var requestBody01 : RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"),file01)
        var body01 : MultipartBody.Part = MultipartBody.Part.createFormData("file01",fileName01,requestBody01)

        var requestBody02 : RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"),file02)
        var body02 : MultipartBody.Part = MultipartBody.Part.createFormData("file02",fileName02,requestBody02)


        mRetrofitAPI.image_Request02(body01,body02).enqueue(object: Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("레트로핏 결과1","응답 실패",t)
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response?.isSuccessful) {
                    Toast.makeText(getApplicationContext(), "File Uploaded Successfully...", Toast.LENGTH_LONG).show();
                    Log.d("레트로핏 결과2","업로드 성공"+response?.body().toString())


                    /**  Call에 넘겨주는 ResponseBody는 okhttp3패키지의 ResponseBody이다.  */
                    /** https://zerodice0.tistory.com/198 참조 */
                    var inputstream : InputStream  = response.body()?.byteStream()!!;
                    var bitmap :Bitmap = BitmapFactory.decodeStream(inputstream);

                    //binding.imageViewLoad.setImageBitmap(bitmap)

                    //Canvas제거
                   binding.customView.removeView(dynamicCanvasView)
                    dynamicCanvasView=null

                    binding.imageView.setImageBitmap(bitmap)

                } else {
                    Toast.makeText(getApplicationContext(), "Some error occurred...", Toast.LENGTH_LONG).show();
                    Log.d("레트로핏 결과3","업로드 실패"+response?.body().toString())
                }
            }
        })
    }



    //todo 뒤로가기 https://hjp845.tistory.com/41
    @Override
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
    }


    // 비트맵 이미지 구하기
    //https://javacan.tistory.com/entry/tip-get-size-of-image-and-resize-in-android
    fun  getBitmapSize( imageFile : File):BitmapFactory.Options  {
        var options:BitmapFactory.Options =  BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        return options;

    }

}



