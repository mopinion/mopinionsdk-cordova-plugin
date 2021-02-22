# mopinionsdk-cordova-plugin
The Mopinion SDK Cordova plugin allows to collect feedback from a mobile Cordova app, based on events.
It uses our webview based mobile SDKs 

* [mopinion-sdk-ios-web](https://github.com/mopinion/mopinion-sdk-ios-web) for iOS
* [mopinion-sdk-android-web](https://github.com/mopinion/mopinion-sdk-ios-web) for Android

to actually implement the feedback forms.

## Folders
* `MopinionSDK` contains the Cordova plug-in. See it's [README for more info](https://github.com/mopinion/mopinionsdk-cordova-plugin/MopinionSDK/README.md).
* `DemoApp` contains a Cordova app that you can build yourself and uses our plug-in.

## Release history

### Release notes 0.1.0
* This is a beta version to give you a sneak peek on our upcoming Cordova plugin release. If you encounter any issues, please inform us via support.
* In Android, the plugin starts another activity. Make sure that your app saves its state before calling `event()` or `openFormAlways()`. 
