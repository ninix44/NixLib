#version 150

uniform float uTime;
uniform float uType;

in vec2 texCoord;
in vec3 vertexPos;
out vec4 fragColor;

void main() {

    if (uType < 0.5) {
        // black with full opacity
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);

        // a little bit purple:
        // fragColor = vec4(0.05, 0.0, 0.05, 1.0);
    }

    else {
        vec2 uv = texCoord - 0.5;

        float len = length(uv);
        float angle = atan(uv.y, uv.x);

        float spiral = sin(angle * 5.0 + len * 20.0 - uTime * 3.0);

        vec3 color = vec3(0.1, 0.0, 0.15);

        color += vec3(0.6, 0.2, 0.9) * smoothstep(0.2, 0.8, spiral);
        color += vec3(0.2, 0.8, 1.0) * smoothstep(0.8, 1.0, spiral) * 0.5;

        float circle = 1.0 - smoothstep(0.35, 0.5, len);

        float pulse = 1.0 + 0.1 * sin(uTime * 2.0);
        circle *= pulse;

        float alpha = circle;

        float core = 1.0 - smoothstep(0.0, 0.15, len);
        color += vec3(1.0) * core;

        if (alpha < 0.01) discard;

        fragColor = vec4(color, alpha);
    }
}
