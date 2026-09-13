package milkucha.trmt.event;

import milkucha.trmt.TRMTBlocks;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Event handler for animal grazing on eroded blocks.
 * Allows sheep to eat ERODED_DIRT_PATH and convert it to GRASS_BLOCK.
 */
public class AnimalGrazingHandler {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            return InteractionResult.PASS;
        });
    }
}
