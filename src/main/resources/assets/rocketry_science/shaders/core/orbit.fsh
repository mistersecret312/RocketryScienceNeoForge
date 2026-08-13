#version 150

in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;

uniform vec4 Color;
uniform float InnerRadius;
uniform float OuterRadius;
uniform float QuadSize;
uniform float SquishFactor;
uniform float DashSegments;

void main() {
    vec2 uv = texCoord0 - vec2(0.5);

    vec2 pixelated = (floor(uv * QuadSize) + 0.5) / QuadSize;

    vec2 orbitPos = pixelated;
    orbitPos.y *= SquishFactor;

    float dist = length(orbitPos);

    float midRadius = (InnerRadius + OuterRadius) / 2.0;
    float halfThickness = (OuterRadius - InnerRadius) / 2.0;

    halfThickness = max(halfThickness, 1.0 / QuadSize);

    float distToRing = abs(dist - midRadius);
    float ringMask = 1.0 - step(halfThickness, distToRing);

    if (DashSegments > 0.0) {
        float angle = atan(pixelated.y, pixelated.x);
        float dashMask = step(0.0, sin(angle * DashSegments));
        ringMask *= dashMask;
    }

    if (ringMask <= 0.0) {
        discard;
    }

    fragColor = vertexColor * Color * vec4(1.0, 1.0, 1.0, ringMask);
}