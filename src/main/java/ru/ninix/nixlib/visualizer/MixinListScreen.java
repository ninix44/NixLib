package ru.ninix.nixlib.visualizer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MixinListScreen extends Screen {
    private final Screen parent;
    private MixinList list;

    public MixinListScreen(Screen parent) {
        super(Component.literal("Mixin Visualizer"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.list = new MixinList(this.minecraft, this.width, this.height, 32, 20);

        //this.list.addMixinEntry(new MixinEntry("Gui Test working "));
        List<String> mixins = MixinReflector.getAllMixins();

        if (mixins.isEmpty()) {
            this.list.addMixinEntry(new MixinEntry("§cScanner returned 0 results. Check console."));
        }

        for (String line : mixins) {
            this.list.addMixinEntry(new MixinEntry(line));
        }

        this.addRenderableWidget(this.list);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.list.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    class MixinList extends ObjectSelectionList<MixinEntry> {
        public MixinList(net.minecraft.client.Minecraft minecraft, int width, int height, int y0, int itemHeight) {
            super(minecraft, width, height, y0, itemHeight);
        }

        public void addMixinEntry(MixinEntry entry) {
            this.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return 300;
        }
    }

    class MixinEntry extends ObjectSelectionList.Entry<MixinEntry> {
        private final String text;

        public MixinEntry(String text) {
            this.text = text;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            guiGraphics.drawString(minecraft.font, this.text, left + 5, top + 5, 0xFFFFFF, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(text);
        }
    }
}
