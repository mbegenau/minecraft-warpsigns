package net.reyemxela.warpsigns;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;

import java.util.Collection;


public class PairingInfo {
    public final Coords pairedSign;
    public final Coords pairedSignDest;
    public final int facing;
    private static final int warpOffset = 1;

    public PairingInfo(final Coords signCoords, final SignBlockEntity signEntity) {
        this.pairedSign = signCoords;
        this.facing = getFacing(signEntity);
        this.pairedSignDest = getDestCoords(signCoords, this.facing);
    }

    public PairingInfo(final Coords pairedSign, final Coords pairedSignDest, final int facing) {
        this.pairedSign = pairedSign;
        this.facing = facing;
        this.pairedSignDest = pairedSignDest;
    }

    public String getKey() {
        return pairedSign.getKey();
    }


    private int getFacing(final SignBlockEntity entity) {
//        0  (0)   faces south (+Z)
//        4  (90)  faces west  (-X)
//        8  (180) faces north (-Z)
//        12 (270) faces east  (+X)

        final var state = entity.getCachedState();
        final var props = state.getProperties();
        int yaw = 0;
        if (props.contains(SignBlock.ROTATION)) {
            yaw = ((state.get(SignBlock.ROTATION) + 2) % 16) / 4 * 90;
        } else if (props.contains(WallSignBlock.FACING)) {
            yaw = (int) state.get(WallSignBlock.FACING).asRotation();
        }
        return yaw;
    }

    private Coords getDestCoords(final Coords coords, final int facing) {
        Coords newCoords = new Coords(coords).offset(switch (facing) {
            case 90 -> Direction.WEST;
            case 180 -> Direction.NORTH;
            case 270 -> Direction.EAST;
            /*case 0, */ default -> Direction.SOUTH;
        }, warpOffset);

        if (isEmpty(newCoords)) {
            for (int i = 0; i < 5; i++) {
                if (!isEmpty(newCoords.offset(Direction.DOWN, i))) {
                    // found ground, go back up one
                    return newCoords.offset(Direction.DOWN, i - 1);
                }
            }
        }
        // no empty space found, fall back to the sign's position
        return coords;
    }

    private boolean isEmpty(final Coords coords) {
        final ServerWorld world = coords.getWorld();
        return world.getBlockState(coords).getBlock() == Blocks.AIR
                && world.getBlockState(coords.offset(Direction.UP, 1)).getBlock() == Blocks.AIR;
    }
}
