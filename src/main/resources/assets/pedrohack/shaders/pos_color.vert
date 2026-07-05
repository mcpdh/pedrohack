#version 330 core

layout(location = 0) in vec4 pos;
layout(location = 1) in vec4 color;

layout(std140) uniform Projection {
    mat4 u_Proj;
};

layout(std140) uniform DynamicTransforms {
    mat4 u_ModelView;
    vec4 u_ColorModulator;
    vec3 u_ModelOffset;
    mat4 u_TextureMatrix;
};

out vec4 v_Color;

void main() {
    gl_Position = u_Proj * u_ModelView * pos;
    v_Color = color * u_ColorModulator;
}