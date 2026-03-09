import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(BackgroundServicePlugin)
public class BackgroundServicePlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "BackgroundServicePlugin"
    public let jsName = "BackgroundService"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "start", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "stop", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "checkLocationPermission", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = BackgroundService()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
    
    @objc func start(_ call: CAPPluginCall) {
        implementation.start()
        call.resolve()
    }
    
    @objc func stop(_ call: CAPPluginCall) {
        implementation.stop()
        call.resolve()
    }
    
    @objc func checkLocationPermission(_ call: CAPPluginCall) {
        let state = implementation.checkPermission()
        if state == "prompt" {
            implementation.requestPermission()
            // In a real app, you'd wait for the delegate callback to resolve.
            // For now, mirroring the Android logic of "check and request".
            call.resolve(["value": "prompt"])
        } else {
            call.resolve(["value": state])
        }
    }
}
