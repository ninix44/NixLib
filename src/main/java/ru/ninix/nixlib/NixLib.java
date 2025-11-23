package ru.ninix.nixlib;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import ru.ninix.nixlib.client.shader.ShaderAPI;
import ru.ninix.nixlib.client.shader.impl.BlackHoleShader;
import ru.ninix.nixlib.visualizer.MixinListScreen;

@Mod(NixLib.MODID)
public class NixLib {
    public static final String MODID = "nixlib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.nixlib"))
        .withTabsBefore(CreativeModeTabs.COMBAT)
        .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
        .displayItems((parameters, output) -> {
            output.accept(EXAMPLE_ITEM.get());
        }).build());

    public static final KeyMapping OPEN_VISUALIZER_KEY = new KeyMapping("key.nixlib.open_visualizer", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.nixlib");
    public static final KeyMapping TEST_SHADER_KEY = new KeyMapping("key.nixlib.test_shader", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.nixlib");

    public NixLib(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onServerStarting);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ClientModEvents::onClientSetup);
            modEventBus.addListener(ClientModEvents::registerKeys);

            NeoForge.EVENT_BUS.addListener(ClientRuntimeEvents::onClientTick);
            NeoForge.EVENT_BUS.addListener(ClientRuntimeEvents::onRenderLevelStage);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("NixLib Common Setup initialized!");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("NixLib: Server starting...");
    }

    public static class ClientModEvents {
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("NixLib Client Setup initialized");
        }

        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_VISUALIZER_KEY);
            event.register(TEST_SHADER_KEY);
        }
    }

    public static class ClientRuntimeEvents {
        public static void onClientTick(ClientTickEvent.Post event) {
            if (OPEN_VISUALIZER_KEY.consumeClick()) {
                Minecraft.getInstance().setScreen(new MixinListScreen(Minecraft.getInstance().screen));
            }

            if (TEST_SHADER_KEY.consumeClick()) {
                ShaderAPI.toggle(new BlackHoleShader(0.8f, 10.0f));
                Minecraft.getInstance().player.displayClientMessage(Component.literal("Shader Toggled!"), true);
            }

            ShaderAPI.tick();
        }

        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
                ShaderAPI.renderWorldShaders(event.getPartialTick().getGameTimeDeltaTicks());
            }
        }
    }
}
