#version 150

in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;

uniform vec4 Color;
uniform float QuadSize;
uniform float Thickness;
uniform float Angle;
uniform float Direction;

const float PI = 3.14159265359;

void main() {
    vec2 pixelPos = (texCoord0 - 0.5) * QuadSize;
    float targetRadius = (QuadSize - Thickness) / 2.0;

    vec2 d = abs(pixelPos) - vec2(targetRadius);
    float distToBoundary = max(d.x, d.y);
    float distToRing = abs(distToBoundary);

    float ringMask = 1.0 - smoothstep((Thickness / 2.0) - 0.5, (Thickness / 2.0) + 0.5, distToRing);
    if (ringMask <= 0.0) {
        discard;
    }

    float pixelAngle = atan(pixelPos.y, pixelPos.x);

    float normalizedPixel = mod(pixelAngle + 2.0 * PI, 2.0 * PI);
    float normalizedTarget = mod(Angle, 2.0 * PI);

    float rawDiff = normalizedTarget - normalizedPixel;
    float angleDiff = mod((rawDiff * Direction) + 2.0 * PI, 2.0 * PI);

    float fadeProgress = 1.0 - (angleDiff / (2.0 * PI));

    float minAlpha = 0.15;
    float tailAlphaMultiplier = mix(minAlpha, 1.0, fadeProgress);

    fragColor = vertexColor * Color * vec4(1.0, 1.0, 1.0, ringMask * tailAlphaMultiplier);
}