package ru.ninix.nixlib.client.model.bedrock;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import ru.ninix.nixlib.NixLib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class BedrockModelLoader {
    private static final Gson GSON = new Gson();

    public static LayerDefinition load(ResourceLocation location, String geometryName) {
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.open()))) {
                BedrockPOJO pojo = GSON.fromJson(reader, BedrockPOJO.class);

                if (pojo.geometryList == null || pojo.geometryList.isEmpty()) {
                    throw new RuntimeException("No geometry found in " + location);
                }

                BedrockPOJO.Geometry geo = pojo.geometryList.getFirst();
                if (geometryName != null) {
                    for (BedrockPOJO.Geometry g : pojo.geometryList) {
                        if (geometryName.equals(g.description.identifier)) {
                            geo = g;
                            break;
                        }
                    }
                }

                return createDefinition(geo);
            }
        } catch (IOException e) {
            NixLib.LOGGER.error("Failed to load Bedrock model: " + location, e);
            MeshDefinition mesh = new MeshDefinition();
            return LayerDefinition.create(mesh, 64, 64);
        }
    }

    private static LayerDefinition createDefinition(BedrockPOJO.Geometry geo) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        Map<String, PartDefinition> parts = new HashMap<>();
        Map<String, float[]> bonePivots = new HashMap<>();
        Map<String, BedrockPOJO.Bone> boneMap = new HashMap<>();
        for (BedrockPOJO.Bone bone : geo.bones) {
            boneMap.put(bone.name, bone);
        }

        for (BedrockPOJO.Bone bone : geo.bones) {
            buildBone(bone, boneMap, parts, bonePivots, root);
        }

        int texWidth = (int) geo.description.texture_width;
        int texHeight = (int) geo.description.texture_height;
        if (texWidth == 0) texWidth = 64;
        if (texHeight == 0) texHeight = 64;

        return LayerDefinition.create(mesh, texWidth, texHeight);
    }

    private static void buildBone(BedrockPOJO.Bone bone,
                                  Map<String, BedrockPOJO.Bone> boneMap,
                                  Map<String, PartDefinition> createdParts,
                                  Map<String, float[]> absolutePivots,
                                  PartDefinition meshRoot) {

        if (createdParts.containsKey(bone.name)) return;

        PartDefinition parentPart = meshRoot;
        float parentPivotX = 0;
        float parentPivotY = 0;
        float parentPivotZ = 0;

        if (bone.parent != null && !bone.parent.isEmpty()) {
            BedrockPOJO.Bone parentBone = boneMap.get(bone.parent);
            if (parentBone != null) {
                if (!createdParts.containsKey(parentBone.name)) {
                    buildBone(parentBone, boneMap, createdParts, absolutePivots, meshRoot);
                }
                parentPart = createdParts.get(parentBone.name);
                float[] pp = absolutePivots.get(parentBone.name);
                parentPivotX = pp[0];
                parentPivotY = pp[1];
                parentPivotZ = pp[2];
            }
        }

        CubeListBuilder builder = CubeListBuilder.create();

        float pivotX = bone.pivot != null ? bone.pivot.get(0) : 0;
        float pivotY = bone.pivot != null ? bone.pivot.get(1) : 0;
        float pivotZ = bone.pivot != null ? bone.pivot.get(2) : 0;

        absolutePivots.put(bone.name, new float[]{pivotX, pivotY, pivotZ});

        float rotX = bone.rotation != null ? (float) Math.toRadians(-bone.rotation.get(0)) : 0;
        float rotY = bone.rotation != null ? (float) Math.toRadians(-bone.rotation.get(1)) : 0;
        float rotZ = bone.rotation != null ? (float) Math.toRadians(bone.rotation.get(2)) : 0;

        if (bone.cubes != null) {
            for (BedrockPOJO.Cube cube : bone.cubes) {
                float originX = cube.origin.get(0);
                float originY = cube.origin.get(1);
                float originZ = cube.origin.get(2);

                float sizeX = cube.size.get(0);
                float sizeY = cube.size.get(1);
                float sizeZ = cube.size.get(2);

                float inflate = cube.inflate != null ? cube.inflate : (bone.inflate != 0 ? bone.inflate : 0);
                boolean mirror = cube.mirror != null ? cube.mirror : bone.mirror;

                int u = 0, v = 0;
                if (cube.uv != null && cube.uv.isJsonArray()) {
                    JsonArray arr = cube.uv.getAsJsonArray();
                    u = arr.get(0).getAsInt();
                    v = arr.get(1).getAsInt();
                }

                builder.mirror(mirror);

                float boxX = -(originX + sizeX) + pivotX;
                float boxY = -(originY + sizeY) + pivotY;
                float boxZ = originZ - pivotZ;

                builder.texOffs(u, v)
                    .addBox(
                        boxX,
                        boxY,
                        boxZ,
                        sizeX, sizeY, sizeZ,
                        new CubeDeformation(inflate)
                    );
            }
        }

        float offsetX = -(pivotX - parentPivotX);
        float offsetY = -(pivotY - parentPivotY);
        float offsetZ = pivotZ - parentPivotZ;

        if (bone.parent == null) {
            offsetY += 24.0F;
        }

        PartDefinition part = parentPart.addOrReplaceChild(
            bone.name,
            builder,
            PartPose.offsetAndRotation(offsetX, offsetY, offsetZ, rotX, rotY, rotZ)
        );

        createdParts.put(bone.name, part);
    }
}
