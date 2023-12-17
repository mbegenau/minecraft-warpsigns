package net.reyemxela.warpsigns.utils;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * The EditSign class provides a static method for players to edit a sign.
 */
public class EditSign {
    /**
     * Opens the edit sign screen for the given player and sign.
     *
     * @param player The player who wants to edit the sign.
     * @param sign   The sign block entity to be edited.
     */
    public static void editSign(final ServerPlayerEntity player, final SignBlockEntity sign) {
        player.openEditSignScreen(sign, true);
    }
}
