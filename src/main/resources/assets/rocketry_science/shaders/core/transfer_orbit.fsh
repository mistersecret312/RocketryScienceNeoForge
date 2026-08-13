#version 150

in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;

uniform vec4 Color;
uniform float QuadSize;
uniform float R1;
uniform float R2;
uniform float Rotation;
uniform float Thickness;
uniform float SquishFactor;
uniform float DashSegments;
uniform float Progress;

void main() {
    vec2 uv = texCoord0 - vec2(0.5);

    vec2 pixelated = (floor(uv * QuadSize) + 0.5) / QuadSize;
    vec2 topDown = pixelated;
    topDown.y /= SquishFactor;

    float cosA = cos(-Rotation);
    float sinA = sin(-Rotation);
    vec2 rotated = vec2(
    topDown.x * cosA - topDown.y * sinA,
    topDown.x * sinA + topDown.y * cosA
    );

    float c = (R1 - R2) / 2.0;
    float a = (R1 + R2) / 2.0;
    float b = sqrt(R1 * R2);

    vec2 ellipseUV = rotated - vec2(c, 0.0);

    float x = ellipseUV.x;
    float y = ellipseUV.y;
    float f = (x*x)/(a*a) + (y*y)/(b*b) - 1.0;
    float dfdx = (2.0 * x) / (a*a);
    float dfdy = (2.0 * y) / (b*b);

    float gradLen = max(sqrt(dfdx*dfdx + dfdy*dfdy), 0.0001);
    float distToEllipse = abs(f) / gradLen;

    float halfThickness = max(Thickness / 2.0, 1.0 / QuadSize);
    float ringMask = 1.0 - step(halfThickness, distToEllipse);

    float halfMask = step(0.0, rotated.y);
    ringMask *= halfMask;

    vec2 normalizedUV = ellipseUV / vec2(a, b);
    float pathAngle = atan(normalizedUV.y, normalizedUV.x);
    float progressAngle = Progress * 3.1415926535;

    float pathOpacity = 1.0;

    if (pathAngle > progressAngle) {
        pathOpacity = 0.4;

        float plannedDashMask = step(0.0, sin(pathAngle * 60.0));
        ringMask *= plannedDashMask;
    }

    if (DashSegments > 0.0) {
        float dashMask = step(0.0, sin(pathAngle * DashSegments));
        ringMask *= dashMask;
    }

    if (ringMask <= 0.0) {
        discard;
    }

    fragColor = vertexColor * Color * vec4(1.0, 1.0, 1.0, ringMask * pathOpacity);
}