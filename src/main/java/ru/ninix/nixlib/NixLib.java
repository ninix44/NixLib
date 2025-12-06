package ru.ninix.nixlib;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import ru.ninix.nixlib.client.cutscene.ClientCutsceneManager;
import ru.ninix.nixlib.client.gui.*;
import ru.ninix.nixlib.client.renderer.ShaderBlockRenderer;
import ru.ninix.nixlib.client.shader.NixLibShaders;
import ru.ninix.nixlib.client.shader.ShaderAPI;
import ru.ninix.nixlib.client.shader.impl.BlackHoleShader;
import ru.ninix.nixlib.client.util.CameraStateManager;
import ru.ninix.nixlib.client.util.NixRenderUtils;
import ru.ninix.nixlib.client.vfx.NixVFXPresets;
import ru.ninix.nixlib.client.vfx.VFXRenderer;
import ru.ninix.nixlib.command.CutsceneCommand;
import ru.ninix.nixlib.common.block.*;
import ru.ninix.nixlib.item.CameraItem;
import ru.ninix.nixlib.network.CutsceneNetwork;
import ru.ninix.nixlib.visualizer.MixinListScreen;

import java.awt.*;

@Mod(NixLib.MODID)
public class NixLib {
    public static final String MODID = "nixlib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // IMPORTANT: lightLevel must be 0!!!!!
    // if you enable lightLevel, the flashlight's yellow light will drown out your RGB glow (or other glow)!!!!!

    // blocks
    public static final DeferredBlock<GlowBlock> EXAMPLE_BLOCK = BLOCKS.register("example_block",
        () -> new GlowBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).lightLevel(state -> 0).noOcclusion()));

    public static final DeferredBlock<VoidBlock> VOID_BLOCK = BLOCKS.register("void_block",
        () -> new VoidBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(3.0f).lightLevel(state -> 0).noOcclusion()));

    public static final DeferredBlock<TestBlock> TEST_BLOCK = BLOCKS.register("test_block",
        () -> new TestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).strength(2.0f).lightLevel(state -> 0).noOcclusion()));


    // block entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GlowBlockEntity>> EXAMPLE_BLOCK_ENTITY = BLOCK_ENTITIES.register("glow_be",
        () -> BlockEntityType.Builder.of(GlowBlockEntity::new, EXAMPLE_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VoidBlockEntity>> VOID_BLOCK_ENTITY = BLOCK_ENTITIES.register("void_be",
        () -> BlockEntityType.Builder.of(VoidBlockEntity::new, VOID_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TestBlockEntity>> TEST_BLOCK_ENTITY = BLOCK_ENTITIES.register("test_be",
        () -> BlockEntityType.Builder.of(TestBlockEntity::new, TEST_BLOCK.get()).build(null));


    // items
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);
    public static final DeferredItem<BlockItem> VOID_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("void_block", VOID_BLOCK);
    public static final DeferredItem<BlockItem> TEST_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("test_block", TEST_BLOCK);
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()));
    public static final DeferredItem<Item> CAMERA_ITEM = ITEMS.register("camera", () -> new CameraItem(new Item.Properties().stacksTo(1)));

    // tabs
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.nixlib"))
        .withTabsBefore(CreativeModeTabs.COMBAT)
        .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
        .displayItems((parameters, output) -> {
            output.accept(EXAMPLE_ITEM.get());
            output.accept(EXAMPLE_BLOCK_ITEM.get());
            output.accept(VOID_BLOCK_ITEM.get());
            output.accept(TEST_BLOCK_ITEM.get());
            output.accept(CAMERA_ITEM.get());
        }).build());

    public static final KeyMapping OPEN_VISUALIZER_KEY = new KeyMapping("key.nixlib.open_visualizer", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.nixlib");
    public static final KeyMapping OPEN_COSMIC_KEY = new KeyMapping("key.nixlib.open_cosmic", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.categories.nixlib");
    public static final KeyMapping TEST_SHADER_KEY = new KeyMapping("key.nixlib.test_shader", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.nixlib");
    public static final KeyMapping OPEN_CONSTELLATION_KEY = new KeyMapping("key.nixlib.open_constellation", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.nixlib");
    public static final KeyMapping OPEN_RAINBOW_KEY = new KeyMapping("key.nixlib.open_rainbow", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.nixlib");
    public static final KeyMapping OPEN_FRACTAL_KEY = new KeyMapping("key.nixlib.open_fractal", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F, "key.categories.nixlib");
    public static final KeyMapping OPEN_CHLADNI_KEY = new KeyMapping("key.nixlib.open_chladni", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.categories.nixlib");
    public static final KeyMapping VFX_KEY_Z = new KeyMapping("key.nixlib.vfx_z", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.nixlib");
    public static final KeyMapping VFX_KEY_X = new KeyMapping("key.nixlib.vfx_x", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.nixlib");
    public static final KeyMapping VFX_KEY_C = new KeyMapping("key.nixlib.vfx_c", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.nixlib");
    public static final KeyMapping VFX_KEY_V = new KeyMapping("key.nixlib.vfx_v", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.nixlib");

    public NixLib(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(CutsceneNetwork::register);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ClientModEvents::onClientSetup);
            modEventBus.addListener(ClientModEvents::registerKeys);
            modEventBus.addListener(ClientModEvents::registerRenderers);
            modEventBus.addListener(ClientModEvents::registerGuiLayers);

            NeoForge.EVENT_BUS.addListener(ClientRuntimeEvents::onClientTick);
            NeoForge.EVENT_BUS.addListener(ClientRuntimeEvents::onRenderLevelStage);
            NeoForge.EVENT_BUS.addListener(ClientRuntimeEvents::onScreenRenderPost);
            NeoForge.EVENT_BUS.addListener(ClientRuntimeEvents::onComputeCameraAngles);
            NeoForge.EVENT_BUS.addListener(ClientRuntimeEvents::onComputeFov);
            NeoForge.EVENT_BUS.addListener(ClientRuntimeEvents::onMouseScroll);
            NeoForge.EVENT_BUS.addListener(ClientRuntimeEvents::onRenderPlayer);

            NeoForge.EVENT_BUS.register(VFXRenderer.class);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("NixLib Common Setup initialized!");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
            event.accept(VOID_BLOCK_ITEM);
            event.accept(TEST_BLOCK_ITEM);
        }
    }

    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("NixLib: Server starting...");
    }

    public void onRegisterCommands(RegisterCommandsEvent event) {
        CutsceneCommand.register(event.getDispatcher());
    }

    public static class ClientModEvents {
        public static void onClientSetup(FMLClientSetupEvent event) {}

        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_VISUALIZER_KEY);
            event.register(OPEN_COSMIC_KEY);
            event.register(TEST_SHADER_KEY);
            event.register(OPEN_CONSTELLATION_KEY);
            event.register(OPEN_RAINBOW_KEY);
            event.register(OPEN_FRACTAL_KEY);
            event.register(OPEN_CHLADNI_KEY);
            event.register(VFX_KEY_Z);
            event.register(VFX_KEY_X);
            event.register(VFX_KEY_C);
            event.register(VFX_KEY_V);
        }

        public static void registerGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MODID, "screen_shader"), (guiGraphics, deltaTracker) -> {
                if (Minecraft.getInstance().screen == null) {
                    ShaderAPI.renderScreenShaders(deltaTracker.getGameTimeDeltaPartialTick(false));
                }
            });
        }

        // Settings cheat sheet
        // You create a new Settings() object, and then call its methods using a dot "."

        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {

            // 1. FLOOR ONLY (without ball inside)
            /*
            ShaderBlockRenderer.Settings floorOnly = new ShaderBlockRenderer.Settings()
                .solid(true)       // draw the block itself? = yes
                .floor(4.0f)       // Turn "ON" the floor and set the radius to 4.0 blocks
                .noCenter();       // turn off the ball inside

            // event.registerBlockEntityRenderer(EXAMPLE_BLOCK_ENTITY.get(),
            //    ctx -> new ShaderBlockRenderer<>(ctx, NixLibShaders::getRgbAuraShader, floorOnly));
            */


            // 2. ONLY THE BALL INSIDE (no floor)
            /*
            ShaderBlockRenderer.Settings centerOnly = new ShaderBlockRenderer.Settings()
                .solid(true)       // draw the block itself? = yes
                .noFloor()         // turn "OFF" the floor
                .center(1.5f);     // turn on the ball inside and set the radius to 1.5

            // event.registerBlockEntityRenderer(EXAMPLE_BLOCK_ENTITY.get(),
            //    ctx -> new ShaderBlockRenderer<>(ctx, NixLibShaders::getRgbAuraShader, centerOnly));
            */


            // 3. EVERYTHING AT ONCE (Both the floor and the ball)

            /*
            ShaderBlockRenderer.Settings allFeatures = new ShaderBlockRenderer.Settings()
                .solid(true)       // draw the block itself? = yes
                .floor(5.0f)       // Turn "ON" the floor and set the radius to 5.0 blocks
                .center(1.2f);     // turn on the ball inside and set the radius to 1.2

            // event.registerBlockEntityRenderer(EXAMPLE_BLOCK_ENTITY.get(),
            //    ctx -> new ShaderBlockRenderer<>(ctx, NixLibShaders::getRgbAuraShader, allFeatures));
            */


            // 4. (Only light, no solid block)
            /*
            ShaderBlockRenderer.Settings ghostSettings = new ShaderBlockRenderer.Settings()
                .solid(false)      // draw the block itself? = NO
                .center(2.0f)      // draw only the glowing ball
                .noFloor();        // turn "OFF" the floor

            // event.registerBlockEntityRenderer(EXAMPLE_BLOCK_ENTITY.get(),
            //    ctx -> new ShaderBlockRenderer<>(ctx, NixLibShaders::getRgbAuraShader, ghostSettings));
            */

            // WITH CUSTOM UNIFORMS (Advanced) - Like Test Block
            /*
            ShaderBlockRenderer.Settings customUniformSettings = new ShaderBlockRenderer.Settings()
                .solid(true)        // draw the block itself? = yes
                .center(2.5f)       // radius of the center ball
                .floor(3.0f)        // radius of the floor effect
                .withUniforms((shader, time) -> {
                    NixRenderUtils.safeSetUniform(shader, "uBlue", 1.0f);  // Example: turn on blue channel
                    NixRenderUtils.safeSetUniform(shader, "uSpeed", 0.5f); // Example: change animation speed
                });

            // event.registerBlockEntityRenderer(TEST_BLOCK_ENTITY.get(),
            //    ctx -> new ShaderBlockRenderer<>(ctx, NixLibShaders::getTestCoreShader, customUniformSettings));
            */

            ShaderBlockRenderer.Settings currentExampleSettings = new ShaderBlockRenderer.Settings()
                .solid(true)
                .floor(4.5f)
                .center(1.3f);

            event.registerBlockEntityRenderer(EXAMPLE_BLOCK_ENTITY.get(),
                ctx -> new ShaderBlockRenderer<>(ctx, NixLibShaders::getRgbAuraShader, currentExampleSettings));


            ShaderBlockRenderer.Settings voidSettings = new ShaderBlockRenderer.Settings()
                .solid(true)
                .noFloor()
                .center(3.0f);

            event.registerBlockEntityRenderer(VOID_BLOCK_ENTITY.get(),
                ctx -> new ShaderBlockRenderer<>(ctx, NixLibShaders::getVoidCoreShader, voidSettings));

            // testBlock static-shader
            ShaderBlockRenderer.Settings testSettings = new ShaderBlockRenderer.Settings()
                .solid(true)
                .center(2.5f)
                .floor(3.0f)
                .withUniforms((shader, time) -> {
                    NixRenderUtils.safeSetUniform(shader, "uBlue", 0.0f);
                    NixRenderUtils.safeSetUniform(shader, "uSpeed", 0.0f);
                });

            event.registerBlockEntityRenderer(TEST_BLOCK_ENTITY.get(),
                ctx -> new ShaderBlockRenderer<>(ctx, NixLibShaders::getTestCoreShader, testSettings));
        }
    }

    public static class ClientRuntimeEvents {
        public static void onClientTick(ClientTickEvent.Post event) {
            ClientCutsceneManager.tick();
            CameraStateManager.tick();

            Minecraft mc = Minecraft.getInstance();

            if (mc.player != null && mc.player.isHolding(CAMERA_ITEM.get())) {
                CameraStateManager.activate();

                long window = mc.getWindow().getWindow();
                if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT)) {
                    CameraStateManager.adjustRoll(-2.0f);
                }
                if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT)) {
                    CameraStateManager.adjustRoll(2.0f);
                }
                if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_DOWN)) {
                    CameraStateManager.resetRoll();
                }
            }

            if (OPEN_VISUALIZER_KEY.consumeClick()) Minecraft.getInstance().setScreen(new MixinListScreen(Minecraft.getInstance().screen));
            if (OPEN_COSMIC_KEY.consumeClick()) Minecraft.getInstance().setScreen(new CosmicGuiScreen());
            if (OPEN_CONSTELLATION_KEY.consumeClick()) Minecraft.getInstance().setScreen(new ConstellationGameScreen());
            if (OPEN_RAINBOW_KEY.consumeClick()) Minecraft.getInstance().setScreen(new TestRainbowScreen());
            if (OPEN_FRACTAL_KEY.consumeClick()) Minecraft.getInstance().setScreen(new FractalGuiScreen());
            if (OPEN_CHLADNI_KEY.consumeClick()) Minecraft.getInstance().setScreen(new ChladniGameScreen());
            if (TEST_SHADER_KEY.consumeClick()) {
                ShaderAPI.toggle(new BlackHoleShader(0.8f, 10.0f));
            }

            if (Minecraft.getInstance().player != null) {

                if (VFX_KEY_Z.consumeClick()) {
                    VFXRenderer.spawnEffect(
                        Minecraft.getInstance().player.position().add(0, 1.5, 0),
                        Minecraft.getInstance().player.getYRot(),
                        0f,
                        NixVFXPresets.GALAXY,
                        4000L,
                        Color.CYAN,
                        Color.MAGENTA
                    );
                }

                if (VFX_KEY_X.consumeClick()) {
                    VFXRenderer.spawnEffect(
                        Minecraft.getInstance().player.position().add(0, 1.5, 0),
                        Minecraft.getInstance().player.getYRot(),
                        0f,
                        NixVFXPresets.DNA_HELIX,
                        5000L,
                        Color.GREEN,
                        new Color(0, 100, 255)
                    );
                }

                if (VFX_KEY_C.consumeClick()) {
                    VFXRenderer.spawnEffect(
                        Minecraft.getInstance().player.position().add(0, 2.5, 0),
                        Minecraft.getInstance().player.getYRot(),
                        0f,
                        NixVFXPresets.LORENZ_ATTRACTOR,
                        6000L,
                        new Color(255, 100, 0),
                        Color.YELLOW
                    );
                }

                if (VFX_KEY_V.consumeClick()) {
                    VFXRenderer.spawnEffect(
                        Minecraft.getInstance().player.position().add(0, 1.5, 0),
                        Minecraft.getInstance().player.getYRot(),
                        0f,
                        NixVFXPresets.REALITY_TEAR,
                        3000L,
                        Color.RED,
                        Color.BLACK
                    );
                }
            }

            ShaderAPI.tick();
        }

        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (ClientCutsceneManager.isPlaying() && event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
                var poseStack = event.getPoseStack();
                var camera = Minecraft.getInstance().gameRenderer.getMainCamera();

                double dx = ClientCutsceneManager.cameraX - camera.getPosition().x;
                double dy = ClientCutsceneManager.cameraY - camera.getPosition().y;
                double dz = ClientCutsceneManager.cameraZ - camera.getPosition().z;

                poseStack.translate(-dx, -dy, -dz);
            }

            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
                ShaderAPI.renderWorldShaders(event.getPartialTick().getGameTimeDeltaTicks());
            }
        }

        public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
            if (ClientCutsceneManager.isPlaying() && event.getEntity().equals(Minecraft.getInstance().player)) {
                event.setCanceled(true);
            }
        }

        public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
            ShaderAPI.renderScreenShaders(event.getPartialTick());
        }

        public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
            if (ClientCutsceneManager.isPlaying()) {
                ClientCutsceneManager.updateCamera(event);
            }
            else if (CameraStateManager.isActive()) {
                event.setRoll(CameraStateManager.getRoll());
            }
        }

        public static void onComputeFov(ViewportEvent.ComputeFov event) {
            if (ClientCutsceneManager.isPlaying()) {
                event.setFOV(ClientCutsceneManager.interpolatedFov);
            }
            else if (CameraStateManager.isActive()) {
                event.setFOV(CameraStateManager.getFov());
            }
        }

        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            if (mc.player.isHolding(CAMERA_ITEM.get())) {
                boolean isCtrl = GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;
                if (isCtrl) {
                    CameraStateManager.updateFov(event.getScrollDeltaY());
                    event.setCanceled(true);
                }
            }
        }
    }
}
