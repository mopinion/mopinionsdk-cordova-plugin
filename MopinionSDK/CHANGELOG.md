# 0.1.2
* Updated for Cordova version 11.0.0
* Uses our currently latest iOS and Android SDKs.
* DemoApp version 0.1.2 might generate some warnings in Android Studio Chipmunk, due to an older gradle version used in the app. They can be ignored, the app will work fine for development.
* Tested on iOS 15 using XCode 13.4.1.

# 0.1.1
* Plugin made for Cordova 9. 
* Added iOS `event` handler. Dark mode support.
* Our plugin uses Swift 5 on iOS, but current Cordova still prefers Swift 4. You can ignore the warnings that Cordova produces upon install about the Swift version.
* On Apple Silicon machines, using Xcode 12.4 iOS simulator, your project might fail building with an error `Module 'MopinionSDK' was created for incompatible target arm64-apple-ios9.0`. To bypass this temporary problem, in Xcode, set the **debug** build setting `Excluded Architectures` for your project and subprojects to `arm64`.
* Our plugin uses the AndroidX library on Android, but current Cordava version 9 still prefers the deprecated android-support-library. You can ignore the Gradle property warnings that Cordova produces upon install about android.useAndroidX and android.enableJetifier.

# 0.1.0
* Beta version to give a sneak peek on our upcoming Cordova plugin release.
* In Android, the plugin starts another activity. Make sure that your app saves its state before calling `event()` or `openFormAlways()`. 
