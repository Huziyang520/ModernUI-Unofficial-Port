#version 330
#extension GL_ARB_separate_shader_objects : require
// This file is part of Modern UI.
// Copyright (C) 2024 BloCamLimb.
// Licensed under LGPL-3.0-or-later.

// MC 26.3: renderpearl compiles GLSL to SPIR-V via shaderc. The custom
// import directive is '#include' (was '#moj_import'), and the separate
// shader objects model requires explicit locations on every user
// input/output, matching the locations emitted by minecraft:core/text.vsh.

// minecraft:core/text.vsh only emits fog varyings when neither IS_GUI
// nor IS_SEE_THROUGH is defined, so guard the fog path the same way.
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
#include <minecraft:fog.glsl>
#endif
#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
layout(location = 0) in float sphericalVertexDistance;
layout(location = 1) in float cylindricalVertexDistance;
#endif
layout(location = 2) in vec4 vertexColor;
layout(location = 3) in vec2 texCoord0;

layout(location = 0) out vec4 fragColor;

// why not try gaussian filter?
void main() {
    vec2 texSize = vec2(textureSize(Sampler0, 0));
    float dsum = 0.0;
    float wsum = 0.0;
    const int nstep = 3;
    const float w[3] = float[3](1.0,2.0,1.0);
    for (int i=0; i<nstep; ++i) {
        for (int j=0; j<nstep; ++j) {
            vec2 delta = vec2(float(i-1), float(j-1))/texSize;
            float wij = w[i]*w[j];
            vec4 samp = textureLod(Sampler0,texCoord0-delta,0.0);
            float dist = samp.w - 127./255.;
            dsum += wij * dist;
            wsum += wij;
        }
    }
    float dist = dsum / wsum;
    dist = abs(dist + 0.15) - 0.2;
    vec4 color = vertexColor * ColorModulator;
    color.a *= 1.0 - clamp(dist / fwidth(dist) + 0.5, 0.0, 1.0);
    if (color.a < 0.01) discard;
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
#else
    fragColor = color;
#endif
}
