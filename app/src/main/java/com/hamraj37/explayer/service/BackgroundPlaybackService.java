package com.hamraj37.explayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;
import com.hamraj37.explayer.PlayerActivity;
import com.hamraj37.explayer.R;

@OptIn(markerClass = UnstableApi.class)
public class BackgroundPlaybackService extends MediaSessionService {

    public static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "background_playback_channel";

    public static final String ACTION_PLAY = "com.hamraj37.explayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.hamraj37.explayer.ACTION_PAUSE";
    public static final String ACTION_PREV = "com.hamraj37.explayer.ACTION_PREV";
    public static final String ACTION_NEXT = "com.hamraj37.explayer.ACTION_NEXT";

    private static MediaSession mediaSession = null;

    public static void setMediaSession(MediaSession session) {
        mediaSession = session;
    }

    public static MediaSession getMediaSession() {
        return mediaSession;
    }

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        createNotificationChannel();

        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (mediaSession != null && mediaSession.getPlayer() != null) {
                Player player = mediaSession.getPlayer();
                if (ACTION_PLAY.equals(action)) {
                    player.play();
                } else if (ACTION_PAUSE.equals(action)) {
                    player.pause();
                } else if (ACTION_PREV.equals(action)) {
                    if (player.hasPreviousMediaItem()) {
                        player.seekToPreviousMediaItem();
                    } else {
                        player.seekTo(0);
                    }
                } else if (ACTION_NEXT.equals(action)) {
                    if (player.hasNextMediaItem()) {
                        player.seekToNextMediaItem();
                    }
                }
            }
        }

        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent prevIntent = new Intent(this, BackgroundPlaybackService.class).setAction(ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        boolean isPlaying = mediaSession != null && mediaSession.getPlayer() != null && mediaSession.getPlayer().isPlaying();
        Intent playPauseIntent = new Intent(this, BackgroundPlaybackService.class).setAction(isPlaying ? ACTION_PAUSE : ACTION_PLAY);
        PendingIntent playPausePendingIntent = PendingIntent.getService(this, 2, playPauseIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, BackgroundPlaybackService.class).setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String title = getString(R.string.app_name);
        String text = getString(R.string.background_play_enabled);
        if (mediaSession != null && mediaSession.getPlayer() != null) {
            MediaItem mediaItem = mediaSession.getPlayer().getCurrentMediaItem();
            if (mediaItem != null && mediaItem.mediaMetadata != null && mediaItem.mediaMetadata.title != null) {
                title = mediaItem.mediaMetadata.title.toString();
            }
        }

        Notification.Action prevAction = new Notification.Action.Builder(
                R.drawable.ic_skip_previous, "Previous", prevPendingIntent).build();

        Notification.Action playPauseAction = new Notification.Action.Builder(
                isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                isPlaying ? "Pause" : "Play",
                playPausePendingIntent).build();

        Notification.Action nextAction = new Notification.Action.Builder(
                R.drawable.ic_skip_next, "Next", nextPendingIntent).build();

        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_play)
                .setContentIntent(pendingIntent)
                .setOngoing(isPlaying)
                .addAction(prevAction)
                .addAction(playPauseAction)
                .addAction(nextAction);

        if (mediaSession != null && mediaSession.getPlatformToken() != null) {
            builder.setStyle(new Notification.MediaStyle()
                    .setMediaSession((android.media.session.MediaSession.Token) mediaSession.getPlatformToken())
                    .setShowActionsInCompactView(0, 1, 2));
        }

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);

        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.background_play_enabled),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        serviceChannel.setSound(null, null);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE);
        if (mediaSession != null) {
            mediaSession = null;
        }
        super.onDestroy();
    }
}
