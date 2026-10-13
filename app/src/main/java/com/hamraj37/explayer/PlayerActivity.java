package com.hamraj37.explayer;

import androidx.activity.EdgeToEdge;
import androidx.core.view.ViewCompat;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Rational;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.google.android.material.button.MaterialButton;
import com.hamraj37.explayer.service.BackgroundPlaybackService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@OptIn(markerClass = UnstableApi.class)
public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_URI = "extra_video_uri";
    public static final String EXTRA_VIDEO_TITLE = "extra_video_title";
    public static final String EXTRA_VIDEO_INDEX = "extra_video_index";
    public static final String EXTRA_VIDEO_PLAYLIST = "extra_video_playlist";
    public static final String EXTRA_VIDEO_TITLE_PLAYLIST = "extra_video_title_playlist";
    public static final String EXTRA_START_POSITION = "extra_start_position";

    public static final String ACTION_PIP_PLAY = "com.hamraj37.explayer.PIP_PLAY";
    public static final String ACTION_PIP_PAUSE = "com.hamraj37.explayer.PIP_PAUSE";
    public static final String ACTION_PIP_REPLAY_10 = "com.hamraj37.explayer.PIP_REPLAY_10";
    public static final String ACTION_PIP_FORWARD_10 = "com.hamraj37.explayer.PIP_FORWARD_10";

    private ExoPlayer player;
    private MediaSession mediaSession;
    private PlayerView playerView;
    private View gestureOverlay;
    private View controlsOverlay;

    // Controls Views
    private View btnBack;
    private TextView tvPlayerTitle;
    private View btnSubtitle;
    private View btnAudioTrack;
    private View btnAspectRatio;
    private View btnImmersive;
    private ImageView ivImmersive;
    private View btnSpeed;
    private boolean isImmersiveDisplayEnabled = false;

    private View btnPrev;
    private View btnRewind10;
    private View btnPlayPause;
    private ImageView ivPlayPause;
    private View btnForward10;
    private View btnNext;

    private TextView tvCurrentTime;
    private SeekBar seekBar;
    private TextView tvTotalTime;
    private TextView tvDuration;
    private View btnOrientationChange;
    private View btnBackgroundPlay;
    private ImageView ivBackgroundPlay;
    private View btnPip;

    // Gesture Overlay & Feedback Views
    private LinearLayout layoutDoubleTapLeft;
    private TextView tvDoubleTapLeft;
    private LinearLayout layoutDoubleTapRight;
    private TextView tvDoubleTapRight;

    private LinearLayout indicatorPopupLayout;
    private ImageView ivIndicatorIcon;
    private ProgressBar pbIndicatorProgress;
    private TextView tvIndicatorValue;
    private TextView tvIndicatorText;

    // Audio & Window Managers
    private AudioManager audioManager;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private float videoScaleFactor = 1.0f;
    private boolean isPinching = false;

    // Auto-Hide & Progress Update Handlers
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Handler hideIndicatorHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoHideRunnable = this::hideControls;

    private boolean controlsVisible = true;
    private boolean isUserSeeking = false;
    private boolean isBackgroundPlayEnabled = true;

    // Gesture control fields
    private boolean isScrollStarted = false;
    private boolean isVerticalScroll = false;
    private boolean isHorizontalScroll = false;
    private boolean isLeftHalf = false;
    private float initialBrightness = 0.5f;
    private int initialVolume = 0;
    private long initialSeekPosition = 0;
    private long targetSeekPosition = 0;
    private int touchSlop;

    // Aspect Ratio Modes
    private final int[] RESIZE_MODES = new int[]{
            AspectRatioFrameLayout.RESIZE_MODE_FIT,
            AspectRatioFrameLayout.RESIZE_MODE_FILL,
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
            AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH,
            AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
    };
    private final int[] RESIZE_MODE_STRINGS = new int[]{
            R.string.aspect_ratio_fit,
            R.string.aspect_ratio_fill,
            R.string.aspect_ratio_zoom,
            R.string.aspect_ratio_fixed_width,
            R.string.aspect_ratio_fixed_height
    };
    private int currentResizeModeIndex = 0;

    // Speed Options
    private final float[] SPEED_VALUES = new float[]{0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f};
    private final String[] SPEED_LABELS = new String[]{"0.25x", "0.5x", "0.75x", "1.0x (Normal)", "1.25x", "1.5x", "2.0x"};

    private final BroadcastReceiver pipReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            String action = intent.getAction();
            if (ACTION_PIP_PLAY.equals(action)) {
                if (player != null) player.play();
            } else if (ACTION_PIP_PAUSE.equals(action)) {
                if (player != null) player.pause();
            } else if (ACTION_PIP_REPLAY_10.equals(action)) {
                seekRelative(-10000);
            } else if (ACTION_PIP_FORWARD_10.equals(action)) {
                seekRelative(10000);
            }
            updatePipParams();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        View rootView = findViewById(R.id.playerActivityRoot);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        SharedPreferences prefs = getSharedPreferences("EXPlayerPrefs", MODE_PRIVATE);
        isImmersiveDisplayEnabled = prefs.getBoolean("immersive_display_enabled", false);

        applyImmersiveDisplayMode();
        initViews();
        setupGestureDetector();
        initExoPlayer();
        loadIntentData();
        setupControlListeners();
        registerPipReceiver();
        showControls();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.hasExtra(EXTRA_VIDEO_URI)) {
            String newUri = intent.getStringExtra(EXTRA_VIDEO_URI);
            MediaItem currentItem = player != null ? player.getCurrentMediaItem() : null;
            String currentUri = (currentItem != null && currentItem.localConfiguration != null && currentItem.localConfiguration.uri != null)
                    ? currentItem.localConfiguration.uri.toString() : null;

            if (currentUri == null || !currentUri.equals(newUri)) {
                loadIntentData();
            }
        }
    }

    private void registerPipReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PIP_PLAY);
        filter.addAction(ACTION_PIP_PAUSE);
        filter.addAction(ACTION_PIP_REPLAY_10);
        filter.addAction(ACTION_PIP_FORWARD_10);
        ContextCompat.registerReceiver(this, pipReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void initViews() {
        playerView = findViewById(R.id.playerView);
        gestureOverlay = findViewById(R.id.gestureOverlay);
        controlsOverlay = findViewById(R.id.controlsOverlay);

        // Top Bar
        btnBack = controlsOverlay.findViewById(R.id.btnBack);
        tvPlayerTitle = controlsOverlay.findViewById(R.id.tvPlayerTitle);
        btnSubtitle = controlsOverlay.findViewById(R.id.btnSubtitle);
        btnAudioTrack = controlsOverlay.findViewById(R.id.btnAudioTrack);
        btnAspectRatio = controlsOverlay.findViewById(R.id.btnAspectRatio);
        btnImmersive = controlsOverlay.findViewById(R.id.btnImmersive);
        if (btnImmersive != null) {
            ivImmersive = btnImmersive.findViewById(R.id.ivImmersive);
        }
        btnSpeed = controlsOverlay.findViewById(R.id.btnSpeed);
        updateImmersiveDisplayUI();

        // Center Actions
        btnPrev = controlsOverlay.findViewById(R.id.btnPrev);
        btnRewind10 = controlsOverlay.findViewById(R.id.btnRewind10);
        btnPlayPause = controlsOverlay.findViewById(R.id.btnPlayPause);
        ivPlayPause = controlsOverlay.findViewById(R.id.ivPlayPause);
        btnForward10 = controlsOverlay.findViewById(R.id.btnForward10);
        btnNext = controlsOverlay.findViewById(R.id.btnNext);

        // Bottom Bar
        tvCurrentTime = controlsOverlay.findViewById(R.id.tvCurrentTime);
        seekBar = controlsOverlay.findViewById(R.id.seekBar);
        tvTotalTime = controlsOverlay.findViewById(R.id.tvTotalTime);
        tvDuration = controlsOverlay.findViewById(R.id.tvDuration);
        btnOrientationChange = controlsOverlay.findViewById(R.id.btnOrientationChange);
        btnBackgroundPlay = controlsOverlay.findViewById(R.id.btnBackgroundPlay);
        ivBackgroundPlay = controlsOverlay.findViewById(R.id.ivBackgroundPlay);
        btnPip = controlsOverlay.findViewById(R.id.btnPip);

        // Double-Tap Feedback
        layoutDoubleTapLeft = findViewById(R.id.layoutDoubleTapLeft);
        tvDoubleTapLeft = findViewById(R.id.tvDoubleTapLeft);
        layoutDoubleTapRight = findViewById(R.id.layoutDoubleTapRight);
        tvDoubleTapRight = findViewById(R.id.tvDoubleTapRight);

        // Indicator Popup
        indicatorPopupLayout = findViewById(R.id.indicatorPopupLayout);
        ivIndicatorIcon = findViewById(R.id.ivIndicatorIcon);
        pbIndicatorProgress = findViewById(R.id.pbIndicatorProgress);
        tvIndicatorValue = findViewById(R.id.tvIndicatorValue);
        tvIndicatorText = findViewById(R.id.tvIndicatorText);

        tvPlayerTitle.setSelected(true);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        updateBackgroundPlayUI();
    }

    private void updateBackgroundPlayUI() {
        if (ivBackgroundPlay != null) {
            if (isBackgroundPlayEnabled) {
                ivBackgroundPlay.setColorFilter(ContextCompat.getColor(this, R.color.liquid_cyan));
            } else {
                ivBackgroundPlay.setColorFilter(ContextCompat.getColor(this, R.color.white));
            }
        }
    }

    private void initExoPlayer() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();

        player = new ExoPlayer.Builder(this).build();
        player.setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true);
        playerView.setPlayer(player);

        Intent launchIntent = new Intent(this, PlayerActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent sessionActivityPendingIntent = PendingIntent.getActivity(
                this,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        mediaSession = new MediaSession.Builder(this, player)
                .setSessionActivity(sessionActivityPendingIntent)
                .setCallback(new MediaSession.Callback() {})
                .build();

        BackgroundPlaybackService.setMediaSession(mediaSession);

        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    if (ivPlayPause != null) ivPlayPause.setImageResource(R.drawable.ic_pause);
                    mainHandler.post(updateProgressRunnable);
                } else {
                    if (ivPlayPause != null) ivPlayPause.setImageResource(R.drawable.ic_play);
                    mainHandler.removeCallbacks(updateProgressRunnable);
                }
                updatePipParams();
                resetAutoHideTimer();

                if (isBackgroundPlayEnabled) {
                    try {
                        Intent serviceIntent = new Intent(PlayerActivity.this, BackgroundPlaybackService.class);
                        ContextCompat.startForegroundService(PlayerActivity.this, serviceIntent);
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onVideoSizeChanged(@NonNull VideoSize videoSize) {
                updatePipParams();
            }

            @Override
            public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                if (mediaItem != null) {
                    String uriString = (mediaItem.localConfiguration != null && mediaItem.localConfiguration.uri != null)
                            ? mediaItem.localConfiguration.uri.toString() : null;
                    String title = (mediaItem.mediaMetadata != null && mediaItem.mediaMetadata.title != null)
                            ? mediaItem.mediaMetadata.title.toString() : null;
                    setPlayerTitle(getTitleFromUri(uriString, title));
                }
                updatePipParams();
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    updateDurationUI();
                }
                updatePipParams();
            }
        });
    }

    private MediaItem createMediaItem(String uriString, String title) {
        if (uriString == null || uriString.trim().isEmpty()) {
            return MediaItem.EMPTY;
        }
        String displayTitle = getTitleFromUri(uriString, title);
        Uri uri = Uri.parse(uriString);

        MediaMetadata metadata = new MediaMetadata.Builder()
                .setTitle(displayTitle)
                .setArtist("eX Player")
                .setAlbumTitle("Video Library")
                .setArtworkUri(uri)
                .build();

        return new MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(metadata)
                .build();
    }

    private void setPlayerTitle(String title) {
        if (tvPlayerTitle != null) {
            tvPlayerTitle.setText(title);
            tvPlayerTitle.setSelected(true);
            tvPlayerTitle.requestFocus();
        }
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent == null) return;

        String uriString = intent.getStringExtra(EXTRA_VIDEO_URI);
        String title = intent.getStringExtra(EXTRA_VIDEO_TITLE);
        int initialIndex = intent.getIntExtra(EXTRA_VIDEO_INDEX, 0);
        long startPosition = intent.getLongExtra(EXTRA_START_POSITION, 0L);
        ArrayList<String> playlist = intent.getStringArrayListExtra(EXTRA_VIDEO_PLAYLIST);
        ArrayList<String> titlePlaylist = intent.getStringArrayListExtra(EXTRA_VIDEO_TITLE_PLAYLIST);

        if (title != null && !title.isEmpty()) {
            setPlayerTitle(title);
        } else if (uriString != null) {
            setPlayerTitle(getTitleFromUri(uriString, null));
        }

        if (playlist != null && !playlist.isEmpty()) {
            List<MediaItem> mediaItems = new ArrayList<>();
            for (int i = 0; i < playlist.size(); i++) {
                String uri = playlist.get(i);
                String itemTitle = (titlePlaylist != null && i < titlePlaylist.size()) ? titlePlaylist.get(i) : null;
                if (uri != null && !uri.trim().isEmpty()) {
                    mediaItems.add(createMediaItem(uri, itemTitle));
                }
            }
            if (!mediaItems.isEmpty()) {
                int safeIndex = Math.max(0, Math.min(initialIndex, mediaItems.size() - 1));
                player.setMediaItems(mediaItems, safeIndex, startPosition > 0 ? startPosition : C.TIME_UNSET);
            }
        } else if (uriString != null && !uriString.trim().isEmpty()) {
            player.setMediaItem(createMediaItem(uriString, title));
            if (startPosition > 0) {
                player.seekTo(startPosition);
            }
        }

        player.prepare();
        player.setPlayWhenReady(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupGestureDetector() {
        GestureDetector.SimpleOnGestureListener doubleTapListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                toggleControls();
                return true;
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (videoScaleFactor != 1.0f) {
                    resetVideoZoom();
                    showIndicatorPopup(R.drawable.ic_aspect_ratio, "Zoom", 20, "100% (Reset)");
                    hideIndicatorWithDelay();
                    return true;
                }
                float x = e.getX();
                int width = gestureOverlay.getWidth();
                if (width <= 0) width = getResources().getDisplayMetrics().widthPixels;
                if (x < width / 2f) {
                    seekRelative(-10000);
                    showDoubleTapFeedback(true);
                } else {
                    seekRelative(10000);
                    showDoubleTapFeedback(false);
                }
                return true;
            }

            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                isScrollStarted = false;
                isVerticalScroll = false;
                isHorizontalScroll = false;
                if (player != null) {
                    initialSeekPosition = player.getCurrentPosition();
                    targetSeekPosition = initialSeekPosition;
                }
                initialVolume = audioManager != null ? audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) : 0;
                initialBrightness = getWindowBrightness();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                if (e1 == null || isPinching) return false;
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();

                if (!isScrollStarted) {
                    if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > touchSlop) {
                        isHorizontalScroll = true;
                        isScrollStarted = true;
                    } else if (Math.abs(deltaY) > Math.abs(deltaX) && Math.abs(deltaY) > touchSlop) {
                        isVerticalScroll = true;
                        isScrollStarted = true;
                        isLeftHalf = e1.getX() < (gestureOverlay.getWidth() / 2f);
                    }
                }

                if (isHorizontalScroll) {
                    handleHorizontalScroll(deltaX);
                } else if (isVerticalScroll) {
                    handleVerticalScroll(deltaY, isLeftHalf);
                }
                return true;
            }
        };

        gestureDetector = new GestureDetector(this, doubleTapListener);
        gestureDetector.setOnDoubleTapListener(doubleTapListener);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
                isPinching = true;
                return true;
            }

            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                videoScaleFactor *= scaleFactor;
                videoScaleFactor = Math.max(0.5f, Math.min(5.0f, videoScaleFactor));

                if (Math.abs(videoScaleFactor - 1.0f) < 0.05f) {
                    videoScaleFactor = 1.0f;
                }

                if (playerView != null) {
                    playerView.setPivotX(playerView.getWidth() / 2f);
                    playerView.setPivotY(playerView.getHeight() / 2f);
                    playerView.setTranslationX(0f);
                    playerView.setTranslationY(0f);
                    playerView.setScaleX(videoScaleFactor);
                    playerView.setScaleY(videoScaleFactor);
                }

                int zoomPercent = (int) (videoScaleFactor * 100);
                showIndicatorPopup(R.drawable.ic_aspect_ratio, "Zoom", Math.min(100, (int) ((videoScaleFactor / 5.0f) * 100)), zoomPercent + "%");
                return true;
            }

            @Override
            public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
                hideIndicatorWithDelay();
            }
        });

        gestureOverlay.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();

            if (event.getPointerCount() >= 2) {
                isPinching = true;
                scaleGestureDetector.onTouchEvent(event);
                return true;
            }

            if (isPinching) {
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_UP) {
                    if (event.getPointerCount() <= 1) {
                        isPinching = false;
                        hideIndicatorWithDelay();
                    }
                }
                return true;
            }

            boolean handled = gestureDetector.onTouchEvent(event);
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (isHorizontalScroll && player != null) {
                    player.seekTo(targetSeekPosition);
                    hideIndicatorWithDelay();
                } else if (isVerticalScroll) {
                    hideIndicatorWithDelay();
                }
                isScrollStarted = false;
                isVerticalScroll = false;
                isHorizontalScroll = false;
            }
            return handled;
        });
    }

    private void handleVerticalScroll(float deltaY, boolean leftHalf) {
        int height = gestureOverlay.getHeight() > 0 ? gestureOverlay.getHeight() : getResources().getDisplayMetrics().heightPixels;
        float percentChange = -deltaY / height;

        if (leftHalf) {
            float newBrightness = Math.max(0.01f, Math.min(1.0f, initialBrightness + percentChange));
            setWindowBrightness(newBrightness);
            int percent = (int) (newBrightness * 100);
            showIndicatorPopup(R.drawable.ic_brightness, "Brightness", percent, percent + "%");
        } else {
            int maxVol = audioManager != null ? audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) : 15;
            int volChange = (int) (percentChange * maxVol);
            int newVol = Math.max(0, Math.min(maxVol, initialVolume + volChange));
            if (audioManager != null) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0);
            }
            int percent = (int) ((newVol / (float) maxVol) * 100);
            int iconRes = newVol == 0 ? R.drawable.ic_volume_mute : R.drawable.ic_volume_up;
            showIndicatorPopup(iconRes, "Volume", percent, percent + "%");
        }
    }

    private void handleHorizontalScroll(float deltaX) {
        int width = gestureOverlay.getWidth() > 0 ? gestureOverlay.getWidth() : getResources().getDisplayMetrics().widthPixels;
        long duration = player != null ? player.getDuration() : 0;
        if (duration <= 0) return;

        float percentChange = deltaX / width;
        long seekOffsetMs = (long) (percentChange * 90000);
        targetSeekPosition = Math.max(0, Math.min(duration, initialSeekPosition + seekOffsetMs));

        long diffSec = (targetSeekPosition - initialSeekPosition) / 1000;
        String sign = diffSec >= 0 ? "+" : "";
        String text = formatTime(targetSeekPosition) + " (" + sign + diffSec + "s)";
        int progress = (int) ((targetSeekPosition / (float) duration) * 100);
        int iconRes = diffSec >= 0 ? R.drawable.ic_forward_10 : R.drawable.ic_replay_10;

        showIndicatorPopup(iconRes, "Seeking", progress, text);
    }

    private void showIndicatorPopup(int iconRes, String title, int progress, String valueText) {
        hideIndicatorHandler.removeCallbacksAndMessages(null);
        indicatorPopupLayout.setVisibility(View.VISIBLE);
        indicatorPopupLayout.setAlpha(1f);

        ivIndicatorIcon.setImageResource(iconRes);
        tvIndicatorText.setText(title);
        pbIndicatorProgress.setProgress(progress);
        tvIndicatorValue.setText(valueText);
    }

    private void hideIndicatorWithDelay() {
        hideIndicatorHandler.removeCallbacksAndMessages(null);
        hideIndicatorHandler.postDelayed(() -> {
            if (indicatorPopupLayout.getVisibility() == View.VISIBLE) {
                indicatorPopupLayout.animate()
                        .alpha(0f)
                        .setDuration(250)
                        .withEndAction(() -> indicatorPopupLayout.setVisibility(View.GONE));
            }
        }, 800);
    }

    private void showDoubleTapFeedback(boolean isLeft) {
        LinearLayout layout = isLeft ? layoutDoubleTapLeft : layoutDoubleTapRight;
        TextView tv = isLeft ? tvDoubleTapLeft : tvDoubleTapRight;
        if (tv != null) {
            tv.setText(isLeft ? "-10s" : "+10s");
        }
        layout.animate().cancel();
        layout.setAlpha(0f);
        layout.setVisibility(View.VISIBLE);
        layout.animate()
                .alpha(1f)
                .setDuration(150)
                .withEndAction(() -> layout.postDelayed(() -> layout.animate()
                        .alpha(0f)
                        .setDuration(250)
                        .withEndAction(() -> layout.setVisibility(View.GONE)), 400));
    }

    private float getWindowBrightness() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (lp.screenBrightness < 0) {
            try {
                int systemBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                return systemBrightness / 255f;
            } catch (Exception e) {
                return 0.5f;
            }
        }
        return lp.screenBrightness;
    }

    private void setWindowBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightness;
        getWindow().setAttributes(lp);
    }

    private void setupControlListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPlayPause.setOnClickListener(v -> {
            resetAutoHideTimer();
            if (player != null) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.play();
                }
            }
        });

        btnRewind10.setOnClickListener(v -> {
            resetAutoHideTimer();
            seekRelative(-10000);
            showDoubleTapFeedback(true);
        });

        btnForward10.setOnClickListener(v -> {
            resetAutoHideTimer();
            seekRelative(10000);
            showDoubleTapFeedback(false);
        });

        btnPrev.setOnClickListener(v -> {
            resetAutoHideTimer();
            if (player != null) {
                if (player.getCurrentPosition() > 3000) {
                    player.seekTo(0);
                } else if (player.hasPreviousMediaItem()) {
                    player.seekToPreviousMediaItem();
                } else {
                    player.seekTo(0);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            resetAutoHideTimer();
            if (player != null && player.hasNextMediaItem()) {
                player.seekToNextMediaItem();
            } else {
                Toast.makeText(this, "No next video in playlist", Toast.LENGTH_SHORT).show();
            }
        });

        btnSubtitle.setOnClickListener(v -> {
            resetAutoHideTimer();
            showSubtitleTrackDialog();
        });

        btnAudioTrack.setOnClickListener(v -> {
            resetAutoHideTimer();
            showAudioTrackDialog();
        });

        btnAspectRatio.setOnClickListener(v -> {
            resetAutoHideTimer();
            toggleAspectRatio();
        });

        if (btnImmersive != null) {
            btnImmersive.setOnClickListener(v -> {
                resetAutoHideTimer();
                toggleImmersiveDisplay();
            });
        }

        btnSpeed.setOnClickListener(v -> {
            resetAutoHideTimer();
            showPlaybackSpeedDialog();
        });

        if (btnOrientationChange != null) {
            btnOrientationChange.setOnClickListener(v -> {
                resetAutoHideTimer();
                changeOrientation();
            });
        }

        if (btnBackgroundPlay != null) {
            btnBackgroundPlay.setOnClickListener(v -> {
                resetAutoHideTimer();
                isBackgroundPlayEnabled = !isBackgroundPlayEnabled;
                updateBackgroundPlayUI();
                String msg = isBackgroundPlayEnabled ?
                        getString(R.string.background_play_enabled) :
                        getString(R.string.background_play_disabled);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        }

        btnPip.setOnClickListener(v -> {
            resetAutoHideTimer();
            enterPipMode();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null && player.getDuration() > 0) {
                    long target = (long) ((progress / 1000f) * player.getDuration());
                    tvCurrentTime.setText(formatTime(target));
                    tvDuration.setText(formatTime(target) + " / " + formatTime(player.getDuration()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
                mainHandler.removeCallbacks(autoHideRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player != null && player.getDuration() > 0) {
                    long target = (long) ((seekBar.getProgress() / 1000f) * player.getDuration());
                    player.seekTo(target);
                }
                isUserSeeking = false;
                resetAutoHideTimer();
            }
        });
    }

    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && !isUserSeeking) {
                long currentPos = player.getCurrentPosition();
                long duration = player.getDuration();
                if (duration > 0) {
                    int progress = (int) ((currentPos * 1000L) / duration);
                    if (seekBar != null) seekBar.setProgress(progress);
                    if (tvCurrentTime != null) tvCurrentTime.setText(formatTime(currentPos));
                    if (tvTotalTime != null) tvTotalTime.setText(formatTime(duration));
                    if (tvDuration != null) tvDuration.setText(formatTime(currentPos) + " / " + formatTime(duration));
                } else {
                    if (tvCurrentTime != null) tvCurrentTime.setText(formatTime(currentPos));
                    if (tvTotalTime != null) tvTotalTime.setText("00:00");
                    if (tvDuration != null) tvDuration.setText(formatTime(currentPos) + " / 00:00");
                }
            }
            if (player != null && player.isPlaying()) {
                mainHandler.postDelayed(this, 200);
            }
        }
    };

    private void updateDurationUI() {
        if (player != null && player.getDuration() > 0) {
            tvTotalTime.setText(formatTime(player.getDuration()));
        }
    }

    private void toggleControls() {
        if (controlsVisible) {
            hideControls();
        } else {
            showControls();
        }
    }

    private void showControls() {
        controlsVisible = true;
        controlsOverlay.animate().cancel();
        controlsOverlay.setVisibility(View.VISIBLE);
        controlsOverlay.setAlpha(1f);
        resetAutoHideTimer();
    }

    private void hideControls() {
        controlsVisible = false;
        mainHandler.removeCallbacks(autoHideRunnable);
        controlsOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> controlsOverlay.setVisibility(View.GONE));
    }

    private void resetAutoHideTimer() {
        mainHandler.removeCallbacks(autoHideRunnable);
        if (controlsVisible && player != null && player.isPlaying()) {
            mainHandler.postDelayed(autoHideRunnable, 6000);
        }
    }

    private void seekRelative(long offsetMs) {
        if (player != null) {
            long current = player.getCurrentPosition();
            long duration = player.getDuration();
            long newPos = Math.max(0, duration > 0 ? Math.min(duration, current + offsetMs) : current + offsetMs);
            player.seekTo(newPos);
        }
    }

    private void resetVideoZoom() {
        videoScaleFactor = 1.0f;
        if (playerView != null) {
            playerView.setPivotX(playerView.getWidth() / 2f);
            playerView.setPivotY(playerView.getHeight() / 2f);
            playerView.setScaleX(1.0f);
            playerView.setScaleY(1.0f);
            playerView.setTranslationX(0f);
            playerView.setTranslationY(0f);
        }
    }

    private void toggleAspectRatio() {
        resetVideoZoom();
        currentResizeModeIndex = (currentResizeModeIndex + 1) % RESIZE_MODES.length;
        playerView.setResizeMode(RESIZE_MODES[currentResizeModeIndex]);
        String modeName = getString(RESIZE_MODE_STRINGS[currentResizeModeIndex]);
        Toast.makeText(this, "Aspect Ratio: " + modeName, Toast.LENGTH_SHORT).show();
    }

    private void toggleImmersiveDisplay() {
        isImmersiveDisplayEnabled = !isImmersiveDisplayEnabled;

        SharedPreferences prefs = getSharedPreferences("EXPlayerPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("immersive_display_enabled", isImmersiveDisplayEnabled).apply();

        applyImmersiveDisplayMode();
        updateImmersiveDisplayUI();

        String msg = isImmersiveDisplayEnabled ?
                getString(R.string.immersive_display_enabled) :
                getString(R.string.immersive_display_disabled);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void applyImmersiveDisplayMode() {
        hideSystemUI();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            if (isImmersiveDisplayEnabled) {
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            } else {
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            }
            getWindow().setAttributes(lp);
        }
    }

    private void updateImmersiveDisplayUI() {
        if (ivImmersive != null) {
            if (isImmersiveDisplayEnabled) {
                ivImmersive.setColorFilter(ContextCompat.getColor(this, R.color.liquid_cyan));
            } else {
                ivImmersive.setColorFilter(ContextCompat.getColor(this, R.color.white));
            }
        }
    }

    private void changeOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            Toast.makeText(this, "Portrait Mode", Toast.LENGTH_SHORT).show();
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            Toast.makeText(this, "Landscape Mode", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePipParams() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        try {
            PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();

            if (player != null) {
                VideoSize videoSize = player.getVideoSize();
                int width = videoSize.width;
                int height = videoSize.height;
                if (width > 0 && height > 0) {
                    float ratio = (float) width / (float) height;
                    if (ratio < 0.4184f) {
                        builder.setAspectRatio(new Rational(4184, 10000));
                    } else if (ratio > 2.39f) {
                        builder.setAspectRatio(new Rational(23900, 10000));
                    } else {
                        builder.setAspectRatio(new Rational(width, height));
                    }
                } else {
                    builder.setAspectRatio(new Rational(16, 9));
                }
            } else {
                builder.setAspectRatio(new Rational(16, 9));
            }

            List<RemoteAction> actions = new ArrayList<>();

            Intent replayIntent = new Intent(ACTION_PIP_REPLAY_10).setPackage(getPackageName());
            PendingIntent replayPending = PendingIntent.getBroadcast(
                    this, 101, replayIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Icon replayIcon = Icon.createWithResource(this, R.drawable.ic_replay_10);
            actions.add(new RemoteAction(replayIcon, "Rewind 10s", "Rewind 10s", replayPending));

            boolean isPlaying = player != null && player.isPlaying();
            String playPauseLabel = isPlaying ? "Pause" : "Play";
            int playPauseIconRes = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play;
            String playPauseActionStr = isPlaying ? ACTION_PIP_PAUSE : ACTION_PIP_PLAY;
            Intent playPauseIntent = new Intent(playPauseActionStr).setPackage(getPackageName());
            PendingIntent playPausePending = PendingIntent.getBroadcast(
                    this, 102, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Icon playPauseIcon = Icon.createWithResource(this, playPauseIconRes);
            actions.add(new RemoteAction(playPauseIcon, playPauseLabel, playPauseLabel, playPausePending));

            Intent forwardIntent = new Intent(ACTION_PIP_FORWARD_10).setPackage(getPackageName());
            PendingIntent forwardPending = PendingIntent.getBroadcast(
                    this, 103, forwardIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Icon forwardIcon = Icon.createWithResource(this, R.drawable.ic_forward_10);
            actions.add(new RemoteAction(forwardIcon, "Forward 10s", "Forward 10s", forwardPending));

            builder.setActions(actions);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.setAutoEnterEnabled(player != null && player.isPlaying());
            }

            setPictureInPictureParams(builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                updatePipParams();
                PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
                if (player != null) {
                    VideoSize videoSize = player.getVideoSize();
                    if (videoSize.width > 0 && videoSize.height > 0) {
                        float ratio = (float) videoSize.width / (float) videoSize.height;
                        if (ratio >= 0.4184f && ratio <= 2.39f) {
                            builder.setAspectRatio(new Rational(videoSize.width, videoSize.height));
                        }
                    }
                }
                enterPictureInPictureMode(builder.build());
            } catch (Exception e) {
                Toast.makeText(this, "PiP mode unavailable", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "PiP requires Android 8.0+", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            controlsOverlay.setVisibility(View.GONE);
            indicatorPopupLayout.setVisibility(View.GONE);
            gestureOverlay.setVisibility(View.GONE);
            layoutDoubleTapLeft.setVisibility(View.GONE);
            layoutDoubleTapRight.setVisibility(View.GONE);
        } else {
            gestureOverlay.setVisibility(View.VISIBLE);
            controlsOverlay.setVisibility(View.VISIBLE);
            showControls();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && player != null && player.isPlaying()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                try {
                    enterPipMode();
                } catch (Exception ignored) {}
            }
        }
    }

    private void setupDialog(Dialog dialog) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void showSubtitleTrackDialog() {
        if (player == null) return;
        Tracks tracks = player.getCurrentTracks();

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_track_selection);
        setupDialog(dialog);

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        tvTitle.setText(R.string.player_subtitle_selector);

        RadioGroup rgTracks = dialog.findViewById(R.id.rgTracks);
        MaterialButton btnClose = dialog.findViewById(R.id.btnDialogClose);

        RadioButton rbOff = new RadioButton(this);
        rbOff.setText(R.string.player_subtitles_off);
        rbOff.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        rgTracks.addView(rbOff);

        boolean isTextDisabled = player.getTrackSelectionParameters().disabledTrackTypes.contains(C.TRACK_TYPE_TEXT);
        if (isTextDisabled) {
            rbOff.setChecked(true);
        }

        List<TrackItem> subtitleItems = new ArrayList<>();
        int itemIndex = 1;

        for (Tracks.Group group : tracks.getGroups()) {
            if (group.getType() == C.TRACK_TYPE_TEXT) {
                for (int i = 0; i < group.length; i++) {
                    Format format = group.getTrackFormat(i);
                    String label = format.label != null ? format.label : (format.language != null ? format.language : "Track " + itemIndex);
                    boolean selected = group.isTrackSelected(i);

                    TrackItem trackItem = new TrackItem(group, i, label);
                    subtitleItems.add(trackItem);

                    RadioButton rb = new RadioButton(this);
                    rb.setText(label);
                    rb.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                    rgTracks.addView(rb);

                    if (!isTextDisabled && selected) {
                        rb.setChecked(true);
                    }
                    itemIndex++;
                }
            }
        }

        rgTracks.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedIndex = rgTracks.indexOfChild(dialog.findViewById(checkedId));
            if (selectedIndex == 0) {
                player.setTrackSelectionParameters(player.getTrackSelectionParameters()
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                        .build());
                Toast.makeText(this, "Subtitles turned Off", Toast.LENGTH_SHORT).show();
            } else if (selectedIndex > 0 && selectedIndex - 1 < subtitleItems.size()) {
                TrackItem selectedTrack = subtitleItems.get(selectedIndex - 1);
                player.setTrackSelectionParameters(player.getTrackSelectionParameters()
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                        .setOverrideForType(new TrackSelectionOverride(selectedTrack.group.getMediaTrackGroup(), selectedTrack.trackIndex))
                        .build());
                Toast.makeText(this, "Selected Subtitle: " + selectedTrack.label, Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
            resetAutoHideTimer();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showAudioTrackDialog() {
        if (player == null) return;
        Tracks tracks = player.getCurrentTracks();

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_track_selection);
        setupDialog(dialog);

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        tvTitle.setText(R.string.player_audio_selector);

        RadioGroup rgTracks = dialog.findViewById(R.id.rgTracks);
        MaterialButton btnClose = dialog.findViewById(R.id.btnDialogClose);

        List<TrackItem> audioItems = new ArrayList<>();
        int itemIndex = 1;

        for (Tracks.Group group : tracks.getGroups()) {
            if (group.getType() == C.TRACK_TYPE_AUDIO) {
                for (int i = 0; i < group.length; i++) {
                    Format format = group.getTrackFormat(i);
                    String label = format.label != null ? format.label : (format.language != null ? format.language : "Audio Track " + itemIndex);
                    if (format.channelCount > 0) {
                        label += " (" + format.channelCount + " ch)";
                    }
                    boolean selected = group.isTrackSelected(i);

                    TrackItem trackItem = new TrackItem(group, i, label);
                    audioItems.add(trackItem);

                    RadioButton rb = new RadioButton(this);
                    rb.setText(label);
                    rb.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                    rgTracks.addView(rb);

                    if (selected) {
                        rb.setChecked(true);
                    }
                    itemIndex++;
                }
            }
        }

        rgTracks.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedIndex = rgTracks.indexOfChild(dialog.findViewById(checkedId));
            if (selectedIndex >= 0 && selectedIndex < audioItems.size()) {
                TrackItem selectedTrack = audioItems.get(selectedIndex);
                player.setTrackSelectionParameters(player.getTrackSelectionParameters()
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
                        .setOverrideForType(new TrackSelectionOverride(selectedTrack.group.getMediaTrackGroup(), selectedTrack.trackIndex))
                        .build());
                Toast.makeText(this, "Selected Audio: " + selectedTrack.label, Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
            resetAutoHideTimer();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showPlaybackSpeedDialog() {
        if (player == null) return;

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_track_selection);
        setupDialog(dialog);

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        tvTitle.setText(R.string.player_playback_speed);

        RadioGroup rgTracks = dialog.findViewById(R.id.rgTracks);
        MaterialButton btnClose = dialog.findViewById(R.id.btnDialogClose);

        float currentSpeed = player.getPlaybackParameters().speed;

        for (int i = 0; i < SPEED_LABELS.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(SPEED_LABELS[i]);
            rb.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            rgTracks.addView(rb);

            if (Math.abs(currentSpeed - SPEED_VALUES[i]) < 0.05f) {
                rb.setChecked(true);
            }
        }

        rgTracks.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedIndex = rgTracks.indexOfChild(dialog.findViewById(checkedId));
            if (selectedIndex >= 0 && selectedIndex < SPEED_VALUES.length) {
                float selectedSpeed = SPEED_VALUES[selectedIndex];
                player.setPlaybackSpeed(selectedSpeed);
                Toast.makeText(this, "Speed set to " + SPEED_LABELS[selectedIndex], Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
            resetAutoHideTimer();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String getTitleFromUri(String uriStr, String fallbackTitle) {
        if (fallbackTitle != null && !fallbackTitle.trim().isEmpty() && !fallbackTitle.matches("^\\d+$")) {
            return fallbackTitle;
        }
        if (uriStr != null && !uriStr.trim().isEmpty()) {
            try {
                Uri uri = Uri.parse(uriStr);
                if ("content".equalsIgnoreCase(uri.getScheme())) {
                    try (Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME, MediaStore.Video.Media.TITLE}, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                            if (nameIndex != -1) {
                                String displayName = cursor.getString(nameIndex);
                                if (displayName != null && !displayName.trim().isEmpty()) {
                                    return displayName;
                                }
                            }
                            int titleIndex = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
                            if (titleIndex != -1) {
                                String mediaTitle = cursor.getString(titleIndex);
                                if (mediaTitle != null && !mediaTitle.trim().isEmpty()) {
                                    return mediaTitle;
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
                String path = uri.getPath();
                if (path != null && path.contains("/")) {
                    String fileName = path.substring(path.lastIndexOf('/') + 1);
                    if (!fileName.matches("^\\d+$")) {
                        return Uri.decode(fileName);
                    }
                }
            } catch (Exception ignored) {}
        }
        if (fallbackTitle != null && !fallbackTitle.trim().isEmpty()) {
            return fallbackTitle;
        }
        return "Video";
    }

    private String formatTime(long timeMs) {
        if (timeMs <= 0) return "00:00";
        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updatePipParams();
        try {
            Intent serviceIntent = new Intent(this, BackgroundPlaybackService.class);
            stopService(serviceIntent);
        } catch (Exception ignored) {}

        if (playerView != null && player != null) {
            playerView.setPlayer(player);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyImmersiveDisplayMode();
        if (playerView != null && player != null) {
            playerView.setPlayer(player);
            playerView.onResume();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyImmersiveDisplayMode();
        }
    }

    private void saveLastPlayedVideo() {
        if (player == null) return;
        long position = player.getCurrentPosition();
        long duration = player.getDuration();
        if (duration < 0) duration = 0;

        String uriString = null;
        MediaItem currentItem = player.getCurrentMediaItem();
        if (currentItem != null && currentItem.localConfiguration != null && currentItem.localConfiguration.uri != null) {
            uriString = currentItem.localConfiguration.uri.toString();
        } else if (getIntent() != null) {
            uriString = getIntent().getStringExtra(EXTRA_VIDEO_URI);
        }

        String title = tvPlayerTitle != null ? tvPlayerTitle.getText().toString() : "";

        if (uriString != null && !uriString.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("EXPlayerPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("last_played_uri", uriString)
                    .putString("last_played_title", title)
                    .putLong("last_played_position", position)
                    .putLong("last_played_duration", duration)
                    .apply();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveLastPlayedVideo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isInPictureInPictureMode()) {
            return;
        }

        if (!isFinishing() && isBackgroundPlayEnabled && player != null && player.isPlaying()) {
            try {
                Intent serviceIntent = new Intent(this, BackgroundPlaybackService.class);
                ContextCompat.startForegroundService(this, serviceIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveLastPlayedVideo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isInPictureInPictureMode()) {
            return;
        }

        if (!(isBackgroundPlayEnabled && player != null && player.isPlaying())) {
            if (player != null) {
                player.pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveLastPlayedVideo();
        mainHandler.removeCallbacksAndMessages(null);
        hideIndicatorHandler.removeCallbacksAndMessages(null);

        try {
            unregisterReceiver(pipReceiver);
        } catch (Exception ignored) {}

        try {
            Intent serviceIntent = new Intent(this, BackgroundPlaybackService.class);
            stopService(serviceIntent);
        } catch (Exception ignored) {}

        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
        BackgroundPlaybackService.setMediaSession(null);

        if (player != null) {
            player.release();
            player = null;
        }
    }

    private static class TrackItem {
        final Tracks.Group group;
        final int trackIndex;
        final String label;

        TrackItem(Tracks.Group group, int trackIndex, String label) {
            this.group = group;
            this.trackIndex = trackIndex;
            this.label = label;
        }
    }
}
