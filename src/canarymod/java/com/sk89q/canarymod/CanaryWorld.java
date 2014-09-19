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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.LazyBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.registry.WorldData;
import net.canarymod.Canary;
import net.canarymod.api.inventory.Enchantment;
import net.canarymod.api.inventory.Inventory;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.world.BiomeType;
import net.canarymod.api.world.Chunk;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.TileEntity;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class CanaryWorld extends AbstractWorld {

    protected final WeakReference<World> worldRef;

    public CanaryWorld(World world) {
        checkNotNull(world);
        this.worldRef = new WeakReference<World>(world);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return getHandle().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setBlock(Vector position, BaseBlock block, boolean notifyAndLight) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        Block b = getHandle().getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        b.setTypeId((short) block.getType());
        b.setData((short) block.getData());

        CompoundTag tag = block.getNbtData();
        if (tag != null) {
            // TODO This appears to be possible directly
            // b.getTileEntity().getMetaTag().getCompoundTag()
        }

        if (notifyAndLight) {
            b.update();
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBlockLightLevel(Vector pt) {
        return getHandle().getLightLevelAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean clearContainerBlockContents(Vector pt) {
        TileEntity block = getHandle().getOnlyTileEntityAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null || !(block instanceof Inventory)) {
            return false;
        }

        Inventory chest = (Inventory) block;
        chest.clearContents();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropItem(Vector pt, BaseItemStack item) {
        Item canary = Canary.factory().getItemFactory().newItem(item.getType(), item.getData(), item.getAmount());
        for (Integer t : item.getEnchantments().keySet()) {
            canary.addEnchantments(Canary.factory().getItemFactory().newEnchantment(Enchantment.Type.fromId(t), item.getEnchantments().get(t).shortValue()));
        }
        getHandle().dropItem(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), canary);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        // Code from l4mRh4x0rs implementation
        BaseBlock[] history = new BaseBlock[16 * 16 * 128];

        for (Vector2D chunk : region.getChunks()) {
            Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);

            // First save all the blocks inside
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 128; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;
                        history[index] = editSession.getBlock(pt);
                    }
                }
            }

            try {
                getHandle().getChunkProvider().regenerateChunk(chunk.getBlockX(), chunk.getBlockZ());
            }
            catch (Throwable t) {
                CanaryWorldEdit.getInstance().logger.trace(t.getMessage(), t);
            }

            // Then restore
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 128; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;

                        // We have to restore the block if it was outside
                        if (!region.contains(pt)) {
                            editSession.smartSetBlock(pt, history[index]);
                        }
                        else { // Otherwise fool with history
                            editSession.rememberChange(pt, history[index], editSession.rawGetBlock(pt));
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, Vector position) throws MaxChangedBlocksException {
        // TODO
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldData getWorldData() {
        return CanaryWorldData.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxY() {
        return getHandle().getHeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidBlockType(int type) {
        return BlockType.fromId(type) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBlockType(Vector pt) {
        return getHandle().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getTypeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBlockData(Vector pt) {
        return getHandle().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropItem(Vector position, BaseItemStack item, int count) {
        getHandle().dropItem(position.getBlockX(), position.getBlockY(), position.getBlockZ(), item.getType(), item.getData(), item.getAmount());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkLoadedChunk(Vector pt) {
        if (!getHandle().isChunkLoaded(pt.getBlockX(), pt.getBlockZ())) {
            getHandle().loadChunk(pt.getBlockX(), pt.getBlockZ());
        }
    }

    /**
     * {@inheritDoc}
     *
     * === CanaryMod ===
     *
     * This ends up just calling {@link com.sk89q.canarymod.CanaryWorld#fixLighting(Iterable)}
     * Hope to make this do it's own thing soon
     */
    @Override
    public void fixAfterFastMode(Iterable<BlockVector2D> chunks) {
        fixLighting(chunks);
        // TODO Not the best thing to do I think...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fixLighting(Iterable<BlockVector2D> chunks) {
        for (BlockVector2D blockVector2D : chunks) {
            int x = blockVector2D.getBlockX();
            int z = blockVector2D.getBlockZ();
            Chunk chunk = getHandle().getChunk(x, z);
            if (!chunk.isLoaded()) {
                getHandle().loadChunk(x, z);
            }
            chunk.updateSkyLightMap(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean playEffect(Vector position, int type, int data) {
        if (getHandle().isChunkLoaded(position.getBlockX(), position.getBlockY(), position.getBlockZ())) {
            getHandle().playNoteAt(position.getBlockX(), position.getBlockY(), position.getBlockZ(), type, (byte) data);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean queueBlockBreakEffect(Platform server, Vector position, int blockId, double priority) {
        // TODO
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector getMinimumPoint() {
        // TODO
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector getMaximumPoint() {
        // TODO
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BaseBlock getBlock(Vector position) {
        Block block = getHandle().getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        int type = block.getTypeId();
        int data = block.getData();
        TileEntity tile = block.getTileEntity();

        BaseBlock bb = new BaseBlock(type, data);
        if (tile != null) {
            return CanaryAdapter.adapt(bb, tile);
        }
        else {
            return bb;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BaseBlock getLazyBlock(Vector position) {
        Block block = getHandle().getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        return new LazyBlock(block.getTypeId(), block.getData(), this, position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BaseBiome getBiome(Vector2D position) {
        return new BaseBiome(getHandle().getBiome(position.getBlockX(), position.getBlockZ()).getBiomeType().getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends Entity> getEntities(Region region) {
        World world = getHandle();

        List<com.sk89q.worldedit.entity.Entity> entities = new ArrayList<Entity>();
        for (Vector2D pt : region.getChunks()) {
            Chunk chunk = world.getChunk(pt.getBlockX(), pt.getBlockZ());
            if (!chunk.isLoaded()) {
                continue;
            }
            final List<net.canarymod.api.entity.Entity>[] ents = chunk.getEntityLists();
            for (List<net.canarymod.api.entity.Entity> entLs : ents) {
                for (net.canarymod.api.entity.Entity ent : entLs) {
                    if (region.contains(CanaryAdapter.toVector(ent.getPosition()))) {
                        entities.add(CanaryAdapter.adapt(ent));
                    }
                }
            }
        }
        return entities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends com.sk89q.worldedit.entity.Entity> getEntities() {
        World world = getHandle();
        List<com.sk89q.worldedit.entity.Entity> entities = new ArrayList<Entity>();
        final List<net.canarymod.api.entity.Entity> ents = world.getTrackedEntities();
        for (net.canarymod.api.entity.Entity ent : ents) {
            entities.add(CanaryAdapter.adapt(ent));
        }
        return entities;
    }

    /**
     * {@inheritDoc}
     *
     * This is likely to return null all the time see {@link CanaryEntity#getState()}
     */
    @Nullable
    @Override
    public com.sk89q.worldedit.entity.Entity createEntity(com.sk89q.worldedit.util.Location location, BaseEntity entity) {
        net.canarymod.api.entity.Entity entityC = Canary.factory().getEntityFactory().newEntity(entity.getTypeId(), CanaryAdapter.adapt(this));
        if (entityC == null) {
            return null;
        }
        if (entity.getNbtData() != null) {
            entityC.setNBT(CanaryAdapter.adapt(entity.getNbtData()));
        }
        entityC.setX(location.getX());
        entityC.setY(location.getY());
        entityC.setZ(location.getZ());
        entityC.setPitch(location.getPitch());
        entityC.setRotation(location.getYaw());
        entityC.spawn();
        return new CanaryEntity(entityC);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        getHandle().setBiome(position.getBlockX(), position.getBlockZ(), BiomeType.fromId((byte) biome.getId()));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getHandle().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof CanaryWorld && getHandle().equals(((CanaryWorld) other).getHandle());
    }

    /**
     * Get the CanaryMod {@link World} that is wrapped in this {@link CanaryWorld}
     *
     * @return the wrapped {@link World}
     */
    public World getHandle() {
        World world = worldRef.get();
        if (world != null) {
            return world;
        }
        else {
            throw new RuntimeException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

}
