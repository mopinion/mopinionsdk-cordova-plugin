/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// Wait for the deviceready event before using any of Cordova's device APIs.
// See https://cordova.apache.org/docs/en/latest/cordova/events/events.html#deviceready

// "use strict";
document.addEventListener('deviceready', onDeviceReady, false);

function onDeviceReady() {
    // Cordova is now initialized. Have fun!

    console.log('Running cordova-' + cordova.platformId + '@' + cordova.version);
    document.getElementById('deviceready').classList.add('ready');

    setUpMopinion();
    setupUIhandlers();

    // set a default event name
    eventName = document.getElementById("eventName").value;
    if (eventName === "") {
        document.getElementById("eventName").value = "_button";
    }

}

function goodbye() {
    navigator.app.exitApp();
    navigator.device.exitApp();
    window.close();
}

function setupUIhandlers() {
    console.log("About to attach eventlistener for button");
    buttonOpenUnconditional = document.getElementById("openWithEvaluate");

    buttonOpenUnconditional.addEventListener('click', function () {
        playWithSomeExtraData();
        eventName = document.getElementById("eventName").value;
        if (eventName === "") {
            alert("Specify an event name");
            return;
        }

        // check if a form would open for this event
        MopinionSDK.evaluate(
            eventName,
            function (result) {
                console.log('SDK evaluate called OK');
                if (result.hasResult) {
                    openMyFeedbackForm(result);
                } else {
                    alert("No form would open from evaluate call.");
                }
            },
            function (err) {
                console.log('SDK evaluate encountered an error ' + err);
            }
        );
    });

    var buttonSendSDKevent = document.getElementById("openWithEvent");

    buttonSendSDKevent.addEventListener('click', function () {
        eventName = document.getElementById("eventName").value;
        if (eventName === "") {
            alert("Specify an event name");
            return;
        }
        alert("Cordova will call the MopinionSDK. Depending on conditions, a form may or may not open.");
        MopinionSDK.event(
            eventName,
            function (result) {
                console.log('SDK event called OK');
            },
            function (err) {
                console.log('SDK event encountered an error');
            }
        );

    });

    var buttonReloadDeployment = document.getElementById("reloadDeployment");

    buttonReloadDeployment.addEventListener('click', function () {
        setUpMopinion();
        alert("Cordova will reload the MopinionSDK");
    });
}

function setUpMopinion() {
    // here your deployment key as default
    var my_deployment_key = "YourDeploymentKey";

    deployment_key = document.getElementById("deploymentKey").value;
    if (deployment_key === "") {
        deployment_key = my_deployment_key;
        document.getElementById("deploymentKey").value = deployment_key;
    }

    isLoggingEnabled = document.getElementById("isLoggingEnabled").checked;

    // init the SDK
    console.log("About to load the Mopinion SDK for key " + deployment_key);
    MopinionSDK.load(
        deployment_key,
        isLoggingEnabled,
        function (result) {
            console.log('SDK loading OK');
        },
        function (err) {
            console.log('SDK encountered an error');
        }
    );
}

// Just a demo.
function playWithSomeExtraData() {
    // extra data sticks with the currently loaded SDK, so clear it first.
    MopinionSDK.removeAllExtraData();
    MopinionSDK.addData("first name", "Ada");
    MopinionSDK.addData("middle name", "Will be deleted");
    MopinionSDK.addData("last name", "King");
    MopinionSDK.removeData("middle name");
}

// open a feedback form with the reponse from the evaluate call.
function openMyFeedbackForm(response_from_evaluate) {
    var formKey = response_from_evaluate.formKey;
    MopinionSDK.openFormAlways(
        formKey,
        function (result) {
            console.log('SDK openFormAlways called OK ');
        },
        function (err) {
            console.log('SDK openFormAlways encountered an error ');
        }

    );
}
