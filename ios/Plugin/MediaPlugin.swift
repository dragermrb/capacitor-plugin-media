import Foundation
import MobileCoreServices
import Photos
import UniformTypeIdentifiers
import Capacitor

public class JSDate {
    static func toString(_ date: Date) -> String {
        let formatter = ISO8601DateFormatter()
        return formatter.string(from: date)
    }
}

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(MediaPlugin)
public class MediaPlugin: CAPPlugin {
    private let implementation = Media()
    
    typealias JSObject = [String:Any]
    static let DEFAULT_QUANTITY = 25
    static let DEFAULT_TYPES = "photos"
    static let DEFAULT_THUMBNAIL_WIDTH = 256
    static let DEFAULT_THUMBNAIL_HEIGHT = 256
    
    // Must be lazy here because it will prompt for permissions on instantiation without it
    lazy var imageManager = PHCachingImageManager()
    
    @objc func getAlbums(_ call: CAPPluginCall) {
        checkAuthorization(allowed: {
            self.fetchAlbumsToJs(call)
        }, notAllowed: {
            call.reject("Access to photos not allowed by user")
        })
    }
    
    @objc func getMedias(_ call: CAPPluginCall) {
        checkAuthorization(allowed: {
            self.fetchResultAssetsToJs(call)
        }, notAllowed: {
            call.reject("Access to photos not allowed by user")
        })
    }
    
    @objc func createAlbum(_ call: CAPPluginCall) {
        guard let name = call.getString("name") else {
            call.reject("Must provide a name")
            return
        }
        
        checkAuthorization(allowed: {
            self.createAlbumByName(name) { phAsset in
                if phAsset == nil {
                    call.reject("Unable to create album")
                    return
                } else {
                    call.resolve([
                        "identifier": phAsset?.localIdentifier as Any,
                        "name": phAsset?.localizedTitle as Any,
                    ])
                    return
                }
            }
        }, notAllowed: {
            call.reject("Access to photos not allowed by user")
        })
    }
    
    @objc func savePhoto(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Must provide the data path")
            return
        }
        guard let album = call.getObject("album") else {
            call.reject("Album is required")
            return
        }
        
        checkAuthorization(allowed: {
            if (album["id"] != nil) {
                self.addMediaToAlbumById(
                    path: path,
                    albumId: album["id"] as! String,
                    successHandler: { (fullPath: String) in
                        call.resolve([
                            "path": fullPath,
                            "name": URL(fileURLWithPath: fullPath).lastPathComponent
                        ])
                    },
                    failHandler: { (errorMsg: String) in
                        call.reject(errorMsg)
                    }
                )
            }
            else if (album["name"] != nil) {
                self.addMediaToAlbumByName(
                    path: path,
                    albumName: album["name"] as! String,
                    successHandler: { (fullPath: String) in
                        call.resolve([
                            "path": fullPath,
                            "name": URL(fileURLWithPath: fullPath).lastPathComponent
                        ])
                    },
                    failHandler: { (errorMsg: String) in
                        call.reject(errorMsg)
                    }
                )
            } else {
                call.reject("Album ID or album NAME required")
                return
            }
        }, notAllowed: {
            call.reject("Access to photos not allowed by user")
            return
        })
    }
    
    @objc func saveVideo(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Must provide the data path")
            return
        }
        guard let album = call.getObject("album") else {
            call.reject("Album is required")
            return
        }
        
        checkAuthorization(allowed: {
            if (album["id"] != nil) {
                self.addMediaToAlbumById(
                    path: path,
                    albumId: album["id"] as! String,
                    successHandler: { (fullPath: String) in
                        call.resolve([
                            "path": fullPath,
                            "name": URL(fileURLWithPath: fullPath).lastPathComponent
                        ])
                    },
                    failHandler: { (errorMsg: String) in
                        call.reject(errorMsg)
                    }
                )
            }
            else if (album["name"] != nil) {
                self.addMediaToAlbumByName(
                    path: path,
                    albumName: album["name"] as! String,
                    successHandler: { (fullPath: String) in
                        call.resolve([
                            "path": fullPath,
                            "name": URL(fileURLWithPath: fullPath).lastPathComponent
                        ])
                    },
                    failHandler: { (errorMsg: String) in
                        call.reject(errorMsg)
                    }
                )
            } else {
                call.reject("Album ID or album NAME required")
                return
            }
        }, notAllowed: {
            call.reject("Access to photos not allowed by user")
            return
        })
    }
    
    @objc func saveGif(_ call: CAPPluginCall) {
        guard let path = call.getString("path") else {
            call.reject("Must provide the data path")
            return
        }
        guard let album = call.getObject("album") else {
            call.reject("Album is required")
            return
        }
        
        checkAuthorization(allowed: {
            if (album["id"] != nil) {
                self.addMediaToAlbumById(
                    path: path,
                    albumId: album["id"] as! String,
                    successHandler: { (fullPath: String) in
                        call.resolve([
                            "path": fullPath,
                            "name": URL(fileURLWithPath: fullPath).lastPathComponent
                        ])
                    },
                    failHandler: { (errorMsg: String) in
                        call.reject(errorMsg)
                    }
                )
            }
            else if (album["name"] != nil) {
                self.addMediaToAlbumByName(
                    path: path,
                    albumName: album["name"] as! String,
                    successHandler: { (fullPath: String) in
                        call.resolve([
                            "path": fullPath,
                            "name": URL(fileURLWithPath: fullPath).lastPathComponent
                        ])
                    },
                    failHandler: { (errorMsg: String) in
                        call.reject(errorMsg)
                    }
                )
            } else {
                call.reject("Album ID or album NAME required")
                return
            }
        }, notAllowed: {
            call.reject("Access to photos not allowed by user")
            return
        })
    }
    
    
    func checkAuthorization(allowed: @escaping () -> Void, notAllowed: @escaping () -> Void) {
        let status = PHPhotoLibrary.authorizationStatus()
        if status == PHAuthorizationStatus.authorized {
            allowed()
        } else {
            PHPhotoLibrary.requestAuthorization({ (newStatus) in
                if newStatus == PHAuthorizationStatus.authorized {
                    allowed()
                } else {
                    notAllowed()
                }
            })
        }
    }
    
    func fetchAlbumsToJs(_ call: CAPPluginCall) {
        var albums = [JSObject]()
        
        let loadSharedAlbums = call.getBool("loadShared", false)
        
        // Load our smart albums
        var fetchResult = PHAssetCollection.fetchAssetCollections(with: .smartAlbum, subtype: .albumRegular, options: nil)
        fetchResult.enumerateObjects({ (collection, count, stop: UnsafeMutablePointer<ObjCBool>) in
            var o = JSObject()
            o["name"] = collection.localizedTitle
            o["identifier"] = collection.localIdentifier
            o["type"] = "smart"
            albums.append(o)
        })
        
        if loadSharedAlbums {
            fetchResult = PHAssetCollection.fetchAssetCollections(with: .album, subtype: .albumCloudShared, options: nil)
            fetchResult.enumerateObjects({ (collection, count, stop: UnsafeMutablePointer<ObjCBool>) in
                var o = JSObject()
                o["name"] = collection.localizedTitle
                o["identifier"] = collection.localIdentifier
                o["type"] = "shared"
                albums.append(o)
            })
        }
        
        // Load our user albums
        PHCollectionList.fetchTopLevelUserCollections(with: nil).enumerateObjects({ (collection, count, stop: UnsafeMutablePointer<ObjCBool>) in
            var o = JSObject()
            o["name"] = collection.localizedTitle
            o["identifier"] = collection.localIdentifier
            o["type"] = "user"
            albums.append(o)
        })
        
        call.resolve([
            "albums": albums
        ])
    }
    
    func fetchResultAssetsToJs(_ call: CAPPluginCall) {
        var assets: [JSObject] = []
        
        let albumId = call.getString("albumIdentifier")
        
        let quantity = call.getInt("quantity", MediaPlugin.DEFAULT_QUANTITY)
        
        var targetCollection: PHAssetCollection?
        
        let options = PHFetchOptions()
        options.fetchLimit = quantity
        options.sortDescriptors = [NSSortDescriptor(key: "creationDate", ascending: true)]
        
        if albumId != nil {
            let albumFetchResult = PHAssetCollection.fetchAssetCollections(withLocalIdentifiers: [albumId!], options: nil)
            albumFetchResult.enumerateObjects({ (collection, count, _) in
                targetCollection = collection
            })
        }
        
        var fetchResult: PHFetchResult<PHAsset>;
        if targetCollection != nil {
            fetchResult = PHAsset.fetchAssets(in: targetCollection!, options: options)
        } else {
            fetchResult = PHAsset.fetchAssets(with: options)
        }
        
        let types = call.getString("types") ?? MediaPlugin.DEFAULT_TYPES
        let thumbnailWidth = call.getInt("thumbnailWidth", MediaPlugin.DEFAULT_THUMBNAIL_WIDTH)
        let thumbnailHeight = call.getInt("thumbnailHeight", MediaPlugin.DEFAULT_THUMBNAIL_HEIGHT)
        let thumbnailSize = CGSize(width: thumbnailWidth, height: thumbnailHeight)
        let thumbnailQuality = call.getInt("thumbnailQuality", 95)
        
        let requestOptions = PHImageRequestOptions()
        requestOptions.isNetworkAccessAllowed = true
        requestOptions.version = .current
        requestOptions.deliveryMode = .opportunistic
        requestOptions.isSynchronous = true
        
        fetchResult.enumerateObjects({ (asset, count: Int, stop: UnsafeMutablePointer<ObjCBool>) in
            
            if asset.mediaType == .image && types == "videos" {
                return
            }
            if asset.mediaType == .video && types == "photos" {
                return
            }
            
            var a = JSObject()
            
            self.imageManager.requestImage(for: asset, targetSize: thumbnailSize, contentMode: .aspectFill, options: requestOptions, resultHandler: { (fetchedImage, _) in
                guard let image = fetchedImage else {
                    return
                }
                
                a["identifier"] = asset.localIdentifier
                
                // TODO: We need to know original type
                a["data"] = image.jpegData(compressionQuality: CGFloat(thumbnailQuality) / 100.0)?.base64EncodedString()
                
                if asset.creationDate != nil {
                    a["creationDate"] = JSDate.toString(asset.creationDate!)
                }
                a["fullWidth"] = asset.pixelWidth
                a["fullHeight"] = asset.pixelHeight
                a["thumbnailWidth"] = image.size.width
                a["thumbnailHeight"] = image.size.height
                a["location"] = self.makeLocation(asset)
                
                assets.append(a)
            })
        })
        
        call.resolve([
            "medias": assets
        ])
    }
    
    
    func makeLocation(_ asset: PHAsset) -> JSObject {
        var loc = JSObject()
        guard let location = asset.location else {
            return loc
        }
        
        loc["latitude"] = location.coordinate.latitude
        loc["longitude"] = location.coordinate.longitude
        loc["altitude"] = location.altitude
        loc["heading"] = location.course
        loc["speed"] = location.speed
        return loc
    }
    
    func findAlbumByName(_ name:String) -> PHAssetCollection? {
        let fetchOptions = PHFetchOptions()
        fetchOptions.predicate = NSPredicate(format: "title = %@", name)
        let fetchResult = PHAssetCollection.fetchAssetCollections(with: .album, subtype: .albumRegular, options: fetchOptions)
        guard let photoAlbum = fetchResult.firstObject else {
            return nil
        }
        
        return photoAlbum
    }
    
    func findAlbumById(_ id:String) -> PHAssetCollection? {
        var targetCollection: PHAssetCollection?
        
        let albumFetchResult = PHAssetCollection.fetchAssetCollections(withLocalIdentifiers: [id], options: nil)
        albumFetchResult.enumerateObjects({ (collection, count, _) in
            targetCollection = collection
        })
        
        
        return targetCollection
    }
    
    func createAlbumByName(_ albumName:String, completion: @escaping (_ phAsset: PHAssetCollection?) -> Void) -> Void{
        
        var albumPlaceholder: PHObjectPlaceholder?
        var targetCollection: PHAssetCollection?
        
        PHPhotoLibrary.shared().performChanges({
            let createAlbumRequest = PHAssetCollectionChangeRequest.creationRequestForAssetCollection(withTitle: albumName)
            albumPlaceholder = createAlbumRequest.placeholderForCreatedAssetCollection
        }, completionHandler: { success, error in
            if (!success || albumPlaceholder == nil) {
                completion(nil)
            } else {
                targetCollection = self.findAlbumById(albumPlaceholder!.localIdentifier)
                
                if targetCollection == nil {
                    completion(nil)
                } else {
                    completion(targetCollection)
                }
            }
        })
    }
    
    func getAssetUrl(_ asset: PHAsset, completion: @escaping (_ fullPath: String?) -> Void) {
        
        let imageRequestOptions = PHImageRequestOptions()
        imageRequestOptions.isSynchronous = false
        imageRequestOptions.resizeMode = .exact
        imageRequestOptions.deliveryMode = .highQualityFormat
        imageRequestOptions.version = .current
        imageRequestOptions.isNetworkAccessAllowed = true
        
        if asset.mediaType == .image {
            let options: PHContentEditingInputRequestOptions = PHContentEditingInputRequestOptions()
            asset.requestContentEditingInput(with: options, completionHandler: {(contentEditingInput: PHContentEditingInput?, info: [AnyHashable : Any]) -> Void in
                if let contentEditingInput = contentEditingInput {
                    completion(contentEditingInput.fullSizeImageURL?.absoluteString)
                } else {
                    completion(nil)
                }
            })
        } else if asset.mediaType == .video {
            PHImageManager.default().requestAVAsset(forVideo: asset, options: nil, resultHandler: { (avAsset: AVAsset?, avAudioMix: AVAudioMix?, info: [AnyHashable : Any]?) in
                
                if( avAsset is AVURLAsset ) {
                    let video_asset = avAsset as! AVURLAsset
                    let url = URL(fileURLWithPath: video_asset.url.relativePath)
                    completion(url.relativePath)
                }
                else if(avAsset is AVComposition) {
                    let token = info?["PHImageFileSandboxExtensionTokenKey"] as! String
                    let path = token.components(separatedBy: ";").last
                    completion(path)
                }
            })
        }
    }
    
    func addMediaToAlbum(
        path: String,
        targetCollection: PHAssetCollection,
        successHandler: @escaping (_ fullPath:String) -> Void,
        failHandler: @escaping (_ errorMsg: String) -> Void
    ) {
        if !targetCollection.canPerform(.addContent) {
            failHandler("Album doesn't support adding content (is this a smart album?)")
            return
        }
        
        var assetId: String = ""
        let url: URL = URL(string: path)!
        
        
        // Add it to the photo library.
        PHPhotoLibrary.shared().performChanges({
            var creationRequest: PHAssetChangeRequest
            if (url.containsVideo){
                creationRequest = PHAssetChangeRequest.creationRequestForAssetFromVideo(atFileURL: url)!
            } else {
                creationRequest = PHAssetChangeRequest.creationRequestForAssetFromImage(atFileURL: url)!
            }
            
            let placeHolder = creationRequest.placeholderForCreatedAsset
            
            
            let addAssetRequest = PHAssetCollectionChangeRequest(for: targetCollection)
            addAssetRequest?.addAssets([placeHolder!] as NSArray)
            assetId = placeHolder!.localIdentifier
            
            
        }, completionHandler: {success, error in
            if !success {
                failHandler("Unable to save media to album")
            } else {
                let asset = PHAsset.fetchAssets(withLocalIdentifiers: [assetId], options: nil).firstObject
                
                if asset == nil{
                    failHandler("Cannot fetch asset")
                } else {
                    self.getAssetUrl(asset!, completion: { (fullPath) in
                        successHandler(fullPath!)
                    })
                }
            }
        })
    }
    
    func addMediaToAlbumById(
        path: String,
        albumId: String,
        successHandler: @escaping (_ fullPath:String) -> Void,
        failHandler: @escaping (_ errorMsg: String) -> Void
    ) {
        let targetCollection: PHAssetCollection? = findAlbumById(albumId)
        
        if targetCollection == nil {
            failHandler("Unable to find that album by id")
            return
        }
        
        self.addMediaToAlbum(
            path: path,
            targetCollection: targetCollection!,
            successHandler: { (fullPath: String) in
                successHandler(fullPath)
            },
            failHandler: { (errorMsg: String) in
                failHandler(errorMsg)
            }
        )
    }
    
    func addMediaToAlbumByName(
        path: String,
        albumName: String,
        successHandler: @escaping (_ fullPath:String) -> Void,
        failHandler: @escaping (_ errorMsg: String) -> Void
    ) {
        let targetCollection: PHAssetCollection? = findAlbumByName(albumName)
        
        if targetCollection != nil {
            self.addMediaToAlbum(
                path: path,
                targetCollection: targetCollection!,
                successHandler: { (fullPath: String) in
                    successHandler(fullPath)
                },
                failHandler: { (errorMsg: String) in
                    failHandler(errorMsg)
                }
            )
        } else {
            self.createAlbumByName(
                albumName,
                completion: { (phAsset: PHAssetCollection?) in
                    if phAsset == nil {
                        failHandler("Unable to create album")
                    } else {
                        self.addMediaToAlbum(
                            path: path,
                            targetCollection: phAsset!,
                            successHandler: { (fullPath: String) in
                                successHandler(fullPath)
                            },
                            failHandler: { (errorMsg: String) in
                                failHandler(errorMsg)
                            }
                        )
                    }
                }
            )
        }
    }
}



extension URL {
    func mimeType() -> String {
        let pathExtension = self.pathExtension
        if let uti = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, pathExtension as NSString, nil)?.takeRetainedValue() {
            if let mimetype = UTTypeCopyPreferredTagWithClass(uti, kUTTagClassMIMEType)?.takeRetainedValue() {
                return mimetype as String
            }
        }
        return "application/octet-stream"
    }
    var containsImage: Bool {
        let mimeType = self.mimeType()
        guard let uti = UTTypeCreatePreferredIdentifierForTag(kUTTagClassMIMEType, mimeType as CFString, nil)?.takeRetainedValue() else {
            return false
        }
        return UTTypeConformsTo(uti, kUTTypeImage)
    }
    var containsAudio: Bool {
        let mimeType = self.mimeType()
        guard let uti = UTTypeCreatePreferredIdentifierForTag(kUTTagClassMIMEType, mimeType as CFString, nil)?.takeRetainedValue() else {
            return false
        }
        return UTTypeConformsTo(uti, kUTTypeAudio)
    }
    var containsVideo: Bool {
        let mimeType = self.mimeType()
        guard  let uti = UTTypeCreatePreferredIdentifierForTag(kUTTagClassMIMEType, mimeType as CFString, nil)?.takeRetainedValue() else {
            return false
        }
        return UTTypeConformsTo(uti, kUTTypeMovie) || UTTypeConformsTo(uti, kUTTypeVideo)
    }

}
