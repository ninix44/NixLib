#version 150

uniform vec2 uResolution;
uniform vec2 uOffset;
uniform float uZoom;

in vec2 texCoord;
out vec4 fragColor;

vec3 palette(float t) {
    vec3 a = vec3(0.5, 0.5, 0.5);
    vec3 b = vec3(0.5, 0.5, 0.5);
    vec3 c = vec3(1.0, 1.0, 1.0);
    vec3 d = vec3(0.30, 0.20, 0.20);
    return a + b * cos(6.28318 * (c * t + d));
}

void main() {
    vec2 uv = texCoord - 0.5;

    if (uResolution.y > 0.0) {
        uv.x *= uResolution.x / uResolution.y;
    }

    vec2 c = uOffset + vec2(uv.x, -uv.y) * uZoom;

    vec2 z = vec2(0.0);

    float iter = 0.0;
    float maxIter = 100.0 + 50.0 * log(1.0 / (uZoom + 0.0000001));
    if (maxIter > 500.0) maxIter = 500.0;

    float d = 0.0;

    for (iter = 0.0; iter < maxIter; iter++) {
        z = vec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + c;
        d = dot(z, z);
        if (d > 40.0) break;
    }

    vec3 col = vec3(0.0);

    if (iter < maxIter) {
        float log_zn = log(d) / 2.0;
        float nu = log(log_zn / log(2.0)) / log(2.0);
        float t = iter + 1.0 - nu;

        t = t * 0.02;
        col = palette(t);

        float edge = smoothstep(0.0, 1.0, iter / 50.0);
        col = mix(vec3(1.0), col, edge);
    }

    fragColor = vec4(col, 1.0);
}
