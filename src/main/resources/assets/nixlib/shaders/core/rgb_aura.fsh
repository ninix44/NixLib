#version 150

uniform float uTime;

in vec2 texCoord;
in vec3 vertexPos;
out vec4 fragColor;

void main() {
    vec3 col = 0.5 + 0.5 * cos(uTime + vertexPos.xyx + vec3(0,2,4));

    float alpha = 0.4 + 0.2 * sin(uTime * 2.0);

    float edge = step(0.95, max(abs(texCoord.x - 0.5) * 2.0, abs(texCoord.y - 0.5) * 2.0));

    col += vec3(edge) * 0.5;

    fragColor = vec4(col, alpha);
}
