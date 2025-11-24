#version 150

uniform float uTime;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 col = 0.5 + 0.5 * cos(uTime + texCoord.xyx + vec3(0,2,4));
    fragColor = vec4(col, 1.0);
}
