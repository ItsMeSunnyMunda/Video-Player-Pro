package xyz.mpv.rex.utils.media

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalPlaylistTest {

  @Test
  fun `extractExternalPlaylist returns null when video_list is absent`() {
    val intent = mockk<Intent>(relaxed = true)
    every { intent.getParcelableArrayListExtra<Uri>(any(), any()) } returns null
    every { intent.getParcelableArrayListExtra<Parcelable>(any()) } returns null
    every { intent.getParcelableArrayExtra(any(), any<Class<Uri>>()) } returns null
    every { intent.getParcelableArrayExtra(any()) } returns null
    every { intent.getStringArrayExtra(any()) } returns null
    every { intent.getStringArrayListExtra(any()) } returns null

    val result = FolderPlaylistOps.extractExternalPlaylist(intent)
    assertNull(result)
  }

  @Test
  fun `extractExternalPlaylist extracts parcelable array of URIs and names`() {
    val uri1 = mockk<Uri>()
    val uri2 = mockk<Uri>()
    val uri3 = mockk<Uri>()
    every { uri1.lastPathSegment } returns "Ep01.mkv"
    every { uri2.lastPathSegment } returns "Ep02.mkv"
    every { uri3.lastPathSegment } returns "Ep03.mkv"
    every { uri1.path } returns "/Videos/Ep01.mkv"
    every { uri2.path } returns "/Videos/Ep02.mkv"
    every { uri3.path } returns "/Videos/Ep03.mkv"

    val intent = mockk<Intent>(relaxed = true)
    every { intent.getParcelableArrayListExtra<Uri>(any(), any()) } returns null
    every { intent.getParcelableArrayListExtra<Parcelable>(any()) } returns null
    every { intent.getParcelableArrayExtra("video_list") } returns arrayOf(uri1, uri2, uri3)
    every { intent.getParcelableArrayExtra("video_list", any<Class<Uri>>()) } returns arrayOf(uri1, uri2, uri3)
    every { intent.getParcelableArrayExtra("video_list", any<Class<Parcelable>>()) } returns arrayOf(uri1, uri2, uri3)
    every { intent.getStringArrayExtra("video_list") } returns null
    every { intent.getStringArrayListExtra("video_list") } returns null
    every { intent.getStringArrayExtra("video_list.name") } returns arrayOf("Episode 1", "Episode 2", "Episode 3")
    every { intent.getStringArrayListExtra("video_list.name") } returns null
    every { intent.getCharSequenceArrayExtra("video_list.name") } returns null
    every { intent.getCharSequenceArrayListExtra("video_list.name") } returns null
    every { intent.getBooleanExtra("video_list_is_explicit", false) } returns true
    every { intent.data } returns uri2

    val playlist = FolderPlaylistOps.extractExternalPlaylist(intent)
    assertNotNull(playlist)
    assertEquals(3, playlist!!.items.size)
    assertEquals(listOf(uri1, uri2, uri3), playlist.items)
    assertEquals(listOf("Episode 1", "Episode 2", "Episode 3"), playlist.titles)
    assertEquals(1, playlist.initialIndex)
    assertTrue(playlist.isExplicit)
  }

  @Test
  fun `extractExternalPlaylist falls back to lastPathSegment when names are absent`() {
    val uri1 = mockk<Uri>()
    val uri2 = mockk<Uri>()
    every { uri1.lastPathSegment } returns "video1.mp4"
    every { uri2.lastPathSegment } returns "video2.mp4"
    every { uri1.path } returns "/video1.mp4"
    every { uri2.path } returns "/video2.mp4"

    val intent = mockk<Intent>(relaxed = true)
    every { intent.getParcelableArrayListExtra<Uri>("video_list", any()) } returns arrayListOf(uri1, uri2)
    every { intent.getParcelableArrayListExtra<Parcelable>("video_list") } returns arrayListOf(uri1, uri2)
    every { intent.getStringArrayExtra("video_list.name") } returns null
    every { intent.getStringArrayListExtra("video_list.name") } returns null
    every { intent.getCharSequenceArrayExtra("video_list.name") } returns null
    every { intent.getCharSequenceArrayListExtra("video_list.name") } returns null
    every { intent.getBooleanExtra("video_list_is_explicit", false) } returns false
    every { intent.data } returns null

    val playlist = FolderPlaylistOps.extractExternalPlaylist(intent)
    assertNotNull(playlist)
    assertEquals(listOf("video1.mp4", "video2.mp4"), playlist!!.titles)
    assertEquals(0, playlist.initialIndex)
  }
}
