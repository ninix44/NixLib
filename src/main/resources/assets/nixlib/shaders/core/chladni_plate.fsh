#version 150

uniform float uTime;
uniform vec2 MousePos;
uniform float Resolution;

in vec2 texCoord;
out vec4 fragColor;

float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898,78.233))) * 43758.5453123);
}

void main() {
    vec2 uv = texCoord;

    float n = 1.0 + MousePos.x * 20.0;
    float m = 1.0 + MousePos.y * 20.0;

    vec2 p = uv * 2.0 - 1.0;

    float pi = 3.14159265;
    float L = 1.0;

    // cos((n*pi*x)/L) * cos((m*pi*y)/L) - cos((m*pi*x)/L) * cos((n*pi*y)/L)
    float term1 = cos((n * pi * p.x) / L) * cos((m * pi * p.y) / L);
    float term2 = cos((m * pi * p.x) / L) * cos((n * pi * p.y) / L);

    float wave = term1 - term2;
    float vibration = abs(wave);

    vec3 basePlate = vec3(0.05, 0.05, 0.08);

    float dist = length(p);
    basePlate += vec3(0.1) * (1.0 - dist * 0.5);

    basePlate += vec3(0.02) * smoothstep(0.0, 0.5, vibration) * sin(uTime * 50.0);

    float nodeWidth = 0.15;

    float sandDensity = 1.0 - smoothstep(0.0, nodeWidth, vibration);

    float staticGrain = random(uv * 120.0);
    float dynamicGrain = random(uv * 120.0 + vec2(uTime * 0.5, uTime * 0.5));

    float finalSand = 0.0;

    if (sandDensity > 0.0) {
        if (staticGrain < sandDensity * 0.8) {
             finalSand = 1.0;
        }
    }

    if (vibration > nodeWidth) {
        if (dynamicGrain > 0.992) {
            finalSand = 0.6;
        }
    }

    vec3 sandColor = vec3(0.95, 0.85, 0.5);
    vec3 sandShadow = vec3(0.6, 0.5, 0.3);

    vec3 col = basePlate;
    if (finalSand > 0.0) {
        vec3 grainCol = mix(sandShadow, sandColor, staticGrain);
        if (finalSand < 1.0) grainCol = vec3(1.0);

        col = mix(col, grainCol, finalSand);
    }

    float borderDist = max(abs(p.x), abs(p.y));
    float borderMask = smoothstep(0.97, 0.98, borderDist);
    vec3 borderColor = vec3(0.3, 0.3, 0.35);

    fragColor = vec4(mix(col, borderColor, borderMask), 1.0);
}
