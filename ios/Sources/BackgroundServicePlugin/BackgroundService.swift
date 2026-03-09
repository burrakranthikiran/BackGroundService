import Foundation
import CoreLocation

@objc public class BackgroundService: NSObject, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    private var timer: Timer?
    private let userDefaults = UserDefaults.standard
    
    // Config matches Android
    private let interval: TimeInterval = 60 // 1 minute
    private let apiUrl = "https://snatchit-api.qztbox.com/customer/notification/locationNotification"
    
    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false
        locationManager.showsBackgroundLocationIndicator = true
    }
    
    @objc public func echo(_ value: String) -> String {
        userDefaults.set(value, forKey: "customer_id")
        return value
    }
    
    @objc public func start() {
        DispatchQueue.main.async {
            self.locationManager.startUpdatingLocation()
            self.startTimer()
        }
    }
    
    @objc public func stop() {
        DispatchQueue.main.async {
            self.locationManager.stopUpdatingLocation()
            self.stopTimer()
        }
    }
    
    private func startTimer() {
        stopTimer() // Clear existing
        timer = Timer.scheduledTimer(withTimeInterval: interval, repeats: true) { [weak self] _ in
            self?.checkLocationAndSend()
        }
    }
    
    private func stopTimer() {
        timer?.invalidate()
        timer = nil
    }
    
    private func checkLocationAndSend() {
        guard let location = locationManager.location else {
            print("BG_SERVICE: Location not available")
            return
        }
        
        let lat = location.coordinate.latitude
        let lng = location.coordinate.longitude
        
        hitLocationApi(lat: lat, lng: lng)
    }
    
    private func hitLocationApi(lat: Double, lng: Double) {
        guard let customerId = userDefaults.string(forKey: "customer_id") else {
            print("BG_SERVICE: Customer ID missing")
            return
        }
        
        guard let url = URL(string: apiUrl) else { return }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "lat": lat,
            "lng": lng,
            "customerId": customerId
        ]
        
        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: body)
        } catch {
            print("BG_SERVICE: JSON error")
            return
        }
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("BG_SERVICE: API error: \(error.localizedDescription)")
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                print("BG_SERVICE: API success: \(httpResponse.statusCode)")
            }
        }.resume()
    }
    
    func checkPermission() -> String {
        let status = locationManager.authorizationStatus
        switch status {
        case .authorizedAlways, .authorizedWhenInUse:
            return "granted"
        case .denied, .restricted:
            return "denied"
        default:
            return "prompt"
        }
    }
    
    public func requestPermission() {
        locationManager.requestAlwaysAuthorization()
    }
}
