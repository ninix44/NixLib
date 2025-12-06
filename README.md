(EN Version)

# 🪷NixLib 

Core rendering library for **NeoForge 1.21**

## 🥞Current Features
- **Easy GLSL Shaders in GUI**: Render any `.fsh` / `.vsh` shader on screens without dealing with `Tesselator` or `BufferBuilder`.
- **Volumetric Block Shaders**: Create complex energy effects (Glowing Orbs, Floor Auras, Solid Cubes) using a simple Builder API (`ShaderBlockRenderer`).
- **Advanced VFX Engine**: Spawn math-based particle systems (Galaxies, DNA, Chaos Attractors) with a single line of code. Fully extensible via `IVFXEffect` interface. (NEW!!!)
- **Custom Geometry Support**: Draw triangles, hexagons, or any complex shapes with shaders using `NixRenderUtils`.
- **Post-Processing API**: Easily apply full-screen shaders (like Black Hole effects) to the game world.
- **Mixin Visualizer**: Built-in tool to see active mixins.
- **Cutscene Engine**: Create smooth camera paths and cinematic sequences.
- **Camera Control**: API to modify **FOV** and apply camera **Roll** (tilt).
- **Interpolation Utils**: Helpers for smooth animations (Lerp, Easing, Bezier, etc);

## TODOs:
*   [x] **Cutscenes** – Camera paths and sequencing.
*   [ ] **Bedrock Parser** – Loading `.json` Bedrock models.
*   [ ] **Particle System** – Custom particle system `later`
  *   [x] **VFX System** – Complex particle rendering logic.
  *   [x] **API** – `IVFXEffect` interface for custom user effects.
*   [x] **Block Shaders** – Glow and bloom effects.
  * [x] **Block Shaders (Configurable Auras)** – Builder pattern for Floor Glow, Center Orbs, and Solid Blocks.
  * [x] **Block Shaders (Visuals)** – Smooth gradients, blending, and distinct brightness settings (Floor and Orb).



## 🛠 Usage Examples

### Drawing a shader in GUI (Easy Mode)
```
NixRenderUtils.drawTexturedQuad(
    guiGraphics.pose().last().pose(), 
    x, y, width, height, 
    myShader, 
    (instance) -> {
        instance.getUniform("uTime").set(time);
    }
);
```

### Drawing Custom Shapes (Flexible Mode)
```
NixRenderUtils.drawCustomGeometry(
    myShader,
    (instance) -> instance.getUniform("uTime").set(time),
    (buffer) -> {
        // Push your custom vertices here
        buffer.addVertex(matrix, x, y, 0).setUv(0, 0);
        buffer.addVertex(matrix, x + 10, y + 20, 0).setUv(1, 1);
        // ...
    }
);
```

### Registering a block with a renderer
```
ShaderBlockRenderer.Settings mySettings = new ShaderBlockRenderer.Settings()
    .solid(true)        // render the solid cube
    .floor(4.5f)        // render glow on the floor (radius 4.5)
    .center(1.3f);      // render orb inside the block

event.registerBlockEntityRenderer(MY_BLOCK_ENTITY.get(),
    ctx -> new ShaderBlockRenderer<>(ctx, MyShaders::getMyShader, mySettings));
```

### (NEW!!!) Use the `VFXRenderer` to spawn particle systems
You can use built-in presets found in `NixVFXPresets`.

```
// Spawn a Galaxy effect at the player's position 
if (Minecraft.getInstance().player != null) {
    VFXRenderer.spawnEffect(
        Minecraft.getInstance().player.position().add(0, 2, 0), // Position
        0f, 0f,                                                 // Rotation (Y, X)
        NixVFXPresets.GALAXY,                                   // Use a PRESET
        5000L,                                                  // Duration (ms)
        Color.CYAN,                                             // Color 1
        Color.MAGENTA                                           // Color 2
    );
}
```

### Creating Custom VFX (Advanced)
In `NixLib` uses the `IVFXEffect` interface, you can create your own effects

```
// 1. Define your effect logic
IVFXEffect myCustomTriangle = (poseStack, buffer, progress, c1, c2) -> {
    Matrix4f matrix = poseStack.last().pose();
    
    // Rotate based on progress
    poseStack.mulPose(Axis.YP.rotationDegrees(progress * 360));
    
    // Helper to blend colors
    Color color = NixVFXPresets.interpolateColor(c1, c2, progress);
    float r = color.getRed() / 255f;
    float g = color.getGreen() / 255f;
    float b = color.getBlue() / 255f;
    
    // Draw vertices using the helper method
    // (x, y, z, r, g, b, alpha)
    NixVFXPresets.addVertex(buffer, matrix, 0, 1, 0, r, g, b, 1.0f);
    NixVFXPresets.addVertex(buffer, matrix, -1, 0, 0, r, g, b, 1.0f);
    NixVFXPresets.addVertex(buffer, matrix, 1, 0, 0, r, g, b, 1.0f);
    NixVFXPresets.addVertex(buffer, matrix, 0, 1, 0, r, g, b, 1.0f); // Close loop
};

// 2. Spawn it!
VFXRenderer.spawnEffect(pos, 0, 0, myCustomTriangle, 3000L, Color.RED, Color.YELLOW);
```
