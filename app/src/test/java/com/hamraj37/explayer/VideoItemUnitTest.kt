package com.hamraj37.explayer

import com.hamraj37.explayer.model.VideoItem
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoItemUnitTest {

    @Test
    fun testVideoItemDurationFormatting() {
        val itemShort = VideoItem(1, null, "Test Short", 65000L, 1024L, "/sdcard/test.mp4", 1600000000L)
        assertEquals("01:05", itemShort.formattedDuration)

        val itemLong = VideoItem(2, null, "Test Long", 3665000L, 2048L, "/sdcard/test2.mp4", 1600000000L)
        assertEquals("01:01:05", itemLong.formattedDuration)

        val itemZero = VideoItem(3, null, "Test Zero", 0L, 0L, "/sdcard/test3.mp4", 0L)
        assertEquals("00:00", itemZero.formattedDuration)
    }

    @Test
    fun testVideoItemSizeFormatting() {
        val itemBytes = VideoItem(1, null, "Bytes", 1000L, 500L, "/path", 0L)
        assertEquals("500 B", itemBytes.formattedSize)

        val itemKb = VideoItem(2, null, "KB", 1000L, 1536L, "/path", 0L)
        assertEquals("1.5 KB", itemKb.formattedSize)

        val itemMb = VideoItem(3, null, "MB", 1000L, 10485760L, "/path", 0L)
        assertEquals("10.0 MB", itemMb.formattedSize)

        val itemGb = VideoItem(4, null, "GB", 1000L, 10737418240L, "/path", 0L)
        assertEquals("10.00 GB", itemGb.formattedSize)
    }

    @Test
    fun testVideoItemGetters() {
        val item = VideoItem(100L, null, "Sample Title", 120000L, 5000000L, "/storage/emulated/0/Movies/sample.mp4", 1600000000L)

        assertEquals(100L, item.id)
        assertEquals("Sample Title", item.title)
        assertEquals("", item.uriString)
        assertEquals("/storage/emulated/0/Movies/sample.mp4", item.path)
        assertEquals(120000L, item.duration)
        assertEquals(5000000L, item.size)
        assertEquals(1600000000L, item.dateAdded)
    }
}
