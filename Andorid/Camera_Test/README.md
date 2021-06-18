


# 참조 코드

# CameraXbasic 코드: https://github.com/googlearchive/android-Camera2Basic

# CameraX 튜토리얼  https://developer.android.com/codelabs/camerax-getting-started#0

# Canvas 튜토리얼 https://developer.android.com/codelabs/advanced-android-kotlin-training-canvas#0









# CameraXbasic

CameraXbasic aims to demonstrate how to use CameraX APIs written in Kotlin.

## Build

To build the app directly from the command line, run:
```sh
./gradlew assembleDebug
```

## Test

Unit testing and instrumented device testing share the same code. To test the app using Robolectric, no device required, run:
```sh
./gradlew test
```

To run the same tests in an Android device connected via ADB, run:
```sh
./gradlew connectedAndroidTest
```

Alternatively, test running configurations can be added to Android Studio for convenience (and a nice UI). To do that:
1. Go to: `Run` > `Edit Configurations` > `Add New Configuration`.
1. For Robolectric select `Android JUnit`, for connected device select `Android Instrumented Tests`.
1. Select `app` module and `com.android.example.cameraxbasic.MainInstrumentedTest` class.
1. Optional: Give the run configuration a name, like `test robolectric` or `test device`