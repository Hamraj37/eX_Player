package com.hamraj37.explayer;

import com.hamraj37.explayer.model.VideoItem;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VideoItemUnitTest {

    @Test
    public void testVideoItemDurationFormatting() {
        VideoItem itemShort = new VideoItem(1, null, "Test Short", 65000L, 1024L, "/sdcard/test.mp4", 1600000000L);
        assertEquals("01:05", itemShort.getFormattedDuration());

        VideoItem itemLong = new VideoItem(2, null, "Test Long", 3665000L, 2048L, "/sdcard/test2.mp4", 1600000000L);
        assertEquals("01:01:05", itemLong.getFormattedDuration());

        VideoItem itemZero = new VideoItem(3, null, "Test Zero", 0L, 0L, "/sdcard/test3.mp4", 0L);
        assertEquals("00:00", itemZero.getFormattedDuration());
    }

    @Test
    public void testVideoItemSizeFormatting() {
        VideoItem itemBytes = new VideoItem(1, null, "Bytes", 1000L, 500L, "/path", 0L);
        assertEquals("500 B", itemBytes.getFormattedSize());

        VideoItem itemKb = new VideoItem(2, null, "KB", 1000L, 1536L, "/path", 0L);
        assertEquals("1.5 KB", itemKb.getFormattedSize());

        VideoItem itemMb = new VideoItem(3, null, "MB", 1000L, 10485760L, "/path", 0L);
        assertEquals("10.0 MB", itemMb.getFormattedSize());

        VideoItem itemGb = new VideoItem(4, null, "GB", 1000L, 10737418240L, "/path", 0L);
        assertEquals("10.00 GB", itemGb.getFormattedSize());
    }

    @Test
    public void testVideoItemGetters() {
        VideoItem item = new VideoItem(100L, null, "Sample Title", 120000L, 5000000L, "/storage/emulated/0/Movies/sample.mp4", 1600000000L);

        assertEquals(100L, item.getId());
        assertEquals("Sample Title", item.getTitle());
        assertEquals("", item.getUriString());
        assertEquals("/storage/emulated/0/Movies/sample.mp4", item.getPath());
        assertEquals(120000L, item.getDuration());
        assertEquals(5000000L, item.getSize());
        assertEquals(1600000000L, item.getDateAdded());
    }
}
