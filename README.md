# mopinionsdk-cordova-plugin
The Mopinion SDK Cordova plugin allows to collect feedback from a mobile Cordova app, based on events.
Under the hood, it uses our webview based mobile SDKs mopinion-sdk-ios-web and mopinion-sdk-android-web to actually implement the feedback forms.

## Folders and documentation
* `MopinionSDK` contains the actual Cordova plug-in. Start with its [README on how to use the plugin](https://github.com/mopinion/mopinionsdk-cordova-plugin/blob/main/MopinionSDK/README.md).
* `DemoApp` contains a Cordova app that you can build yourself and uses our plug-in.

## Dependencies
Cordova includes our dependencies automatically, no manual actions needed. But if you want to look into our webview based mobile SDKs, they are also on github: 

* [mopinion-sdk-ios-web](https://github.com/mopinion/mopinion-sdk-ios-web) for iOS
* [mopinion-sdk-android-web](https://github.com/mopinion/mopinion-sdk-ios-web) for Android