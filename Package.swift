// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "WhiteguruCapacitorPluginMedia",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "WhiteguruCapacitorPluginMedia",
            targets: ["MediaPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "MediaPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/MediaPlugin"),
        .testTarget(
            name: "MediaPluginTests",
            dependencies: ["MediaPlugin"],
            path: "ios/Tests/MediaPluginTests")
    ]
)