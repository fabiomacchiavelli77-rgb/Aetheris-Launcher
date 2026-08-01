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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FinderQueue {

    private static final AtomicLong TASK_GENERATION = new AtomicLong(0);
    private final static FinderQueue INSTANCE = new FinderQueue();
    private static final Logger log = LoggerFactory.getLogger(FinderQueue.class);
    public static ExecutorService SERVICE = Executors.newFixedThreadPool(5);

    public FinderControl finderControl = new FinderControl();
    private volatile Set<Cuboid> currentCuboids = Collections.emptySet();

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

        long currentGen = TASK_GENERATION.get();

        getActiveFinderTypes().forEach(type -> {
            SERVICE.submit(() -> {
                try {
                    if (currentGen != TASK_GENERATION.get() || !Config.get().active) return;

                    List<Finder> finders = type.finderBuilder.build(world, chunkPos);

                    finders.forEach(finder -> {
                        if (currentGen != TASK_GENERATION.get() || !Config.get().active) return;
                        if (finder.isValidDimension(world.dimensionType())) {
                            finder.findInChunk();
                            if (currentGen == TASK_GENERATION.get() && Config.get().active) {
                                this.finderControl.addFinder(type, finder);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void extractCuboids(Camera camera) {
        if (!Config.get().active || Config.get().render == Config.RenderType.OFF) {
            this.currentCuboids = Collections.emptySet();
            return;
        }
        Set<Cuboid> cuboids = new HashSet<>();
        this.finderControl.getActiveFinders().forEach(finder -> {
            if (finder.shouldRender()) {
                finder.cuboids.forEach(cuboid -> cuboids.add(cuboid.offset(camera)));
            }
        });
        this.currentCuboids = cuboids;
    }

    public void renderCuboids(MultiBufferSource submitter, PoseStack poseStack) {
        if (!Config.get().active || Config.get().render == Config.RenderType.OFF) {
            this.currentCuboids = Collections.emptySet();
            return;
        }
        Set<Cuboid> cuboids = this.currentCuboids;
        if (cuboids == null || cuboids.isEmpty()) {
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
        if (TASK_GENERATION != null) {
            TASK_GENERATION.incrementAndGet();
        }
        if (this.finderControl != null) {
            this.finderControl.deleteFinders();
        } else {
            this.finderControl = new FinderControl();
        }
        this.currentCuboids = Collections.emptySet();
    }
}
