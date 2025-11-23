#version 150

uniform float uTime;
uniform vec2 ScreenSize;
uniform sampler2D Sampler0;

in vec2 texCoord;
in vec4 vertexColor;
in vec2 vPos;

out vec4 fragColor;

float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898,78.233))) * 43758.5453123);
}

float noise(vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a)* u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

void main() {
    vec2 st = gl_FragCoord.xy / ScreenSize.xy;

    vec3 color = vec3(0.05, 0.0, 0.1);

    float n = noise(st * 3.0 + uTime * 0.1);
    color += vec3(0.4, 0.0, 0.8) * n * 0.5;

    float n2 = noise(st * 6.0 - uTime * 0.05);
    color += vec3(0.0, 0.2, 0.6) * n2 * 0.3;

    float stars = random(st + uTime * 0.0001);
    if (stars > 0.985) {
        float intensity = (stars - 0.985) * 80.0;
        color += vec3(0.8, 0.9, 1.0) * intensity;
    }

    float vignette = 1.0 - length(st - 0.5) * 1.2;
    color *= clamp(vignette, 0.0, 1.0);

    fragColor = vec4(color, 1.0);
}
