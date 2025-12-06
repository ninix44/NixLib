package ru.ninix.nixlib.cutscene;

import java.util.ArrayList;
import java.util.List;

public class Cutscene {
    public List<Keyframe> keyframes = new ArrayList<>();
    public boolean useEntity = false;
    public String entityName;
    public boolean lookAtEntity = false;
}
