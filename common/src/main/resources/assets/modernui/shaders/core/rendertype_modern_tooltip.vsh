#version 330
#extension GL_ARB_separate_shader_objects : require
// This file is part of Modern UI.
// Copyright (C) 2024 BloCamLimb.
// Licensed under LGPL-3.0-or-later.

// MC 26.3: '#moj_import' -> '#include'; explicit locations are required
// for SPIR-V user input/output (POSITION_COLOR: Position=0, Color=1).
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

// MC 26.3 DynamicTransforms layout: modelView(0), textureMatrix(64),
// colorModulator(128), modelOffset(144). TooltipRenderer.writeTransform maps:
//   localMat -> modelView(0), colorMatrix -> textureMatrix(64, 4 columns),
//   pushData0 -> colorModulator(128), pushData1 -> modelOffset(144).
layout(std140) uniform ModernTooltip {
    mat4 u_LocalMat;    // offset 0
    mat4 u_ColorMatrix; // offset 64: border gradient colors (columns 0-3)
    vec4 u_PushData0;   // offset 128: xy=size, z=corner radius, w=half border width
    vec3 u_PushData1;   // offset 144: x=shadow alpha, y=shadow spread, z=background alpha
};

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;

layout(location = 0) out vec2 f_Position;

void main() {
    f_Position = Position.xy;
    // localMat is 2D affine, z/w is ignored
    vec4 localPos = u_LocalMat * vec4(Position, 1.0);

    gl_Position = ProjMat * ModelViewMat * vec4(localPos.xy, Position.z, 1.0);
}
