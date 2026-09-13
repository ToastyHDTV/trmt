package milkucha.trmt.mixin;

import milkucha.trmt.TRMTBlocks;
import milkucha.trmt.erosion.ErosionMapManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows sheep to eat ERODED_DIRT_PATH, ERODED_GRASS_BLOCK, and ERODED_DIRT blocks,
 * converting them to GRASS_BLOCK. Works with Minecraft 1.21.1 Fabric API.
 */
@Mixin(Sheep.class)
public class SheepMixin {

    @Inject(method = "eatBlock", at = @At("HEAD"), cancellable = true)
    private void trmt$eatErodedBlocks(CallbackInfo ci) {
        Sheep sheep = (Sheep) (Object) this;
        Level world = sheep.level();
        
        if (world.isClientSide()) return;
        
        BlockPos pos = sheep.blockPosition();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Check if it's one of our eroded blocks
        if (block == TRMTBlocks.ERODED_DIRT_PATH || 
            block == TRMTBlocks.ERODED_GRASS_BLOCK || 
            block == TRMTBlocks.ERODED_DIRT ||
            block == TRMTBlocks.ERODED_COARSE_DIRT) {
            
            // Convert to GRASS_BLOCK
            world.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            ErosionMapManager.getInstance().removeEntry(pos);
            ci.cancel();
        }
    }
}
