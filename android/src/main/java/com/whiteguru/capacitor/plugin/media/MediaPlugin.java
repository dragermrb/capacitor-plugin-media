package com.whiteguru.capacitor.plugin.media;

import android.Manifest;
import android.os.Build;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

@CapacitorPlugin(
    name = "Media",
    permissions = {
        /*
        SDK VERSIONS 30-32
        This alias is a placeholder and the PHOTOS alias will be updated to use this permission
        so that the end user does not need to explicitly use separate aliases depending
        on the SDK version.
         */
        @Permission(
            strings = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
            alias = MediaPlugin.PUBLIC_STORAGE
        ),
        /*
        SDK VERSIONS 33 AND ABOVE
        This alias is a placeholder and the PHOTOS alias will be updated to use these permissions
        so that the end user does not need to explicitly use separate aliases depending
        on the SDK version.
         */
        @Permission(
            strings = { Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO },
            alias = MediaPlugin.MEDIA_STORAGE
        )
    }
)
public class MediaPlugin extends Plugin {

    // Permission alias constants
    static final String PUBLIC_STORAGE = "publicStorage";
    static final String MEDIA_STORAGE = "mediaStorage";

    private static final String STORAGE_PERMISSION = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ? MEDIA_STORAGE
        : PUBLIC_STORAGE;

    private final Media implementation = new Media();

    @PluginMethod
    public void createAlbum(PluginCall call) {
        call.unavailable("Not available on Android");
    }

    @PluginMethod
    public void getAlbums(PluginCall call) {
        if (isStoragePermissionGranted()) {
            _getAlbums(call);
        } else {
            requestPermissionForAlias(STORAGE_PERMISSION, call, "permissionCallback");
        }
    }

    // @todo
    @PluginMethod
    public void getMedias(PluginCall call) {
        call.unimplemented();
    }

    @PluginMethod
    public void savePhoto(PluginCall call) {
        if (isStoragePermissionGranted()) {
            _saveMedia(call, "PICTURES");
        } else {
            requestPermissionForAlias(STORAGE_PERMISSION, call, "permissionCallback");
        }
    }

    @PluginMethod
    public void saveVideo(PluginCall call) {
        if (isStoragePermissionGranted()) {
            _saveMedia(call, "MOVIES");
        } else {
            requestPermissionForAlias(STORAGE_PERMISSION, call, "permissionCallback");
        }
    }

    @PluginMethod
    public void saveGif(PluginCall call) {
        if (isStoragePermissionGranted()) {
            _saveMedia(call, "PICTURES");
        } else {
            requestPermissionForAlias(STORAGE_PERMISSION, call, "permissionCallback");
        }
    }

    @PluginMethod
    public void saveDocument(PluginCall call) {
        if (isStoragePermissionGranted()) {
            _saveMedia(call, "DOCUMENTS");
        } else {
            requestPermissionForAlias(STORAGE_PERMISSION, call, "permissionCallback");
        }
    }

    @PluginMethod
    public void saveAudio(PluginCall call) {
        if (isStoragePermissionGranted()) {
            _saveMedia(call, "MUSIC");
        } else {
            requestPermissionForAlias(STORAGE_PERMISSION, call, "permissionCallback");
        }
    }

    @PermissionCallback
    private void permissionCallback(PluginCall call) {
        if (!isStoragePermissionGranted()) {
            Logger.debug(getLogTag(), "User denied storage permission");
            call.reject("Unable to do file operation, user denied permission request");
            return;
        }

        switch (call.getMethodName()) {
            case "getMedias":
                call.unimplemented();
                break;
            case "getAlbums":
                _getAlbums(call);
                break;
            case "savePhoto":
            case "saveGif":
                _saveMedia(call, "PICTURES");
                break;
            case "saveVideo":
                _saveMedia(call, "MOVIES");
                break;
            case "saveDocument":
                _saveMedia(call, "DOCUMENTS");
                break;
            case "saveAudio":
                _saveMedia(call, "MUSIC");
                break;
            case "createAlbum":
                _createAlbum(call);
                break;
        }
    }

    private boolean isStoragePermissionGranted() {
        return getPermissionState(STORAGE_PERMISSION) == PermissionState.GRANTED;
    }

    private void _getAlbums(PluginCall call) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            call.unavailable("getAlbums() requires API 29+");
            return;
        }

        JSArray albums = this.implementation.getAlbums(this.getContext());

        JSObject response = new JSObject();
        response.put("albums", albums);

        call.resolve(response);
    }

    private void _saveMedia(PluginCall call, String destination) {
        String inputPath = call.getString("path");
        if (inputPath == null) {
            call.reject("Input file path is required");
            return;
        }

        JSObject album = call.getObject("album", null);
        String albumName = album != null ? album.getString("name", null) : null;

        if (albumName == null) {
            call.reject("Album name is required");
            return;
        }

        try {
            JSObject saveMediaResult = this.implementation.saveMedia(this.getContext(), inputPath, albumName, destination);

            call.resolve(saveMediaResult);
        } catch (Exception e) {
            call.reject("RuntimeException occurred", e);
        }
    }

    private void _createAlbum(PluginCall call) {
        String albumName = call.getString("name");
        if (albumName == null) {
            call.reject("Album name is required");
            return;
        }

        try {
            String albumNameCreated = this.implementation.createAlbum(albumName);

            JSObject album = new JSObject();

            album.put("name", albumNameCreated);

            call.resolve(album);
        } catch (Exception e) {
            call.reject("RuntimeException occurred", e);
        }
    }
}
