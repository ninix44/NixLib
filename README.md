(EN Version)

# 🪷NixLib 

Core rendering library for **NeoForge 1.21**

## 🥞Current Features
- **Easy GLSL Shaders in GUI**: Render any `.fsh` / `.vsh` shader on screens without dealing with `Tesselator` or `BufferBuilder`.
- **Volumetric Block Shaders**: Create complex energy effects (Glowing Orbs, Floor Auras, Solid Cubes) using a simple Builder API (`ShaderBlockRenderer`).
- **Custom Geometry Support**: Draw triangles, hexagons, or any complex shapes with shaders using `NixRenderUtils`.
- **Post-Processing API**: Easily apply full-screen shaders (like Black Hole effects) to the game world.
- **Mixin Visualizer**: Built-in tool to see active mixins.
- 
## TODOs:
*   [ ] **Cutscenes** – Camera paths and sequencing.
*   [ ] **Bedrock Parser** – Loading `.json` Bedrock models.
*   [ ] **Particle System** – Custom particle rendering and logic.
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

### Registering a block with a renderer (NEW!!!)
```
ShaderBlockRenderer.Settings mySettings = new ShaderBlockRenderer.Settings()
    .solid(true)        // render the solid cube
    .floor(4.5f)        // render glow on the floor (radius 4.5)
    .center(1.3f);      // render orb inside the block

event.registerBlockEntityRenderer(MY_BLOCK_ENTITY.get(),
    ctx -> new ShaderBlockRenderer<>(ctx, MyShaders::getMyShader, mySettings));
```

