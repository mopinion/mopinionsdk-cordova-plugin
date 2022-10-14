package com.mopinion.plugin;

import androidx.annotation.Nullable;

import com.mopinion.mopinionsdkweb.Mopinion;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
* Bridges the cordova plugin calls from JavaScript to the mopinionwebsdk.
*/
public class MopinionCordova extends CordovaPlugin {

    private Mopinion M;
    private CallbackContext pendingCallbackContext; // the context of any pending async call

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        // our init code for the first time the plugin is activated
        M = null;
        pendingCallbackContext = null;
    }

    // TODO: maybe add plugin destroy and resume in onSaveInstanceState and onRestoreStateForActivityResult

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject params;
        switch (action) {
            case "addData":
                params = args.getJSONObject(0);
                this.addData(params, callbackContext);
                return true;
            case "evaluate":
                params = args.getJSONObject(0);
                this.evaluate(params, callbackContext);
                return true;
            case "event":
                params = args.getJSONObject(0);
                this.event(params, callbackContext);
                return true;
            case "load":
                params = args.getJSONObject(0);
                this.load(params, callbackContext);
                return true;
            case "openFormAlways":
                params = args.getJSONObject(0);
                this.openFormAlways(params, callbackContext);
                return true;
            case "removeAllExtraData":
//                params = args.getJSONObject(0);
                this.removeAllExtraData(callbackContext);
                return true;
            case "removeData":
                params = args.getJSONObject(0);
                this.removeData(params, callbackContext);
                return true;
            default:
                return false;   // indicates INVALID_ACTION
        }
//        return false;
    }

    private void load(JSONObject params, CallbackContext callbackContext) {
        pendingCallbackContext = callbackContext;

        String deploymentKey;
        try {
            deploymentKey = params.getString("deploymentKey");
        } catch (JSONException jse) {
            sendPluginError("Error reading deployment key:" + jse.getMessage(), params);
            return;
        }
        boolean enableLogging;
        enableLogging = params.optBoolean("enableLogging", false);

        // prepare return args
        JSONObject outparams = new JSONObject();
        try {
            outparams.put("event", "deployment_will_load");
            outparams.put("details", params);
        } catch (JSONException jse) {
            sendPluginError("Problem creating JSON: " + jse.getMessage(), params);
            return;
        }

        if (deploymentKey.isEmpty()) {
            sendPluginError("No deployment key given.", params);
        } else {
            // load the SDK. TODO: make this async when the SDK becomes Async
            M = new Mopinion(cordova.getContext(), deploymentKey, enableLogging);
            sendPluginResultOK(outparams);
        }
    }

    // TODO: replace with a function that actually uses the callback
    private void event(JSONObject params, CallbackContext callbackContext) {
        pendingCallbackContext = callbackContext;

        String event;
        try {
            event = params.getString("event");
        } catch (JSONException jse) {
            sendPluginError("Error reading event: " + jse.getMessage(), params);
            return;
        }

        if (event.isEmpty()) {
            sendPluginError("No event specified, provide an event to the call.", params);
            return;
        } else {
            // this will return immediately without waiting for a form to open
            M.event(event);
            sendPluginResultOK(params); // pretend it always works
        }
    }

    private void evaluate(JSONObject params, CallbackContext callbackContext) {
        pendingCallbackContext = callbackContext;

        String event;
        try {
            event = params.getString("event");
        } catch (JSONException jse) {
            sendPluginError("Error reading event: " + jse.getMessage(), params);
            return;
        }

        if (event.isEmpty()) {
            sendPluginError("No event specified, provide an event to the call.", params);
            return;
        } else {
            // evaluate() will pass the formkey, if any, to its callback handler
            M.evaluate(event, new Mopinion.MopinionOnEvaluateListener() {
                @Override
                public void onMopinionEvaluate(boolean hasResult, String event, @Nullable String formKey, @Nullable JSONObject response) {
                    // prepare return args
                    JSONObject outparams = new JSONObject();
                    try {
                        outparams.put("hasResult", hasResult);
                        outparams.put("event", event);
                        outparams.put("formKey", (formKey != null) ? formKey : JSONObject.NULL);
                        outparams.put("response", (response != null) ? response : JSONObject.NULL);
                    } catch (JSONException jse) {
                        sendPluginError("Problem creating JSON: " + jse.getMessage(), params);
                        return;
                    }

                    sendPluginResultOK(outparams);

//                    // here the code to check the parameters
//                    if(hasResult) {
//                        // at least one form was found and all optional parameters are non-null
//                        M.openFormAlways(formKey); // because conditions can change every time, use the formkey
//                    }else{
//                        // no form would open
//                        if(formKey !=null) {
//                            M.openFormAlways(formKey); // because conditions can change every time, use the formkey
//                        }else{
////                            Toast.makeText(getApplicationContext(), "Evaluate: no form found for event '" + event + "'.", Toast.LENGTH_LONG).show();
//                        }
//                    }
                }
            } );
        }
    }

    private void openFormAlways(JSONObject params, CallbackContext callbackContext) {
        pendingCallbackContext = callbackContext;

        String formKey;
        try {
            formKey = params.getString("formKey");
        } catch (JSONException jse) {
            sendPluginError("Error reading formKey: " + jse.getMessage(), params);
            return;
        }

        // prepare return args
        JSONObject outparams = new JSONObject();
        try {
            outparams.put("details", params);
        } catch (JSONException jse) {
            sendPluginError("Problem creating JSON: " + jse.getMessage(), params);
            return;
        }

        if (formKey.isEmpty()) {
            sendPluginError("No formKey specified, provide a formKey to the call.", outparams);
            return;
        } else {
            // TODO: later convert into async call with success/error-handling
            M.openFormAlways(formKey);
            sendPluginResultOK(outparams);
        }
    }

    private void addData(JSONObject params, CallbackContext callbackContext) {
        pendingCallbackContext = callbackContext;

        String forDataKey;
        forDataKey = params.optString("forDataKey");

        String dataValue;
        dataValue = params.optString("dataValue");

        // prepare return args
        JSONObject outparams = new JSONObject();
        try {
            outparams.put("event", "add_extra_data");
            outparams.put("details", params);
        } catch (JSONException jse) {
            sendPluginError("Problem creating JSON: " + jse.getMessage(), params);
            return;
        }

        if (forDataKey.isEmpty()) {
            sendPluginError("No DataKey specified, provide a forDataKey to the call.", params);
        } else {
            M.data(forDataKey,dataValue);
            sendPluginResultOK(outparams);
        }
    }

    private void removeData(JSONObject params, CallbackContext callbackContext) {
        pendingCallbackContext = callbackContext;

        String forDataKey;
        forDataKey = params.optString("forDataKey");

        // prepare return args
        JSONObject outparams = new JSONObject();
        try {
            outparams.put("event", "remove_extra_data");
            outparams.put("details", params);
        } catch (JSONException jse) {
            sendPluginError("Problem creating JSON: " + jse.getMessage(), params);
            return;
        }

        if (forDataKey.isEmpty()) {
            sendPluginError("No DataKey specified, provide a forDataKey to the call.", params);
        } else {
            M.removeData(forDataKey);
            sendPluginResultOK(outparams);
        }
    }

    private void removeAllExtraData(CallbackContext callbackContext) {
        pendingCallbackContext = callbackContext;

        // prepare return args
        JSONObject outparams = new JSONObject();
        try {
            outparams.put("event", "remove_all_extra_data");
//            outparams.put("details", params);
        } catch (JSONException jse) {
            sendPluginError("Problem creating JSON: " + jse.getMessage());
            return;
        }

        M.removeData();
        sendPluginResultOK(outparams); // pretend it always works
    }

    // MARK: helper functions

    // MARK: submit plugin result, if any. Use wasSuccessful = false to report error.
    // Don't call this directly, instead use sendPluginResultOK or sendPluginError
    private void sendPluginResult(boolean wasSuccessful, JSONObject withParameters) {
        if(pendingCallbackContext == null) {
            LOG.e("PluginResultError", "CallbackContext is not set.");
            return;
        }

        if ((withParameters) != null) {
            if (wasSuccessful) {
                pendingCallbackContext.success(withParameters);
            } else {
                pendingCallbackContext.error(withParameters);
            }
        } else {
            if (wasSuccessful) {
                pendingCallbackContext.success();
            } else {
                // this should never happen, an error message is mandatory
                pendingCallbackContext.error("No error message was supplied.");
            }
        }
        pendingCallbackContext = null;
    }

    private void sendPluginResultOK(JSONObject withParameters) {
        sendPluginResult(true, withParameters);
    }

    private void sendPluginResultOK() {
        sendPluginResult(true, null);
    }

    // let the plugin caller know that the call failed
    private void sendPluginError(String errMsg, JSONObject withParameters) {
        if(errMsg.isEmpty()) {
            sendPluginResult(false, withParameters );
        } else {
            JSONObject extendedParams = (withParameters != null) ? withParameters : new JSONObject();
            try {
                extendedParams.put("error", errMsg);
            } catch (JSONException jse) {
                if(pendingCallbackContext != null) {
                    pendingCallbackContext.error("Problem while preparing JSON error: " + jse.getMessage());
                }
            }
            sendPluginResult( false, extendedParams );
        }
    }

    private void sendPluginError(String errMsg) {
        sendPluginError(errMsg, null);
    }
}