package com.android.example.cameraxbasic

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.android.example.cameraxbasic.databinding.ActivityObjectEraserBinding
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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.AccessController.getContext
import java.text.SimpleDateFormat
import java.util.*

class ObjectEraserActivity : AppCompatActivity() {

    lateinit private var binding : ActivityObjectEraserBinding
    private val OPEN_GALLERY = 100

    var bitmap_tmp: Bitmap? = null //화면에 보여질 이미지
    //var bitmap_modified :Bitmap? = null //수정된 이미지 경로(내부 캐시)

    var image_content_uri :String? =null // 원본 이미지 절대 경로 ID
    var image_absolutely_path :String? =null // 원본 이미지 절대 경로
    var image_modified_path :String? = null //수정된 이미지 경로(내부 캐시)

    var canvas_cach_path :String? =null // 페인팅 이미지 canvas 경로

    var dynamicCanvasView : MyCanvasView? = null // Canvas View
    var selectedPhotoUri : Uri? =null

    lateinit var mRetrofit : Retrofit // 사용할 레트로핏 객체입니다.
    lateinit var mRetrofitAPI: RetrofitAPI // 레트로핏 api객체입니다.


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityObjectEraserBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //intent 받음
        setResult(1001)


        //todo Retrofit객체 생성
        mRetrofit = RetrofitClient.mRetrofit
        mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)

        //todo갤러리 열기 버튼 클릭
        binding.imgViewOpenGallery.setOnClickListener {
            openGallery()

            bitmap_tmp?.recycle()
            bitmap_tmp=null
        }


        //todo post02 이미지 전송
        binding.imgViewObjectEraser.setOnClickListener {
            // URI     : image_content_uri
            // 절대 경로 : image_absolutely_path

            // CanvasView가 동적으로 아직 생성 안될때
            if (dynamicCanvasView  == null) {
                canvas_cach_path=null
                Log.d("도화지"," 사용 안됨")


            //이미지 경로 없는 경우(실행X)
            } else if (image_modified_path  == null) {
                Log.d("sendButton02클릭","경로 없음")


            } else {
                //1)이미지 경로 는 경우(실행O) +  2)// CanvasView가 동적으로 생성 될때

                // todo paint된 canvas 이미지 내부캐 저장소 저장
                canvas_cach_path=dynamicCanvasView!!.saveInternalStorage(this,"Canvas_paint_img")
                Log.d("도화지","사용 됨")

                Log.d("sendButton02클릭","경로 존재")


                // todo 원본이미지,Paint이미지 서버에 전송
                sendImageRetrofit02(image_modified_path!!,canvas_cach_path!!)

            }
        }



        //todo 동적으로 MyCanvasView 생성
        //https://gwynn.tistory.com/58
        binding.imgViewCanvasDraw.setOnClickListener {

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

        //Canvas 실행
        binding.imgViewSave.setOnClickListener{
            Log.d("버튼클릭","imgViewCanvasEraser실행")

            var bitmap:Bitmap = BitmapFactory.decodeFile(image_modified_path);

            try {
                // 메인액티비티 getOutputDirectory의 getOutputDirectory메소드 사
                var outputDirectory = MainActivity.getOutputDirectory(applicationContext)
                var tmpFile =File(outputDirectory, SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.KOREA)
                    .format(System.currentTimeMillis())+".jpg")  //파일명까지 포함한 경로


                var fos : FileOutputStream= FileOutputStream(tmpFile)

                Log.d("tmpFile","${tmpFile}")
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                Toast.makeText(getApplicationContext(), "image saved ", Toast.LENGTH_LONG).show();
            }catch(e: Exception) {
                Log.e("error", e.toString());
                e.printStackTrace()
            }



        }


    }




    // todo 갤러리 열기
    private fun openGallery(){

        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        startActivityForResult(intent,OPEN_GALLERY)

        DlogUtil.d(TAG, "갤러리 화면이동 $")

    }




    override fun onResume() {
        super.onResume()


        //겹치는 View중 상단으로 올린다.
        binding.imgViewObjectEraser.bringToFront();
        binding.imgViewSave.bringToFront();
        binding.imgViewCanvasDraw.bringToFront();
        binding.imgViewOpenGallery.bringToFront();

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
        //Canvas제거

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

            // 갤러리 실행시 canvas 삭제
            binding.customView.removeView(dynamicCanvasView)
            dynamicCanvasView=null

            selectedPhotoUri = data.data

            //media content URI
            image_content_uri = selectedPhotoUri.toString()

            //Uri-> 절대 경로 저장
            image_absolutely_path =getabsolutelyPath(selectedPhotoUri!!)

            // 수정 작성 할 이미지 경로
            image_modified_path=image_absolutely_path


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
                    val inputstream : InputStream = response.body()?.byteStream()!!;
                    val bitmap_modified = BitmapFactory.decodeStream(inputstream);



                    //Canvas제거
                    binding.customView.removeView(dynamicCanvasView)
                    dynamicCanvasView=null

                    binding.imageView.setImageBitmap(bitmap_modified)

                    // 캐시저장소에 이미지 저장, 저장 경로 저장
                    image_modified_path = saveInternalStorage(getApplicationContext(),"img_erased",bitmap_modified!!)

                } else {
                    Toast.makeText(getApplicationContext(), "Some error occurred...", Toast.LENGTH_LONG).show();
                    Log.d("레트로핏 결과3","업로드 실패"+response?.body().toString())
                }
            }
        })
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
            var path = cursor.getString(idx).toString()
            cursor.close()
            return path as String
        }
        Log.e("tag", "절대경로 "+ result)
        return result
    }

    // todo 내부 저장소에 저장
//https://boilerplate.tistory.com/43
    fun saveInternalStorage(context: Context, fileName: String, bitmpa:Bitmap) :String{
        val INTERNAL_STORAGE_IMAGE_FOLDER = "imgDIr"//app_imgDir에 저장
        val storageDir = context.getDir(INTERNAL_STORAGE_IMAGE_FOLDER, Context.MODE_PRIVATE)
        val imgFile = File(storageDir, fileName + ".png")

        val fileOutputStream = FileOutputStream(imgFile.absoluteFile)
        bitmpa.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.close()

        return imgFile.absoluteFile.toString()
    }





    //todo 뒤로가기 https://hjp845.tistory.com/41
    @Override
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "WiperXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

}



