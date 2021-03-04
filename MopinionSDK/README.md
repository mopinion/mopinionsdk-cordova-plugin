# mopinionsdk-cordova-plugin
The Mopinion SDK Cordova plugin allows to collect feedback from a mobile Cordova app, based on events.
It uses our webview based mobile SDKs 

* [mopinion-sdk-ios-web](https://github.com/mopinion/mopinion-sdk-ios-web) for iOS
* [mopinion-sdk-android-web](https://github.com/mopinion/mopinion-sdk-ios-web) for Android

to actually implement the feedback forms.

## Release notes 0.1.1
* This is a beta version to give you a sneak peek on our Cordova plugin release. If you encounter any issues, please inform us via support.
* Added iOS `event` handler. Dark mode support.

### Release notes 0.1.0
* Beta version to give you a sneak peek on our upcoming Cordova plugin release.
* In Android, the plugin starts another activity. Make sure that your app saves its state before calling `event()` or `openFormAlways()`. 

### Known issues
* Our plugin uses Swift 5 on iOS, but current Cordova version 9 still prefers Swift 4. You can ignore the warnings that Cordova produces upon install about the Swift version.
* Our plugin uses the AndroidX library on Android, but current Cordava version 9 still prefers the deprecated android-support-library. You can ignore the Gradle property warnings that Cordova produces upon install about android.useAndroidX and android.enableJetifier.
* On Apple Silicon machines, using Xcode 12.4 iOS simulator, your project might fail building with an error `Module 'MopinionSDK' was created for incompatible target arm64-apple-ios9.0`. To bypass this temporary problem, in Xcode, set the **debug** build setting `Excluded Architectures` for your project and subprojects to `arm64`.
* The current `event()` method doesn't actually use the callback as would be optimal for Cordova. If you need to run code after a callback, prefer the `evaluate()` and `openFormAlways()` methods instead. 
* The `load()` method currently doesn't use the callback but completes autonomously in the background. Try to use the method at the application start.

## Installation
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

## Usage

### Implement the SDK
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

### Send an event to open a form
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

### Passing meta data from your app to Mopinion forms
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

### Evaluate if a form will open
The `event()` method autonomously checks deployment conditions and opens a form, or not.

Alternatively, use the `evaluate()` and `openFormAlways()` methods to give your app more control on opening a form for proactive events or take actions when no form would have opened.

It can also be used on passive events, but such forms will always be allowed to open.

#### Procedure overview

1. Call the `evaluate()` method and pass it the event name to check and a callback function.
2. In the callback function, check the response parameters. If the `hasResult` flag in the response is `true`, you can retrieve the `formKey`.
3. Optionally, pass the `formKey` to the method `openFormAlways()` to open your form directly, ignoring any conditions in the deployment.

#### Callback example: evaluate() and openFormAlways()

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

## Demo app
The github repo folder `DemoApp` contains a Cordova app that you can build yourself.

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

* Use the button "Reload Deployment" if you change the deployment key after the app has started.  

## Support
The Mopinion SDK plugin for Cordova is maintained by the Mopinion Development Team. For support, please contact support@mopinion.com
