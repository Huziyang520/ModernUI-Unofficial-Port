#version 330
// This file is part of Modern UI.
// Copyright (C) 2024 BloCamLimb.
// Licensed under LGPL-3.0-or-later.

// MC 26.2: minecraft:core/text.vsh only emits fog varyings when neither IS_GUI
// nor IS_SEE_THROUGH is defined, so guard the fog path the same way.
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
#moj_import <minecraft:fog.glsl>
#endif
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
in float sphericalVertexDistance;
in float cylindricalVertexDistance;
#endif
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    // lodBias should be -0.5/guiScale in Minecraft normalized GUI coordinates
    // lodBias guarantees NEAREST sampling (sharpen) in despite of float errors
    vec4 texColor = texture(Sampler0, texCoord0, -0.11875);
    vec4 color = texColor * vertexColor * ColorModulator;
    if (color.a < 0.01) discard;
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
#else
    fragColor = color;
#endif
}
