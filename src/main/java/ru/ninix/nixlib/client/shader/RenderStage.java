package ru.ninix.nixlib.client.shader;

public enum RenderStage {
    // everything works +W
    /**
     * Applies to the world (pre-GUI).
     */
    WORLD,
    /**
     * Applies to the entire screen (on top).
     */
    SCREEN
}
