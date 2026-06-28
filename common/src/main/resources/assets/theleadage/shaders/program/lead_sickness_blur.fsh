#version 150

uniform sampler2D DiffuseSampler;

uniform vec2 InSize;
uniform vec2 BlurDir;
uniform float Radius;
uniform float Intensity;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    // Edge weight: ~0 in the centre, ramping to 1 toward the edges, so the blur grows from the
    // sides inward (reaching well toward the centre). The loop count stays uniform (driven by
    // Radius); the per-fragment edge factor scales the sample spread, leaving the very centre sharp.
    float dist = clamp(length(texCoord - vec2(0.5)) * 1.41421356, 0.0, 1.0);
    float edge = smoothstep(0.08, 0.8, dist);

    vec3 color;
    if (Radius < 0.5 || edge <= 0.0) {
        color = texture(DiffuseSampler, texCoord).rgb;
    } else {
        vec4 sum = vec4(0.0);
        float total = 0.0;
        for (float o = -Radius; o <= Radius; o += 1.0) {
            float w = 1.0 - abs(o / Radius);
            vec2 off = BlurDir / InSize * o * edge;
            sum += texture(DiffuseSampler, texCoord + off) * w;
            total += w;
        }
        color = sum.rgb / total;
    }

    // Slight toxic-green edge vignette, applied once (only on the final vertical pass) so it isn't
    // doubled — makes the effect read more clearly without darkening the view.
    if (BlurDir.y > 0.5) {
        // Vignette strength follows the effect intensity (fade × sickness level): it eases in/out and
        // grows with the stack.
        float vignette = smoothstep(0.5, 1.0, dist) * Intensity;
        color = mix(color, vec3(0.333, 0.761, 0.196), vignette * 0.22);
    }

    fragColor = vec4(color, 1.0);
}
