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

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.world.snapshot.SnapshotRepository;
import net.canarymod.logger.Logman;
import net.visualillusionsent.utils.PropertiesFile;

import java.io.File;

/**
 * TODO find out what half this stuff does
 *
 * @author Larry1123
 */
public class CanaryConfiguration extends LocalConfiguration {

    protected Logman logger;
    protected PropertiesFile properties;

    protected String keyDefaultChangeLimit = "limits.max-blocks-changed.default";
    protected String keyDisallowedBlocks = "limits.disallowed-blocks";
    protected String keyProfile = "debug";
    protected String keyMaxChangeLimit = "limits.max-blocks-changed.maximum";
    protected String keyDefaultMaxPolygonalPoints = "limits.max-polygonal-points.default";
    protected String keyMaxPolygonalPoints = "limits.max-polygonal-points.maximum";
    protected String keyDefaultMaxPolyhedronPoints = "limits.max-polyhedron-points.default";
    protected String keyMaxPolyhedronPoints = "limits.max-polyhedron-points.maximum";
    protected String keyShellSaveType = "shell-save-type";
    protected String keySnapshotRepo = "snapshots.directory";
    protected String keyMaxRadius = "limits.max-radius";
    protected String keyMaxSuperPickaxeSize = "limits.max-brush-radius";
    protected String keyMaxBrushRadius = "limits.max-super-pickaxe-size";
    protected String keyLogCommands = "logging.log-commands";
    protected String keyLogFile = "logging.file";
    protected String keyRegisterHelp = "register-help";
    protected String keyWandItem = "wand-item";
    protected String keySuperPickaxeDrop = "super-pickaxe.drop-items";
    protected String keySuperPickaxeManyDrop = "super-pickaxe.many-drop-items";
    protected String keyNoDoubleSlash = "no-double-slash";
    protected String keyUseInventory = "use-inventory.enable";
    protected String keyUseInventoryOverride = "use-inventory.allow-override";
    protected String keyUseInventoryCreativeOverride = "use-inventory.creative-mode-overrides";
    protected String keyNavigationUseGlass = "navigation.use-glass";
    protected String keyNavigationWand = "navigation-wand.item";
    protected String keyNavigationWandMaxDistance = "navigation-wand.max-distance";
    protected String keyScriptTimeout = "scripting.timeout";
    protected String keyAllowedDataCycleBlocks = "limits.allowed-data-cycle-blocks";
    protected String keySaveDir = "saving.dir";
    protected String keyScriptsDir = "scripting.dir";
    protected String keyShowHelpInfo = "show-help-on-first-use";
    protected String keyButcherDefaultRadius = "limits.butcher-radius.default";
    protected String keyButcherMaxRadius = "limits.butcher-radius.maximum";
    protected String keyAllowSymlinks = "files.allow-symbolic-links";

    protected String keyMaxHistorySize = "history.size";
    protected String keyExpirationGrace = "history.expiration";

    public CanaryConfiguration(Logman logger, PropertiesFile properties) {
        this.logger = logger;
        this.properties = properties;
    }

    /**
     * Save the config
     */
    public void save() {
        properties.save();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        // Wand item
        {
            wandItem = properties.getInt(keyWandItem, wandItem);
        }
        // Default Change Limit
        {
            defaultChangeLimit = properties.getInt(keyDefaultChangeLimit, defaultChangeLimit);
            properties.addComment(keyDefaultChangeLimit, "");
        }
        // Max Change Limit
        {
            maxChangeLimit = properties.getInt(keyMaxChangeLimit, -1);
            properties.addComment(keyMaxChangeLimit, "");
        }
        // Default Max Polygonal Points
        {
            defaultMaxPolygonalPoints = properties.getInt(keyDefaultMaxPolygonalPoints, defaultMaxPolygonalPoints);
        }

        {
            maxPolygonalPoints = properties.getInt(keyMaxPolygonalPoints, maxPolygonalPoints);
        }
        {
            defaultMaxPolyhedronPoints = properties.getInt(keyDefaultMaxPolyhedronPoints, defaultMaxPolyhedronPoints);
        }
        {
            maxPolyhedronPoints = properties.getInt(keyMaxPolyhedronPoints, maxPolyhedronPoints);
        }
        {
            maxRadius = properties.getInt(keyMaxRadius, maxRadius);
        }
        {
            maxSuperPickaxeSize = properties.getInt(keyMaxSuperPickaxeSize, maxSuperPickaxeSize);
        }
        {
            maxBrushRadius = properties.getInt(keyMaxBrushRadius, maxBrushRadius);
        }
        {
            butcherDefaultRadius = properties.getInt(keyButcherDefaultRadius, butcherDefaultRadius);
        }
        {
            butcherMaxRadius = properties.getInt(keyButcherMaxRadius, butcherMaxRadius);
        }
        {
            // TODO
            // disallowedBlocks = new HashSet<Integer>(Ints.asList(properties.getIntArray(keyDisallowedBlocks)));
        }
        {
            // TODO
            // allowedDataCycleBlocks = new HashSet<Integer>(Ints.asList(properties.getIntArray(keyAllowedDataCycleBlocks)));
        }
        {
            registerHelp = properties.getBoolean(keyRegisterHelp, registerHelp);
            properties.addComment(keyRegisterHelp, "What is the point of this, it's not even used");
        }
        {
            logCommands = properties.getBoolean(keyLogCommands, logCommands);
        }
        {
            logFile = properties.getString(keyLogFile, logFile);
        }
        {
            superPickaxeDrop = properties.getBoolean(keySuperPickaxeDrop, superPickaxeDrop);
        }
        {
            superPickaxeManyDrop = properties.getBoolean(keySuperPickaxeManyDrop, superPickaxeManyDrop);
        }
        {
            noDoubleSlash = properties.getBoolean(keyNoDoubleSlash, noDoubleSlash);
        }
        {
            useInventory = properties.getBoolean(keyUseInventory, useInventory);
        }
        {
            useInventoryOverride = properties.getBoolean(keyUseInventoryOverride, useInventoryOverride);
        }
        {
            useInventoryCreativeOverride = properties.getBoolean(keyUseInventoryCreativeOverride, useInventoryCreativeOverride);
        }
        {
            navigationWand = properties.getInt(keyNavigationWand, navigationWand);
        }
        {
            navigationUseGlass = properties.getBoolean(keyNavigationUseGlass, navigationUseGlass);
        }
        {
            navigationWandMaxDistance = properties.getInt(keyNavigationWandMaxDistance, navigationWandMaxDistance);
        }
        {
            scriptTimeout = properties.getInt(keyScriptTimeout, scriptTimeout);
        }
        {
            saveDir = properties.getString(keySaveDir, saveDir);
        }
        {
            scriptsDir = properties.getString(keyScriptsDir, scriptsDir);
        }
        {
            allowSymlinks = properties.getBoolean(keyAllowSymlinks, allowSymlinks);
        }
        {
            LocalSession.MAX_HISTORY_SIZE = properties.getInt(keyMaxHistorySize, 15);
        }
        {
            SessionManager.EXPIRATION_GRACE = properties.getInt(keyExpirationGrace, 10);
        }
        {
            showHelpInfo = properties.getBoolean(keyShowHelpInfo, showHelpInfo);
        }
        {
            String snapshotDir = properties.getString(keySnapshotRepo, "");
            if (!snapshotDir.isEmpty()) {
                snapshotRepo = new SnapshotRepository(snapshotDir);
            }
        }
        {
            String shellType = properties.getString(keyShellSaveType, "").trim();
            shellSaveType = shellType.equals("") ? null : shellType;
        }
        {
            profile = properties.getBoolean(keyProfile, profile);
        }
    }

    public File getWorkingDirectory() {
        return new File("./worldedit/");
    }

    public void reload() {
        properties.reload();
        load();
    }

}
