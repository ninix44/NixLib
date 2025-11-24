package ru.ninix.nixlib.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.function.Consumer;

/**
 * Rendering Utilities
 * A collection of helper methods to simplify shader rendering in GUIs
 * Handles OpenGL state management (Blending, Culling, Depth Test) automatically
 */
public class NixRenderUtils {

    private static final ResourceLocation WHITE_TEX = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    /**
     * Easy Mode: Draw a textured rectangle with a Shader
     * Use this for cards, backgrounds, icons, etc
     *
     * @param poseMatrix   The position matrix (usually {@code guiGraphics.pose().last().pose()}).
     * @param x            X position on screen.
     * @param y            Y position on screen.
     * @param w            Width of the quad.
     * @param h            Height of the quad.
     * @param shader       The shader instance to use.
     * @param uniformSetup A lambda to set shader uniforms (e.g. Time, MousePos). Can be null.
     */

    public static void drawTexturedQuad(Matrix4f poseMatrix, float x, float y, float w, float h, ShaderInstance shader, Consumer<ShaderInstance> uniformSetup) {
        drawCustomGeometry(shader, uniformSetup, (buffer) -> {
            float z = 0.0f;
            // standard quadrilateral (counter-clockwise)
            buffer.addVertex(poseMatrix, x, y + h, z).setUv(0, 1);
            buffer.addVertex(poseMatrix, x + w, y + h, z).setUv(1, 1);
            buffer.addVertex(poseMatrix, x + w, y, z).setUv(1, 0);
            buffer.addVertex(poseMatrix, x, y, z).setUv(0, 0);
        });
    }

    /**
     * Flexible Mode: Draw any shape (Triangle, Hexagon, Star)
     * Automatically disables Backface Culling and Depth Testing to ensure the geometry
     * is visible in the GUI layer, regardless of vertex order or Z-level
     *
     * @param shader       The shader instance.
     * @param uniformSetup A lambda to set shader uniforms.
     * @param vertexPusher A lambda where you define your vertices using the provided {@link BufferBuilder}.
     */

    public static void drawCustomGeometry(ShaderInstance shader, Consumer<ShaderInstance> uniformSetup, Consumer<BufferBuilder> vertexPusher) {
        if (shader == null) return;

        // setup RenderSystem

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // IMPORTANT!!! for custom GUI shapes
        RenderSystem.disableCull(); // show faces from both sides
        RenderSystem.disableDepthTest(); // draw on top of everything

        RenderSystem.setShaderTexture(0, WHITE_TEX);
        RenderSystem.setShader(() -> shader);

        // setup uniforms
        if (uniformSetup != null) {
            uniformSetup.accept(shader);
        }

        // draw
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        // user geometry logic
        vertexPusher.accept(buffer);


        // upload and draw
        try {
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        } catch (Exception e) {
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Hardcore Mode: Full control
     * Use this if you need a different VertexFormat (e.g. with Color) or Mode (e.g. TRIANGLE_STRIP)
     */

    public static void drawRaw(ShaderInstance shader, VertexFormat.Mode mode, VertexFormat format, Consumer<ShaderInstance> uniformSetup, Consumer<BufferBuilder> vertexPusher) {
        if (shader == null) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();

        RenderSystem.setShader(() -> shader);

        if (uniformSetup != null) uniformSetup.accept(shader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(mode, format);

        vertexPusher.accept(buffer);

        try {
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        } catch (Exception ignored) {}

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * 3D Block Mode: Draw a Cube with a Shader
     * @param matrix           The pose matrix.
     * @param shader           The shader instance.
     * @param seeThroughWalls  IF TRUE: You will see the block through walls (Wallhack). IF FALSE: Normal behavior.
     * @param uniformSetup     A lambda to set shader uniforms.
     */
    public static void drawCubeWithShader(Matrix4f matrix, ShaderInstance shader, boolean seeThroughWalls, Consumer<ShaderInstance> uniformSetup) {
        if (shader == null) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        if (seeThroughWalls) {
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
        }

        RenderSystem.depthMask(false);

        RenderSystem.setShader(() -> shader);
        if (uniformSetup != null) uniformSetup.accept(shader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        builder.addVertex(matrix, 0, 1, 0).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 0, 1, 1).setUv(0, 1).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 1, 1, 1).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 1, 1, 0).setUv(1, 0).setColor(255, 255, 255, 255);

        builder.addVertex(matrix, 0, 0, 0).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 1, 0, 0).setUv(1, 0).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 1, 0, 1).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 0, 0, 1).setUv(0, 1).setColor(255, 255, 255, 255);

        builder.addVertex(matrix, 0, 0, 1).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 1, 0, 1).setUv(1, 0).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 1, 1, 1).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 0, 1, 1).setUv(0, 1).setColor(255, 255, 255, 255);

        builder.addVertex(matrix, 0, 0, 0).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 0, 1, 0).setUv(0, 1).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 1, 1, 0).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 1, 0, 0).setUv(1, 0).setColor(255, 255, 255, 255);

        builder.addVertex(matrix, 0, 0, 0).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 0, 0, 1).setUv(1, 0).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 0, 1, 1).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 0, 1, 0).setUv(0, 1).setColor(255, 255, 255, 255);

        builder.addVertex(matrix, 1, 0, 0).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 1, 1, 0).setUv(0, 1).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 1, 1, 1).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(matrix, 1, 0, 1).setUv(1, 0).setColor(255, 255, 255, 255);

        try {
            BufferUploader.drawWithShader(builder.buildOrThrow());
        } catch (Exception ignored) {}

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
