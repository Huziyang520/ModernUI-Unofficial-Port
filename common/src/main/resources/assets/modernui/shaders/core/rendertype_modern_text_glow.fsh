#version 330
#extension GL_ARB_separate_shader_objects : require
// This file is part of Modern UI.
// Copyright (C) 2024 BloCamLimb.
// Licensed under LGPL-3.0-or-later.

// MC 26.3: '#moj_import' -> '#include'; explicit locations are required
// for SPIR-V user input/output. Legacy glow pass, world-text interface.
#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in float sphericalVertexDistance;
layout(location = 1) in float cylindricalVertexDistance;
layout(location = 2) in vec4 vertexColor;
layout(location = 3) in vec2 texCoord0;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 color = vec4(0.0);

    vec2 oneTexel = 1.0 / textureSize(Sampler0, 0);
    color += textureLod(Sampler0, texCoord0 - oneTexel, 1.0) * 0.13533528323; // e^(-2)
    color += textureLod(Sampler0, texCoord0 + oneTexel * vec2(0.0, -1.0), 1.0) * 0.36787944117; // e^(-1)
    color += textureLod(Sampler0, texCoord0 + oneTexel * vec2(1.0, -1.0), 1.0) * 0.13533528323; // e^(-2)
    color += textureLod(Sampler0, texCoord0 + oneTexel * vec2(-1.0, 0.0), 1.0) * 0.36787944117; // e^(-1)
    color += textureLod(Sampler0, texCoord0, 1.0);
    color += textureLod(Sampler0, texCoord0 + oneTexel * vec2(1.0, 0.0),  1.0) * 0.36787944117; // e^(-1)
    color += textureLod(Sampler0, texCoord0 + oneTexel * vec2(-1.0, 1.0), 1.0) * 0.13533528323; // e^(-2)
    color += textureLod(Sampler0, texCoord0 + oneTexel * vec2(0.0, 1.0),  1.0) * 0.36787944117; // e^(-1)
    color += textureLod(Sampler0, texCoord0 + oneTexel, 1.0) * 0.13533528323; // e^(-2)
    color /= 3.0128588976; // 4 * (e^(-1) + e^(-2)) + 1

    color = color * color * (3.0 - 2.0 * color); // smoothstep
    color *= vertexColor * ColorModulator; // multiply
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
