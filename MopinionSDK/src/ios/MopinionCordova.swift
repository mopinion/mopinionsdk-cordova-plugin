import UIKit
import MopinionSDK

@objc(MopinionCordova) class MopinionCordova : CDVPlugin {
    // for async functions
    var pendingCommand: CDVInvokedUrlCommand?
    
    @objc(load:)
    func load(command: CDVInvokedUrlCommand) {
        pendingCommand = command

      // retrieve args from a dictionary
      let params = command.arguments[0] as? [String: Any]
      let deploymentKey = params?["deploymentKey"] as? String ?? ""
      let enableLogging = params?["enableLogging"] as? Bool ?? false

        // prepare return args
        var outparams: [String: Any] = [:]
        outparams["event"]="deployment_will_load"
        outparams["details"]=params

      if !deploymentKey.isEmpty {
          
        MopinionSDK.load(deploymentKey as NSString, enableLogging)

          // the success parameters to give to the callback
        sendPluginResult(wasSuccessful: true, withParameters: outparams)
      } else {
        outparams["error"]="No deployment key specified"
        sendPluginResult(wasSuccessful: false, withParameters: outparams)
      }

    }

    @objc(evaluate:)
    func evaluate(command: CDVInvokedUrlCommand) {
        pendingCommand = command
        // retrieve args from a dictionary
        let params = command.arguments[0] as? [String: Any]
        let event = params?["event"] as? String ?? ""

        if !event.isEmpty {
            // result will go via the mopinionOnEvaluateHandler
            MopinionSDK.evaluate(event, onEvaluateDelegate: self)
        } else {
            sendPluginError(errMsg: "No event specified, provide an event to the call.", withParameters: params)
        }
    }
    
    @objc(openFormAlways:)
    func openFormAlways(command: CDVInvokedUrlCommand) {
        pendingCommand = command
        // retrieve args from a dictionary
        let params = command.arguments[0] as? [String: Any]
        let formKey = params?["formKey"] as? String ?? ""

        // prepare return args
        var outparams: [String: Any] = [:]
        outparams["details"]=params

        if !formKey.isEmpty {
            MopinionSDK.openFormAlways(getCordovaVC(), formKey)
            
            sendPluginResultOK(withParameters: outparams)  // pretend it allways works
        } else {
            sendPluginError(errMsg: "No formKey specified, provide a formKey to the call.", withParameters: outparams)
        }
    }

    @objc(addData:)
    func addData(command: CDVInvokedUrlCommand) {
        pendingCommand = command
        // retrieve args from a dictionary
        let params = command.arguments[0] as? [String: Any]
        let forDataKey = params?["forDataKey"] as? String ?? ""
        let dataValue = params?["dataValue"] as? String ?? ""

        // prepare return args
        var outparams: [String: Any] = [:]
        outparams["event"]="add_extra_data"
        outparams["details"]=params

        if !forDataKey.isEmpty {
            MopinionSDK.data(forDataKey, dataValue)
            
            sendPluginResultOK(withParameters: outparams)  // pretend it allways works
        } else {
            sendPluginError(errMsg: "No DataKey specified, provide a forDataKey to the call.", withParameters: outparams)
        }
    }

    // uses only MopinionSDK.removeData(forKey: String)
    @objc(removeData:)
    func removeData(command: CDVInvokedUrlCommand) {
        pendingCommand = command
        // retrieve args from a dictionary
        let params = command.arguments[0] as? [String: Any]
        let forDataKey = params?["forDataKey"] as? String ?? ""

        // prepare return args
        var outparams: [String: Any] = [:]
        outparams["event"]="remove_extra_data"
        outparams["details"]=params

        if !forDataKey.isEmpty {
            MopinionSDK.removeData(forKey: forDataKey)
            
            sendPluginResultOK(withParameters: outparams)  // pretend it allways works
        } else {
            sendPluginError(errMsg: "No DataKey specified, provide a forDataKey to the call.", withParameters: outparams)
        }
    }

    // uses MopinionSDK.removeData(), renamed here because no function overloading in JS
    @objc(removeAllExtraData:)
    func removeAllExtraData(command: CDVInvokedUrlCommand) {
        pendingCommand = command

        // prepare return args
        var outparams: [String: Any] = [:]
        outparams["event"]="remove_all_extra_data"

        MopinionSDK.removeData()
        sendPluginResultOK(withParameters: outparams)  // pretend it allways works
    }

    
    // MARK: helper functions

    // MARK: submit plugin result, if any. Use wasSuccessful = false to report error
    func sendPluginResult(wasSuccessful: Bool, withParameters: [String : Any]?) {
        let commandStatus = wasSuccessful ? CDVCommandStatus_OK : CDVCommandStatus_ERROR
        var pluginResult: CDVPluginResult
        
        if((withParameters) != nil) {
            pluginResult = CDVPluginResult( status: commandStatus, messageAs: withParameters )
        } else {
            pluginResult = CDVPluginResult( status: commandStatus )
        }
        self.commandDelegate?.send(
            pluginResult,
            callbackId: pendingCommand?.callbackId
        )
        pendingCommand = nil
    }
    
    func sendPluginResult(wasSuccessful: Bool) {
        sendPluginResult(wasSuccessful: wasSuccessful, withParameters: nil)
    }
    
    func sendPluginResultOK(withParameters: [String : Any]?) {
        sendPluginResult(wasSuccessful: true, withParameters: withParameters )
    }
    
    // let the plugin caller know that the call failed
    func sendPluginError(errMsg: String, withParameters: [String : Any]?) {
        if(errMsg.isEmpty) {
            sendPluginResult(wasSuccessful: false, withParameters: withParameters )
        } else {
            var extendedParams = withParameters ?? [:]
            extendedParams["error"] = errMsg
            
            sendPluginResult(wasSuccessful: false, withParameters: extendedParams )
        }
    }
    
    func sendPluginError(errMsg: String) {
        if(errMsg.isEmpty) {
            sendPluginResult(wasSuccessful: false, withParameters: nil )
        } else {
            sendPluginError(errMsg: errMsg, withParameters: nil )
        }
    }

    // get the application root viewcontroller. Probably never needed.
    func getRootVC() -> UIViewController? {
        return UIApplication.shared.keyWindow?.rootViewController;
    }
    
    // get the cordova viewcontroller
    func getCordovaVC() -> UIViewController {
        return self.viewController // you can also get the webview or view
    }
    
}

// MARK: extensions for our own protocols

// this is called by the evaluate() method
extension MopinionCordova: MopinionOnEvaluateDelegate {
    func mopinionOnEvaluateHandler(hasResult: Bool, event: String, formKey: String?, response: [String : Any]?) {

        var params: [String: Any] = [:]
        
        params["hasResult"] = hasResult
        params["event"] = event
        params["formKey"] = formKey ?? NSNull()
        params["response"] = response ?? NSNull()

        // TODO: also add something to return errors
        sendPluginResultOK(withParameters: params)
    }
}
