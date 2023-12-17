package net.reyemxela.warpsigns.utils;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.reyemxela.warpsigns.WarpSigns;

/**
 * The Help class provides a static method to display usage instructions for a given player.
 * This class is used in conjunction with the WarpSigns plugin.
 */
public class Help {
    /**
     * Displays the usage help for WarpSigns to the specified player.
     *
     * @param player the player to display the help to
     */
    public static void showHelp(final ServerPlayerEntity player) {
        player.sendMessage(Text.of("------ WarpSigns usage: ------"));
        player.sendMessage(Text.of(String.format("Pairing item: %s", WarpSigns.config.pairingItem)));
        player.sendMessage(Text.of("- Right-click with item to pair signs"));
        player.sendMessage(Text.of("- Sneak-right-click for global pairing (another player can finish pairing)"));
        player.sendMessage(Text.of("- Sneak-break a sign to break it and start re-pairing"));
        player.sendMessage(Text.of("- Sneak-right-click to edit sign text"));
    }
}
