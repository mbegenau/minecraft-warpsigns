package net.reyemxela.warpsigns.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.HangingSignBlock;
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
 * This class is a mixin for the HangingSignBlock class that provides additional functionality for handling neighbor updates.
 */
@Mixin(HangingSignBlock.class)
public abstract class HangingSignBlockMixin {
    @Inject(at = @At("HEAD"), method = "getStateForNeighborUpdate")
    private void neighborUpdate(final BlockState state, final Direction direction, final BlockState neighborState,
                                final WorldAccess world, final BlockPos pos, final BlockPos neighborPos,
                                final CallbackInfoReturnable<BlockState> info) {
        if (world.isClient()) {
            return;
        }
        if (!((HangingSignBlock) (Object) this).canPlaceAt(state, world, pos)) {
            Pairing.breakSign((ServerWorld) world, null, pos);
        }
    }
}