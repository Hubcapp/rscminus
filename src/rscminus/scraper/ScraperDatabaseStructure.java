/**
 * rscminus
 *
 * This file is part of rscminus.
 *
 * rscminus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * rscminus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with rscminus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * Authors: see <https://github.com/RSCPlus/rscminus>
 */

package rscminus.scraper;

import rscminus.common.Logger;

import java.lang.reflect.Field;

class ScraperDatabaseStructure {

    private static final String indexName = "keyCRC";
    static int createTableJavaCount = 0;
    static int createTableSQLCount = 0;
    static String[] tableNames;

    static final ScraperDatabaseTable replayDictionaryTable = new ScraperDatabaseTable (
        "replayDictionary",
        new String[] { indexName, "filePath" },
        new int[] { 0, 512 }
    );

    static final ScraperDatabaseTable sendMessageTable = new ScraperDatabaseTable (
        "SEND_MESSAGE",
        new String[] { indexName, "timestamp", "messageType", "infoContained", "message", "sender", "sender2", "color" },
        new int[] { 0, 0, 0, 0, 1024, 64, 64, 16 }
    );

    static final ScraperDatabaseTable dialogueOptionTable = new ScraperDatabaseTable (
        "DIALOGUE_OPTION",
        new String[] { indexName, "timestamp", "numberOfOptions", "choice1", "choice2", "choice3", "choice4", "choice5" },
        new int[] { 0, 0, 0, 256, 256, 256, 256, 256 }
    );

    static final ScraperDatabaseTable updatePlayersType1Table = new ScraperDatabaseTable (
        "UPDATE_PLAYERS_TYPE_1",
        new String[] { indexName, "timestamp", "pid", "localPlayerMessageCount", "message" },
        new int[] { 0, 0, 0, 0, 1024 }
    );

    static final ScraperDatabaseTable updatePlayersType6Table = new ScraperDatabaseTable (
        "UPDATE_PLAYERS_TYPE_6",
        new String[] { indexName, "timestamp", "pid", "message" },
        new int[] { 0, 0, 0, 256 }
    );

    static final ScraperDatabaseTable receivePMTable = new ScraperDatabaseTable (
        "RECEIVE_PM",
        new String[] { indexName, "timestamp", "sendersRepeated", "moderator", "messageID", "message" },
        new int[] { 0, 0, 8, 0, 64, 1024 }
    );

    static final ScraperDatabaseTable sendPMServerEchoTable = new ScraperDatabaseTable (
        "SEND_PM_SERVER_ECHO",
        new String[] { indexName, "timestamp", "messageCount", "message" },
        new int[] { 0, 0, 0, 1024 }
    );

    static final ScraperDatabaseTable unambiguousRangedTable = new ScraperDatabaseTable (
        "unambiguousRanged",
        new String[] { indexName, "timestamp", "npcId", "damageTaken", "currentHP", "maxHP",
            "lastAmmo", "bow", "rangedLevel", "killshot" },
        new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
    );

    static final ScraperDatabaseTable npcLocationsTable = new ScraperDatabaseTable (
        "NPC_LOCATIONS",
        new String[] { indexName, "timestamp", "npcId", "npcServerIndex", "npcX", "npcY" },
        new int[] { 0, 0, 0, 0, 0, 0}
    );

    static final ScraperDatabaseTable updateNpcsType1Table = new ScraperDatabaseTable (
        "UPDATE_NPCS_TYPE_1",
        new String[] { indexName, "timestamp", "npcId", "pidTalkingTo", "message" },
        new int[] { 0, 0, 0, 0, 256 }
    );

    static final ScraperDatabaseTable inventoriesTable = new ScraperDatabaseTable (
        "INVENTORIES",
        new String[] { indexName, "timestamp", "slot", "equipped", "itemID", "amount", "opcode" },
        new int[] { 0, 0, 0, 0, 0, 0, 0 }
    );

    static final ScraperDatabaseTable shopsTable = new ScraperDatabaseTable (
        "SHOW_SHOP",
        new String[] { indexName, "timestamp", "shopOwner", "playerX", "playerY", "shopType", "sellGenerosity", "buyGenerosity", "stockSensitivity", "itemId", "amountInStock", "baseAmountInStock" },
        new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
    );

    static final ScraperDatabaseTable shopEventsTable = new ScraperDatabaseTable (
        "SHOP_EVENTS",
        new String[] { indexName, "timestamp", "tickCounter", "shop", "itemId", "eventType", "amountInStock", "baseAmountInStock", "restockCount", "destockCount" },
        new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
    );

    static final ScraperDatabaseTable sendChatTable = new ScraperDatabaseTable (
        "SEND_CHAT",
        new String[] { indexName, "timestamp", "pid", "sendCount", "message" },
        new int[] { 0, 0, 0, 0, 1024 }
    );

    static final ScraperDatabaseTable sendPMTable = new ScraperDatabaseTable (
        "SEND_PM",
        new String[] { indexName, "timestamp", "messageCount", "message" },
        new int[] { 0, 0, 0, 1024 }
    );

    static final ScraperDatabaseTable chooseDialogueOptionTable = new ScraperDatabaseTable (
        "CLIENT_CHOOSE_DIALOGUE_OPTION",
        new String[] { indexName, "timestamp", "choice" },
        new int[] { 0, 0, 0 }
    );

    static final ScraperDatabaseTable cookingTable = new ScraperDatabaseTable (
        "cooking",
        new String[] { indexName, "timestamp", "message", "messageStage", "stove", "itemCooked", "cookingLevel" },
        new int[] { 0, 0, 256, 0, 32, 0, 0 }
    );

    static final ScraperDatabaseTable woodcutTable = new ScraperDatabaseTable (
        "woodcut",
        new String[] { indexName, "timestamp", "message", "messageStage", "axeType",
            "treeType", "woodcuttingLevel", "lastInteractionOpcode" },
        new int[] { 0, 0, 256, 0, 20, 0, 0, 0 }
    );

    static final ScraperDatabaseTable fishingTable = new ScraperDatabaseTable (
        "fishing",
        new String[] { indexName, "timestamp", "message", "messageStage", "fishingSpotType", "fishingLevel", "lastInteractOpcode" },
        new int[] { 0, 0, 256, 0, 0, 0, 0 }
    );

    static void createTablesIfNotExists() {
        // get count of defined tables & ensure they exist in sql database
        int tableCount = 0;
        for (Field a : ScraperDatabaseStructure.class.getDeclaredFields()) {
            try {
                // get all tables in this file via reflection
                ScraperDatabaseTable table = ((ScraperDatabaseTable) a.get(ScraperDatabaseTable.class));
                ScraperDatabase.createTableIfNotExists(table);
                tableCount++;
            } catch (Exception e) {} // don't catch b/c it's probably just a bad cast
        }

        // cycle through again to store table names
        tableNames = new String[tableCount];
        int i = 0;
        for (Field a : ScraperDatabaseStructure.class.getDeclaredFields()) {
            try {
                // get all tables in this file via reflection
                ScraperDatabaseTable table = ((ScraperDatabaseTable) a.get(ScraperDatabaseTable.class));
                tableNames[i++] = table.getTableName();
            } catch (Exception e) {} // don't catch b/c it's probably just a bad cast
        }

        if (createTableJavaCount != createTableSQLCount) {
            Logger.Error("@|red Something went wrong creating the SQL Tables. "
                + createTableJavaCount + " != " + createTableSQLCount + "|@");
            System.exit(1); // Bad build, don't want to continue.
        } else {
            Logger.Info("Verified existence of " + createTableJavaCount + " SQL tables.");
        }
    }
    static int getRowcountFromAllTables() {
        int rowCount = 0;
        for (Field a : ScraperDatabaseStructure.class.getDeclaredFields()) {
            try {
                // get all tables in this file via reflection
                ScraperDatabaseTable table = ((ScraperDatabaseTable) a.get(ScraperDatabaseTable.class));
                rowCount += ScraperDatabase.countRowsInTable(table);
            } catch (Exception e) {} // don't catch b/c it's probably just a bad cast
        }
        return rowCount;
    }
}
