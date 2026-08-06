package com.example.data.preferences

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class DecoderMode(val displayName: String, val description: String) {
    HARDWARE("Hardware Accelerated (GPU)", "Uses GPU hardware decoders for maximum performance & battery efficiency"),
    SOFTWARE("Software Decoding (CPU)", "Uses CPU software decoders to play legacy or non-standard video formats"),
    AUTO("Auto Fallback (Recommended)", "Attempts hardware decoding first, automatically falling back to software if needed")
}

@OptIn(UnstableApi::class)
class VideoSettingsManager(private val context: Context) {

    private val preferencesManager = PreferencesManager(context)

    val decoderModeFlow: Flow<DecoderMode> = preferencesManager.userPreferencesFlow.map { it.decoderMode }

    suspend fun setDecoderMode(mode: DecoderMode) {
        preferencesManager.updateDecoderMode(mode)
    }

    fun applyDecoderConfig(renderersFactory: DefaultRenderersFactory, mode: DecoderMode) {
        when (mode) {
            DecoderMode.HARDWARE -> {
                renderersFactory.setEnableDecoderFallback(false)
                renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            }
            DecoderMode.SOFTWARE -> {
                renderersFactory.setEnableDecoderFallback(true)
                renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            }
            DecoderMode.AUTO -> {
                renderersFactory.setEnableDecoderFallback(true)
                renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            }
        }
    }
}
