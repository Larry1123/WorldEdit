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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.extension.platform.*;
import com.sk89q.worldedit.util.command.Dispatcher;
import net.canarymod.Canary;
import net.canarymod.Translator;
import net.canarymod.api.entity.EntityType;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.CanaryCommand;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.tasks.ServerTask;
import net.canarymod.tasks.ServerTaskManager;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class CanaryServer extends AbstractPlatform implements MultiUserPlatform {

    protected CanaryWorldEdit plugin;
    protected int taskid = 0;
    protected CanaryConfiguration config;

    public CanaryServer(CanaryWorldEdit plugin) {
        this.plugin = plugin;
        config = new CanaryConfiguration(plugin.logger, plugin.getConfig());
        config.load();
        config.save();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int resolveItem(String name) {
        ItemType t = ItemType.fromString(name);
        if (t != null) {
            return t.getId();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidMobType(String type) {
        try {
            EntityType.valueOf(type);
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reload() {
        config.reload();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public com.sk89q.worldedit.entity.Player matchPlayer(com.sk89q.worldedit.entity.Player player) {
        return new CanaryPlayer(Canary.getServer().getPlayerFromUUID(player.getUniqueId()));
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public CanaryWorld matchWorld(com.sk89q.worldedit.world.World world) {
        if (world instanceof CanaryWorld) {
            return (CanaryWorld) world;
        }
        else {
            for (net.canarymod.api.world.World aWorld : Canary.getServer().getWorldManager().getAllWorlds()) {
                if (aWorld.getName().equals(world.getName())) {
                    return new CanaryWorld(aWorld);
                }
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerCommands(Dispatcher dispatcher) {
        // TODO find out what in the world this is all about
        // commands are handled by the listener currently
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerGameHooks() {
        Canary.hooks().registerListener(new WorldEditListener(), plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalConfiguration getConfiguration() {
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return plugin.getVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPlatformName() {
        return Canary.getSpecificationTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPlatformVersion() {
        return Canary.getSpecificationVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Capability, Preference> getCapabilities() {
        Map<Capability, Preference> capabilityPreferenceMap = new HashMap<Capability, Preference>();
        capabilityPreferenceMap.put(Capability.CONFIGURATION, Preference.PREFERRED);
        capabilityPreferenceMap.put(Capability.GAME_HOOKS, Preference.PREFERRED);
        capabilityPreferenceMap.put(Capability.PERMISSIONS, Preference.PREFERRED);
        capabilityPreferenceMap.put(Capability.USER_COMMANDS, Preference.PREFERRED);
        capabilityPreferenceMap.put(Capability.WORLD_EDITING, Preference.PREFERRED);
        capabilityPreferenceMap.put(Capability.WORLDEDIT_CUI, Preference.PREFERRED);
        return capabilityPreferenceMap;
    }

    public void onCommandRegistration(List<Command> commands, CommandsManager<LocalPlayer> manager) {
        for (final Command command : commands) {
            final Method cmdMethod = manager.getMethods().get(null).get(command.aliases()[0]);

            if (cmdMethod != null && cmdMethod.isAnnotationPresent(CommandPermissions.class)) {
                String[] permissions = cmdMethod.getAnnotation(CommandPermissions.class).value();

                HashMap<Command, net.canarymod.commandsys.Command> children = new HashMap<Command, net.canarymod.commandsys.Command>();

                net.canarymod.commandsys.Command cmd = createCommandAnnotation(command.usage(), permissions, "", command.min(), command.max(), command.desc(), command.aliases());

                // Makes children net.canarymod.commandsys.Command if there are any to make
                // Will also do the same for any children of these
                checkForNested(cmdMethod, command, children);

                CanaryCommand canaryCmd = new CanaryCommand(cmd, plugin, Translator.getInstance()) {

                    @Override
                    protected void execute(MessageReceiver arg0, String[] arg1) {
                        if (arg0 instanceof Player) {
                            String commands = "";
                            for (String command : arg1) {
                                if (commands.isEmpty()) {
                                    commands = command;
                                }
                                else {
                                    commands += " " + command;
                                }
                            }
                            CommandEvent commandEvent = new CommandEvent(new CanaryPlayer((Player) arg0), commands);
                            WorldEdit.getInstance().getEventBus().post(commandEvent);
                        }
                    }

                    @Override
                    protected List<String> tabComplete(MessageReceiver caller, String[] command) {
                        if (caller instanceof Player) {
                            String commands = "";
                            for (String com : command) {
                                if (commands.isEmpty()) {
                                    commands = com;
                                }
                                else {
                                    commands += " " + com;
                                }
                            }
                            CommandSuggestionEvent commandEvent = new CommandSuggestionEvent(new CanaryPlayer((Player) caller), commands);
                            WorldEdit.getInstance().getEventBus().post(commandEvent);
                            return commandEvent.getSuggestions();
                        }
                        return null;
                    }
                };

                try {
                    Canary.commands().registerCommand(canaryCmd, plugin, false);
                }
                catch (CommandDependencyException e) {
                    Canary.help().registerCommand(plugin, canaryCmd);
                }

                for (net.canarymod.commandsys.Command child : children.values()) {

                    CanaryCommand childCanaryCmd = new CanaryCommand(cmd, plugin, Translator.getInstance()) {

                        @Override
                        protected void execute(MessageReceiver arg0, String[] arg1) {
                            if (arg0 instanceof Player) {
                                String commands = "";
                                for (String command : arg1) {
                                    if (commands.isEmpty()) {
                                        commands = command;
                                    }
                                    else {
                                        commands += " " + command;
                                    }
                                }
                                CommandEvent commandEvent = new CommandEvent(new CanaryPlayer((Player) arg0), commands);
                                WorldEdit.getInstance().getEventBus().post(commandEvent);
                            }
                        }

                        @Override
                        protected List<String> tabComplete(MessageReceiver caller, String[] command) {
                            if (caller instanceof Player) {
                                String commands = "";
                                for (String com : command) {
                                    if (commands.isEmpty()) {
                                        commands = com;
                                    }
                                    else {
                                        commands += " " + com;
                                    }
                                }
                                CommandSuggestionEvent commandEvent = new CommandSuggestionEvent(new CanaryPlayer((Player) caller), commands);
                                WorldEdit.getInstance().getEventBus().post(commandEvent);
                                return commandEvent.getSuggestions();
                            }
                            return null;
                        }
                    };

                    try {
                        Canary.commands().registerCommand(childCanaryCmd, plugin, false);
                    }
                    catch (CommandDependencyException e) {
                        Canary.help().registerCommand(plugin, childCanaryCmd);
                    }
                }
            }
        }
    }

    private void checkForNested(Method command, Command parent, HashMap<Command, net.canarymod.commandsys.Command> children) {

        if (command.isAnnotationPresent(NestedCommand.class)) {
            NestedCommand nestedCmd = command.getAnnotation(NestedCommand.class);

            for (Class<?> nestedCls : nestedCmd.value()) {
                for (Method method : nestedCls.getMethods()) {
                    if (!method.isAnnotationPresent(Command.class)) {
                        continue;
                    }
                    Command nestedCommand = method.getAnnotation(Command.class);
                    if (command != null && command.isAnnotationPresent(CommandPermissions.class)) {
                        String[] nestedPermissions = command.getAnnotation(CommandPermissions.class).value();
                        String parentString = children.get(parent).parent();

                        children.put(nestedCommand, createCommandAnnotation(nestedCommand.usage(), nestedPermissions, parentString + parent.aliases()[0], nestedCommand.min(), nestedCommand.max(), nestedCommand.desc(), nestedCommand.aliases()));
                    }
                    checkForNested(method, nestedCommand, children);
                }
            }
        }
    }

    protected net.canarymod.commandsys.Command createCommandAnnotation(final String toolTip, final String[] permissions, final String parent, final int min, final int max, final String description, final String[] aliases) {
        net.canarymod.commandsys.Command cmd = new net.canarymod.commandsys.Command() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Command.class;
            }

            @Override
            public String[] aliases() {
                return aliases;
            }

            @Override
            public String[] permissions() {
                return permissions;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public String toolTip() {
                return toolTip;
            }

            @Override
            public String parent() {
                return parent;
            }

            @Override
            public String helpLookup() {
                return "";
            }

            @Override
            public String[] searchTerms() {
                return new String[0];
            }

            @Override
            public int min() {
                return min;
            }

            @Override
            public int max() {
                return max;
            }

            @Override
            public int version() {
                return 1;
            }
        };

        return cmd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int schedule(long delay, long period, final Runnable task) {
        if (ServerTaskManager.addTask(new ServerTask(plugin, period, period > 0) {

            Runnable r = task;

            @Override
            public void run() {
                r.run();
            }
        })) {
            taskid++;
            return taskid;
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Actor> getConnectedUsers() {
        ArrayList<Actor> actors = new ArrayList<Actor>();

        for (Player player : Canary.getServer().getPlayerList()) {
            actors.add(new CanaryPlayer(player));
        }

        return actors;
    }
}
