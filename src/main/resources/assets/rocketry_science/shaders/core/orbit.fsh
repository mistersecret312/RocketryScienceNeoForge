#version 150

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

uniform vec4 Color;
uniform float InnerRadius;
uniform float OuterRadius;
uniform float QuadSize;

void main() {
    vec2 uv = texCoord0 - vec2(0.5);
    vec2 pixelated = floor(uv* QuadSize) / QuadSize;

    float dist = length(uv);

    float edgeSmoothness = 0.005;
    float alphaInner = step(InnerRadius, dist);
    float alphaOuter = 1.0 - step(OuterRadius, dist);

    float ringMask = alphaInner * alphaOuter;

    if (ringMask <= 0.0) {
        discard;
    }

    fragColor = vertexColor * Color * vec4(1.0, 1.0, 1.0, ringMask);
}