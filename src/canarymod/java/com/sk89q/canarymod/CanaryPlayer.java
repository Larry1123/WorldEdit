/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.canarymod;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.bukkit.CUIChannelListener;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TargetBlock;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.world.World;
import net.canarymod.Canary;
import net.canarymod.api.GameMode;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.user.Group;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class CanaryPlayer implements Actor, com.sk89q.worldedit.entity.Player, Cloneable {

    protected Player player;

    public CanaryPlayer(Player player) {
        checkNotNull(player);
        this.player = player;
    }

    /**
     * Returns direction according to rotation. May return null.
     *
     * @param rot yaw
     *
     * @return the direction
     */
    private static PlayerDirection getDirection(double rot) {
        if (0 <= rot && rot < 22.5) {
            return PlayerDirection.SOUTH;
        }
        else if (22.5 <= rot && rot < 67.5) {
            return PlayerDirection.SOUTH_WEST;
        }
        else if (67.5 <= rot && rot < 112.5) {
            return PlayerDirection.WEST;
        }
        else if (112.5 <= rot && rot < 157.5) {
            return PlayerDirection.NORTH_WEST;
        }
        else if (157.5 <= rot && rot < 202.5) {
            return PlayerDirection.NORTH;
        }
        else if (202.5 <= rot && rot < 247.5) {
            return PlayerDirection.NORTH_EAST;
        }
        else if (247.5 <= rot && rot < 292.5) {
            return PlayerDirection.EAST;
        }
        else if (292.5 <= rot && rot < 337.5) {
            return PlayerDirection.SOUTH_EAST;
        }
        else if (337.5 <= rot && rot < 360.0) {
            return PlayerDirection.SOUTH;
        }
        else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public World getWorld() {
        return CanaryAdapter.adapt(player.getWorld());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHoldingPickAxe() {
        int item = getItemInHand();
        return item == ItemID.IRON_PICK || item == ItemID.WOOD_PICKAXE || item == ItemID.STONE_PICKAXE || item == ItemID.DIAMOND_PICKAXE || item == ItemID.GOLD_PICKAXE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerDirection getCardinalDirection(int yawOffset) {
        if (getPitch() > 67.5) {
            return PlayerDirection.DOWN;
        }
        if (getPitch() < -67.5) {
            return PlayerDirection.UP;
        }
        // From hey0's code
        double rot = (getYaw() + yawOffset) % 360; //let's use real yaw now
        if (rot < 0) {
            rot += 360.0;
        }
        return getDirection(rot);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemInHand() {
        if (player.getItemHeld() != null) {
            return player.getItemHeld().getId();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BaseBlock getBlockInHand() throws WorldEditException {
        final int typeId = getItemInHand();
        if (!getWorld().isValidBlockType(typeId)) {
            throw new NotABlockException(typeId);
        }
        return new BaseBlock(typeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void giveItem(int type, int amount) {
        player.getInventory().addItem(type, amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockBag getInventoryBlockBag() {
        return new CanaryPlayerBlockBag(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasCreativeMode() {
        return player.getMode().equals(GameMode.CREATIVE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void findFreePosition(WorldVector searchPos) {
        World world = searchPos.getWorld();
        int x = searchPos.getBlockX();
        int y = Math.max(0, searchPos.getBlockY());
        int origY = y;
        int z = searchPos.getBlockZ();

        byte free = 0;

        while (y <= world.getMaxY() + 2) {
            if (BlockType.canPassThrough(world.getBlock(new Vector(x, y, z)))) {
                ++free;
            }
            else {
                free = 0;
            }

            if (free == 2) {
                if (y - 1 != origY) {
                    final Vector pos = new Vector(x, y - 2, z);
                    final int id = world.getBlockType(pos);
                    final int data = world.getBlockData(pos);
                    setPosition(new Vector(x + 0.5, y - 2 + BlockType.centralTopLimit(id, data), z + 0.5));
                }

                return;
            }

            ++y;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnGround(WorldVector searchPos) {
        World world = searchPos.getWorld();
        int x = searchPos.getBlockX();
        int y = Math.max(0, searchPos.getBlockY());
        int z = searchPos.getBlockZ();

        while (y >= 0) {
            final Vector pos = new Vector(x, y, z);
            final int id = world.getBlockType(pos);
            final int data = world.getBlockData(pos);
            if (!BlockType.canPassThrough(id, data)) {
                setPosition(new Vector(x + 0.5, y + BlockType.centralTopLimit(id, data), z + 0.5));
                return;
            }

            --y;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void findFreePosition() {
        findFreePosition(getBlockIn());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ascendLevel() {
        final WorldVector pos = getBlockIn();
        final int x = pos.getBlockX();
        int y = Math.max(0, pos.getBlockY());
        final int z = pos.getBlockZ();
        final World world = pos.getWorld();

        byte free = 0;
        byte spots = 0;

        while (y <= world.getMaxY() + 2) {
            if (BlockType.canPassThrough(world.getBlock(new Vector(x, y, z)))) {
                ++free;
            }
            else {
                free = 0;
            }

            if (free == 2) {
                ++spots;
                if (spots == 2) {
                    final Vector platform = new Vector(x, y - 2, z);
                    final BaseBlock block = world.getBlock(platform);
                    final int type = block.getId();

                    // Don't get put in lava!
                    if (type == BlockID.LAVA || type == BlockID.STATIONARY_LAVA) {
                        return false;
                    }

                    setPosition(platform.add(0.5, BlockType.centralTopLimit(block), 0.5));
                    return true;
                }
            }

            ++y;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean descendLevel() {
        final WorldVector pos = getBlockIn();
        final int x = pos.getBlockX();
        int y = Math.max(0, pos.getBlockY() - 1);
        final int z = pos.getBlockZ();
        final World world = pos.getWorld();

        byte free = 0;

        while (y >= 1) {
            if (BlockType.canPassThrough(world.getBlock(new Vector(x, y, z)))) {
                ++free;
            }
            else {
                free = 0;
            }

            if (free == 2) {
                // So we've found a spot, but we have to drop the player
                // lightly and also check to see if there's something to
                // stand upon
                while (y >= 0) {
                    final Vector platform = new Vector(x, y, z);
                    final BaseBlock block = world.getBlock(platform);
                    final int type = block.getId();

                    // Don't want to end up in lava
                    if (type != BlockID.AIR && type != BlockID.LAVA && type != BlockID.STATIONARY_LAVA) {
                        // Found a block!
                        setPosition(platform.add(0.5, BlockType.centralTopLimit(block), 0.5));
                        return true;
                    }

                    --y;
                }

                return false;
            }

            --y;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ascendToCeiling(int clearance) {
        return ascendToCeiling(clearance, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ascendToCeiling(int clearance, boolean alwaysGlass) {
        Vector pos = getBlockIn();
        int x = pos.getBlockX();
        int initialY = Math.max(0, pos.getBlockY());
        int y = Math.max(0, pos.getBlockY() + 2);
        int z = pos.getBlockZ();
        World world = getPosition().getWorld();

        // No free space above
        if (world.getBlockType(new Vector(x, y, z)) != 0) {
            return false;
        }

        while (y <= world.getMaxY()) {
            // Found a ceiling!
            if (!BlockType.canPassThrough(world.getBlock(new Vector(x, y, z)))) {
                int platformY = Math.max(initialY, y - 3 - clearance);
                floatAt(x, platformY + 1, z, alwaysGlass);
                return true;
            }

            ++y;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ascendUpwards(int distance) {
        return ascendUpwards(distance, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ascendUpwards(int distance, boolean alwaysGlass) {
        final Vector pos = getBlockIn();
        final int x = pos.getBlockX();
        final int initialY = Math.max(0, pos.getBlockY());
        int y = Math.max(0, pos.getBlockY() + 1);
        final int z = pos.getBlockZ();
        final int maxY = Math.min(getWorld().getMaxY() + 1, initialY + distance);
        final World world = getPosition().getWorld();

        while (y <= world.getMaxY() + 2) {
            if (!BlockType.canPassThrough(world.getBlock(new Vector(x, y, z)))) {
                break; // Hit something
            }
            else if (y > maxY + 1) {
                break;
            }
            else if (y == maxY + 1) {
                floatAt(x, y - 1, z, alwaysGlass);
                return true;
            }

            ++y;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void floatAt(int x, int y, int z, boolean alwaysGlass) {
        getPosition().getWorld().setBlockType(new Vector(x, y - 1, z), BlockID.GLASS);
        setPosition(new Vector(x + 0.5, y, z + 0.5));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldVector getBlockIn() {
        WorldVector pos = getPosition();
        return WorldVector.toBlockPoint(pos.getWorld(), pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldVector getBlockOn() {
        WorldVector pos = getPosition();
        return WorldVector.toBlockPoint(pos.getWorld(), pos.getX(), pos.getY() - 1, pos.getZ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldVector getBlockTrace(int range, boolean useLastBlock) {
        TargetBlock tb = new TargetBlock(this, range, 0.2);
        return (useLastBlock ? tb.getAnyTargetBlock() : tb.getTargetBlock());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldVectorFace getBlockTraceFace(int range, boolean useLastBlock) {
        TargetBlock tb = new TargetBlock(this, range, 0.2);
        return (useLastBlock ? tb.getAnyTargetBlockFace() : tb.getTargetBlockFace());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldVector getBlockTrace(int range) {
        return getBlockTrace(range, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldVector getSolidBlockTrace(int range) {
        TargetBlock tb = new TargetBlock(this, range, 0.2);
        return tb.getSolidTargetBlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerDirection getCardinalDirection() {
        return getCardinalDirection(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldVector getPosition() {
        return new WorldVector(CanaryAdapter.adapt(player.getLocation()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPitch() {
        return player.getPitch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getYaw() {
        return player.getRotation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean passThroughForwardWall(int range) {
        int searchDist = 0;
        TargetBlock hitBlox = new TargetBlock(this, range, 0.2);
        World world = getPosition().getWorld();
        BlockWorldVector block;
        boolean firstBlock = true;
        int freeToFind = 2;
        boolean inFree = false;

        while ((block = hitBlox.getNextBlock()) != null) {
            boolean free = BlockType.canPassThrough(world.getBlock(block));

            if (firstBlock) {
                firstBlock = false;

                if (!free) {
                    --freeToFind;
                    continue;
                }
            }

            ++searchDist;
            if (searchDist > 20) {
                return false;
            }

            if (inFree != free) {
                if (free) {
                    --freeToFind;
                }
            }

            if (freeToFind == 0) {
                setOnGround(block);
                return true;
            }

            inFree = free;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        player.teleportTo(pos.getX(), pos.getY(), pos.getZ(), pitch, yaw);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPosition(Vector pos) {
        setPosition(pos, (float) getPitch(), (float) getYaw());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return player.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            player.message(part);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            player.message("\u00A77" + part);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void print(String msg) {
        for (String part : msg.split("\n")) {
            player.message("\u00A7d" + part);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            player.message("\u00A7c" + part);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canDestroyBedrock() {
        return hasPermission("worldedit.override.bedrock");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPlayer() {
        return player.isPlayer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File openFileOpenDialog(String[] extensions) {
        // TODO
        printError("File dialogs are not supported in your environment.");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File openFileSaveDialog(String[] extensions) {
        // TODO
        printError("File dialogs are not supported in your environment.");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }
        Canary.channels().sendCustomPayloadToPlayer(WorldEditPlugin.CUI_PLUGIN_CHANNEL, send.getBytes(CUIChannelListener.UTF_8_CHARSET), player);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException("Cannot create a state from this object");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getLocation() {
        return CanaryAdapter.adapt(player.getLocation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Extent getExtent() {
        return getWorld();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove() {
        // TODO
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        // TODO
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getUniqueId() {
        return player.getUUID();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SessionKey getSessionKey() {
        return new SessionKeyImpl(getUniqueId(), getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getGroups() {
        Set<String> groups = new HashSet<String>();
        for (Group group : player.getPlayerGroups()) {
            groups.add(group.getName());
        }
        return groups.toArray(new String[groups.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPermission(String permission) throws AuthorizationException {
        if (!hasPermission(permission)) {
            throw new AuthorizationException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LocalPlayer)) {
            return false;
        }
        LocalPlayer other2 = (LocalPlayer) other;
        return other2.getName().equals(getName());
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Object clone() throws CloneNotSupportedException {
        // TODO
        throw new CloneNotSupportedException("Not supported");
    }

    private static class SessionKeyImpl implements SessionKey {
        // If not static, this will leak a reference

        private final UUID uuid;
        private final String name;

        private SessionKeyImpl(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        @Nullable
        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isActive() {
            // TODO while I believe this to be a thread safe call, it may not be
            return Canary.getServer().getPlayerFromUUID(uuid) != null;
        }

        @Override
        public boolean isPersistent() {
            return true;
        }

        @Override
        public UUID getUniqueId() {
            return uuid;
        }

    }

}
