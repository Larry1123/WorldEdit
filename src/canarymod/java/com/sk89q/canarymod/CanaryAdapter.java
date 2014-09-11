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

import com.sk89q.jnbt.CompoundTagBuilder;
import com.sk89q.jnbt.ListTagBuilder;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import net.canarymod.Canary;
import net.canarymod.api.entity.Entity;
import net.canarymod.api.factory.NBTFactory;
import net.canarymod.api.nbt.BaseTag;
import net.canarymod.api.nbt.ListTag;
import net.canarymod.api.nbt.NBTTagType;
import net.canarymod.api.world.blocks.TileEntity;
import net.canarymod.api.world.position.Position;

import java.util.List;
import java.util.ListIterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapts between Canary and WorldEdit equivalent objects.
 */
final class CanaryAdapter {

    private CanaryAdapter() {}

    /**
     * Convert any WorldEdit world into an equivalent wrapped Canary world.
     * <p/>
     * <p>If a matching world cannot be found, a {@link RuntimeException}
     * will be thrown.</p>
     *
     * @param world the world
     *
     * @return a wrapped Canary world
     */
    public static CanaryWorld asCanaryWorld(World world) {
        if (world instanceof CanaryWorld) {
            return (CanaryWorld) world;
        }
        else {
            CanaryWorld canaryWorld = CanaryWorldEdit.getInstance().getCanaryServer().matchWorld(world);
            if (canaryWorld == null) {
                throw new RuntimeException("World '" + world.getName() + "' has no matching version in Canary");
            }
            return canaryWorld;
        }
    }

    /**
     * Create a WorldEdit world from a Canary world.
     *
     * @param world the Canary world
     *
     * @return a WorldEdit world
     */
    public static World adapt(net.canarymod.api.world.World world) {
        checkNotNull(world);
        return new CanaryWorld(world);
    }

    /**
     * Create a Canary world from a WorldEdit world.
     *
     * @param world the WorldEdit world
     *
     * @return a Canary world
     */
    public static net.canarymod.api.world.World adapt(World world) {
        checkNotNull(world);
        if (world instanceof CanaryWorld) {
            return ((CanaryWorld) world).getHandle();
        }
        else {
            net.canarymod.api.world.World match = Canary.getServer().getWorld(world.getName());
            if (match != null) {
                return match;
            }
            else {
                throw new IllegalArgumentException("Can't find a Canary world for " + world);
            }
        }
    }

    /**
     * Create a WorldEdit location from a Canary location.
     *
     * @param location the Canary location
     *
     * @return a WorldEdit location
     */
    public static Location adapt(net.canarymod.api.world.position.Location location) {
        checkNotNull(location);
        Vector position = toVector(location);
        return new com.sk89q.worldedit.util.Location(adapt(location.getWorld()), position, location.getRotation(), location.getPitch());
    }

    /**
     * Create a Canary location from a WorldEdit location.
     *
     * @param location the WorldEdit location
     *
     * @return a Canary location
     */
    public static net.canarymod.api.world.position.Location adapt(Location location) {
        checkNotNull(location);
        Vector position = location.toVector();
        return new net.canarymod.api.world.position.Location(adapt((World) location.getExtent()), position.getX(), position.getY(), position.getZ(), location.getYaw(), location.getPitch());
    }

    /**
     * Create a Canary location from a WorldEdit position with a Canary world.
     *
     * @param world    the Canary world
     * @param position the WorldEdit position
     *
     * @return a Canary location
     */
    public static net.canarymod.api.world.position.Location adapt(net.canarymod.api.world.World world, Vector position) {
        checkNotNull(world);
        checkNotNull(position);
        return new net.canarymod.api.world.position.Location(world, position.getX(), position.getY(), position.getZ(), 0, 0);
    }

    /**
     * Create a Canary location from a WorldEdit location with a Canary world.
     *
     * @param world    the Canary world
     * @param location the WorldEdit location
     *
     * @return a Canary location
     */
    public static net.canarymod.api.world.position.Location adapt(net.canarymod.api.world.World world, Location location) {
        checkNotNull(world);
        checkNotNull(location);
        return new net.canarymod.api.world.position.Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    /**
     * Create a WorldEdit entity from a Canary entity.
     *
     * @param entity the Canary entity
     *
     * @return a WorldEdit entity
     */
    public static CanaryEntity adapt(Entity entity) {
        checkNotNull(entity);
        return new CanaryEntity(entity);
    }

    /**
     * Creates a {@link com.sk89q.worldedit.blocks.TileEntityBlock}
     *
     * @param base          What {@link com.sk89q.worldedit.blocks.BaseBlock} this is
     * @param tileEntity    The {@link net.canarymod.api.world.blocks.TileEntity} to adapt
     * @return returns {@link com.sk89q.canarymod.CanaryTileEntity} that has adapted {@link net.canarymod.api.world.blocks.TileEntity}
     */
    public static CanaryTileEntity adapt(BaseBlock base, TileEntity tileEntity) {
        return new CanaryTileEntity(base.getId(), base.getData(), tileEntity);
    }

    /**
     * Converts a WorldEdit {@link com.sk89q.jnbt.CompoundTag} to a CanaryLib {@link net.canarymod.api.nbt.CompoundTag}
     *
     * @param compoundTag    {@link com.sk89q.jnbt.CompoundTag} to be adapted
     * @return returns the new {@link net.canarymod.api.nbt.CompoundTag}
     */
    public static net.canarymod.api.nbt.CompoundTag adapt(com.sk89q.jnbt.CompoundTag compoundTag) {
        net.canarymod.api.nbt.CompoundTag compoundTagC = Canary.factory().getNBTFactory().newCompoundTag(compoundTag.getName());
        NBTFactory factory = Canary.factory().getNBTFactory();
        for (String key : compoundTag.getValue().keySet()) {
            com.sk89q.jnbt.Tag tag = compoundTag.getValue().get(key);
            switch (NBTTagType.getTypeFromId((byte) NBTUtils.getTypeCode(tag.getClass()))) {
                case BYTE:
                    compoundTagC.put(key, factory.newByteTag(((com.sk89q.jnbt.ByteTag) tag).getValue()));
                    break;
                case BYTE_ARRAY:
                    compoundTagC.put(key, factory.newByteArrayTag(((com.sk89q.jnbt.ByteArrayTag) tag).getValue()));
                    break;
                case DOUBLE:
                    compoundTagC.put(key, factory.newDoubleTag(((com.sk89q.jnbt.DoubleTag) tag).getValue()));
                    break;
                case FLOAT:
                    compoundTagC.put(key, factory.newFloatTag(((com.sk89q.jnbt.FloatTag) tag).getValue()));
                    break;
                case INT:
                    compoundTagC.put(key, factory.newIntTag(((com.sk89q.jnbt.IntTag) tag).getValue()));
                    break;
                case INT_ARRAY:
                    compoundTagC.put(key, factory.newIntArrayTag(((com.sk89q.jnbt.IntArrayTag) tag).getValue()));
                    break;
                case LIST:
                    compoundTagC.put(key, adaptTag(((com.sk89q.jnbt.ListTag) tag).getValue()));
                    break;
                case LONG:
                    compoundTagC.put(key, factory.newLongTag(((com.sk89q.jnbt.LongTag) tag).getValue()));
                    break;
                case SHORT:
                    compoundTagC.put(key, factory.newShortTag(((com.sk89q.jnbt.ShortTag) tag).getValue()));
                    break;
                case STRING:
                    compoundTagC.put(key, factory.newStringTag(((com.sk89q.jnbt.StringTag) tag).getValue()));
                    break;
                case COMPOUND:
                    // Loop all the things!
                    compoundTagC.put(key, adapt(((com.sk89q.jnbt.CompoundTag) tag)));
                    break;
                case ANY_NUMERIC:
                    break;
                case UNKNOWN:
                    break;
            }
        }
        return compoundTagC;
    }

    protected static ListTag<BaseTag> adaptTag(List<? extends Tag> tagList) {
        NBTFactory factory = Canary.factory().getNBTFactory();
        ListTag<BaseTag> listTag = Canary.factory().getNBTFactory().newListTag();
        for (Tag tag : tagList) {
            switch (NBTTagType.getTypeFromId((byte) NBTUtils.getTypeCode(tag.getClass()))) {
                case BYTE:
                    listTag.add(factory.newByteTag(((com.sk89q.jnbt.ByteTag) tag).getValue()));
                    break;
                case BYTE_ARRAY:
                    listTag.add(factory.newByteArrayTag(((com.sk89q.jnbt.ByteArrayTag) tag).getValue()));
                    break;
                case DOUBLE:
                    listTag.add(factory.newDoubleTag(((com.sk89q.jnbt.DoubleTag) tag).getValue()));
                    break;
                case FLOAT:
                    listTag.add(factory.newFloatTag(((com.sk89q.jnbt.FloatTag) tag).getValue()));
                    break;
                case INT:
                    listTag.add(factory.newIntTag(((com.sk89q.jnbt.IntTag) tag).getValue()));
                    break;
                case INT_ARRAY:
                    listTag.add(factory.newIntArrayTag(((com.sk89q.jnbt.IntArrayTag) tag).getValue()));
                    break;
                case LIST:
                    // Hey look a Loop!
                    listTag.add(adaptTag(((com.sk89q.jnbt.ListTag) tag).getValue()));
                    break;
                case LONG:
                    listTag.add(factory.newLongTag(((com.sk89q.jnbt.LongTag) tag).getValue()));
                    break;
                case SHORT:
                    listTag.add(factory.newShortTag(((com.sk89q.jnbt.ShortTag) tag).getValue()));
                    break;
                case STRING:
                    listTag.add(factory.newStringTag(((com.sk89q.jnbt.StringTag) tag).getValue()));
                    break;
                case COMPOUND:
                    listTag.add(adapt(((com.sk89q.jnbt.CompoundTag) tag)));
                    break;
                case ANY_NUMERIC:
                    break;
                case UNKNOWN:
                    break;
            }
        }
        return listTag;
    }

    /**
     * Converts a CanaryLib {@link net.canarymod.api.nbt.CompoundTag} to a WorldEdit {@link com.sk89q.jnbt.CompoundTag}
     *
     * @param compoundTag    {@link net.canarymod.api.nbt.CompoundTag} to be adapted
     * @return returns the new {@link com.sk89q.jnbt.CompoundTag}
     */
    public static com.sk89q.jnbt.CompoundTag adapt(net.canarymod.api.nbt.CompoundTag compoundTag) {
        CompoundTagBuilder compoundTagBuilder = CompoundTagBuilder.create();
        for (String key : compoundTag.keySet()) {
            net.canarymod.api.nbt.BaseTag tag = compoundTag.get(key);
            switch (NBTTagType.getTypeFromId(tag.getTypeId())) {
                case BYTE:
                    compoundTagBuilder.put(key, new com.sk89q.jnbt.ByteTag(((net.canarymod.api.nbt.ByteTag) tag).getValue()));
                    break;
                case BYTE_ARRAY:
                    compoundTagBuilder.put(key, new com.sk89q.jnbt.ByteArrayTag(((net.canarymod.api.nbt.ByteArrayTag) tag).getValue()));
                    break;
                case DOUBLE:
                    compoundTagBuilder.put(key, new com.sk89q.jnbt.DoubleTag(((net.canarymod.api.nbt.DoubleTag) tag).getValue()));
                    break;
                case FLOAT:
                    compoundTagBuilder.put(key, new com.sk89q.jnbt.FloatTag(((net.canarymod.api.nbt.FloatTag) tag).getValue()));
                    break;
                case INT:
                    compoundTagBuilder.put(key, new com.sk89q.jnbt.IntTag(((net.canarymod.api.nbt.IntTag) tag).getValue()));
                    break;
                case INT_ARRAY:
                    compoundTagBuilder.put(key, new com.sk89q.jnbt.IntArrayTag(((net.canarymod.api.nbt.IntArrayTag) tag).getValue()));
                    break;
                case LIST:
                    compoundTagBuilder.put(key, adaptBaseTag(((net.canarymod.api.nbt.ListTag) tag).listIterator()));
                    break;
                case LONG:
                    compoundTagBuilder.put(key, new com.sk89q.jnbt.LongTag(((net.canarymod.api.nbt.LongTag) tag).getValue()));
                    break;
                case SHORT:
                    compoundTagBuilder.put(key, new com.sk89q.jnbt.ShortTag(((net.canarymod.api.nbt.ShortTag) tag).getValue()));
                    break;
                case STRING:
                    compoundTagBuilder.put(key, new com.sk89q.jnbt.StringTag(((net.canarymod.api.nbt.StringTag) tag).getValue()));
                    break;
                case COMPOUND:
                    // Loop all the things!
                    compoundTagBuilder.put(key, adapt(((net.canarymod.api.nbt.CompoundTag) tag)));
                    break;
                case ANY_NUMERIC:
                    break;
                case UNKNOWN:
                    break;
            }
        }
        return compoundTagBuilder.build();
    }

    protected static com.sk89q.jnbt.ListTag adaptBaseTag(ListIterator<? extends BaseTag> tagList) {
        ListTagBuilder listTagBuilder = ListTagBuilder.create(null);
        while (tagList.hasNext()) {
            net.canarymod.api.nbt.BaseTag tag = tagList.next();
            switch (NBTTagType.getTypeFromId(tag.getTypeId())) {
                case BYTE:
                    listTagBuilder.add(new com.sk89q.jnbt.ByteTag(((net.canarymod.api.nbt.ByteTag) tag).getValue()));
                    break;
                case BYTE_ARRAY:
                    listTagBuilder.add(new com.sk89q.jnbt.ByteArrayTag(((net.canarymod.api.nbt.ByteArrayTag) tag).getValue()));
                    break;
                case DOUBLE:
                    listTagBuilder.add(new com.sk89q.jnbt.DoubleTag(((net.canarymod.api.nbt.DoubleTag) tag).getValue()));
                    break;
                case FLOAT:
                    listTagBuilder.add(new com.sk89q.jnbt.FloatTag(((net.canarymod.api.nbt.FloatTag) tag).getValue()));
                    break;
                case INT:
                    listTagBuilder.add(new com.sk89q.jnbt.IntTag(((net.canarymod.api.nbt.IntTag) tag).getValue()));
                    break;
                case INT_ARRAY:
                    listTagBuilder.add(new com.sk89q.jnbt.IntArrayTag(((net.canarymod.api.nbt.IntArrayTag) tag).getValue()));
                    break;
                case LIST:
                    // Hey look a Loop!
                    listTagBuilder.add(adaptBaseTag(((net.canarymod.api.nbt.ListTag) tag).listIterator()));
                    break;
                case LONG:
                    listTagBuilder.add(new com.sk89q.jnbt.LongTag(((net.canarymod.api.nbt.LongTag) tag).getValue()));
                    break;
                case SHORT:
                    listTagBuilder.add(new com.sk89q.jnbt.ShortTag(((net.canarymod.api.nbt.ShortTag) tag).getValue()));
                    break;
                case STRING:
                    listTagBuilder.add(new com.sk89q.jnbt.StringTag(((net.canarymod.api.nbt.StringTag) tag).getValue()));
                    break;
                case COMPOUND:
                    listTagBuilder.add(adapt(((net.canarymod.api.nbt.CompoundTag) tag)));
                    break;
                case ANY_NUMERIC:
                    break;
                case UNKNOWN:
                    break;
            }
        }
        return listTagBuilder.build();
    }

    /**
     * Turn a position into a world edit vector
     *
     * @param position Position to be changed into vector
     *
     * @return A vector equivalent
     */
    public static Vector toVector(Position position) {
        return new Vector(position.getX(), position.getY(), position.getZ());
    }

    /**
     * Turn a worldedit vector to a canarymod location
     *
     * @param world  What world is this vector of
     * @param vector The vector to make Location from
     *
     * @return A Location equivalent
     */
    public static net.canarymod.api.world.position.Location toLocation(net.canarymod.api.world.World world, Vector vector) {
        return new net.canarymod.api.world.position.Location(world, vector.getX(), vector.getY(), vector.getZ(), 0f, 0f);
    }
}