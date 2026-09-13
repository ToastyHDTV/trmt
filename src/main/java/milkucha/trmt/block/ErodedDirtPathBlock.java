package milkucha.trmt.block;

import milkucha.trmt.TRMTBlocks;
import milkucha.trmt.TRMTConfig;
import milkucha.trmt.erosion.BlockThresholds;
import milkucha.trmt.erosion.ChunkErosionMap;
import milkucha.trmt.erosion.ErosionEntry;
import milkucha.trmt.erosion.ErosionMapManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Final erosion stage for dirt after extended foot-traffic.
 * Visually appears like a dirt path with reduced height (similar to vanilla dirt_path).
 * Stores a {@link #FACING} direction to preserve rotation.
 * De-erodes back to eroded_dirt and continues the regeneration chain.
 * Never placed by players or generated naturally — only set by the erosion system.
 * 
 * Right-click with bone meal to instantly regenerate to GRASS_BLOCK.
 * Sheep and other animals can eat this block to restore grass.
 */
public class ErodedDirtPathBlock extends Block {

    /** Collision and outline shape matching vanilla dirt_path (15 pixels tall, 1 pixel lower). */
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16);

    /** Preserves the rotation established during earlier erosion stages. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ErodedDirtPathBlock(Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, sourceBlock, notify);
        if (!world.isClientSide() && world.getBlockState(pos.above()).canOcclude()) {
            world.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        // Right-click with bone meal to regenerate
        if (itemStack.is(Items.BONE_MEAL)) {
            if (!world.isClientSide()) {
                world.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                
                // Consume bone meal
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                
                // Particle effect (optional visual feedback)
                world.levelEvent(2005, pos, 0);
            }
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (world.getBlockState(pos.above()).canOcclude()) {
            world.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }

        if (!TRMTConfig.get().deErosion.dirtEnabled) return;

        ErosionMapManager manager = ErosionMapManager.getInstance();
        ChunkErosionMap chunkMap = manager.getChunkMap(ChunkPos.containing(pos));
        ErosionEntry entry = chunkMap != null ? chunkMap.getEntry(pos) : null;

        long currentTime = world.getGameTime();
        long timeout = BlockThresholds.getDirtPathDeErosionTimeout();
        if (BlockThresholds.isIsolated(world, pos, manager)) timeout /= 2;
        if (entry != null && currentTime - entry.getLastTouchedGameTime() <= timeout) return;
        long newCooldownTime = (entry != null) ? entry.getLastTouchedGameTime() + timeout : currentTime;

        Direction facing = state.getValue(FACING);

        // De-erode to eroded_dirt at its most-eroded stage (stage 3).
        world.setBlock(pos,
                TRMTBlocks.ERODED_DIRT.defaultBlockState()
                        .setValue(ErodedDirtBlock.FACING, facing)
                        .setValue(ErodedDirtBlock.STAGE, 3),
                Block.UPDATE_ALL);
        manager.removeEntry(pos);
        manager.writeCooldownEntry(pos, TRMTBlocks.ERODED_DIRT, newCooldownTime);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Allows animals (sheep, etc.) to eat this block and restore grass.
     * Returning true indicates the block can be eaten.
     */
    public boolean isValidBonemealTarget(BlockGetter world, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    /**
     * When bone meal is applied by animals or other sources, convert to GRASS_BLOCK.
     */
    public boolean performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        world.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }
}
