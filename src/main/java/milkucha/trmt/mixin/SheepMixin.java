package milkucha.trmt.mixin;

import milkucha.trmt.TRMTBlocks;
import milkucha.trmt.erosion.ErosionMapManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows sheep to eat ERODED_DIRT_PATH blocks and convert them to GRASS_BLOCK.
 * Also allows them to eat ERODED_GRASS_BLOCK and ERODED_DIRT.
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Sheep")
public class SheepMixin {

    @Inject(method = "eat", at = @At("HEAD"), cancellable = true)
    private void trmt$allowEatingErodedBlocks(Level world, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (world.isClientSide()) return;
        if (!(world instanceof ServerLevel)) return;

        Block block = state.getBlock();

        // Check if it's an eroded grass/dirt block
        if (block == TRMTBlocks.ERODED_GRASS_BLOCK || 
            block == TRMTBlocks.ERODED_DIRT || 
            block == TRMTBlocks.ERODED_DIRT_PATH ||
            block == TRMTBlocks.ERODED_COARSE_DIRT) {
            
            // Convert to GRASS_BLOCK
            world.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            
            // Remove from erosion tracking
            ErosionMapManager.getInstance().removeEntry(pos);
            
            // Trigger eating animation on this object
            try {
                Object thisObj = (Object) this;
                thisObj.getClass().getMethod("eatBlock").invoke(thisObj);
            } catch (Exception ignored) {
                // If method doesn't exist, that's okay - we still converted the block
            }
            
            // Don't call super, we've handled it
            ci.cancel();
        }
    }
}
