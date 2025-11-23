package ru.ninix.nixlib.visualizer;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import ru.ninix.nixlib.NixLib;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class MixinReflector {
    private static List<String> cachedMixins = null;

    public static List<String> getAllMixins() {
        if (cachedMixins != null) {
            return cachedMixins;
        }

        NixLib.LOGGER.info("Mixin Visualizer: Starting scan");

        List<String> mixinLines = new ArrayList<>();

        try {
            Collection<?> configs = findConfigsInMixinsClass();

            if (configs == null || configs.isEmpty()) {
                configs = findConfigsViaEnvironment();
            }

            if (configs != null && !configs.isEmpty()) {
                NixLib.LOGGER.info("Mixin Visualizer: Found {} config objects.", configs.size());

                for (Object configObj : configs) {
                    IMixinConfig iConfig = getInterfaceFromConfig(configObj);

                    if (iConfig != null) {
                        String configName = iConfig.getName();
                        String pkg = iConfig.getMixinPackage();

                        List<?> mixins = findMixinsListInConfig(configObj);
                        int mixinCount = (mixins != null) ? mixins.size() : 0;
                        String color = mixinCount > 0 ? "§6" : "§7";

                        mixinLines.add(color + "Config: " + configName + " §8(" + pkg + ")");

                        if (mixinCount > 0) {
                            for (Object mixinObj : mixins) {
                                if (mixinObj instanceof IMixinInfo info) {
                                    String status = info.isDetachedSuper() ? "§c[Detached]" : "§a[Loaded]";
                                    String name = info.getClassName().substring(info.getClassName().lastIndexOf('.') + 1);
                                    mixinLines.add("  " + status + " §f" + name);
                                }
                            }
                            mixinLines.add("");
                        }
                    }
                }
            } else {
                mixinLines.add("§cError: Could not locate Mixin configs.");
                NixLib.LOGGER.error("Mixin Visualizer: Could not locate Mixin configs via reflection.");
            }

        } catch (Exception e) {
            mixinLines.add("§cError: " + e.getClass().getSimpleName());
            NixLib.LOGGER.error("Mixin Visualizer error: ", e);
        }

        cachedMixins = mixinLines;
        return mixinLines;
    }

    private static Collection<?> findConfigsInMixinsClass() {
        try {
            try {
                Field f = Mixins.class.getDeclaredField("configs");
                f.setAccessible(true);
                Collection<?> c = (Collection<?>) f.get(null);
                if (c != null && !c.isEmpty()) return c;
            } catch (Exception ignored) {}

            for (Field field : Mixins.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && Collection.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Collection<?> c = (Collection<?>) field.get(null);
                    if (c != null && !c.isEmpty()) {
                        Object first = c.iterator().next();
                        if (first.getClass().getName().contains("Config")) return c;
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static Collection<?> findConfigsViaEnvironment() {
        try {
            Class<?> envClass = Class.forName("org.spongepowered.asm.mixin.MixinEnvironment");
            Method getDefaultEnv = envClass.getMethod("getDefaultEnvironment");
            Object env = getDefaultEnv.invoke(null);

            Method getTransformer = envClass.getMethod("getActiveTransformer");
            Object transformer = getTransformer.invoke(env);
            if (transformer == null) return null;

            Object processor = null;
            for (Field f : transformer.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(transformer);
                if (val != null && val.getClass().getName().contains("Processor")) {
                    processor = val;
                    break;
                }
            }
            if (processor == null) return null;

            for (Field f : processor.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (Collection.class.isAssignableFrom(f.getType())) {
                    Collection<?> c = (Collection<?>) f.get(processor);
                    if (c != null && !c.isEmpty()) {
                        Object first = c.iterator().next();
                        if (first.getClass().getName().contains("Config") && !first.getClass().getName().contains("Configuration")) {
                            return c;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static IMixinConfig getInterfaceFromConfig(Object configWrapper) {
        try {
            Method m = configWrapper.getClass().getMethod("getConfig");
            return (IMixinConfig) m.invoke(configWrapper);
        } catch (Exception e) {
            if (configWrapper instanceof IMixinConfig) return (IMixinConfig) configWrapper;
        }
        return null;
    }

    private static List<?> findMixinsListInConfig(Object configWrapper) {
        try {
            for (Field field : configWrapper.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (List.class.isAssignableFrom(field.getType())) {
                    List<?> list = (List<?>) field.get(configWrapper);
                    if (list != null && !list.isEmpty()) {
                        Object first = list.get(0);
                        if (first instanceof IMixinInfo) return list;
                    } else if (field.getName().toLowerCase().contains("mixin")) {
                        return list;
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
