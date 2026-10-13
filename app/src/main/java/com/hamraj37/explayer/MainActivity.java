package com.hamraj37.explayer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.hamraj37.explayer.adapter.VideoAdapter;
import com.hamraj37.explayer.model.VideoItem;
import com.hamraj37.explayer.viewmodel.VideoViewModel;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements VideoAdapter.OnVideoClickListener {

    private VideoViewModel videoViewModel;
    private VideoAdapter videoAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewVideos;
    private TextView tvVideoCountBadge;
    private EditText etSearch;
    private ImageView btnClearSearch;
    private View headerToolbar;
    private View btnThemeToggle;
    private View btnMoreMenu;

    private LinearLayout emptyStateLayout;
    private TextView tvEmptyTitle;
    private TextView tvEmptyDesc;
    private MaterialButton btnGrantPermission;
    private ExtendedFloatingActionButton fabContinue;

    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("EXPlayerPrefs", MODE_PRIVATE);
        int themeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View rootView = findViewById(R.id.mainActivityRoot);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        setupRecyclerView();
        setupViewModel();
        setupPermissionLauncher();
        setupSearchAndRefresh();
        setupThemeToggle();
        setupMoreMenu();
        setupFab();

        checkPermissionsAndLoadVideos();
    }

    private void initViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerViewVideos = findViewById(R.id.recyclerViewVideos);
        tvVideoCountBadge = findViewById(R.id.tvVideoCountBadge);
        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        headerToolbar = findViewById(R.id.headerToolbar);
        btnThemeToggle = findViewById(R.id.btnThemeToggle);
        btnMoreMenu = findViewById(R.id.btnMoreMenu);

        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyDesc = findViewById(R.id.tvEmptyDesc);
        btnGrantPermission = findViewById(R.id.btnGrantPermission);
        fabContinue = findViewById(R.id.fabContinue);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(
                    ContextCompat.getColor(this, R.color.liquid_cyan),
                    ContextCompat.getColor(this, R.color.liquid_purple)
            );
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(
                    ContextCompat.getColor(this, R.color.surface_header)
            );
        }
    }

    private void setupRecyclerView() {
        videoAdapter = new VideoAdapter(this);
        recyclerViewVideos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewVideos.setAdapter(videoAdapter);
    }

    private void setupViewModel() {
        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);

        videoViewModel.getVideos().observe(this, this::updateVideoListUI);

        videoViewModel.getIsLoading().observe(this, isLoading -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(Boolean.TRUE.equals(isLoading));
            }
        });
    }

    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Boolean granted : result.values()) {
                        if (!Boolean.TRUE.equals(granted)) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        emptyStateLayout.setVisibility(View.GONE);
                        videoViewModel.loadVideos();
                    } else {
                        showPermissionDeniedState();
                        Toast.makeText(this, "Permission required to display videos", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        if (btnGrantPermission != null) {
            btnGrantPermission.setOnClickListener(v -> requestRequiredPermissions());
        }
    }

    private void setupSearchAndRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::checkPermissionsAndLoadVideos);
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s != null ? s.toString() : "";
                    if (videoAdapter != null) {
                        videoAdapter.filter(query);
                        updateVideoCountBadge(videoAdapter.getItemCount());
                    }
                    if (btnClearSearch != null) {
                        btnClearSearch.setVisibility(query.trim().isEmpty() ? View.GONE : View.VISIBLE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                if (etSearch != null) {
                    etSearch.setText("");
                }
            });
        }
    }

    private void setupThemeToggle() {
        if (btnThemeToggle != null) {
            btnThemeToggle.setOnClickListener(v -> showThemeSelectionDialog());
        }
    }

    private void setupMoreMenu() {
        if (btnMoreMenu != null) {
            btnMoreMenu.setOnClickListener(this::showPopupMenu);
        }
    }

    private void showPopupMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_main, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_refresh) {
                checkPermissionsAndLoadVideos();
                return true;
            } else if (itemId == R.id.action_sort) {
                showSortDialog();
                return true;
            } else if (itemId == R.id.action_theme) {
                showThemeSelectionDialog();
                return true;
            } else if (itemId == R.id.action_about) {
                showAboutDialog();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showSortDialog() {
        String[] sortOptions = {
                getString(R.string.sort_by_title),
                getString(R.string.sort_by_date),
                getString(R.string.sort_by_size),
                getString(R.string.sort_by_duration)
        };

        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.Widget_EXPlayer_Dialog)
                .setTitle(R.string.sort_dialog_title)
                .setItems(sortOptions, (dialog, which) -> {
                    if (videoAdapter != null) {
                        videoAdapter.sortVideos(which);
                        updateVideoCountBadge(videoAdapter.getItemCount());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showAboutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.Widget_EXPlayer_Dialog)
                .setTitle(R.string.about_dialog_title)
                .setMessage(R.string.about_dialog_message)
                .setPositiveButton(R.string.dialog_close, null)
                .show();
    }

    private void showThemeSelectionDialog() {
        String[] themes = {
                getString(R.string.theme_system),
                getString(R.string.theme_light),
                getString(R.string.theme_dark)
        };

        SharedPreferences prefs = getSharedPreferences("EXPlayerPrefs", MODE_PRIVATE);
        int currentMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        int checkedItem = 0;
        if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
            checkedItem = 1;
        } else if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            checkedItem = 2;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.Widget_EXPlayer_Dialog)
                .setTitle(R.string.theme_dialog_title)
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    int selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    if (which == 1) {
                        selectedMode = AppCompatDelegate.MODE_NIGHT_NO;
                    } else if (which == 2) {
                        selectedMode = AppCompatDelegate.MODE_NIGHT_YES;
                    }

                    prefs.edit().putInt("theme_mode", selectedMode).apply();
                    AppCompatDelegate.setDefaultNightMode(selectedMode);
                    dialog.dismiss();
                    recreate();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void checkPermissionsAndLoadVideos() {
        if (hasRequiredPermissions()) {
            emptyStateLayout.setVisibility(View.GONE);
            videoViewModel.loadVideos();
        } else {
            showPermissionDeniedState();
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private boolean hasRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean mediaGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                    == PackageManager.PERMISSION_GRANTED;
            boolean notifyGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
            return mediaGranted && notifyGranted;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.POST_NOTIFICATIONS
            });
        } else {
            permissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
        }
    }

    private void showPermissionDeniedState() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(R.string.permission_required_title);
        tvEmptyDesc.setText(R.string.permission_required_desc);
        btnGrantPermission.setVisibility(View.VISIBLE);
        tvVideoCountBadge.setText(R.string.video_count_zero);
    }

    private void updateVideoListUI(List<VideoItem> videoItems) {
        if (!hasRequiredPermissions()) {
            showPermissionDeniedState();
            return;
        }

        if (videoItems == null || videoItems.isEmpty()) {
            videoAdapter.setVideoList(new ArrayList<>());
            emptyStateLayout.setVisibility(View.VISIBLE);
            tvEmptyTitle.setText(R.string.no_videos_found);
            tvEmptyDesc.setText(R.string.no_videos_found_sub);
            btnGrantPermission.setVisibility(View.GONE);
            updateVideoCountBadge(0);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            videoAdapter.setVideoList(videoItems);
            if (etSearch != null && etSearch.getText() != null) {
                videoAdapter.filter(etSearch.getText().toString());
            }
            updateVideoCountBadge(videoAdapter.getItemCount());
        }
    }

    private void updateVideoCountBadge(int count) {
        if (tvVideoCountBadge != null) {
            if (count == 1) {
                tvVideoCountBadge.setText(getString(R.string.video_count_single));
            } else {
                tvVideoCountBadge.setText(getString(R.string.video_count_format, count));
            }
        }
    }

    private void setupFab() {
        if (fabContinue == null) return;

        fabContinue.setOnClickListener(v -> resumeLastPlayedVideo());

        if (recyclerViewVideos != null) {
            recyclerViewVideos.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 10 && fabContinue.isExtended()) {
                        fabContinue.shrink();
                    } else if (dy < -10 && !fabContinue.isExtended()) {
                        fabContinue.extend();
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFabState();
    }

    private void updateFabState() {
        if (fabContinue == null) return;

        SharedPreferences prefs = getSharedPreferences("EXPlayerPrefs", MODE_PRIVATE);
        String lastUri = prefs.getString("last_played_uri", null);
        String lastTitle = prefs.getString("last_played_title", null);

        if (lastUri != null && !lastUri.isEmpty()) {
            if (lastTitle != null && !lastTitle.isEmpty()) {
                String shortTitle = lastTitle;
                if (shortTitle.length() > 18) {
                    shortTitle = shortTitle.substring(0, 16) + "…";
                }
                fabContinue.setText(getString(R.string.fab_continue_playing_title, shortTitle));
            } else {
                fabContinue.setText(R.string.fab_continue_playing);
            }
            fabContinue.show();
        } else {
            fabContinue.hide();
        }
    }

    private void resumeLastPlayedVideo() {
        SharedPreferences prefs = getSharedPreferences("EXPlayerPrefs", MODE_PRIVATE);
        String lastUri = prefs.getString("last_played_uri", null);
        String lastTitle = prefs.getString("last_played_title", null);
        long lastPosition = prefs.getLong("last_played_position", 0L);
        long lastDuration = prefs.getLong("last_played_duration", 0L);

        if (lastUri == null || lastUri.isEmpty()) {
            Toast.makeText(this, R.string.toast_no_last_played, Toast.LENGTH_SHORT).show();
            return;
        }

        if (lastDuration > 0 && lastPosition >= lastDuration - 2000) {
            lastPosition = 0;
        }

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_URI, lastUri);
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_TITLE, lastTitle);
        intent.putExtra(PlayerActivity.EXTRA_START_POSITION, lastPosition);

        if (videoAdapter != null && videoAdapter.getItemCount() > 0) {
            List<VideoItem> currentList = videoAdapter.getFilteredList();
            if (currentList != null && !currentList.isEmpty()) {
                ArrayList<String> uriPlaylist = new ArrayList<>();
                int matchedIndex = -1;
                for (int i = 0; i < currentList.size(); i++) {
                    VideoItem item = currentList.get(i);
                    if (item != null && item.getUriString() != null) {
                        uriPlaylist.add(item.getUriString());
                        if (lastUri.equals(item.getUriString())) {
                            matchedIndex = i;
                        }
                    }
                }
                if (!uriPlaylist.isEmpty()) {
                    intent.putStringArrayListExtra(PlayerActivity.EXTRA_VIDEO_PLAYLIST, uriPlaylist);
                    if (matchedIndex != -1) {
                        intent.putExtra(PlayerActivity.EXTRA_VIDEO_INDEX, matchedIndex);
                    }
                }
            }
        }

        startActivity(intent);
    }

    @Override
    public void onVideoClick(@NonNull VideoItem videoItem, int position, List<VideoItem> currentList) {
        SharedPreferences prefs = getSharedPreferences("EXPlayerPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("last_played_uri", videoItem.getUriString())
                .putString("last_played_title", videoItem.getTitle())
                .apply();

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_URI, videoItem.getUriString());
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_TITLE, videoItem.getTitle());
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_INDEX, position);

        ArrayList<String> uriPlaylist = new ArrayList<>();
        if (currentList != null) {
            for (VideoItem item : currentList) {
                if (item != null && item.getUriString() != null) {
                    uriPlaylist.add(item.getUriString());
                }
            }
        }
        intent.putStringArrayListExtra(PlayerActivity.EXTRA_VIDEO_PLAYLIST, uriPlaylist);

        startActivity(intent);
    }
}
