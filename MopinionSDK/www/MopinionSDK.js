var exec = require('cordova/exec');

/**
 * Initializes the Mopinion SDK, loads a deployment
 * @param deploymentKey string with your specific mopinion deployment key, as can be found in your Mopinion account 
 * @param enableLogging boolean, if true enables the logging option of the SDK. Default is false.  
 * @param callback(arg) success callback, will be called from native side
 * @param errorCallback(arg) may be called in case of error, .i.e. when loading failed
 * @returns as argument to the callBack function, response details JSON Object with the following parameters:
 *              details: object, optional. Original arguments to the call.
 *              error: string, optional. Error message, in case of error.
 */
exports.load = function (deploymentKey, enableLogging, callback, errorCallback) {
    var params = {
        deploymentKey: deploymentKey,
        enableLogging: enableLogging
    };

    exec(callback, errorCallback, 'MopinionSDK', 'load', [params]);
};

/**
 * Opens the first matching form, if any, associated with the specified event, if the conditions allow it in case of a proactive form.
 * @param event string, name of the event as defined in the deployment. For instance "_button".
 * @param callback(arg) success callback, is always called unless an error occurred
 * @param errorCallback(arg) may be called in case of error
 * @returns as argument to the callBack function, the response details as JSON Object with the following parameters:
 *              event: string, the original event name that was passed to the event() call.
 *              error: string, optional. Error message, in case of error.
 */
exports.event = function (event, callback, errorCallback) {
    var params = {
        event: event
    };
    exec(callback, errorCallback, 'MopinionSDK', 'event', [params]);
};

/**
 * Evaluates whether or not a form would have opened for the specified event and returns the formkey of the first matching form.
 * @param event string, name of the event as defined in the deployment. For instance "_button".
 * @param callback(arg) success callback, is always called unless an error occurred
 * @param errorCallback(arg) may be called in case of error
 * @returns as argument to the callBack function, the response details as JSON Object with the following parameters:
 *              hasResult: boolean, if true then the form identified by the formKey would have opened. If false then the form would not have opened and the formKey might be null in case no forms were found associated with the event.
 *              event: string, the original event name that was passed to the evaluate call to check in the deployment.
 *              formKey: string, identifying key of the first feedback form found associated with the event. Only one formKey will be selected even if multiple forms matched the event name in the deployment.
 *              response: optional object for extra response details on success/failure and forms. Reserved for future extensions.
 */
exports.evaluate = function (event, callback, errorCallback) {
    var params = {
        event: event
    };
    exec(callback, errorCallback, 'MopinionSDK', 'evaluate', [params]);
};

/**
 * Opens the form specified by the formkey, regardless of any proactive conditions set in the deployment.
 * @param formKey string, key of a feedback form as provided by the Mopinion.evaluate() call
 * @param callback(arg) success callback, is always called unless an error occurred
 * @param errorCallback(arg) may be called in case of error
 * @returns as argument to the callBack function, the response details as JSON Object with the following parameters:
 *              details: object, optional. Original arguments to the call.
 *              error: string, optional. Error message, in case of error.
 */
exports.openFormAlways = function (formKey, callback, errorCallback) {
    var params = {
        formKey: formKey
    };
    exec(callback, errorCallback, 'MopinionSDK', 'openFormAlways', [params]);
};

/**
 * Add a single key-value pair of extra data to any form that you will open. 
 * Use before opening your form with the event() method, if you want to include the data in the form that comes up for that event.
 * Note: In the set of extra data, the keys are unique. If you re-use a key, the previous value for that key will be overwritten.
 * @param forDataKey string, key of an extra data item.
 * @param dataValue string, actual value to store associated with the key.
 * @param callback(arg) success callback, is always called unless an error occurred
 * @param errorCallback(arg) may be called in case of error
 * @returns as argument to the callBack function, the response details as JSON Object with the following parameters:
 *              details: object, optional. Original arguments to the call.
 *              error: string, optional. Error message, in case of error.
 */
exports.addData = function (forDataKey, dataValue, callback, errorCallback) {
    var params = {
        forDataKey: forDataKey,
        dataValue: dataValue
    };
    exec(callback, errorCallback, 'MopinionSDK', 'addData', [params]);
};

/**
 * Remove a single key-value pair from the extra data previously supplied with the data(key,value) method
 * @param forDataKey string, key of an extra data item that you previously added
 * @param callback(arg) success callback, is always called unless an error occurred
 * @param errorCallback(arg) may be called in case of error
 * @returns as argument to the callBack function, the response details as JSON Object with the following parameters:
 *              details: object, optional. Original arguments to the call.
 *              error: string, optional. Error message, in case of error.
 */
exports.removeData = function (forDataKey, callback, errorCallback) {
    var params = {
        forDataKey: forDataKey
    };
    exec(callback, errorCallback, 'MopinionSDK', 'removeData', [params]);
};

// implements MopinionSDK.removeData() because no function overloading in JS
/**
 * Remove all extra data that was previously supplied with the data(key,value) method
 * @param callback(arg) success callback, is always called unless an error occurred
 * @param errorCallback(arg) may be called in case of error
 * @returns as argument to the callBack function, the response details as JSON Object with the following parameters:
 *              error: string, optional. Error message, in case of error.
 */
exports.removeAllExtraData = function (callback, errorCallback) {
    exec(callback, errorCallback, 'MopinionSDK', 'removeAllExtraData');
};
