package com.example.playsymphonyapp.Editor

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal suspend fun ffmpeg(arguments: List<String>) {
    val result = suspendCancellableCoroutine<FFmpegSession> { continuation ->
        val session = FFmpegKit.executeWithArgumentsAsync(
            arguments.toTypedArray(),
            { finished ->
                if (continuation.isActive) {
                    continuation.resume(finished)
                }
            }
        )

        continuation.invokeOnCancellation {
            FFmpegKit.cancel(session.sessionId)
        }
    }

    check(ReturnCode.isSuccess(result.returnCode)) {
        "Audio conversion failed: ${result.returnCode}"
    }
}