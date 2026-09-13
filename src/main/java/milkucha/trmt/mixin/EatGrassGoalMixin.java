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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects into EatGrassGoal to allow sheep to eat TRMT custom blocks.
 * - canStart(): Allows sheep to START eating our blocks
 * - tick(): Handles conversion to GRASS_BLOCK instead of DIRT
 */
@Mixin(targets = "net.minecraft.world.entity.ai.goal.EatGrassGoal")
public class EatGrassGoalMixin {
    
    @Shadow private Mob mob;
    @Shadow private Level level;

    /**
     * Inject into canStart() to allow starting eating our custom blocks.
     * The vanilla code only checks for SHORT_GRASS and GRASS_BLOCK.
     */
    @Inject(method = "canStart", 
            at = @At("HEAD"), 
            cancellable = true)
    private void trmt$allowStartEatingCustomBlocks(CallbackInfoReturnable<Boolean> cir) {
        BlockPos pos = this.mob.blockPosition();
        BlockState state = this.level.getBlockState(pos);
        Block block = state.getBlock();
        
        // Check if standing on our custom blocks
        if (block == TRMTBlocks.ERODED_DIRT_PATH ||
            block == TRMTBlocks.ERODED_GRASS_BLOCK ||
            block == TRMTBlocks.ERODED_DIRT ||
            block == TRMTBlocks.ERODED_COARSE_DIRT) {
            cir.setReturnValue(true);
            return;
        }
        
        // Also check the block below
        BlockState belowState = this.level.getBlockState(pos.below());
        Block belowBlock = belowState.getBlock();
        
        if (belowBlock == TRMTBlocks.ERODED_DIRT_PATH ||
            belowBlock == TRMTBlocks.ERODED_GRASS_BLOCK ||
            belowBlock == TRMTBlocks.ERODED_DIRT ||
            belowBlock == TRMTBlocks.ERODED_COARSE_DIRT) {
            cir.setReturnValue(true);
        }
    }
    
    /**
     * Inject into tick() to handle eating our custom blocks.
     * Convert them to GRASS_BLOCK instead of DIRT and call mob.ate().
     */
    @Inject(method = "tick", 
            at = @At(value = "INVOKE", 
                     target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
                     ordinal = 0),
            cancellable = true)
    private void trmt$handleCustomBlockEating(CallbackInfoReturnable<Integer> cir) {
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
            
            // Cancel the vanilla setBlock call to prevent unwanted conversion
            cir.cancel();
        }
    }
}
