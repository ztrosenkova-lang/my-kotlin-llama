package org.nehuatl.sample

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object VideoFrameExtractor {
    private const val TAG = "VideoFrameExtractor"
    private const val MAX_FRAMES = 24
    private const val MIN_ACCEPTABLE_FRAMES = 16

    data class Result(
        val framePaths: List<String>,
        val durationSeconds: Int,
        val frameCount: Int
    )

        fun extract(
        context: Context,
        videoUri: Uri,
        onProgress: ((extracted: Int, total: Int) -> Unit)? = null
    ): Result? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, videoUri)
            val durationMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L

            if (durationMs <= 0) {
                Log.e(TAG, "durationMs=$durationMs, cannot extract frames")
                return null
            }

            val durationSec = (durationMs / 1000).toInt()
            val intervalMs = durationMs / MAX_FRAMES

            val cacheDir = File(context.cacheDir, "video_frames").apply {
                if (exists()) listFiles()?.forEach { it.delete() } else mkdirs()
            }

            val frames = mutableListOf<String>()
            var useClosest = false

            for (i in 0 until MAX_FRAMES) {
                val timeUs = i * intervalMs * 1000
                val option = if (useClosest)
                    MediaMetadataRetriever.OPTION_CLOSEST
                else
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC

        val bitmap = retriever.getFrameAtTime(timeUs, option) ?: continue
                val file = File(cacheDir, "frame_$i.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                bitmap.recycle()
                frames.add(Uri.fromFile(file).toString())
                onProgress?.invoke(frames.size, MAX_FRAMES)
            }

            // Fallback: если ключевых кадров мало — берём точные
            if (frames.size < MIN_ACCEPTABLE_FRAMES) {
                Log.d(TAG, "Only ${frames.size} sync frames, retrying with OPTION_CLOSEST")
                frames.forEach { File(Uri.parse(it).path!!).delete() }
                frames.clear()
                useClosest = true

                for (i in 0 until MAX_FRAMES) {
                    val timeUs = i * intervalMs * 1000
        val bitmap = retriever.getFrameAtTime(
                        timeUs,
                        MediaMetadataRetriever.OPTION_CLOSEST
                    ) ?: continue
                    val file = File(cacheDir, "frame_$i.jpg")
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    bitmap.recycle()
                    frames.add(Uri.fromFile(file).toString())
                    onProgress?.invoke(frames.size, MAX_FRAMES)
                }
            }

            if (frames.isEmpty()) return null
            Log.d(TAG, "Extracted ${frames.size} frames from ${durationSec}s video")
            Result(frames, durationSec, frames.size)
        } catch (e: Exception) {
            Log.e(TAG, "Frame extraction failed: ${e.message}", e)
            null
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    fun cleanup(context: Context) {
        try {
            File(context.cacheDir, "video_frames").listFiles()?.forEach { it.delete() }
        } catch (_: Exception) {}
    }
}
