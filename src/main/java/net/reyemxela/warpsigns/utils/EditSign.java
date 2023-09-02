package net.reyemxela.warpsigns.utils;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class EditSign {
    public static void editSign(ServerPlayerEntity player, SignBlockEntity sign) {
        player.openEditSignScreen(sign, true);
    }
}
