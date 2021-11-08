import Foundation

@objc public class Media: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
