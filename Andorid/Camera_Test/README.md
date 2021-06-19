


# 참조 코드

### Camera 예시코드: https://github.com/android/camera-samples

### CameraX 튜토리얼 : https://developer.android.com/codelabs/camerax-getting-started#0

### Canvas 튜토리얼 : https://developer.android.com/codelabs/advanced-android-kotlin-training-canvas#0




---
# 변경사항

기존 kt파일 RetrofitActivit.kr사용X     →  0bjectEraserActivity 생성

기존 xml파일 activity_retrofit.xml사용X → activity_object_eraser생성

GalleryFragment.kt에서 [R.id.gallery_button] 클릭리스너 호출시 기존 RetrofitActivit.kr가 아닌  0bjectEraserActivity 호출한다.


- 0bjectEraserActivity의 버튼 종류

1. 이미지 서버 전송
2. 캔버스 생성 
3. 갤러리 열기
4. 수정된 이미지 저장




