#version 150

in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;

uniform vec2 QuadSize;
uniform vec2 StartSize;
uniform vec2 EndSize;
uniform float StartAngle;
uniform float EndAngle;
uniform float CurrentAngle;
uniform float Direction;
uniform float Thickness;
uniform vec4 Color;
uniform vec4 PassedColor;

const float PI = 3.14159265359;

float getSweep(float from, float to, float dir) {
    if (dir > 0.0) {
        return mod((to - from) + 2.0 * PI, 2.0 * PI);
    } else {
        return mod((from - to) + 2.0 * PI, 2.0 * PI);
    }
}

void main() {
    vec2 pixelPos = (texCoord0 - 0.5) * QuadSize;
    float pixelAngle = atan(pixelPos.y, pixelPos.x);

    float totalSweep = getSweep(StartAngle, EndAngle, Direction);
    float pixelSweep = getSweep(StartAngle, pixelAngle, Direction);

    if (pixelSweep > totalSweep + 0.01) {
        discard;
    }

    float progress = pixelSweep / totalSweep;
    vec2 currentTargetSize = mix(StartSize, EndSize, progress);
    vec2 targetExtents = (currentTargetSize - Thickness) / 2.0;

    vec2 d = abs(pixelPos) - targetExtents;
    float distToBoundary = max(d.x, d.y);
    float distToRing = abs(distToBoundary);

    float ringMask = 1.0 - smoothstep((Thickness / 2.0) - 0.5, (Thickness / 2.0) + 0.5, distToRing);

    if (ringMask <= 0.0) {
        discard;
    }

    float currentSweep = getSweep(StartAngle, CurrentAngle, Direction);

    vec4 finalColor;
    float tailAlphaMultiplier = 1.0;

    if (pixelSweep <= currentSweep) {
        finalColor = PassedColor;

        float angleBehind = currentSweep - pixelSweep;
        float fadeProgress = 1.0 - (angleBehind / (2.0 * PI));

        float minAlpha = 0.15;
        tailAlphaMultiplier = mix(minAlpha, 1.0, clamp(fadeProgress, 0.0, 1.0));
    }
    else {
        finalColor = Color;
        tailAlphaMultiplier = 1.0;
    }

    fragColor = vertexColor * finalColor * vec4(1.0, 1.0, 1.0, ringMask * tailAlphaMultiplier);
}