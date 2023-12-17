package net.reyemxela.warpsigns.utils;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.reyemxela.warpsigns.Coords;
import net.reyemxela.warpsigns.PairingInfo;
import net.reyemxela.warpsigns.WarpSigns;

import java.util.HashMap;
import java.util.Objects;

/**
 * The Pairing class is responsible for managing the pairing functionality of the WarpSigns mod.
 * It provides methods for starting and finishing pairings, handling sign interactions, and breaking signs.
 */
public class Pairing {
    public static final HashMap<String, PairingInfo> playerPairingSign = new HashMap<>();
    public static final HashMap<String, String> signPairingPlayer = new HashMap<>();
    public static final String globalPairingName = "__GLOBAL_PAIRING__";

    private static ServerPlayerEntity globalPairingPlayer;

    /**
     * Starts pairing between a player and a sign.
     *
     * @param player      The player involved in the pairing.
     * @param pairingName The name of the pairing.
     * @param signEntity  The SignBlockEntity representing the sign.
     * @param signCoords  The coordinates of the sign.
     */
    public static void startPairing(final ServerPlayerEntity player, final String pairingName,
                                    final SignBlockEntity signEntity, final Coords signCoords) {
        final var startInfo = new PairingInfo(signCoords, signEntity);
        playerPairingSign.put(pairingName, startInfo);
        signPairingPlayer.put(startInfo.getKey(), pairingName);

        if (Objects.equals(pairingName, globalPairingName)) {
            globalPairingPlayer = player; // keep track of both players to notify when pairing is finished
            player.sendMessage(Text.of("Started global pairing"));
        } else {
            player.sendMessage(Text.of("Started pairing"));
        }
        WarpSigns.LOGGER.info("Started pairing sign at " + signCoords);
    }

    /**
     * Finish pairing between a player and a sign.
     *
     * @param player      The player involved in the pairing.
     * @param pairingName The name of the pairing.
     * @param signEntity  The SignBlockEntity representing the sign.
     * @param signCoords  The coordinates of the sign.
     */
    public static void finishPairing(final ServerPlayerEntity player, final String pairingName,
                                     final SignBlockEntity signEntity, final Coords signCoords) {
        final var startInfo = playerPairingSign.get(pairingName);
        final var endInfo = new PairingInfo(signCoords, signEntity);
        WarpSigns.warpSignData.put(startInfo.getKey(), endInfo);
        WarpSigns.warpSignData.put(endInfo.getKey(), startInfo);

        if (Objects.equals(pairingName, globalPairingName)) {
            player.sendMessage(Text.of("Finished global pairing"));
            if (player != globalPairingPlayer)
                globalPairingPlayer.sendMessage(Text.of("Finished global pairing"));
            globalPairingPlayer = null;
        } else {
            player.sendMessage(Text.of("Finished pairing"));
        }
        WarpSigns.LOGGER.info(String.format("Finished pairing %s in %s to %s in %s",
                startInfo.pairedSign.getStr(),
                startInfo.pairedSign.getWorld(),
                endInfo.pairedSign.getStr(),
                endInfo.pairedSign.getWorld()));

        player.getMainHandStack().decrement(1);
        playerPairingSign.remove(pairingName);
        signPairingPlayer.remove(startInfo.getKey());
        Save.saveData();
    }

    /**
     * Use a sign.
     *
     * @param player The player interacting with the sign.
     * @param world  The world where the sign is located.
     * @param hand   The hand used to interact with the sign.
     * @param sign   The SignBlockEntity representing the sign.
     * @param pos    The coordinates of the sign.
     * @return The result of using the sign.
     */
    public static ActionResult useSign(final ServerPlayerEntity player, final World world, Hand hand,
                                       final SignBlockEntity sign, final BlockPos pos) {
        final var signCoords = new Coords(pos.getX(), pos.getY(), pos.getZ(), (ServerWorld) world);
        final var signKey = signCoords.getKey();
        final var heldItem = player.getStackInHand(hand).getItem();

        final var isSignPairing = signPairingPlayer.containsKey(signKey);
        final var isSignPaired = WarpSigns.warpSignData.containsKey(signKey);
        final var holdingPairingItem =
                heldItem == Registries.ITEM.get(Identifier.tryParse(WarpSigns.config.pairingItem));
        final var holdingAir = heldItem == Items.AIR;
        final var isSneaking = player.isSneaking();

        if (holdingPairingItem) {
            final var pairingName = isSneaking ? globalPairingName : player.getName().getString();
            final var isPlayerPairing = playerPairingSign.containsKey(pairingName);

            if (isSignPaired) {
                return ActionResult.PASS;
            }
            if (!isAllowed(player)) {
                player.sendMessage(Text.of("You don't have permission to pair signs"));
                return ActionResult.PASS;
            }

            if (isSignPairing) {
                if (Objects.equals(signPairingPlayer.get(signKey), player.getName().getString())) {
                    playerPairingSign.remove(pairingName); // same player clicked same sign, cancel pairing
                    signPairingPlayer.remove(signKey);
                    player.sendMessage(Text.of("Cancelled pairing"));
                }
                return ActionResult.PASS;
            }

            if (isPlayerPairing) {
                finishPairing(player, pairingName, sign, signCoords);
            } else {
                startPairing(player, pairingName, sign, signCoords);
            }

            return ActionResult.SUCCESS;
        } else if (holdingAir) {
            if (isSneaking) {
                if (!isSignPaired || isAllowed(player)) {
                    EditSign.editSign(player, sign);
                    return ActionResult.CONSUME;
                } else {
                    player.sendMessage(Text.of("You don't have permission to edit this sign"));
                }
            } else if (isSignPaired) {
                Teleport.teleport(player, signCoords);
                return ActionResult.SUCCESS;
            } else {
                Help.showHelp(player);
            }
        }

        return ActionResult.PASS;
    }

    /**
     * Break a sign.
     *
     * @param world  The server world where the sign is located.
     * @param player The player breaking the sign.
     * @param pos    The coordinates of the sign.
     * @return True if the sign is successfully broken, false otherwise.
     */
    public static boolean breakSign(final ServerWorld world, final ServerPlayerEntity player, final BlockPos pos) {
        // TODO:
        //  when cancelling a sign break, the client will desync and not show the text on the sign.
        //  the text is still there, and disconnecting/reconnecting to the server will show it again.
        //  not sure if this is something that can easily be fixed or not.
        final var brokenSignCoords = new Coords(pos.getX(), pos.getY(), pos.getZ(), world);
        final var brokenSignKey = brokenSignCoords.getKey();
        final var isSignPaired = WarpSigns.warpSignData.containsKey(brokenSignKey);
        final var isSignPairing = signPairingPlayer.containsKey(brokenSignKey);
        if (isSignPaired) {
            final var otherSignInfo = WarpSigns.warpSignData.get(brokenSignKey);
            if (player != null) {
                if (!isAllowed(player)) {
                    player.sendMessage(Text.of("You don't have permission to break this sign"));
                    return false;
                }
                if (player.isSneaking()) {
                    final var pairingName = player.getName().getString();
                    if (playerPairingSign.containsKey(pairingName)) {
                        player.sendMessage(Text.of("Pairing already in progress, can't start re-pairing"));
                        return false;
                    }
                    playerPairingSign.put(pairingName, otherSignInfo);
                    signPairingPlayer.put(otherSignInfo.getKey(), pairingName);
                    player.sendMessage(Text.of("Re-pairing"));
                } else {
                    player.sendMessage(Text.of("Warp Sign removed"));
                }
            }
            WarpSigns.warpSignData.remove(otherSignInfo.getKey());
            WarpSigns.warpSignData.remove(brokenSignKey);
            final var stack = new ItemStack(Registries.ITEM.get(Identifier.tryParse(WarpSigns.config.pairingItem)));
            final var itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            world.spawnEntity(itemEntity);
            Save.saveData();
        } else if (isSignPairing) {
            if (player != null && !isAllowed(player)) {
                player.sendMessage(Text.of("You don't have permission to break this sign"));
            }
            playerPairingSign.remove(signPairingPlayer.get(brokenSignKey));
            signPairingPlayer.remove(brokenSignKey);
        }
        return true;
    }

    /**
     * Check if a player is allowed to break a sign.
     *
     * @param player The player to check.
     * @return True if the player is allowed to break the sign, false otherwise.
     */
    private static boolean isAllowed(final ServerPlayerEntity player) {
        return !WarpSigns.config.adminOnly
                || WarpSigns.serverInstance.getPlayerManager().isOperator(player.getGameProfile());
    }
}
