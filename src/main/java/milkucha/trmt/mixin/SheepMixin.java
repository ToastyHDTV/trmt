package milkucha.trmt.mixin;

import milkucha.trmt.TRMTBlocks;
import milkucha.trmt.block.ErodedDirtBlock;
import milkucha.trmt.block.ErodedDirtPathBlock;
import milkucha.trmt.block.ErodedGrassBlock;
import milkucha.trmt.erosion.ErosionMapManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows sheep to eat ERODED blocks and restore them to GRASS_BLOCK.
 * Injects into canEatBlock to allow eating, and into eatBlock to perform the conversion.
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Sheep")
public class SheepMixin {
    
    @Unique
    private BlockPos trmt$lastEatenPos = null;

    /**
     * Allow sheep to eat our eroded blocks by spoofing the check.
     */
    @Inject(method = "canEatBlock", at = @At("HEAD"), cancellable = true)
    private void trmt$canEatErodedBlocks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        Block block = state.getBlock();
        if (block == TRMTBlocks.ERODED_DIRT_PATH ||
            block == TRMTBlocks.ERODED_GRASS_BLOCK ||
            block == TRMTBlocks.ERODED_DIRT ||
            block == TRMTBlocks.ERODED_COARSE_DIRT) {
            cir.setReturnValue(true);
        }
    }

    /**
     * When a sheep eats one of our blocks, convert it to GRASS_BLOCK.
     */
    @Inject(method = "eatBlock", at = @At("HEAD"), cancellable = true)
    private void trmt$eatErodedBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        try {
            Object thisObj = (Object) this;
            var world = (net.minecraft.world.level.Level) thisObj.getClass().getMethod("level").invoke(thisObj);
            
            if (world == null || world.isClientSide()) return;
            
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            
            // Prevent eating the same block twice
            if (pos.equals(trmt$lastEatenPos)) return;
            
            if (block == TRMTBlocks.ERODED_DIRT_PATH ||
                block == TRMTBlocks.ERODED_GRASS_BLOCK ||
                block == TRMTBlocks.ERODED_DIRT ||
                block == TRMTBlocks.ERODED_COARSE_DIRT) {
                
                trmt$lastEatenPos = pos.immutable();
                
                // Convert to GRASS_BLOCK
                world.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                ErosionMapManager.getInstance().removeEntry(pos);
                
                cir.setReturnValue(true);
            }
        } catch (Exception ignored) {
            // Fallback
        }
    }
}
