package milkucha.trmt.mixin;

import milkucha.trmt.TRMTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects into EatGrassGoal.tick() to handle TRMT custom blocks.
 * When sheep eat our eroded blocks, we convert them to GRASS_BLOCK instead of DIRT.
 */
@Mixin(targets = "net.minecraft.world.entity.ai.goal.EatGrassGoal")
public class EatGrassGoalMixin {
    
    @Shadow private Mob mob;
    @Shadow private Level level;

    /**
     * Inject right before the setBlock(DIRT) call in tick().
     * If it's one of our custom blocks, handle it specially and cancel the vanilla logic.
     */
    @Inject(method = "tick", 
            at = @At(value = "INVOKE", 
                     target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
                     ordinal = 0),
            cancellable = true)
    private void trmt$handleCustomBlockEating(CallbackInfo ci) {
        BlockPos pos = this.mob.blockPosition().below();
        BlockState state = this.level.getBlockState(pos);
        Block block = state.getBlock();

        // Check if it's one of our custom eroded blocks
        if (block == TRMTBlocks.ERODED_DIRT_PATH ||
            block == TRMTBlocks.ERODED_GRASS_BLOCK ||
            block == TRMTBlocks.ERODED_DIRT ||
            block == TRMTBlocks.ERODED_COARSE_DIRT) {
            
            // Play particle effect for the eaten block
            this.level.levelEvent(2001, pos, Block.getId(state));
            
            // Tell the mob it ate (wool regrows if sheared)
            this.mob.ate();
            
            // Cancel the vanilla setBlock call to prevent DIRT conversion
            ci.cancel();
        }
    }
}
