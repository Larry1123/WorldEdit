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

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.EntityType;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.NullWorld;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

import static com.google.common.base.Preconditions.checkNotNull;

public class CanaryEntity implements Entity {

    private final WeakReference<net.canarymod.api.entity.Entity> entityRef;

    /**
     * Create a new instance.
     *
     * @param entity the entity
     */
    CanaryEntity(net.canarymod.api.entity.Entity entity) {
        checkNotNull(entity);
        this.entityRef = new WeakReference<net.canarymod.api.entity.Entity>(entity);
    }

    /**
     * NOPE!
     * Sorry but can't do this as removing and respawning just does not work, not everything is stored in NBT...
     */
    @Nullable
    @Override
    public BaseEntity getState() {
            return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getLocation() {
        net.canarymod.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return CanaryAdapter.adapt(entity.getLocation());
        }
        else {
            return new Location(NullWorld.getInstance());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Extent getExtent() {
        net.canarymod.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return new CanaryWorld(entity.getWorld());
        }
        else {
            return NullWorld.getInstance();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove() {
        net.canarymod.api.entity.Entity entity = entityRef.get();
        if (entity != null) {
            entity.destroy();
            return entity.isDead();
        }
        else {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        net.canarymod.api.entity.Entity entity = entityRef.get();
        if (entity != null && EntityType.class.isAssignableFrom(cls)) {
            return (T) new CanaryEntityType(entity);
        }
        else {
            return null;
        }
    }

}
