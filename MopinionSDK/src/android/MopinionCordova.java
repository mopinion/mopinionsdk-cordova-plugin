package com.mopinion.plugin;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;

import com.mopinion.android_sdk_web.Mopinion;   // version 1.x
import com.mopinion.android_sdk_web.domain.events.Reason;
import com.mopinion.android_sdk_web.ui.states.FormState;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

import kotlin.Unit;

/**
 * Bridges the cordova plugin calls from JavaScript to the mopinionwebsdk.
 */
public class MopinionCordova extends CordovaPlugin {

    private Mopinion M;
//    private CallbackContext pendingCallbackContext; // the context of any pending async call

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        // our init code for the first time the plugin is activated
        M = null;
//        pendingCallbackContext = null;
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
//        pendingCallbackContext = callbackContext;

        String deploymentKey;
        try {
            deploymentKey = params.getString("deploymentKey");
        } catch (JSONException jse) {
            sendPluginError(callbackContext,"Error reading deployment key:" + jse.getMessage(), params);
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
            sendPluginError(callbackContext,"Problem creating JSON: " + jse.getMessage(), params);
            return;
        }

        if (deploymentKey.isEmpty()) {
            sendPluginError(callbackContext, "No deployment key given.", params);
        } else {
            // mopinion needs a companion object before doing anything else
            AppCompatActivity activity = cordova.getActivity();
            if ( activity != null ) {
                Mopinion.Companion.initialise(activity, deploymentKey, enableLogging);
                // load the SDK. TODO: make this async when this SDK method becomes Async
                // the 2.0.0 sdk version no longer needs this
                M = new Mopinion(activity);
                sendPluginResultOK(callbackContext, outparams);
            } else {
                sendPluginError(callbackContext, "MopinionSDK requires a non-null Activity.", params);
            }
        }
    }

    private void event(JSONObject params, CallbackContext callbackContext) {
//        pendingCallbackContext = callbackContext;

        String event;
        try {
            event = params.getString("event");
        } catch (JSONException jse) {
            sendPluginError(callbackContext,"Error reading event: " + jse.getMessage(), params);
            return;
        }

        if (event.isEmpty()) {
            sendPluginError(callbackContext,"No event specified, provide an event to the call.", params);
            return;
        } else {
            // this will return immediately without waiting for a form to open
            addCordovaPluginVersionToData();
            M.event(event, false, formState -> {
                if (formState instanceof FormState.FormOpened) {
                    sendPluginResultOK(callbackContext, params);
                } else if (formState instanceof FormState.HasNotBeenShown) {
                    Reason reasonWhyTheFormDidNotShow = ((FormState.HasNotBeenShown) formState).getReason();
                    if (reasonWhyTheFormDidNotShow instanceof Reason.ErrorWhileFetchingForm) {
                        sendPluginError(callbackContext, "Error while fetching the form");
                    } else {
                        // all fine if the form can not be shown because of conditions or missing event
                        sendPluginResultOK(callbackContext, params);
                    }
                } else {
                    sendPluginResultOK(callbackContext, params); // pretend it always works for the other cases
                }
                return Unit.INSTANCE;
            });
        }
    }

    private void evaluate(JSONObject params, CallbackContext callbackContext) {
//        pendingCallbackContext = callbackContext;

        String event;
        try {
            event = params.getString("event");
        } catch (JSONException jse) {
            sendPluginError(callbackContext, "Error reading event: " + jse.getMessage(), params);
            return;
        }

        if (event.isEmpty()) {
            sendPluginError(callbackContext, "No event specified, provide an event to the call.", params);
            return;
        } else {
            // sdk version 1.x/2.0.0 didn't implement this method
            sendPluginError(callbackContext, "evaluate() method currently not implemented in Android");
//            // evaluate() will pass the formkey, if any, to its callback handler
//            M.evaluate(event, new Mopinion.MopinionOnEvaluateListener() {
//                @Override
//                public void onMopinionEvaluate(boolean hasResult, String event, @Nullable String formKey, @Nullable JSONObject response) {
//                    // prepare return args
//                    JSONObject outparams = new JSONObject();
//                    try {
//                        outparams.put("hasResult", hasResult);
//                        outparams.put("event", event);
//                        outparams.put("formKey", (formKey != null) ? formKey : JSONObject.NULL);
//                        outparams.put("response", (response != null) ? response : JSONObject.NULL);
//                    } catch (JSONException jse) {
//                        sendPluginError(callbackContext, "Problem creating JSON: " + jse.getMessage(), params);
//                        return;
//                    }
//
//                    sendPluginResultOK(callbackContext, outparams);
//
////                    // here the code to check the parameters
////                    if(hasResult) {
////                        // at least one form was found and all optional parameters are non-null
////                        M.openFormAlways(formKey); // because conditions can change every time, use the formkey
////                    }else{
////                        // no form would open
////                        if(formKey !=null) {
////                            M.openFormAlways(formKey); // because conditions can change every time, use the formkey
////                        }else{
//////                            Toast.makeText(getApplicationContext(), "Evaluate: no form found for event '" + event + "'.", Toast.LENGTH_LONG).show();
////                        }
////                    }
//                }
//            } );
        }
    }

    private void openFormAlways(JSONObject params, CallbackContext callbackContext) {
//        pendingCallbackContext = callbackContext;

        String formKey;
        try {
            formKey = params.getString("formKey");
        } catch (JSONException jse) {
            sendPluginError(callbackContext,"Error reading formKey: " + jse.getMessage(), params);
            return;
        }

        // prepare return args
        JSONObject outparams = new JSONObject();
        try {
            outparams.put("details", params);
        } catch (JSONException jse) {
            sendPluginError(callbackContext, "Problem creating JSON: " + jse.getMessage(), params);
            return;
        }

        if (formKey.isEmpty()) {
            sendPluginError(callbackContext, "No formKey specified, provide a formKey to the call.", outparams);
            return;
        } else {
            // sdk 1.x, 2.0.0 didn't implement this method
            sendPluginError(callbackContext, "openFormAlways() method currently not implemented in Android");

//            // TODO: later convert into async call with success/error-handling
//            addCordovaPluginVersionToData();
//            M.openFormAlways(formKey);
//            sendPluginResultOK(callbackContext, outparams);
        }
    }

    private void addData(JSONObject params, CallbackContext callbackContext) {
//        pendingCallbackContext = callbackContext;

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
            sendPluginError(callbackContext,"Problem creating JSON: " + jse.getMessage(), params);
            return;
        }

        if (forDataKey.isEmpty()) {
            sendPluginError(callbackContext, "No DataKey specified, provide a forDataKey to the call.", params);
        } else {
            M.data(forDataKey,dataValue);
            sendPluginResultOK(callbackContext, outparams);
        }
    }

    private void removeData(JSONObject params, CallbackContext callbackContext) {
//        pendingCallbackContext = callbackContext;

        String forDataKey;
        forDataKey = params.optString("forDataKey");

        // prepare return args
        JSONObject outparams = new JSONObject();
        try {
            outparams.put("event", "remove_extra_data");
            outparams.put("details", params);
        } catch (JSONException jse) {
            sendPluginError(callbackContext, "Problem creating JSON: " + jse.getMessage(), params);
            return;
        }

        if (forDataKey.isEmpty()) {
            sendPluginError(callbackContext, "No DataKey specified, provide a forDataKey to the call.", params);
        } else {
            M.removeData(forDataKey);
            sendPluginResultOK(callbackContext, outparams);
        }
    }

    private void removeAllExtraData(CallbackContext callbackContext) {
//        pendingCallbackContext = callbackContext;

        // prepare return args
        JSONObject outparams = new JSONObject();
        try {
            outparams.put("event", "remove_all_extra_data");
//            outparams.put("details", params);
        } catch (JSONException jse) {
            sendPluginError(callbackContext, "Problem creating JSON: " + jse.getMessage());
            return;
        }

        M.removeData();
        sendPluginResultOK(callbackContext, outparams); // pretend it always works
    }

    // MARK: helper functions

    // MARK: submit plugin result, if any. Use wasSuccessful = false to report error.
    // Don't call this directly, instead use sendPluginResultOK or sendPluginError
    private void sendPluginResult(CallbackContext callbackContext, boolean wasSuccessful, JSONObject withParameters) {
        if(callbackContext == null) {
            LOG.e("PluginResultError", "CallbackContext is not set.");
            return;
        }

        if ((withParameters) != null) {
            if (wasSuccessful) {
                callbackContext.success(withParameters);
            } else {
                callbackContext.error(withParameters);
            }
        } else {
            if (wasSuccessful) {
                callbackContext.success();
            } else {
                // this should never happen, an error message is mandatory
                callbackContext.error("No error message was supplied.");
            }
        }
//        pendingCallbackContext = null;
    }

    private void sendPluginResultOK(CallbackContext callbackContext, JSONObject withParameters) {
        sendPluginResult(callbackContext, true, withParameters);
    }

    private void sendPluginResultOK(CallbackContext callbackContext) {
        sendPluginResult(callbackContext, true, null);
    }

    // let the plugin caller know that the call failed
    private void sendPluginError(CallbackContext callbackContext, String errMsg, JSONObject withParameters) {
        if(errMsg.isEmpty()) {
            sendPluginResult(callbackContext, false, withParameters);
        } else {
            JSONObject extendedParams = (withParameters != null) ? withParameters : new JSONObject();
            try {
                extendedParams.put("error", errMsg);
            } catch (JSONException jse) {
                if(callbackContext != null) {
                    callbackContext.error("Problem while preparing JSON error: " + jse.getMessage());
                }
            }
            sendPluginResult(callbackContext, false, extendedParams);
        }
    }

    private void sendPluginError(CallbackContext callbackContext, String errMsg) {
        sendPluginError(callbackContext, errMsg, null);
    }

    private String getCordovaPluginSemanticVersion() {
        // Cordova creates our buildConfig class in the namespace of the app.
        // But we don't know the name of the app, so can't import its BuildConfig.
        // Instead we use reflection to read our version from the app's BuildConfig.
        final String BUILD_CONFIG_CLASSNAME = "BuildConfig";
        String semanticVersion = null;

        // Load PackageInfo
        Activity activity = cordova.getActivity();
        String packageName = activity.getPackageName();
        String buildConfigClassFullyQualifiedName = null;

        // Try to get BuildConfig class using Reflection
        Class bcClass = null;

        try {
            buildConfigClassFullyQualifiedName = packageName + "." + BUILD_CONFIG_CLASSNAME;
            bcClass = Class.forName(buildConfigClassFullyQualifiedName);
        } catch (ClassNotFoundException e) {
            LOG.e("MopinionCordovaError", "Failed to get semantic version.");
//          callbackContext.error("MopinionCordova exception: " + e.getMessage());
        }

        if (bcClass == null) {
            Package basePackage = activity.getClass().getPackage();
            if (basePackage != null) {
                String basePackageName = basePackage.getName();
                buildConfigClassFullyQualifiedName = basePackageName + "." + BUILD_CONFIG_CLASSNAME;
            }
            try {
                bcClass = Class.forName(buildConfigClassFullyQualifiedName);
            } catch (ClassNotFoundException e) {
                LOG.e("MopinionCordovaError", "Failed to get semantic version.");
//          callbackContext.error("MopinionCordova exception: " + e.getMessage());
                return null;
            }
        }

        // now retrieve the value from the BuildConfig class
        try {
            Field versionField = bcClass.getField("MOPINION_CORDOVA_PLUGIN_VERSION");
            semanticVersion = (String) versionField.get(bcClass);
        } catch(Exception e) {
            LOG.e("MopinionCordovaError", "Failed to get semantic version.");
//          callbackContext.error("MopinionCordova internal exception: " + e.getMessage());
            return null;
        }

        return semanticVersion;
    }

    private void addCordovaPluginVersionToData() {
        String forDataKey = "Cordova plugin version";

        String dataValue;
        dataValue = getCordovaPluginSemanticVersion();
        if(dataValue != null && M != null) {
            M.data(forDataKey, dataValue);
        }
    }
}