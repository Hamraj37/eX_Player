package com.hamraj37.explayer.model;

import android.net.Uri;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VideoItem implements Serializable {
    private final long id;
    private final String uriString;
    private final String title;
    private final long duration;
    private final long size;
    private final String path;
    private final long dateAdded;

    public VideoItem(long id, Uri uri, String title, long duration, long size, String path, long dateAdded) {
        this.id = id;
        this.uriString = uri != null ? uri.toString() : "";
        this.title = title != null ? title : "Unknown Video";
        this.duration = duration;
        this.size = size;
        this.path = path != null ? path : "";
        this.dateAdded = dateAdded;
    }

    public long getId() {
        return id;
    }

    public Uri getUri() {
        return uriString != null && !uriString.isEmpty() ? Uri.parse(uriString) : Uri.EMPTY;
    }

    public String getUriString() {
        return uriString;
    }

    public String getTitle() {
        return title;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public String getFormattedDuration() {
        if (duration <= 0) {
            return "00:00";
        }
        long totalSeconds = duration / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    public String getFormattedSize() {
        if (size <= 0) {
            return "0 B";
        }
        if (size < 1024) {
            return size + " B";
        }
        double kb = size / 1024.0;
        if (kb < 1024) {
            return String.format(Locale.getDefault(), "%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format(Locale.getDefault(), "%.1f MB", mb);
        }
        double gb = mb / 1024.0;
        return String.format(Locale.getDefault(), "%.2f GB", gb);
    }

    public String getFormattedDate() {
        if (dateAdded <= 0) {
            return "";
        }
        try {
            Date date = new Date(dateAdded * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return "";
        }
    }
}