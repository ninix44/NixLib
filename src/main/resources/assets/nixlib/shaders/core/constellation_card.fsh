#version 150

uniform float uTime;
uniform vec2 MousePos;
uniform float HasFocus;
uniform float StarCoords[10];
uniform float GameProgress;

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

float fbm(vec2 st) {
    float v = 0.0;
    float a = 0.5;
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));
    for (int i = 0; i < 5; i++) {
        v += a * noise(st);
        st = rot * st * 2.0 + vec2(uTime * 0.1);
        a *= 0.5;
    }
    return v;
}

float star(vec2 uv, float size) {
    float d = length(uv);
    float m = 0.02 / d;
    m *= smoothstep(size, 0.0, d);
    m += smoothstep(0.02, 0.0, abs(uv.x)) * smoothstep(size, 0.0, abs(uv.y)) * 0.8;
    m += smoothstep(0.02, 0.0, abs(uv.y)) * smoothstep(size, 0.0, abs(uv.x)) * 0.8;
    return m;
}

float distToLine(vec2 p, vec2 a, vec2 b) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
    return length(pa - ba * h);
}

void main() {
    vec2 uv = texCoord;
    int progress = int(GameProgress);

    vec3 bgColor = vec3(0.05, 0.0, 0.15);

    vec2 activePos = MousePos;
    if (progress > 0 && progress < 5) {
        activePos = vec2(StarCoords[(progress-1)*2], StarCoords[(progress-1)*2+1]);
    }
    float distToActive = length(uv - activePos);
    float resonance = smoothstep(0.4, 0.0, distToActive) * HasFocus;

    float nebula = fbm(uv * 3.0 + vec2(0.0, uTime * 0.05));
    vec3 nebulaColor = mix(vec3(0.2, 0.0, 0.4), vec3(0.8, 0.2, 0.5), resonance);
    bgColor += nebulaColor * nebula * 0.6;

    vec3 lineColor = vec3(1.0, 0.8, 0.2);

    for (int i = 0; i < 4; i++) {
        if (i < progress - 1) {
            vec2 p1 = vec2(StarCoords[i*2], StarCoords[i*2+1]);
            vec2 p2 = vec2(StarCoords[(i+1)*2], StarCoords[(i+1)*2+1]);

            float d = distToLine(uv, p1, p2);
            float lineIntensity = smoothstep(0.008, 0.0, d);
            lineIntensity *= (0.8 + 0.2 * noise(uv * 20.0 + uTime * 2.0));
            bgColor += lineColor * lineIntensity;
        }
    }

    if (progress > 0 && progress < 5 && HasFocus > 0.5) {
        vec2 lastStar = vec2(StarCoords[(progress-1)*2], StarCoords[(progress-1)*2+1]);

        float d = distToLine(uv, lastStar, MousePos);
        float lineIntensity = smoothstep(0.015, 0.0, d);

        lineIntensity *= (0.5 + 0.5 * sin(uTime * 10.0));

        bgColor += vec3(0.5, 0.8, 1.0) * lineIntensity;
    }

    for (int i = 0; i < 5; i++) {
        vec2 starPos = vec2(StarCoords[i*2], StarCoords[i*2+1]);
        float dist = length(uv - starPos);

        bool isConnected = (i < progress);
        bool isNextTarget = (i == progress);

        float starSize = 0.05;
        vec3 starCol = vec3(0.0);

        if (isConnected) {
            starCol = vec3(1.0, 0.9, 0.2);
            bgColor += star(uv - starPos, 0.15) * starCol * 1.5;
        } else if (isNextTarget) {
            float distToCursor = length(starPos - MousePos);
            float visibility = smoothstep(0.3, 0.05, distToCursor) * HasFocus;

            starCol = vec3(1.0, 1.0, 1.0);
            bgColor += star(uv - starPos, 0.2) * starCol * visibility * (1.0 + 0.5*sin(uTime * 5.0));
        } else {
             bgColor += smoothstep(0.01, 0.0, dist) * vec3(0.3);
        }
    }

    float distToMouse = length(uv - MousePos);
    float mouseGlow = (0.015 / (distToMouse + 0.01)) * HasFocus;
    bgColor += vec3(0.5, 0.2, 1.0) * mouseGlow * 0.5;

    vec2 d = abs(uv - 0.5) - 0.45;
    float borderDist = length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
    float mask = smoothstep(0.01, 0.0, borderDist);
    float borderGlow = smoothstep(0.02, 0.0, abs(borderDist));
    if (progress == 5) {
        bgColor += vec3(0.0, 1.0, 0.0) * borderGlow;
    } else {
        bgColor += vec3(1.0, 0.6, 0.0) * borderGlow * HasFocus;
    }

    fragColor = vec4(bgColor, mask);
}
