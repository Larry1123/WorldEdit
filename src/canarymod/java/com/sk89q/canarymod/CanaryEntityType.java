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

import com.sk89q.worldedit.entity.metadata.EntityType;
import net.canarymod.api.entity.*;
import net.canarymod.api.entity.hanging.ItemFrame;
import net.canarymod.api.entity.hanging.Painting;
import net.canarymod.api.entity.living.EntityLiving;
import net.canarymod.api.entity.living.Golem;
import net.canarymod.api.entity.living.animal.Tameable;
import net.canarymod.api.entity.living.humanoid.Human;
import net.canarymod.api.entity.living.humanoid.NonPlayableCharacter;
import net.canarymod.api.entity.vehicle.Boat;
import net.canarymod.api.entity.vehicle.Minecart;
import net.canarymod.api.entity.vehicle.TNTMinecart;
import net.canarymod.api.inventory.Item;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Larry1123
 * @since 9/9/2014 - 10:46 AM
 */
public class CanaryEntityType implements EntityType {

    protected final Entity entity;

    CanaryEntityType(Entity entity) {
        checkNotNull(entity);
        this.entity = entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPlayerDerived() {
        return entity instanceof Human;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProjectile() {
        return entity instanceof Projectile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isItem() {
        return entity instanceof Item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFallingBlock() {
        return entity instanceof FallingBlock;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPainting() {
        return entity instanceof Painting;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isItemFrame() {
        return entity instanceof ItemFrame;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBoat() {
        return entity instanceof Boat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMinecart() {
        return entity instanceof Minecart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTNT() {
        return entity instanceof TNTPrimed || entity instanceof TNTMinecart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExperienceOrb() {
        return entity instanceof XPOrb;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLiving() {
        return entity.isLiving();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnimal() {
        return entity.isAnimal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAmbient() {
        return entity.isAmbient();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNPC() {
        return entity instanceof NonPlayableCharacter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGolem() {
        return entity instanceof Golem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTamed() {
        return entity instanceof Tameable && ((Tameable) entity).isTamed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTagged() {
        if (entity instanceof EntityLiving) {
            String displayName = ((EntityLiving) entity).getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                return true;
            }
        }
        return false;
    }

}
