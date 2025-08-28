package com.whiteguru.capacitor.plugin.media;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(
    name = "Media",
    permissions = {}
)
public class MediaPlugin extends Plugin {
    private final Media implementation = new Media();

    @PluginMethod
    public void savePhoto(PluginCall call) {
        _saveMedia(call, "PICTURES");
    }

    @PluginMethod
    public void saveVideo(PluginCall call) {
        _saveMedia(call, "MOVIES");
    }

    @PluginMethod
    public void saveGif(PluginCall call) {
        _saveMedia(call, "PICTURES");
    }

    @PluginMethod
    public void saveDocument(PluginCall call) {
        _saveMedia(call, "DOCUMENTS");
    }

    @PluginMethod
    public void saveAudio(PluginCall call) {
        _saveMedia(call, "MUSIC");
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
}
