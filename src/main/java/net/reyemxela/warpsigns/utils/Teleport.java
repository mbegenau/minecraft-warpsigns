package net.reyemxela.warpsigns.utils;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.reyemxela.warpsigns.Coords;
import net.reyemxela.warpsigns.WarpSigns;

/**
 * The Teleport class provides a static method to teleport a player to a destination specified by warp signs.
 */
public class Teleport {
    /**
     * Moves the player to a specified location, triggering teleportation effects and sounds.
     *
     * @param player     The player to teleport.
     * @param signCoords The coordinates of the sign triggering the teleport.
     */
    public static void teleport(final ServerPlayerEntity player, final Coords signCoords) {
        final var destInfo = WarpSigns.warpSignData.get(signCoords.getKey());
        final var newWorld = destInfo.pairedSignDest.getWorld();
        final var prevWorld = player.getWorld();
        final var prevPos = player.getBlockPos();

        prevWorld.playSound(null, prevPos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1f, 1f);
        prevWorld.sendEntityStatus(player, EntityStatuses.ADD_PORTAL_PARTICLES);

        player.teleport(
                newWorld,
                destInfo.pairedSignDest.getX() + 0.5f,
                destInfo.pairedSignDest.getY(),
                destInfo.pairedSignDest.getZ() + 0.5f,
                destInfo.facing,
                player.getPitch()
        );

        newWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1f, 1f);
        newWorld.sendEntityStatus(player, EntityStatuses.ADD_PORTAL_PARTICLES);
    }
}
