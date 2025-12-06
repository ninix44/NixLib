package ru.ninix.nixlib.cutscene.math;

public class Interpolations {

    public static float linear(float t) {
        return t;
    }

    public static float easeInSine(float t) {
        return (float) (1 - Math.cos((t * Math.PI) / 2));
    }

    public static float easeOutSine(float t) {
        return (float) Math.sin((t * Math.PI) / 2);
    }

    public static float easeInOutSine(float t) {
        return (float) (-(Math.cos(Math.PI * t) - 1) / 2);
    }

    public static float easeInQuad(float t) {
        return t * t;
    }

    public static float easeOutQuad(float t) {
        return 1 - (1 - t) * (1 - t);
    }

    public static float easeInOutQuad(float t) {
        return t < 0.5 ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2;
    }

    public static float easeInCubic(float t) {
        return t * t * t;
    }

    public static float easeOutCubic(float t) {
        return 1 - (float) Math.pow(1 - t, 3);
    }

    public static float easeInOutCubic(float t) {
        return t < 0.5 ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }

    public static float easeInExpo(float t) {
        return t == 0 ? 0 : (float) Math.pow(2, 10 * t - 10);
    }

    public static float easeOutExpo(float t) {
        return t == 1 ? 1 : 1 - (float) Math.pow(2, -10 * t);
    }

    public static float easeInOutExpo(float t) {

        if (t == 0) return 0;
        if (t == 1) return 1;
        if (t < 0.5) return (float) Math.pow(2, 20 * t - 10) / 2;
        return (2 - (float) Math.pow(2, -20 * t + 10)) / 2;
    }

    public static float easeInBack(float t) {
        final float c1 = 1.70158f;
        final float c3 = c1 + 1;
        return c3 * t * t * t - c1 * t * t;
    }

    public static float easeOutBack(float t) {
        final float c1 = 1.70158f;
        final float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    public static float easeInOutBack(float t) {
        final float c1 = 1.70158f;
        final float c2 = c1 * 1.525f;
        if (t < 0.5) return ((float) Math.pow(2 * t, 2) * ((c2 + 1) * 2 * t - c2)) / 2;
        return ((float) Math.pow(2 * t - 2, 2) * ((c2 + 1) * (t * 2 - 2) + c2) + 2) / 2;
    }
}
