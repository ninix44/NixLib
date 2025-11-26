#version 150

uniform float uTime;
uniform vec2 uResolution;
uniform vec2 uMouse;

in vec2 texCoord;
out vec4 fragColor;

vec2 cSq(vec2 z) {
    return vec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y);
}

void main() {
    vec2 uv = texCoord;

    vec2 p = (uv - 0.5) * 2.0;

    if (uResolution.y > 0.0) {
        p.x *= uResolution.x / uResolution.y;
    }

    float zoom = 1.8 + 0.4 * sin(uTime * 0.2);
    vec2 z = p * zoom;

    vec2 c;

    if (length(uMouse) < 10.0) {
        float t = uTime * 0.5;
        c = vec2(
            0.7885 * cos(t),
            0.7885 * sin(t * 1.4)
        );
        c += vec2(0.05 * sin(t * 3.0), 0.05 * cos(t * 3.0));
    } else {
        vec2 m = (uMouse / uResolution - 0.5) * 2.0;
        if (uResolution.y > 0.0) m.x *= uResolution.x / uResolution.y;
        c = m * 2.0;
        c.y = -c.y;
    }

    float i = 0.0;
    float maxIter = 128.0;
    float d = 0.0;

    for(i = 0.0; i < maxIter; i++) {
        z = cSq(z) + c;
        d = dot(z, z);
        if(d > 20.0) break;
    }

    vec3 col = vec3(1.0);

    if (i < maxIter) {

        float smoothVal = i - log2(log2(d)) + 4.0;
        smoothVal = smoothVal / maxIter;

        float intensity = smoothstep(0.0, 0.2, smoothVal);


        float shade = 1.0 - pow(i / maxIter, 0.5);
        col = vec3(shade);

    } else {
        col = vec3(0.0);
    }

    fragColor = vec4(col, 1.0);
}
