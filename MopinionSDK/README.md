# mopinionsdk-cordova-plugin
The Mopinion SDK Cordova plugin allows to collect feedback from a mobile Cordova app, based on events.
It uses our webview based mobile SDKs 

* [mopinion-sdk-ios-web](https://github.com/mopinion-com/mopinion-sdk-ios-web) for iOS
* [mopinion-sdk-android-web](https://github.com/mopinion-com/mopinion-sdk-android-web) for Android

to actually implement the feedback forms.

## Contents
- [Release notes](#release-notes)
- [Installation](#installation)
- [Usage](#usage)
	- [Implement the SDK](#usage-implement)
	- [Send an event to open a form](#usage-send-event)
	- [Passing extra/meta data](#usage-meta-data)
	- [Evaluate if a form will open](#usage-evaluate)
- [DemoApp](#demo-app)

## <a name="release-notes">Release notes 1.0.0</a>

### Changes
* Updated for Cordova version 12.0.0
* Implements callbacks for the event() method.
* Uses our iOS web SDK 0.7.3 and Android web SDK 1.0.8.
* On iOS, requires iOS version 12 or newer.

### Known issues
* Cordova is picky on runtime-configurations. For certain combinations of simulator and ios versions, when trying to run the DemoApp from the command line (cordova run ios), Cordova 12 can fail to build as it generates miminum project versions to iOS 11. Open the `DemoApp.xcworkspace` from XCode instead and try to fix it there. The DemoApp has been tested to work with XCode 16.2 and iPhone SE simulator on iOS 15.5.
* Cordova 12 also seems to have issues (cordova run android) with particular Android versions. If it doesn't build at all because of a missing xml2js, try install it manually `npm install xml2js`. 
* If Cordova still doesn't build Android, check if it installed the plugin whitelist, which is incompatible with Android 10 devices. If so, manually remove the plugin: 

```
$ cordova plugin rm cordova-plugin-whitelist
$ cordova platform rm android && cordova platform add android
``` 

Next open the project in Android Studio.

* If Android Studio still fails to build the generated project, then in Android Studio try to tweak the project configuration settings to match [the exact requirements for cordova 12] (https://cordova.apache.org/docs/en/12.x/guide/platforms/android/index.html).
* Android Studio Ladybug might generate some Java8 warnings while building the generated Android project. You can ignore those, the app will run. We tested with Android Studio Ladybug, Android 10 and Android 14.
* Android side SDK 1.0.8 reports itself as `Mopinion Android Web SDK 1.0.7`, which is a known issue with this version.
* The methods evaluate() and openFormAlways() work only on iOS at the moment, as the Android SDK 1.0.8 does not implement them. 


## <a name="installation">Installation</a>

### npm
Cordova relies on npm. Install [Node.js/npm](https://www.npmjs.com/get-npm) first.

### Cordova
Install [cordova](https://cordova.apache.org/) using npm:

```
$ npm install -g cordova
```

### Create or open your project
Use the terminal to navigate to the main folder of your Cordova project. Or create a new Cordova project following the [instructions on the Cordova site](https://cordova.apache.org/#getstarted). The main folder is the folder that contains the platforms and www folders.

```
$ cd {your-project-main-folder}
```
For a newly created project, add the mobile platforms that you want to target:

```
$ cordova platform add ios
$ cordova platform add android
```

### Add the plugin
From the terminal, in your project's main folder, add our plugin to your Cordova project. :

```
$ cordova plugin add mopinionsdk-cordova-plugin
```

### Rebuild your platforms
If you added the plugin to a project that already contained iOS or Android platforms, you must save the plugins and regenerate the mobile platforms (careful, any existing changes in platforms folder will be lost):

```
$ cordova plugin save
$ cordova platform rm ios
$ cordova platform add ios
$ cordova platform rm android
$ cordova platform add android
```

Now implement the SDK and after that your project should be ready to run.

## <a name="usage">Usage</a>

### <a name="usage-implement">Implement the SDK</a>
In your index.js file, in the `onDeviceReady()` function, include the method to load the SDK  

```javascript
...
function onDeviceReady() {
...
        MopinionSDK.load(
            key, 
            enableLogging,
            function(result) {
                console.log('OK');
                ...
            },
            function (err) {
                console.log('Error');
                ...
            } 
        );
...
}
```

The `key` should be replaced with your specific deployment key. This key can be found in your Mopinion account at the `Feedback forms` section under `Deployments`.
The `log` flag can be set to `true` while developing the app to see log messages from the MopinionSDK in Android Studio/Xcode. (The default is `false` if not supplied.)

### <a name="usage-send-event">Send an event to open a form</a>
After the SDK has been loaded, then elsewhere you can send an event to open your form, for instance in a button handler.

```javascript
    buttonOpenFormViaEvent.addEventListener('click', function() {
        MopinionSDK.event(
            "_button", 
            function(result) {
					... // here code to run after completion
	            },
            function (err) {
                console.log('Error ');
                ...
            } 
        );

    })

```

The `event` is a specific event that can be connected to a feedback form action in the Mopinion system.  
The default `_button` event triggers the form, but it can have any applicable name to define your custom events.

### <a name="usage-meta-data">Passing meta data from your app to Mopinion forms</a>
Optionally, you can send extra data from your app to your feedback form. 
Provide the `addData()` method with a key and a value parameter for each key-value pair that you want to add.
The data should be added before using the `event()` or `evaluate()` methods, if you want to include the data in the form that comes up for that event.

```javascript
MopinionSDK.addData(dataKey, dataValue);
```

Example:

```javascript
...
MopinionSDK.load("12345abcde", true);
...
MopinionSDK.addData("first name", "Ada");
MopinionSDK.addData("last name", "King");
MopinionSDK.addData("aka", "Lovelace");
...
MopinionSDK.event("_button");
```

### clear extra data

The extra data is submitted with every form that opens. To remove all or a single key-value pair from the extra data previously supplied with the `addData(dataKey,dataValue)` method, use this method:

```javascript
MopinionSDK.removeData(String dataKey);
```

Example:

```javascript
MopinionSDK.removeData("first name");
```

If you want to remove all the extra data, use this method instead:

```javascript
MopinionSDK.removeAllExtraData();
```

### <a name="usage-evaluate">Evaluate if a form will open (discouraged in 1.0.0)</a>

The `evaluate()` and `openFormAlways()` methods from plugin version 0.1.2 would give your app more control on opening a form for events with conditions that might not have allowed a form to open.

The current plugin version 1.0.0 discourages the use of these methods as for the moment they only work on iOS.

Suggest to only use the `event()` method which autonomously checks deployment conditions and opens a form, or not.


#### Procedure overview

1. Call the `evaluate()` method and pass it the event name to check and a callback function.
2. In the callback function, check the response parameters. If the `hasResult` flag in the response is `true`, you can retrieve the `formKey`.
3. Optionally, pass the `formKey` to the method `openFormAlways()` to open your form directly, ignoring any conditions in the deployment.

#### Callback example (0.1.2): evaluate() and openFormAlways()

```javascript
function onSomeButtonPressed() {   
	...
        MopinionSDK.evaluate(
            "_button", 
            function (result) {
                if(result.hasResult) {
                    openMyFeedbackForm(result);
                }else{
                    alert("No form would open.");
                }
            },
            function (err) {
                console.log('Error');
                ...
            } 
        );
	...
}

...

function openMyFeedbackForm(response_from_evaluate) {
    formKey = response_from_evaluate.formKey;
    MopinionSDK.openFormAlways(
        formKey,
        function (result) {
            console.log('SDK openFormAlways OK');
        },
        function (err) {
            console.log('Error');
            ...
        } 

    );
} 
	
```

## <a name="demo-app">Demo app</a>
The github repo folder [`DemoApp`](https://github.com/mopinion/mopinionsdk-cordova-plugin/tree/main/DemoApp) contains a Cordova app that you can build yourself.

1. You'll need to have a Mopinion deployment key to actually use any forms. In the file `DemoApp/www/js/index.js`, change the placeholder `YourDeploymentKey` to your deployment key.
2. In the terminal, execute:

```
$ cd DemoApp
$ cordova plugin add mopinionsdk-cordova-plugin
$ cordova plugin save
$ cordova platform rm ios 
$ cordova platform add ios
$ cordova platform rm android
$ cordova platform add android
```
Next you can run it with either
`$ cordova run ios` or `$cordova run android`.

Note:

* If `cordova run ios` fails, try opening the `platforms/ios/DemoApp.xcworkspace` directly from XCode.
* Use the button "Reload Deployment" if you change the deployment key after the app has started.

## Support
The Mopinion SDK plugin for Cordova is maintained by the Mopinion Development Team. For support, please contact support@mopinion.com
