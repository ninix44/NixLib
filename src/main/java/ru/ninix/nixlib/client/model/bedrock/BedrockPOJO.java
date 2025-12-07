package ru.ninix.nixlib.client.model.bedrock;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BedrockPOJO {
    public String format_version;
    @SerializedName("minecraft:geometry")
    public List<Geometry> geometryList;

    public static class Geometry {
        public Description description;
        public List<Bone> bones;
    }

    public static class Description {
        public String identifier;
        public float texture_width;
        public float texture_height;
        public float visible_bounds_width;
        public float visible_bounds_height;
        public List<Float> visible_bounds_offset;
    }

    public static class Bone {
        public String name;
        public String parent;
        public List<Float> pivot;
        public List<Float> rotation;
        public List<Cube> cubes;
        public boolean mirror;
        public float inflate;
    }

    public static class Cube {
        public List<Float> origin;
        public List<Float> size;
        public List<Float> rotation;
        public List<Float> pivot;
        public JsonElement uv;
        public Float inflate;
        public Boolean mirror;
    }
}
