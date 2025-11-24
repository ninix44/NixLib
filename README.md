(EN Version)

# 🪷NixLib 

Core rendering library for **NeoForge 1.21**

## 🥞Current Features
- **Easy GLSL Shaders in GUI**: Render any `.fsh` / `.vsh` shader on screens without dealing with `Tesselator` or `BufferBuilder`.
- **Custom Geometry Support**: Draw triangles, hexagons, or any complex shapes with shaders using `NixRenderUtils`.
- **Post-Processing API**: Easily apply full-screen shaders (like Black Hole effects) to the game world.
- **Mixin Visualizer**: Built-in tool to see active mixins.
- 
## TODOs:
*   [ ] **Cutscenes** – Camera paths and sequencing.
*   [ ] **Bedrock Parser** – Loading `.json` Bedrock models.
*   [ ] **Particle System** – Custom particle rendering and logic.
*   [x] **Block Shaders** – Glow and bloom effects.
  * [ ] **Block Shaders** - Make the block glow dependent on the block rendering, now it glows like a normal torch



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

(RU Version)

# 🪷NixLib

Библиотека для рендеринга под **NeoForge 1.21**

## 🥞Возможности
- **Простые GLSL Шейдеры в GUI**: Рендер любых `.fsh` / `.vsh` шейдеров в интерфейсах без необходимости возиться с `Tesselator` или `BufferBuilder`.
- **Кастомная геометрия**: Рисование треугольников, шестиугольников и любых сложных фигур с наложением шейдеров через `NixRenderUtils`.
- **API Пост-процессинга**: Применение шейдеров на весь экран (например, эффекты "Black Hole").
- **Визуализатор миксинов**: Встроенная инструментальная функция для просмотра активных миксинов.

## В планах (TODO)
*   [ ] **Катсцены** – Управление камерой и сценарии.
*   [ ] **Парсер Bedrock** – Загрузка моделей формата Bedrock.
*   [ ] **Система партиклов** – Рендер и логика частиц.
*   [x] **Шейдеры блоков** – Свечение (Bloom) и эффекты.
  *   [ ] **Шейдеры блоков** – Сделайте свечение блока зависимым от рендеринга блока, теперь он светится как обычный факел!!!


## 🛠 Примеры использования

### Рисование шейдера в GUI (Простой режим)
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

### Рисование кастомных фигур (Гибкий режим)
```
NixRenderUtils.drawCustomGeometry(
    myShader,
    (instance) -> instance.getUniform("uTime").set(time),
    (buffer) -> {
        // Добавьте свои вершины здесь
        buffer.addVertex(matrix, x, y, 0).setUv(0, 0);
        buffer.addVertex(matrix, x + 10, y + 20, 0).setUv(1, 1);
        // ...
    }
);
```

