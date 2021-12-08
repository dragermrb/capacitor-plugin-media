package com.whiteguru.capacitor.plugin.media;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@CapacitorPlugin(
    name = "Media",
    permissions = {
        @Permission(
            strings = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
            alias = "publicStorage"
        )
    }
)
public class MediaPlugin extends Plugin {

    private Media implementation = new Media();

    // @todo
    @PluginMethod
    public void getMedias(PluginCall call) {
        call.unimplemented();
    }

    @PluginMethod
    public void getAlbums(PluginCall call) {
        Log.d("DEBUG LOG", "GET ALBUMS");
        if (isStoragePermissionGranted()) {
            Log.d("DEBUG LOG", "HAS PERMISSION");
            _getAlbums(call);
        } else {
            Log.d("DEBUG LOG", "NOT ALLOWED");
            this.bridge.saveCall(call);
            requestAllPermissions(call, "permissionCallback");
        }
    }

    @PluginMethod
    public void savePhoto(PluginCall call) {
        Log.d("DEBUG LOG", "SAVE PHOTO TO ALBUM");
        if (isStoragePermissionGranted()) {
            Log.d("DEBUG LOG", "HAS PERMISSION");
            _saveMedia(call, "PICTURES");
        } else {
            Log.d("DEBUG LOG", "NOT ALLOWED");
            this.bridge.saveCall(call);
            requestAllPermissions(call, "permissionCallback");
            Log.d("DEBUG LOG", "___SAVE PHOTO TO ALBUM AFTER PERMISSION REQUEST");
        }
    }

    @PluginMethod
    public void saveVideo(PluginCall call) {
        Log.d("DEBUG LOG", "SAVE VIDEO TO ALBUM");
        if (isStoragePermissionGranted()) {
            Log.d("DEBUG LOG", "HAS PERMISSION");
            _saveMedia(call, "MOVIES");
        } else {
            Log.d("DEBUG LOG", "NOT ALLOWED");
            this.bridge.saveCall(call);
            requestAllPermissions(call, "permissionCallback");
        }
    }

    @PluginMethod
    public void saveGif(PluginCall call) {
        Log.d("DEBUG LOG", "SAVE GIF TO ALBUM");
        if (isStoragePermissionGranted()) {
            Log.d("DEBUG LOG", "HAS PERMISSION");
            _saveMedia(call, "PICTURES");
        } else {
            Log.d("DEBUG LOG", "NOT ALLOWED");
            this.bridge.saveCall(call);
            requestAllPermissions(call, "permissionCallback");
        }
    }

    @PluginMethod
    public void saveDocument(PluginCall call) {
        Log.d("DEBUG LOG", "SAVE DOCUMENT TO ALBUM");
        if (isStoragePermissionGranted()) {
            Log.d("DEBUG LOG", "HAS PERMISSION");
            _saveMedia(call, "DOCUMENTS");
        } else {
            Log.d("DEBUG LOG", "NOT ALLOWED");
            this.bridge.saveCall(call);
            requestAllPermissions(call, "permissionCallback");
        }
    }

    @PluginMethod
    public void createAlbum(PluginCall call) {
        Log.d("DEBUG LOG", "CREATE ALBUM");
        if (isStoragePermissionGranted()) {
            Log.d("DEBUG LOG", "HAS PERMISSION");
            _createAlbum(call);
        } else {
            Log.d("DEBUG LOG", "NOT ALLOWED");
            this.bridge.saveCall(call);
            requestAllPermissions(call, "permissionCallback");
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
                _saveMedia(call, "PICTURES");
                break;
            case "saveVideo":
                _saveMedia(call, "MOVIES");
                break;
            case "saveGif":
                _saveMedia(call, "PICTURES");
                break;
            case "saveDocument":
                _saveMedia(call, "DOCUMENTS");
                break;
            case "createAlbum":
                _createAlbum(call);
                break;
        }
    }

    private boolean isStoragePermissionGranted() {
        return getPermissionState("publicStorage") == PermissionState.GRANTED;
    }

    private void _getAlbums(PluginCall call) {
        Log.d("DEBUG LOG", "___GET ALBUMS");

        JSObject response = new JSObject();
        JSArray albums = new JSArray();
        Set<String> bucketIds = new HashSet<String>();

        String[] projection = new String[] { MediaStore.MediaColumns.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.BUCKET_ID };
        Cursor cur = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

        while (cur.moveToNext()) {
            String albumName = cur.getString((cur.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)));
            String bucketId = cur.getString((cur.getColumnIndex(MediaStore.MediaColumns.BUCKET_ID)));

            if (!bucketIds.contains(bucketId)) {
                JSObject album = new JSObject();

                album.put("identifier", bucketId);
                album.put("name", albumName);
                albums.put(album);

                bucketIds.add(bucketId);
            }
        }

        response.put("albums", albums);
        Log.d("DEBUG LOG", String.valueOf(response));
        Log.d("DEBUG LOG", "___GET ALBUMS FINISHED");

        call.resolve(response);
    }

    private void _saveMedia(PluginCall call, String destination) {
        String dest;
        if (destination == "MOVIES") {
            dest = Environment.DIRECTORY_MOVIES;
        } else {
            dest = Environment.DIRECTORY_PICTURES;
        }

        Log.d("DEBUG LOG", "___SAVE MEDIA TO ALBUM");
        String inputPath = call.getString("path");
        if (inputPath == null) {
            call.reject("Input file path is required");
            return;
        }

        JSObject album = call.getObject("album", new JSObject());
        String albumName = album.getString("name", null);

        if (albumName == null) {
            call.reject("Album name is required");
            return;
        }

        Uri inputUri = Uri.parse(inputPath);
        File inputFile = new File(inputUri.getPath());

        File albumDir = null;
        String albumPath;
        Log.d("SDK BUILD VERSION", String.valueOf(Build.VERSION.SDK_INT));

        if (Build.VERSION.SDK_INT >= 29) {
            albumPath = getContext().getExternalMediaDirs()[0].getAbsolutePath();
        } else {
            albumPath = Environment.getExternalStoragePublicDirectory(dest).getAbsolutePath();
        }

        albumDir = new File(albumPath, albumName);

        Log.d("ENV LOG - ALBUM DIR", String.valueOf(albumDir));

        try {
            File expFile = copyFile(inputFile, albumDir);
            scanPhoto(expFile);

            JSObject result = new JSObject();
            result.put("filePath", expFile.toString());
            call.resolve(result);
        } catch (RuntimeException e) {
            call.reject("RuntimeException occurred", e);
        }
    }

    private void _createAlbum(PluginCall call) {
        Log.d("DEBUG LOG", "___CREATE ALBUM");
        String folderName = call.getString("name");
        String folder;

        if (Build.VERSION.SDK_INT >= 29) {
            folder = getContext().getExternalMediaDirs()[0].getAbsolutePath() + "/" + folderName;
        } else {
            folder = Environment.getExternalStoragePublicDirectory(folderName).toString();
        }

        Log.d("ENV STORAGE", folder);

        File f = new File(folder);

        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.d("DEBUG LOG", "___ERROR ALBUM");
                call.reject("Cant create album");
            } else {
                Log.d("DEBUG LOG", "___SUCCESS ALBUM CREATED");

                JSObject album = new JSObject();

                album.put("name", folderName);

                call.resolve(album);
            }
        } else {
            Log.d("DEBUG LOG", "___ERROR ALBUM ALREADY EXISTS");
            call.reject("Album already exists");
        }
    }

    private File copyFile(File inputFile, File albumDir) {
        // if destination folder does not exist, create it
        if (!albumDir.exists()) {
            if (!albumDir.mkdir()) {
                throw new RuntimeException("Destination folder does not exist and cannot be created.");
            }
        }

        String absolutePath = inputFile.getAbsolutePath();
        String baseName = inputFile.getName().replaceFirst("[.][^.]+$", "");
        String extension = absolutePath.substring(absolutePath.lastIndexOf(".") + 1).toLowerCase();

        // generate image file name using current date and time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        File newFile = new File(albumDir, toSlug(baseName) + "_" + timeStamp + "." + extension);

        // Read and write image files
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(inputFile).getChannel();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Source file not found: " + inputFile + ", error: " + e.getMessage());
        }
        try {
            outChannel = new FileOutputStream(newFile).getChannel();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Copy file not found: " + newFile + ", error: " + e.getMessage());
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw new RuntimeException("Error transfering file, error: " + e.getMessage());
        } finally {
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    Log.d("SaveImage", "Error closing input file channel: " + e.getMessage());
                    // does not harm, do nothing
                }
            }
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    Log.d("SaveImage", "Error closing output file channel: " + e.getMessage());
                    // does not harm, do nothing
                }
            }
        }

        return newFile;
    }

    private void scanPhoto(File imageFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        bridge.getActivity().sendBroadcast(mediaScanIntent);
    }

    private String toSlug(String input) {
        Pattern NONLATIN = Pattern.compile("[^\\w_-]");
        Pattern SEPARATORS = Pattern.compile("[\\s\\p{Punct}&&[^-]]");

        String noseparators = SEPARATORS.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noseparators, Normalizer.Form.NFD);
        String slug = NONLATIN
            .matcher(normalized)
            .replaceAll("")
            .toLowerCase(Locale.ENGLISH)
            .replaceAll("-{2,}", "-")
            .replaceAll("^-|-$", "");

        return slug;
    }
}
