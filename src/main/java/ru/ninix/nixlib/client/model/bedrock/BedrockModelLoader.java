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
                    throw new RuntimeException("BedrockModelLoader: No geometry found in " + location);
                }

                BedrockPOJO.Geometry geo = pojo.geometryList.get(0);
                if (geometryName != null) {
                    for (BedrockPOJO.Geometry g : pojo.geometryList) {
                        if (g.description.identifier.equals(geometryName)) {
                            geo = g;
                            break;
                        }
                    }
                }
                return createDefinition(geo);
            }
        } catch (IOException e) {
            NixLib.LOGGER.error("Failed to load Bedrock model: " + location, e);
            return LayerDefinition.create(new MeshDefinition(), 64, 64);
        }
    }

    private static LayerDefinition createDefinition(BedrockPOJO.Geometry geo) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        Map<String, BedrockPOJO.Bone> boneDataMap = new HashMap<>();
        Map<String, PartDefinition> createdParts = new HashMap<>();
        Map<String, float[]> absolutePivots = new HashMap<>();

        if (geo.bones != null) {
            for (BedrockPOJO.Bone bone : geo.bones) {
                boneDataMap.put(bone.name, bone);
            }
            for (BedrockPOJO.Bone bone : geo.bones) {
                if (!createdParts.containsKey(bone.name)) {
                    buildBone(bone, boneDataMap, createdParts, absolutePivots, root);
                }
            }
        }

        int texW = (int) geo.description.texture_width;
        int texH = (int) geo.description.texture_height;
        if (texW <= 0) texW = 64;
        if (texH <= 0) texH = 64;

        return LayerDefinition.create(mesh, texW, texH);
    }

    private static void buildBone(BedrockPOJO.Bone bone,
                                  Map<String, BedrockPOJO.Bone> boneMap,
                                  Map<String, PartDefinition> createdParts,
                                  Map<String, float[]> absolutePivots,
                                  PartDefinition root) {

        PartDefinition parentPart = root;
        float pX = 0, pY = 0, pZ = 0;

        if (bone.parent != null) {
            BedrockPOJO.Bone parentBone = boneMap.get(bone.parent);
            if (parentBone != null) {
                if (!createdParts.containsKey(parentBone.name)) {
                    buildBone(parentBone, boneMap, createdParts, absolutePivots, root);
                }
                parentPart = createdParts.get(parentBone.name);
                float[] pp = absolutePivots.get(parentBone.name);
                pX = pp[0]; pY = pp[1]; pZ = pp[2];
            }
        }

        float bX = bone.pivot != null ? bone.pivot.get(0) : 0;
        float bY = bone.pivot != null ? bone.pivot.get(1) : 0;
        float bZ = bone.pivot != null ? bone.pivot.get(2) : 0;
        absolutePivots.put(bone.name, new float[]{bX, bY, bZ});

        float rotX = bone.rotation != null ? (float) Math.toRadians(-bone.rotation.get(0)) : 0;
        float rotY = bone.rotation != null ? (float) Math.toRadians(-bone.rotation.get(1)) : 0;
        float rotZ = bone.rotation != null ? (float) Math.toRadians(bone.rotation.get(2)) : 0;

        CubeListBuilder builder = CubeListBuilder.create();
        if (bone.cubes != null) {
            for (BedrockPOJO.Cube cube : bone.cubes) {
                float originX = cube.origin.get(0);
                float originY = cube.origin.get(1);
                float originZ = cube.origin.get(2);

                float sizeX = cube.size.get(0);
                float sizeY = cube.size.get(1);
                float sizeZ = cube.size.get(2);

                float inflate = cube.inflate != null ? cube.inflate : (bone.inflate != null ? bone.inflate : 0);
                boolean mirror = cube.mirror != null ? cube.mirror : (bone.mirror);

                int u = 0, v = 0;
                if (cube.uv != null && cube.uv.isJsonArray()) {
                    JsonArray arr = cube.uv.getAsJsonArray();
                    u = arr.get(0).getAsInt();
                    v = arr.get(1).getAsInt();
                }

                builder.mirror(mirror);
                builder.texOffs(u, v);

                float relX = originX - bX;
                float relY = -(originY + sizeY) + bY;
                float relZ = originZ - bZ;


                builder.addBox(relX, relY, relZ, sizeX, sizeY, sizeZ, new CubeDeformation(inflate));
            }
        }

        float offsetX = bX - pX;
        float offsetY = -(bY - pY);
        float offsetZ = bZ - pZ;

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
