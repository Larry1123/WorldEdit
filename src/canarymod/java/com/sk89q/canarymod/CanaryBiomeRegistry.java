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

import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.registry.BiomeRegistry;
import net.canarymod.api.world.BiomeType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CanaryBiomeRegistry implements BiomeRegistry {

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public BaseBiome createFromId(int id) {
        return new BaseBiome(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BaseBiome> getBiomes() {
        List<BaseBiome> biomes = new ArrayList<BaseBiome>();
        for (BiomeType biome : BiomeType.values()) {
            biomes.add(new BaseBiome(biome.getId()));
        }
        return biomes;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public BiomeData getData(BaseBiome biome) {
        final BiomeType type = BiomeType.fromId((byte) biome.getId());
        return new BiomeData() {

            @Override
            public String getName() {
                return type.name();
            }
        };
    }
}