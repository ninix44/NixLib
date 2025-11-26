#version 150

uniform float uTime;
uniform float uType;

in vec2 texCoord;
in vec3 vertexPos;
out vec4 fragColor;

void main() {
    vec3 baseColor = 0.5 + 0.5 * cos(uTime + vec3(0, 2, 4));

    if (uType < 0.5) {
        float alpha = 0.6;
        float edge = step(0.9, max(abs(texCoord.x - 0.5) * 2.0, abs(texCoord.y - 0.5) * 2.0));
        vec3 finalColor = mix(baseColor, vec3(1.0), edge * 0.7);
        fragColor = vec4(finalColor, alpha);
    }
    // glow
    else {
        float dist = distance(texCoord, vec2(0.5));

        float glow = 1.0 - (dist * 2.5);
        glow = clamp(glow, 0.0, 1.0);
        glow = pow(glow, 3.0);

        float brightness = 2.0; // standard brightness for the ball (uType = 1.0)

        if (uType > 1.5) {
            brightness = 2.0; // the more, the brighter
        }

        vec3 lightColor = baseColor * brightness;

        // fog density adjustment
        float alpha = clamp(glow * 1.5, 0.0, 1.0);
        //float alpha = glow * (0.6 + 0.1 * sin(uTime * 4.0)); -  translucent

        if (alpha < 0.005) discard;

        fragColor = vec4(lightColor, alpha);
    }
}
