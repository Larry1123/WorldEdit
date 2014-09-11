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

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.Event;
import com.sk89q.worldedit.event.platform.*;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.eventbus.EventBus;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.channels.ChannelListener;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.command.PlayerCommandHook;
import net.canarymod.hook.player.*;
import net.canarymod.plugin.PluginListener;

import java.nio.charset.Charset;

public class WorldEditListener extends ChannelListener implements PluginListener {

    protected WorldEdit worldEdit = WorldEdit.getInstance();
    protected EventBus eventBus = worldEdit.getEventBus();

    public WorldEditListener() {}

    @HookHandler
    public void onDisconnect(DisconnectionHook hook) {
        worldEdit.getSessionManager().remove(wrapPlayer(hook.getPlayer()));
    }

    @HookHandler
    public void onLeftClick(PlayerArmSwingHook hook) {
        Event event = new PlayerInputEvent(wrapPlayer(hook.getPlayer()), InputType.PRIMARY);
        eventBus.post(event);
    }

    @HookHandler
    public void onBlockRightClick(BlockRightClickHook hook) {
        Location location = CanaryAdapter.adapt(hook.getBlockClicked().getLocation());
        BlockInteractEvent event = new BlockInteractEvent(wrapPlayer(hook.getPlayer()), location, Interaction.OPEN);
        eventBus.post(event);
        if (event.isCancelled()) {
            hook.setCanceled();
        }
    }

    @HookHandler
    public void onitemUse(ItemUseHook hook) {
        PlayerInputEvent event = new PlayerInputEvent(wrapPlayer(hook.getPlayer()), InputType.SECONDARY);
        eventBus.post(event);
        if (event.isCancelled()) {
            hook.setCanceled();
        }
    }

    @HookHandler
    public void onBlockDestroy(BlockDestroyHook hook) {
        Location location = CanaryAdapter.adapt(hook.getBlock().getLocation());
        BlockInteractEvent event = new BlockInteractEvent(wrapPlayer(hook.getPlayer()), location, Interaction.HIT);
        eventBus.post(event);
        if (event.isCancelled()) {
            hook.setCanceled();
        }
    }

    @HookHandler
    public void onPlayerCommand(PlayerCommandHook hook) {
        // This is a little dirty right now, as it is circumventing the command system.
        // Made Catch for if the Command System could not register the command
        String commands = "";
        for (String command : hook.getCommand()) {
            if (commands.isEmpty()) {
                commands = command;
            }
            else {
                commands += " " + command;
            }
        }
        CommandEvent event = new CommandEvent(wrapPlayer(hook.getPlayer()), commands);
        eventBus.post(event);
        if (event.isCancelled()) {
            hook.setCanceled();
        }
    }

    @HookHandler
    public void onBlockLeftClick(BlockLeftClickHook hook) {
        Location location = CanaryAdapter.adapt(hook.getBlock().getLocation());
        BlockInteractEvent event = new BlockInteractEvent(wrapPlayer(hook.getPlayer()), location, Interaction.HIT);
        eventBus.post(event);
        if (event.isCancelled()) {
            hook.setCanceled();
        }
    }

    /**
     * Handles CUI starup server Side
     */
    @Override
    public void onChannelInput(String channel, Player player, byte[] byteStream) {
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wrapPlayer(player));
        if (session.hasCUISupport()) { // Already initialized
            return;
        }
        String text = new String(byteStream, Charset.forName("UTF-8"));
        session.handleCUIInitializationMessage(text);
    }

    protected CanaryPlayer wrapPlayer(Player player) {
        return new CanaryPlayer(player);
    }
}
