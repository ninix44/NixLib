#version 150

uniform float uTime;
uniform float uType;

uniform float uBlue;
uniform float uSpeed;

in vec2 texCoord;
in vec3 vertexPos;
out vec4 fragColor;

void main() {

    vec3 orangeColor = vec3(1.0, 0.5, 0.0);

    if (uType < 0.5) {

        vec3 glassColor = vec3(0.05, 0.05, 0.05);

        fragColor = vec4(glassColor, 0.3);
    }

    else {
        float dist = distance(texCoord, vec2(0.5));

        float glow = 1.0 - (dist * 2.0);
        glow = clamp(glow, 0.0, 1.0);

        glow = pow(glow, 2.5);

        float alpha = glow;

        fragColor = vec4(orangeColor, alpha * 1.5);
    }
}
