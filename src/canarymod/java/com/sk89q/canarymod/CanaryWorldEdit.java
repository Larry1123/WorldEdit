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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import net.canarymod.logger.Logman;
import net.canarymod.plugin.Plugin;
import net.canarymod.tasks.TaskOwner;

public class CanaryWorldEdit extends Plugin implements TaskOwner {

    protected static CanaryWorldEdit instance = null;
    public final Logman logger;
    protected final CanaryServer server;

    public CanaryWorldEdit() {
        instance = this;
        logger = getLogman();
        server = new CanaryServer(this);
    }

    /**
     * Get the WorldEdit Plugin instance
     *
     * @return Get the plugin instance
     */
    public static CanaryWorldEdit getInstance() {
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enable() {
        WorldEdit.getInstance().getPlatformManager().register(server);
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disable() {
        // Everything we would need to do, such as unregistering help content,
        // is handled in CanaryMods internals.
    }

    public CanaryServer getCanaryServer() {
        return server;
    }

}
