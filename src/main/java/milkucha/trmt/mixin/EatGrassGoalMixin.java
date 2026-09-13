package milkucha.trmt.mixin;

import milkucha.trmt.TRMTBlocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Extends EatGrassGoal to allow eating of custom eroded blocks.
 * The goal checks BlockTags.SHEEP_FOOD which we've populated in our data pack.
 */
@Mixin(targets = "net.minecraft.world.entity.ai.goal.EatGrassGoal")
public class EatGrassGoalMixin {

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void trmt$extendCanEat(CallbackInfoReturnable<Boolean> cir) {
        // The parent canUse() method already checks BlockTags.SHEEP_FOOD
        // which we've added our blocks to. No need to override - just let it pass through.
    }
}
