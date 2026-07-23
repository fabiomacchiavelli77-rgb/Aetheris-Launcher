package kaptainwutax.seedcrackerX.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.seedfinding.mcfeature.structure.RegionStructure;
import kaptainwutax.seedcrackerX.SeedCracker;
import kaptainwutax.seedcrackerX.config.StructureSave;
import kaptainwutax.seedcrackerX.cracker.DataAddedEvent;
import kaptainwutax.seedcrackerX.cracker.storage.DataStorage;
import kaptainwutax.seedcrackerX.util.Log;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class DataCommand extends ClientCommand {

    @Override
    public String getName() {
        return "data";
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        builder.executes(this::printBits);

        builder.then(literal("clear")
                .executes(this::clear)
        );

        builder.then(literal("bits")
                .executes(this::printBits)
        );

        builder.then(literal("restore")
                .executes(this::restoreData)
        );
    }

    public int clear(CommandContext<FabricClientCommandSource> context) {
        SeedCracker.get().reset();

        sendFeedback(Language.getInstance().getOrDefault("data.clearData"), ChatFormatting.GREEN);
        return 0;
    }

    public int printBits(CommandContext<FabricClientCommandSource> context) {
        DataStorage s = SeedCracker.get().getDataStorage();
        int baseBits = (int) s.getBaseBits();
        int wantedBits = (int) s.getWantedBits();
        int liftingBits = (int) s.getLiftingBits();

        sendFeedback("§e[SeedCrackerX] Data Progress:", ChatFormatting.YELLOW);
        sendFeedback("  - Base Bits: §a" + baseBits + " / " + wantedBits + " §7(Structures/Decorators)", ChatFormatting.GRAY);
        sendFeedback("  - Lifting Bits: §b" + liftingBits + " / 40 §7(World Seed calculation)", ChatFormatting.GRAY);

        if (baseBits >= wantedBits && liftingBits >= 40) {
            sendFeedback("§a[+] Ready to crack world seed! Run /seedcracker cracker data", ChatFormatting.GREEN);
        } else {
            sendFeedback("§c[-] Need more structures/pillars to calculate world seed.", ChatFormatting.RED);
        }
        return 0;
    }

    private int restoreData(CommandContext<FabricClientCommandSource> context) {
        var preloaded = StructureSave.loadStructures();
        if (!preloaded.isEmpty()) {
            for (RegionStructure.Data<?> data : preloaded) {
                SeedCracker.get().getDataStorage().addBaseData(data, DataAddedEvent.POKE_LIFTING);
            }
            Log.warn("data.restoreStructures",preloaded.size());
        } else {
            Log.warn("data.restoreFailed");
        }
        return 0;
    }

}

