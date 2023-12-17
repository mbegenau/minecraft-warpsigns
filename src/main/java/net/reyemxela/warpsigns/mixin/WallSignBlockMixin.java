package net.reyemxela.warpsigns.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.reyemxela.warpsigns.utils.Pairing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixin class modifies the behavior of the WallSignBlock class by injecting code into the
 * `getStateForNeighborUpdate` method. The injected code checks if the block state can still be
 * placed at its current position in the world. If it cannot, it breaks the sign using the
 * Pairing.breakSign method.
 */
@Mixin(WallSignBlock.class)
public abstract class WallSignBlockMixin {
    /**
     * Updates the state of a block when one of its neighbors changes.
     *
     * @param state         The current state of the block.
     * @param direction     The direction from which the neighbor update occurred.
     * @param neighborState The state of the neighbor block that has changed.
     * @param world         The world in which the block exists.
     * @param pos           The position of the block.
     * @param neighborPos   The position of the neighbor block.
     * @param info          An object that can be modified to change the return value of the method.
     */
    @Inject(at = @At("HEAD"), method = "getStateForNeighborUpdate")
    private void neighborUpdate(final BlockState state, final Direction direction, final BlockState neighborState,
                                final WorldAccess world, final BlockPos pos, final BlockPos neighborPos,
                                final CallbackInfoReturnable<BlockState> info) {
        if (world.isClient()) {
            return;
        }
        if (!state.canPlaceAt(world, pos)) {
            Pairing.breakSign((ServerWorld) world, null, pos);
        }
    }
}
