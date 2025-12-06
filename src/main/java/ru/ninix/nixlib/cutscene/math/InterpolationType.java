package ru.ninix.nixlib.cutscene.math;

import com.google.gson.annotations.SerializedName;

public enum InterpolationType {
    @SerializedName("catmull-rom") CATMULL_ROM,
    @SerializedName("linear") LINEAR,
    @SerializedName("ease-in-sine") EASE_IN_SINE,
    @SerializedName("ease-out-sine") EASE_OUT_SINE,
    @SerializedName("ease-in-out-sine") EASE_IN_OUT_SINE,
    @SerializedName("ease-in-quad") EASE_IN_QUAD,
    @SerializedName("ease-out-quad") EASE_OUT_QUAD,
    @SerializedName("ease-in-out-quad") EASE_IN_OUT_QUAD,
    @SerializedName("ease-in-cubic") EASE_IN_CUBIC,
    @SerializedName("ease-out-cubic") EASE_OUT_CUBIC,
    @SerializedName("ease-in-out-cubic") EASE_IN_OUT_CUBIC,
    @SerializedName("ease-in-expo") EASE_IN_EXPO,
    @SerializedName("ease-out-expo") EASE_OUT_EXPO,
    @SerializedName("ease-in-out-expo") EASE_IN_OUT_EXPO,
    @SerializedName("ease-in-back") EASE_IN_BACK,
    @SerializedName("ease-out-back") EASE_OUT_BACK,
    @SerializedName("ease-in-out-back") EASE_IN_OUT_BACK
}
