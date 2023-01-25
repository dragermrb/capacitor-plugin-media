package com.whiteguru.capacitor.plugin.media;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;

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

public class Media {

    private static final String LOG_TAG = "Capacitor/MediaPlugin";

    public JSArray getAlbums(Context context) throws RuntimeException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            throw new RuntimeException("getAlbums() requires API 29+");
        }

        JSArray albums = new JSArray();
        Set<String> bucketIds = new HashSet<>();

        String[] projection = new String[] { MediaStore.MediaColumns.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.BUCKET_ID };
        Cursor cur = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

        while (cur.moveToNext()) {
            String albumName = cur.getString((cur.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)));
            String bucketId = cur.getString((cur.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_ID)));

            if (!bucketIds.contains(bucketId)) {
                JSObject album = new JSObject();

                album.put("identifier", bucketId);
                album.put("name", albumName);
                albums.put(album);

                bucketIds.add(bucketId);
            }
        }

        cur.close();

        return albums;
    }

    public JSObject saveMedia(@NonNull Context context, String inputPath, String albumName, String destination) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && destination.equals("DOCUMENTS")) {
            return saveDocumentBelowQ(context, inputPath, albumName);
        }

        String displayName = Uri.parse(inputPath).getLastPathSegment();
        Long size = (new File(inputPath)).length();
        String mimeType = this.getMimeType(inputPath);

        ContentResolver resolver = context.getContentResolver();

        Uri mediaCollection = this.getMediaCollectionUri(destination);
        String relativePath = this.getRelativePath(destination);

        ContentValues newMediaDetails = new ContentValues();

        newMediaDetails.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        newMediaDetails.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        newMediaDetails.put(MediaStore.MediaColumns.SIZE, size);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            newMediaDetails.put(MediaStore.MediaColumns.IS_PENDING, 1);
            newMediaDetails.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath + albumName);
        }

        Uri mediaContentUri = resolver.insert(mediaCollection, newMediaDetails);

        try {
            this.copyContent(resolver, inputPath, mediaContentUri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Release the "pending" status
                newMediaDetails.clear();
                newMediaDetails.put(MediaStore.MediaColumns.IS_PENDING, 0);
                resolver.update(mediaContentUri, newMediaDetails, null, null);
            }

            JSObject result = new JSObject();
            result.put("path", mediaContentUri.toString());
            result.put("name", this.getFileName(context, mediaContentUri));

            return result;
        } catch (Exception e) {
            resolver.delete(mediaContentUri, null, null);

            throw e;
        }
    }

    private JSObject saveDocumentBelowQ(Context context, String inputPath, String albumName) {
        JSObject res;
        Uri inputUri = Uri.parse(inputPath);
        File inputFile = new File(inputUri.getPath());

        String albumPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File albumDir = new File(albumPath, albumName);

        try {
            File expFile = copyFile(inputFile, albumDir);

            JSObject result = new JSObject();
            result.put("path", expFile.toString());
            result.put("name", this.getFileName(context, inputUri));

            res = result;
        } catch (Exception e) {
            throw e;
        }
        return res;
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.ENGLISH).format(new Date());
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
                    Logger.debug(LOG_TAG, "Error closing input file channel: " + e.getMessage());
                    // does not harm, do nothing
                }
            }
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    Logger.debug(LOG_TAG, "Error closing output file channel: " + e.getMessage());
                    // does not harm, do nothing
                }
            }
        }

        return newFile;
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

    private String getFileName(Context context, Uri uri) throws RuntimeException {
        String fileName = null;

        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                }
            }
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            fileName = uri.getLastPathSegment();
        }

        if (fileName == null) {
            throw new RuntimeException("Cannot get filename from Uri");
        }

        return fileName;
    }

    public String createAlbum(String albumName) throws RuntimeException {
        throw new RuntimeException("Not available on Android");
    }

    private void copyContent(ContentResolver resolver, String inputPath, Uri outputUri) throws RuntimeException {
        FileChannel inChannel;
        FileChannel outChannel;

        try {
            Uri inputUri = Uri.parse(inputPath);
            inChannel = new FileInputStream(inputUri.getPath()).getChannel();
        } catch (Exception e) {
            throw new RuntimeException("Source file not found: " + inputPath + ", error: " + e.getMessage());
        }

        try {
            ParcelFileDescriptor targetFd = resolver.openFileDescriptor(outputUri, "w", null);
            outChannel = (new FileOutputStream(targetFd.getFileDescriptor())).getChannel();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Copy file not found: " + outputUri.toString() + ", error: " + e.getMessage());
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
                    Logger.debug(LOG_TAG, "Error closing input file channel: " + e.getMessage());
                    // does not harm, do nothing
                }
            }

            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    Logger.debug(LOG_TAG, "Error closing output file channel: " + e.getMessage());
                    // does not harm, do nothing
                }
            }
        }
    }

    private Uri getMediaCollectionUri(@NonNull String destination) {
        switch (destination) {
            case "PICTURES":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                } else {
                    return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
            case "MOVIES":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                } else {
                    return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
            case "DOCUMENTS":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                } else {
                    return MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                }
            case "MUSIC":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                } else {
                    return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
    }

    private String getRelativePath(@NonNull String destination) {
        switch (destination) {
            case "PICTURES":
                return "Pictures/";
            case "MOVIES":
                return "Movies/";
            case "DOCUMENTS":
                return "Download/";
            case "MUSIC":
                return "Music/";
        }

        return "Download/";
    }

    private String getMimeType(String filename) {
        String type = null;
        final String url = filename.toLowerCase(Locale.ROOT);
        final String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }

        return type != null ? type : "*/*";
    }
}
