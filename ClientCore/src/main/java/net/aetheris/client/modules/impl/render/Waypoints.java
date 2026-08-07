package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class Waypoints extends Module {

    private final BooleanSetting showDistance = new BooleanSetting("showDistance", "Show Distance", "Mostra Distanza", true);
    private final SliderSetting markerScale = new SliderSetting("markerScale", "Marker Scale", "Dimensione Marker", 1.0, 0.5, 3.0, 0.1);

    private final List<Waypoint> waypoints = new ArrayList<>();

    public Waypoints() {
        super("Waypoints", "Mostra marker 3D personalizzati nel mondo.", Category.RENDER);
        addSetting(showDistance);
        addSetting(markerScale);
    }

    public static class Waypoint {
        private String name;
        private BlockPos pos;
        private int color;

        public Waypoint(String name, BlockPos pos, int color) {
            this.name = name;
            this.pos = pos;
            this.color = color;
        }

        public String getName() { return name; }
        public BlockPos getPos() { return pos; }
        public int getColor() { return color; }
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void addWaypoint(String name, BlockPos pos, int color) {
        waypoints.add(new Waypoint(name, pos, color));
    }

    public boolean removeWaypoint(String name) {
        return waypoints.removeIf(w -> w.getName().equalsIgnoreCase(name));
    }

    public BooleanSetting getShowDistance() {
        return showDistance;
    }

    public SliderSetting getMarkerScale() {
        return markerScale;
    }
}
