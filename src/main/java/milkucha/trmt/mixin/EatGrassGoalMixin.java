package milkucha.trmt.mixin;

import milkucha.trmt.TRMTBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects into EatGrassGoal.canEatBlock() to allow sheep to eat TRMT custom blocks.
 * This is called BEFORE tick(), so sheep will recognize and eat our blocks.
 */
@Mixin(targets = "net.minecraft.world.entity.ai.goal.EatGrassGoal")
public class EatGrassGoalMixin {

    /**
     * Inject into canEatBlock() to allow eating our custom eroded blocks.
     * Return true if it's one of our blocks, otherwise let vanilla logic proceed.
     */
    @Inject(method = "canEatBlock", 
            at = @At("HEAD"), 
            cancellable = true)
    private void trmt$allowEatingCustomBlocks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        Block block = state.getBlock();
        
        // Check if it's one of our custom eroded blocks
        if (block == TRMTBlocks.ERODED_DIRT_PATH ||
            block == TRMTBlocks.ERODED_GRASS_BLOCK ||
            block == TRMTBlocks.ERODED_DIRT ||
            block == TRMTBlocks.ERODED_COARSE_DIRT) {
            
            // Tell the game sheep CAN eat this block
            cir.setReturnValue(true);
        }
    }
}
