package kaptainwutax.seedcrackerX.finder;

import com.mojang.blaze3d.vertex.PoseStack;
import kaptainwutax.seedcrackerX.config.Config;
import kaptainwutax.seedcrackerX.render.Cuboid;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class FinderQueue {

    private final static FinderQueue INSTANCE = new FinderQueue();
    private static final Logger log = LoggerFactory.getLogger(FinderQueue.class);
    public static ExecutorService SERVICE = Executors.newFixedThreadPool(5);


    public FinderControl finderControl = new FinderControl();

    private FinderQueue() {
        this.clear();
    }

    public static void registerEvents() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            FinderQueue.get().extractCuboids(context.camera());
            FinderQueue.get().renderCuboids(context.consumers(), context.matrixStack());
        });
    }

    public static FinderQueue get() {
        return INSTANCE;
    }

    public void onChunkData(Level world, ChunkPos chunkPos) {
        if (!Config.get().active) return;

        getActiveFinderTypes().forEach(type -> {
            SERVICE.submit(() -> {
                try {
                    List<Finder> finders = type.finderBuilder.build(world, chunkPos);

                    finders.forEach(finder -> {
                        if (finder.isValidDimension(world.dimensionType())) {
                            finder.findInChunk();
                            this.finderControl.addFinder(type, finder);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void extractCuboids(Camera camera) {
        if (Config.get().render == Config.RenderType.OFF) {
            // Note: RenderStateDataKey is usually tied to a state, but since we don't have it, we just store it locally. Wait!
            return;
        }
        Set<Cuboid> cuboids = new HashSet<>();
        this.finderControl.getActiveFinders().forEach(finder -> {
            if (finder.shouldRender()) {
                finder.cuboids.forEach(cuboid -> cuboids.add(cuboid.offset(camera)));
            }
        });
        // We will store it in a local variable or directly pass it to renderCuboids since we run them in the same event now!
        this.currentCuboids = cuboids;
    }

    private Set<Cuboid> currentCuboids = Collections.emptySet();

    public void renderCuboids(MultiBufferSource submitter, PoseStack poseStack) {
        Set<Cuboid> cuboids = this.currentCuboids;
        if (cuboids == null) {
            return;
        }
        cuboids.forEach(cuboid -> cuboid.render(poseStack, submitter));
    }

    public List<Finder.Type> getActiveFinderTypes() {
        return Arrays.stream(Finder.Type.values())
                .filter(type -> type.enabled.get())
                .collect(Collectors.toList());
    }

    public void clear() {
        this.finderControl = new FinderControl();
    }
}
