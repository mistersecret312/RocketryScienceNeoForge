#version 150

in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;

uniform vec4 Color;
uniform float QuadSize;
uniform float Thickness;
uniform float DashSegments;

const float PI = 3.14159265359;

void main() {
    vec2 pixelPos = (texCoord0 - vec2(0.5)) * QuadSize;
    float dist = max(abs(pixelPos.x), abs(pixelPos.y));

    float targetRadius = (QuadSize - Thickness) / 2.0;
    float distToRing = abs(dist - targetRadius);
    float ringMask = 1.0 - smoothstep((Thickness / 2.0) - 0.5, (Thickness / 2.0) + 0.5, distToRing);

    if (ringMask <= 0.0) {
        discard;
    }

    float pixelAngle = atan(pixelPos.y, pixelPos.x);
    float dashSignal = sin(pixelAngle * DashSegments);

    if (dashSignal < 0.0) {
        discard;
    }

    fragColor = vertexColor * Color * vec4(1.0, 1.0, 1.0, ringMask);
}