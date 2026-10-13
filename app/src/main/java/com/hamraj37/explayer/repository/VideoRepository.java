package com.hamraj37.explayer.repository;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.hamraj37.explayer.model.VideoItem;

import java.util.ArrayList;
import java.util.List;

public class VideoRepository {

    private static final String TAG = "VideoRepository";

    public List<VideoItem> fetchVideos(Context context) {
        List<VideoItem> videoList = new ArrayList<>();
        if (context == null) {
            return videoList;
        }

        ContentResolver contentResolver = context.getContentResolver();
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_ADDED
        };

        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = contentResolver.query(
                videoUri,
                projection,
                null,
                null,
                sortOrder
        )) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int titleColumn = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
                int nameColumn = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
                int durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
                int sizeColumn = cursor.getColumnIndex(MediaStore.Video.Media.SIZE);
                int dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                int dateColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);

                    String rawTitle = titleColumn != -1 ? cursor.getString(titleColumn) : null;
                    String title;
                    if (rawTitle != null && !rawTitle.trim().isEmpty()) {
                        title = rawTitle;
                    } else {
                        title = nameColumn != -1 ? cursor.getString(nameColumn) : "Video_" + id;
                    }

                    long duration = durationColumn != -1 ? cursor.getLong(durationColumn) : 0L;
                    long size = sizeColumn != -1 ? cursor.getLong(sizeColumn) : 0L;
                    String path = dataColumn != -1 ? cursor.getString(dataColumn) : "";
                    long dateAdded = dateColumn != -1 ? cursor.getLong(dateColumn) : 0L;

                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

                    VideoItem item = new VideoItem(
                            id,
                            contentUri,
                            title,
                            duration,
                            size,
                            path,
                            dateAdded
                    );
                    videoList.add(item);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching videos from MediaStore", e);
        }

        return videoList;
    }
}