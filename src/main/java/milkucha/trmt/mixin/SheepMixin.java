package milkucha.trmt.mixin;

import milkucha.trmt.TRMTBlocks;
import milkucha.trmt.erosion.ErosionMapManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows sheep to graze on ERODED_DIRT_PATH and other eroded blocks.
 * Injects into the GrassBlock eating logic to extend it to our custom blocks.
 */
@Mixin(targets = "net.minecraft.world.level.block.GrassBlock")
public class SheepMixin {

    @Inject(method = "performBonemeal", at = @At("HEAD"), cancellable = true)
    private void trmt$allowEatingErodedBlocks(Level world, net.minecraft.util.RandomSource random, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (world.isClientSide()) return;
        
        // This is called when a sheep grazes
        BlockPos belowPos = pos.below();
        BlockState belowState = world.getBlockState(belowPos);
        Block block = belowState.getBlock();

        // Check if it's one of our eroded blocks
        if (block == TRMTBlocks.ERODED_DIRT_PATH || 
            block == TRMTBlocks.ERODED_GRASS_BLOCK || 
            block == TRMTBlocks.ERODED_DIRT ||
            block == TRMTBlocks.ERODED_COARSE_DIRT) {
            
            // Convert to GRASS_BLOCK
            world.setBlock(belowPos, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            ErosionMapManager.getInstance().removeEntry(belowPos);
            cir.setReturnValue(true);
        }
    }
}
