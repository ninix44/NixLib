package ru.ninix.nixlib.client.vfx;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NixVFXPresets {
    private static final Random random = new Random();

    public static void addVertex(BufferBuilder builder, Matrix4f matrix, float x, float y, float z, float r, float g, float b, float a) {
        Vector4f vector = new Vector4f(x, y, z, 1.0F);
        vector.mul(matrix);
        builder.addVertex(vector.x(), vector.y(), vector.z())
            .setColor((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255));
    }

    public static Color interpolateColor(Color c1, Color c2, float ratio) {
        int r = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int g = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int b = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(r, g, b);
    }


    public static final IVFXEffect GALAXY = (poseStack, bufferBuilder, progress, color1, color2) -> {
        Matrix4f matrix = poseStack.last().pose();
        float overallAlpha = (float) Math.sin(progress * Math.PI);
        float rotation = progress * 2.0f;
        float zoom = 0.8f;
        int numParticlesPerArm = 1500;
        float revolutions = 2.5f;

        for (int arm = 0; arm < 2; arm++) {
            Color color = (arm == 0) ? color1 : color2;
            float armOffset = arm * (float) Math.PI;

            for (int i = 1; i < numParticlesPerArm; i++) {
                float t = (float) i / numParticlesPerArm;
                float angle = t * revolutions * 2.0f * (float) Math.PI;
                float radius = (float) Math.exp(t * 1.2f) * zoom * 2.0f;
                float finalAngle = angle + rotation + armOffset;

                float x = radius * (float) Math.cos(finalAngle);
                float y = radius * (float) Math.sin(finalAngle);
                float size = (0.01f + t * 0.1f);
                float alpha = Math.min(t * t * overallAlpha, 1.0f);

                if (alpha > 0.01f) {
                    float r = color.getRed() / 255f; float g = color.getGreen() / 255f; float b = color.getBlue() / 255f;
                    addVertex(bufferBuilder, matrix, x - size, y - size, 0, r, g, b, alpha);
                    addVertex(bufferBuilder, matrix, x - size, y + size, 0, r, g, b, alpha);
                    addVertex(bufferBuilder, matrix, x + size, y + size, 0, r, g, b, alpha);
                    addVertex(bufferBuilder, matrix, x + size, y - size, 0, r, g, b, alpha);
                }
            }
        }
    };

    public static final IVFXEffect DNA_HELIX = (poseStack, bufferBuilder, progress, color1, color2) -> {
        Matrix4f matrix = poseStack.last().pose();
        float overallAlpha = (float) Math.sin(progress * Math.PI);
        int numParticles = 800;
        float radius = 1.5f;
        float height = 8.0f;

        for (int i = 0; i < numParticles; i++) {
            float t = (float) i / numParticles;
            float y = (t - 0.5f) * height * (1.0f + progress * 0.5f);
            float angle = t * 4.0f * 2.0f * (float) Math.PI + progress * 5.0f;
            float size = 0.08f;

            float x1 = radius * (float) Math.cos(angle);
            float z1 = radius * (float) Math.sin(angle);
            float r1 = color1.getRed() / 255f; float g1 = color1.getGreen() / 255f; float b1 = color1.getBlue() / 255f;
            addVertex(bufferBuilder, matrix, x1 - size, y, z1 - size, r1, g1, b1, overallAlpha);
            addVertex(bufferBuilder, matrix, x1 - size, y, z1 + size, r1, g1, b1, overallAlpha);
            addVertex(bufferBuilder, matrix, x1 + size, y, z1 + size, r1, g1, b1, overallAlpha);
            addVertex(bufferBuilder, matrix, x1 + size, y, z1 - size, r1, g1, b1, overallAlpha);

            float x2 = radius * (float) Math.cos(angle + Math.PI);
            float z2 = radius * (float) Math.sin(angle + Math.PI);
            float r2 = color2.getRed() / 255f; float g2 = color2.getGreen() / 255f; float b2 = color2.getBlue() / 255f;
            addVertex(bufferBuilder, matrix, x2 - size, y, z2 - size, r2, g2, b2, overallAlpha);
            addVertex(bufferBuilder, matrix, x2 - size, y, z2 + size, r2, g2, b2, overallAlpha);
            addVertex(bufferBuilder, matrix, x2 + size, y, z2 + size, r2, g2, b2, overallAlpha);
            addVertex(bufferBuilder, matrix, x2 + size, y, z2 - size, r2, g2, b2, overallAlpha);
        }
    };

    private static final List<Vec3> lorenzPoints = new ArrayList<>();

    public static final IVFXEffect LORENZ_ATTRACTOR = (poseStack, bufferBuilder, progress, color1, color2) -> {
        Matrix4f matrix = poseStack.last().pose();
        float overallAlpha = (float) Math.sin(progress * Math.PI);
        float sigma = 10.0f;
        float rho = 28.0f;
        float beta = 8.0f / 3.0f;
        float dt = 0.008f;

        if (progress < 0.05f && lorenzPoints.size() > 50) lorenzPoints.clear();
        if (lorenzPoints.isEmpty()) lorenzPoints.add(new Vec3(0.1, 0, 0));

        if (lorenzPoints.size() < 2000 * progress) {
            for (int k = 0; k < 20; k++) {
                Vec3 last = lorenzPoints.get(lorenzPoints.size() - 1);
                double x = last.x; double y = last.y; double z = last.z;
                double dx = sigma * (y - x);
                double dy = x * (rho - z) - y;
                double dz = x * y - beta * z;
                lorenzPoints.add(new Vec3(x + dx * dt, y + dy * dt, z + dz * dt));
            }
        }

        int limit = (int) (lorenzPoints.size() * progress) + 1;
        List<Vec3> points = lorenzPoints.subList(0, Math.min(limit, lorenzPoints.size()));

        for (int i = 0; i < points.size(); i++) {
            Vec3 p = points.get(i);
            float t = (float) i / points.size();
            Color color = interpolateColor(color1, color2, t);
            float r = color.getRed() / 255f; float g = color.getGreen() / 255f; float b = color.getBlue() / 255f;
            float size = 0.04f;
            float scale = 0.15f;
            float finalX = (float) p.x * scale;
            float finalY = (float) p.y * scale;
            float finalZ = (float) (p.z - 25) * scale;

            addVertex(bufferBuilder, matrix, finalX - size, finalY - size, finalZ, r, g, b, overallAlpha);
            addVertex(bufferBuilder, matrix, finalX + size, finalY - size, finalZ, r, g, b, overallAlpha);
            addVertex(bufferBuilder, matrix, finalX + size, finalY + size, finalZ, r, g, b, overallAlpha);
            addVertex(bufferBuilder, matrix, finalX - size, finalY + size, finalZ, r, g, b, overallAlpha);
        }
    };

    public static final IVFXEffect REALITY_TEAR = (poseStack, bufferBuilder, progress, color1, color2) -> {
        Matrix4f matrix = poseStack.last().pose();
        float overallAlpha = (float) Math.sin(progress * Math.PI);
        int numParticles = 1500;
        float tearLength = 6.0f;
        float tearWidth = 2.0f * (float) Math.sin(progress * Math.PI);

        for (int i = 0; i < numParticles; i++) {
            float y = (random.nextFloat() - 0.5f) * tearLength;
            float z = (random.nextFloat() - 0.5f) * tearWidth;
            float x = (float) (Math.sin(y * 2 + progress * 10) * (z / tearWidth) * 0.5f + (random.nextFloat() - 0.5f) * 0.2f);
            Color color = random.nextBoolean() ? color1 : color2;
            float r = color.getRed() / 255f; float g = color.getGreen() / 255f; float b = color.getBlue() / 255f;
            float size = 0.04f;
            float alpha = overallAlpha * (1 - Math.abs(z / tearWidth)) * 0.7f;

            addVertex(bufferBuilder, matrix, x - size, y, z, r, g, b, alpha);
            addVertex(bufferBuilder, matrix, x + size, y, z, r, g, b, alpha);
            addVertex(bufferBuilder, matrix, x + size, y + size, z, r, g, b, alpha);
            addVertex(bufferBuilder, matrix, x - size, y + size, z, r, g, b, alpha);
        }
    };
}
