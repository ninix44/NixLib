#version 150

uniform sampler2D DiffuseSampler;

uniform float uTime;

uniform float Strength;
uniform float Swirl;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 center = vec2(0.5);
    vec2 toCenter = center - texCoord;
    float dist = length(toCenter);

    float distortion = pow(1.0 - dist, 2.0) * Strength;

    float angle = dist * Swirl + uTime;

    float c = cos(angle);
    float s = sin(angle);
    mat2 rotationMatrix = mat2(c, -s, s, c);

    vec2 rotatedOffset = rotationMatrix * toCenter;
    vec2 distortedCoord = texCoord + (rotatedOffset - toCenter) * distortion;

    if (distortedCoord.x < 0.0 || distortedCoord.x > 1.0 || distortedCoord.y < 0.0 || distortedCoord.y > 1.0) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    } else {
        fragColor = texture(DiffuseSampler, distortedCoord);
    }
}
