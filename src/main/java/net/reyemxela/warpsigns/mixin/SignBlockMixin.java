package net.reyemxela.warpsigns.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
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
 * This class is a mixin for the SignBlock class.
 * It injects new functionality into the `getStateForNeighborUpdate` method.
 */
@Mixin(SignBlock.class)
public abstract class SignBlockMixin {
    /**
     * Updates the state of this block when a neighbor's state changes.
     *
     * @param state         The current state of this block.
     * @param direction     The direction from which the neighbor update is coming.
     * @param neighborState The state of the neighboring block that has changed.
     * @param world         The world in which this block is located.
     * @param pos           The position of this block.
     * @param neighborPos   The position of the neighboring block.
     * @param info          Additional information about the callback.
     */
    @Inject(at = @At("HEAD"), method = "getStateForNeighborUpdate")
    private void neighborUpdate(final BlockState state, final Direction direction, final BlockState neighborState,
                                final WorldAccess world, final BlockPos pos, final BlockPos neighborPos,
                                final CallbackInfoReturnable<BlockState> info) {
        if (world.isClient()) {
            return;
        }
        if (!((SignBlock) (Object) this).canPlaceAt(state, world, pos)) {
            Pairing.breakSign((ServerWorld) world, null, pos);
        }
    }
}