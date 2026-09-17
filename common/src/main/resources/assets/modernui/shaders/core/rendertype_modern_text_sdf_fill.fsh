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

void main() {
    // must be BILINEAR sampling
    vec4 texColor = textureLod(Sampler0, texCoord0, 0.0);

    // apply distance field
    float dist = texColor.a - 127./255. + 0.04;

    /*vec2 grad = vec2(dFdx(dist), dFdy(dist));
    float afwidth = 0.7 * length(grad);*/ // L2 norm (exact)

    // Minecraft uses non-premultiplied alpha blending
    texColor.a = clamp(dist / fwidth(dist) + 0.5, 0.0, 1.0);

    vec4 color = texColor * vertexColor * ColorModulator;
    if (color.a < 0.01) discard; // requires alpha test
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
#else
    fragColor = color;
#endif
}
