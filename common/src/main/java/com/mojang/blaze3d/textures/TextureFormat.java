/*
 * Compatibility stub for MC 26.2.
 * TextureFormat was moved/removed in the new GpuTexture API.
 * Kept as an opaque enum so ModernUI's texture wrapper compiles.
 */
package com.mojang.blaze3d.textures;

public enum TextureFormat {
    RGBA8,
    RED8,
    RGBA16F,
    DEPTH,
    DEPTH_STENCIL
}
