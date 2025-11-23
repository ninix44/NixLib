#version 150

uniform float uTime;
uniform vec2 MousePos;

in vec2 texCoord;
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
    vec2 st = texCoord;

    vec2 toMouse = st - MousePos;
    float dist = length(toMouse);
    float mouseRadius = 0.4;

    if (dist < mouseRadius) {
        float pull = smoothstep(mouseRadius, 0.0, dist);
        st -= toMouse * pull * 0.4;
    }

    vec3 color = vec3(0.0, 0.0, 0.05);

    float n1 = noise(st * 3.0 + uTime * 0.05);
    float n2 = noise(st * 6.0 - uTime * 0.03);

    float cloud1 = smoothstep(0.4, 0.8, n1);
    float cloud2 = smoothstep(0.4, 0.8, n2);

    color += vec3(0.4, 0.0, 0.6) * cloud1 * 0.8;
    color += vec3(0.0, 0.3, 0.8) * cloud2 * 0.6;


    float vig = st.x * (1.0 - st.x) * st.y * (1.0 - st.y) * 50.0;
    color *= clamp(pow(vig, 0.3), 0.0, 1.0);

    fragColor = vec4(color, 1.0);
}
