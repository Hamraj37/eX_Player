package com.hamraj37.explayer.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hamraj37.explayer.model.VideoItem;
import com.hamraj37.explayer.repository.VideoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoViewModel extends AndroidViewModel {

    private final VideoRepository repository;
    private final MutableLiveData<List<VideoItem>> videosLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> searchQueryLiveData = new MutableLiveData<>("");
    private final ExecutorService executorService;

    public VideoViewModel(@NonNull Application application) {
        super(application);
        this.repository = new VideoRepository();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<VideoItem>> getVideos() {
        return videosLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getSearchQuery() {
        return searchQueryLiveData;
    }

    public void setSearchQuery(String query) {
        searchQueryLiveData.setValue(query != null ? query : "");
    }

    public void loadVideos() {
        isLoadingLiveData.setValue(true);
        executorService.execute(() -> {
            List<VideoItem> fetched = repository.fetchVideos(getApplication());
            videosLiveData.postValue(fetched);
            isLoadingLiveData.postValue(false);
        });
    }

    @Override
    protected void onCleared() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}