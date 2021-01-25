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

import rscminus.common.FileUtil;
import rscminus.common.JGameData;
import rscminus.common.Logger;
import rscminus.common.Settings;
import rscminus.game.ClientOpcodes;
import rscminus.game.PacketBuilder;
import rscminus.game.constants.Game;
import rscminus.game.world.ViewRegion;
import rscminus.scraper.client.Character;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static rscminus.scraper.ReplayEditor.appendingToReplay;
import static rscminus.scraper.ScraperDatabaseStructure.*;

public class ScraperProcessor implements Runnable {

    private static HashMap<Integer, Integer> sceneryLocs = new HashMap<Integer, Integer>();
    private static HashMap<Integer, Integer> boundaryLocs = new HashMap<Integer, Integer>();

    private Character[] npcsCache = new Character[500];
    private Character[] npcs = new Character[500];
    private Character[] npcsServer = new Character[5000];
    private int npcCount = 0;
    private int npcCacheCount = 0;
    private int highestOption = 0;

    private int[] inventoryItems = new int[30];
    private long[] inventoryStackAmounts = new long[30];
    private int[] inventoryItemEquipped = new int[30];
    private int[] inventoryItemsAllCount = new int[1290];
    private int lastAmmo = -1;

    private int[] playerBaseStat = new int[18];
    private int[] playerCurStat = new int[18];
    private long[] playerXP = new long[18];

    private String lastSound = "";
    private int lastSoundTimestamp = -1;
    private int lastAmmoTimestamp = -1;

    static final int SCENERY_BLANK = 65536;

    String fname;

    @Override
    public void run() {
        processReplay(fname);
    }

    public ScraperProcessor(String filename) {
        this.fname = filename;
    }

    private Character createNpc(int serverIndex, int type, int x, int y, int sprite) {
        if (npcsServer[serverIndex] == null) {
            npcsServer[serverIndex] = new Character();
            npcsServer[serverIndex].serverIndex = serverIndex;
        }

        Character character = npcsServer[serverIndex];
        boolean foundNpc = false;

        for (int var9 = 0; npcCacheCount > var9; ++var9) {
            if (serverIndex == npcsCache[var9].serverIndex) {
                foundNpc = true;
                break;
            }
        }

        if (foundNpc) {
            character.animationNext = sprite;
            character.npcId = type;
            int waypointIdx = character.waypointCurrent;
            if (character.waypointsX[waypointIdx] != x || y != character.waypointsY[waypointIdx]) {
                character.waypointCurrent = waypointIdx = (1 + waypointIdx) % 10;
                character.waypointsX[waypointIdx] = x;
                character.waypointsY[waypointIdx] = y;
            }
        } else {
            character.waypointsX[0] = character.currentX = x;
            character.waypointCurrent = 0;
            character.serverIndex = serverIndex;
            character.movingStep = 0;
            character.stepCount = 0;
            character.npcId = type;
            character.waypointsY[0] = character.currentY = y;
            character.animationNext = character.animationCurrent = sprite;
        }

        npcs[npcCount++] = character;
        return character;
    }


    private static boolean sceneryIDBlacklisted(int id, int x, int y) {
        boolean blacklist = false;
        if (id == 1147) // Spellcharge
            blacklist = true;
        else if (id == 1142) // clawspell
            blacklist = true;
        else if (id == 490) // Tree (Mithril Seeds)
            blacklist = true;
        else if (id == 1031) // Lightning
            blacklist = true;
        else if (id == 830) // Flames of zamorak
            blacklist = true;
        else if (id == 946) // dwarf multicannon base
            blacklist = true;
        else if (id == 947) // dwarf multicannon stand
            blacklist = true;
        else if (id == 948) // dwarf multicannon barrels
            blacklist = true;
        else if (id == 943) // dwarf multicannon base
            blacklist = true;
        else if (id == 1036) // Flames
            blacklist = true;
        else if (id == 1071) // Leak
            blacklist = true;
        else if (id == 1077) // Leak
            blacklist = true;

        if (blacklist)
            Logger.Debug("Scenery id " + id + " at " + x + ", " + y + " was blacklisted");

        return blacklist;
    }

    private static boolean sceneryIDRemoveList(int id, int x, int y) {
        boolean remove = false;

        if (id == 97) // fire
            remove = true;

        if (remove)
            Logger.Debug("Scenery id " + id + " at " + x + ", " + y + " was removed");

        return remove;
    }

    private static boolean boundaryIDBlacklisted(int id, int x, int y) {
        boolean blacklist = false;

        if (blacklist)
            Logger.Debug("Boundary id " + id + " at " + x + ", " + y + " was blacklisted");

        return blacklist;
    }

    private static int handleSceneryIDConflict(int before, int after) {
        if (before == SCENERY_BLANK)
            return after;

        if (before == after)
            return before;

        if (after == 4) // Treestump
            return before;
        else if (before == 4)
            return after;

        if (after == 1087) // Jungle tree stump
            return before;
        else if (before == 1087)
            return after;

        if (after == 314) // Large treestump
            return before;
        else if (before == 314)
            return after;

        Logger.Warn("unhandled scenery conflict; before: " + before + ", after: " + after);

        return before;
    }

    private static int handleSceneryIDConvert(int id) {
        if (id == 63) // doors
            id = 64;
        else if (id == 203) // Coffin
            id = 202;
        else if (id == 58) // gate
            id = 57;
        else if (id == 59) // gate
            id = 60;
        else if (id == 40) // Coffin
            id = 39;
        else if (id == 63) // doors
            id = 64;
        else if (id == 71) // cupboard
            id = 56;
        else if (id == 17) // Chest
            id = 18;
        else if (id == 136) // Chest
            id = 135;
        else if (id == 79) // manhole
            id = 78;
        else if (id == 141) // cupboard
            id = 140;

        return id;
    }

    private static int handleBoundaryIDConvert(int value) {
        int id = getPackedX(value);
        int direction = getPackedY(value);

        if (id == 1) // Doorframe
            id = 2;
        else if (id == 9) // Doorframe
            id = 8;

        return packCoordinate(id, direction);
    }

    private static int handleBoundaryIDConflict(int before, int after) {
        if (before == after)
            return before;

        int beforeID = getPackedX(before);
        int beforeDirection = getPackedY(before);
        int afterID = getPackedX(after);
        int afterDirection = getPackedY(after);

        if (beforeID == 24) // Web
            return packCoordinate(beforeID, beforeDirection);
        else if (afterID == 24) // Web
            return packCoordinate(afterID, afterDirection);
        if (beforeID == 11) // Doorframe
            return packCoordinate(afterID, afterDirection);
        else if (afterID == 11)
            return packCoordinate(beforeID, beforeDirection);

        Logger.Warn("unhandled boundary conflict; before: " + beforeID + ", after: " + afterID);

        return before;
    }

    //convertImage & saveBitmap are mostly courtesy of aposbot, altered a little
    //used for opcode 117, sleepwords
    private static byte[] convertImage(byte[] data) {
        int dataIndex = 1;
        byte color = 0;
        final byte[] imageBytes = new byte[10200];
        int index;
        int height;
        int width;
        for (index = 0; index < 255; color = (byte) (255 - color)) {
            height = data[dataIndex++] & 255;
            for (width = 0; width < height; ++width) {
                imageBytes[index++] = color;
            }
        }
        for (height = 1; height < 40; ++height) {
            width = 0;
            while (width < 255) {
                if (dataIndex++ >= data.length - 1)
                    break;

                // run length encoded
                final int rle = data[dataIndex] & 255;
                for (int i = 0; i < rle; ++i) {
                    imageBytes[index] = imageBytes[index - 255];
                    ++index;
                    ++width;
                }
                if (width < 255) {
                    imageBytes[index] = (byte) (255 - imageBytes[index - 255]);
                    ++index;
                    ++width;
                }
            }
        }
        return imageBytes;
    }
    private static byte[] saveBitmap(byte[] data) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        out.write(66);
        out.write(77);
        short var3 = 1342;
        out.write(var3 & 255);
        out.write(var3 >> 8 & 255);
        out.write(0);
        out.write(0);
        out.write(0);
        out.write(0);
        out.write(0);
        out.write(0);
        byte var10 = 62;
        out.write(var10 & 255);
        out.write(var10 >> 8 & 255);
        out.write(0);
        out.write(0);
        var10 = 40;
        out.write(var10 & 255);
        out.write(var10 >> 8 & 255);
        out.write(0);
        out.write(0);
        var3 = 256;
        out.write(var3 & 255);
        out.write(var3 >> 8 & 255);
        out.write(0);
        out.write(0);
        var10 = 40;
        out.write(var10 & 255);
        out.write(var10 >> 8 & 255);
        out.write(0);
        out.write(0);
        var10 = 1;
        out.write(var10 & 255);
        out.write(var10 >> 8 & 255);
        var10 = 1;
        out.write(var10 & 255);
        out.write(var10 >> 8 & 255);
        var10 = 0;
        out.write(var10 & 255);
        out.write(var10 >> 8 & 255);
        out.write(0);
        out.write(0);
        var10 = 0;
        out.write(var10 & 255);
        out.write(var10 >> 8 & 255);
        out.write(0);
        out.write(0);
        var10 = 0;
        out.write(var10 & 255);
        out.write(var10 >> 8 & 255);
        out.write(0);
        out.write(0);
        var10 = 0;
        out.write(var10 & 255);
        out.write(var10 >> 8 & 255);
        out.write(0);
        out.write(0);
        var10 = 0;
        out.write(var10 & 255);
        out.write(var10 >> 8 & 255);
        out.write(0);
        out.write(0);
        var10 = 0;
        out.write(var10 & 255);
        out.write(var10 >> 8 & 255);
        out.write(0);
        out.write(0);
        out.write(0);
        out.write(0);
        out.write(0);
        out.write(0);
        out.write(255);
        out.write(255);
        out.write(255);
        out.write(0);
        int var4 = 9945;
        for (int var5 = 0; var5 < 40; ++var5) {
            for (int var6 = 0; var6 < 32; ++var6) {
                byte var7 = 0;
                for (int var8 = 0; var8 < 8; ++var8) {
                    var7 = (byte) (2 * var7);
                    if (var6 != 31 || var8 != 7) {
                        if (data[var4] != 0) {
                            ++var7;
                        }
                        ++var4;
                    }
                }
                out.write(var7);
            }
            var4 -= 510;
        }
        out.close();
        return out.toByteArray();
    }

    private static int packCoordinate(int x, int y) {
        return ((x & 0xFFFF) << 16) | (y & 0xFFFF);
    }

    public static int getPackedX(int value) {
        return (value >> 16) & 0xFFFF;
    }

    public static int getPackedY(int value) {
        return value & 0xFFFF;
    }

    private static boolean validCoordinates(int x, int y) {
        if (x < 0 || y < 0)
            return false;

        int viewX = x >> 3;
        int width = Game.WORLD_WIDTH >> 3;
        if (viewX >= width)
            return false;
        int height = Game.WORLD_HEIGHT >> 3;
        int viewY = y >> 3;
        if (viewY >= height)
            return false;
        return true;
    }

    private static void fillView(int playerX, int playerY, HashMap<Integer, Integer> scenery) {
        int viewX = (playerX >> 3) << 3;
        int viewY = (playerY >> 3) << 3;
        int size = ViewRegion.VIEW_DISTANCE << 3;
        int index = (ViewRegion.VIEW_DISTANCE / 2) << 3;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int updateX = viewX + (x - index);
                int updateY = viewY + (y - index);
                int key = packCoordinate(updateX, updateY);
                if (!scenery.containsKey(key))
                    scenery.put(key, SCENERY_BLANK);
            }
        }
    }

    private static int getShopId(int lastNPCTalkedTo, int playerX, int playerY, int timestamp, String fname) {
        if (lastNPCTalkedTo != -1) {
            String sql = "SELECT * FROM shops WHERE ownerID like " + lastNPCTalkedTo + " OR assistantID like " + lastNPCTalkedTo + ";";
            int[] shopInfo = ScraperDatabase.getShopBoundaries(sql, (int) Thread.currentThread().getId() % Settings.threads);
            int northEastX = shopInfo[3];
            int southWestX = shopInfo[5];
            int northEastY = shopInfo[4];
            int southWestY = shopInfo[6];

            if (playerX >= northEastX && playerX <= southWestX && playerY >= northEastY && playerY <= southWestY) {
                return shopInfo[0];
            } else {
                Logger.Error("sanity check failed, player last talked to NPC outside their expected area! "
                    + fname  + " ;; timestamp: " + timestamp + " ;; playerX: " + playerX
                    + " ;; playerY: " + playerY + " ;; LAST NPC ID: " + lastNPCTalkedTo);
            }
        } else {
            // out.bin is likely corrupt. Must fall back to coordinate based shop detection.
            String sql = "SELECT * FROM shops WHERE northEastX <= " + playerX + " AND northEastY <= " + playerY +
                " AND southWestX >= " + playerX + " AND southWestY >= " + playerY + ";";
            int[] shopInfo = ScraperDatabase.getShopBoundaries(sql,
                (int) Thread.currentThread().getId() % Settings.threads);
            if (shopInfo != null) {
                return shopInfo[0];
            } else {
                Logger.Error("Problem identifying shop using the fallback coordinates method; playerX: " +
                    playerX + " playerY: " + playerY);
            }
        }
        return -1;
    }

    private static boolean isAmmo(int itemID) {
        switch (itemID) {
            case 11:
            case 190:
            case 574:
            case 592:
            case 638:
            case 639:
            case 640:
            case 641:
            case 642:
            case 643:
            case 644:
            case 645:
            case 646:
            case 647:
            case 723:
            case 786:
            case 984:
            case 985:
            case 827:

            case 1013:
            case 1014:
            case 1015:
            case 1024:
            case 1068:
            case 1069:
            case 1070:

            case 1075:
            case 1076:
            case 1077:
            case 1078:
            case 1079:
            case 1080:
            case 1081:

            case 1088:
            case 1089:
            case 1090:
            case 1091:
            case 1092:
            case 1122:
            case 1123:
            case 1124:
            case 1125:
            case 1126:
            case 1127:
            case 1128:
            case 1129:
            case 1130:
            case 1131:
            case 1132:
            case 1133:
            case 1134:
            case 1135:
            case 1136:
            case 1137:
            case 1138:
            case 1139:
            case 1140:
                return true;
            default:
                return false;
        }
    }

    private boolean removalIsKillshot(Character npc, int localPID, int playerX, int playerY, int keyCRC, int timestamp) {
        int soundThreshold = 80;
        int ammoThreshold = 110;

        // if ammo hasn't been removed from the inventory recently, there's no way that this is a killshot.
        if (timestamp - lastAmmoTimestamp > ammoThreshold) {
            return false;
        }

        // npc actively has an unprocessed hit against them, and it is by the local player.
        if (npc.attackingPlayerServerIndex == localPID) {
            // The NPC can path to the player & begin melee
            // then the game removes the NPC for a moment so it can update their animation
            if (lastSound.equals("victory") && (timestamp - lastSoundTimestamp < soundThreshold)) {
                return true;
            }
        }

        // Player is close enough to the NPC when it despawns that it is not removed due to being too far away
        if (Math.abs(npc.currentX - playerX) < 12 &&
            Math.abs(npc.currentY - playerY) < 12) {
            // Player can't possibly be in melee combat, because they are not on the same tile
            if (Math.abs(npc.currentX - playerX) > 0 ||
                Math.abs(npc.currentY - playerY) > 0 ) {
                // Player previously was in ranged combat with this NPC
                if (npc.lastAttackerIndex != -1 && npc.lastAttackerIndex == localPID) {
                    // Player's last major action involved defeating an enemy, and recently
                    if (lastSound.equals("victory") && (timestamp - lastSoundTimestamp < soundThreshold)) {
                        return true;
                    } else {
                        Logger.Info("@|white [" + keyCRC + "]|@ last sound wasn't 'victory', surprisingly");
                    }
                }
            }
        }
        return false;
    }

    // this sets the metadata byte in the replayeditor for when it gets written out to file
    private void setFlags(ReplayEditor editor) {
        byte[] metadata = editor.getMetadata();
        if (Settings.sanitizePublicChat)
            metadata[ReplayEditor.METADATA_FLAGS_OFFSET] |= ReplayEditor.FLAG_SANITIZE_PUBLIC;
        if (Settings.sanitizePrivateChat)
            metadata[ReplayEditor.METADATA_FLAGS_OFFSET] |= ReplayEditor.FLAG_SANITIZE_PRIVATE;
        if (Settings.sanitizeFriendsIgnore)
            metadata[ReplayEditor.METADATA_FLAGS_OFFSET] |= ReplayEditor.FLAG_SANITIZE_FRIENDSIGNORES;
        if (Scraper.sanitizeVersion != -1 && editor.getReplayVersion().version != Scraper.sanitizeVersion)
            metadata[ReplayEditor.METADATA_FLAGS_OFFSET] |= ReplayEditor.FLAG_SANITIZE_VERSION;
    }

    private void processReplay(String fname) {

        ReplayEditor editor = new ReplayEditor();
        setFlags(editor);
        boolean success = false;
        int keyCRC = 0;

        List<String> thisReplaySqlStatements = new ArrayList<>();

        if (editor.importNonPacketData(fname)) {
            keyCRC = editor.getKeyCRC();
            String visitedReplay = Scraper.m_replaysKeysProcessed.get(keyCRC);
            if (visitedReplay != null) {
                Logger.Warn("@|red Skipping replay |@@|yellow " + fname + "|@@|red , seems to be a duplicate of |@@|green " + visitedReplay + "|@");
                return;
            } else {
                Scraper.m_replaysKeysProcessed.put(keyCRC, fname);
            }

            success = editor.importPacketData(fname);
        }

        if (!success) {
            Logger.Warn("Replay " + fname + " is not valid, skipping");
            return;
        }

        Logger.Info("@|cyan Started sanitizing |@@|white [" + keyCRC + "]|@ aka "+ fname);

        Logger.Info("@|white [" + keyCRC + "]|@ client version: " + editor.getReplayVersion().clientVersion);
        Logger.Info("@|white [" + keyCRC + "]|@ replay version: " + editor.getReplayVersion().version);

        if (!Settings.sanitizeForce && !editor.authenticReplay()) {
            Logger.Warn("Replay " + keyCRC + " is not an authentic rsc replay. skipping");
            return;
        }

        Logger.Debug(fname);

        int localPID = -1;
        int playerX = -1;
        int playerY = -1;
        int playerAnim = -1;
        int planeX = -1;
        int planeY = -1;
        int planeFloor = -1;
        int floorYOffset = -1;
        int length = -1;

        if (Settings.dumpMessages || Settings.dumpChat) {
            thisReplaySqlStatements.add(
                ScraperDatabase.newSQLStatement(
                    Scraper.m_replayDictionarySQL,
                    ScraperDatabase.getInsertStatement(
                        replayDictionaryTable,
                        new Object[] {
                             keyCRC,
                             fname.replaceFirst(Settings.sanitizePath, "")
                        }
                    )
                )
            );
        }

        int op234type1EchoCount = 0;
        int sendPMEchoCount = 0;
        for (int slot = 0; slot < 30; slot++) {
            inventoryItems[slot] = -1;
            inventoryStackAmounts[slot] = -1;
            inventoryItemEquipped[slot] = -1;
        }
        for (int itemID = 0; itemID < 1290; itemID++) {
            inventoryItemsAllCount[itemID] = 0;
        }

        ArrayList<ReplayPacket> interestingSleepPackets = new ArrayList<ReplayPacket>();

        RNGEventIdentifiers rng = new RNGEventIdentifiers();
        int op216Count = 0;
        int sendPMCount = 0;
        int tickCounter = 0;

        Shop[] shops = new Shop[86];


        LinkedList<ReplayPacket> packets = editor.getPackets();

        int useItemWithSceneryX = -1;
        int useItemWithSceneryY = -1;
        int useItemWithSceneryItemID = -1;
        int interactWithSceneryOption2X = -1;
        int interactWithSceneryOption2Y = -1;
        int interactWithSceneryOption1X = -1;
        int interactWithSceneryOption1Y = -1;
        int lastSceneryInteractX = useItemWithSceneryX;
        int lastSceneryInteractY = useItemWithSceneryY;
        int lastInteractOpcode = -1;
        int lastNPCTalkedTo = -1;
        int lastBuyItem = -1;
        int lastBuyTimestamp = -1;
        int lastBuyAmount = -1;
        int lastSellItem = -1;
        int lastSellTimestamp = -1;
        int lastSellAmount = -1;
        boolean shopWasClosed = true;

        for (ReplayPacket packet : packets) {
            if (packet.incoming) {
                Logger.Debug("@|white [" + keyCRC + "]|@ " + String.format("incoming opcode: %d", packet.opcode));
                try {
                    switch (packet.opcode) {
                        case ReplayEditor.VIRTUAL_OPCODE_CONNECT:
                            Logger.Info("@|white [" + keyCRC + "]|@ loginresponse: " + packet.data[0] + " (timestamp: " + packet.timestamp + ")");
                            if (Settings.dumpSleepWords) {
                                interestingSleepPackets.add(packet);
                            }

                            break;

                        case PacketBuilder.OPCODE_FLOOR_SET:
                            localPID = packet.readUnsignedShort();
                            planeX = packet.readUnsignedShort();
                            planeY = packet.readUnsignedShort();
                            planeFloor = packet.readUnsignedShort();
                            floorYOffset = packet.readUnsignedShort();
                            break;
                        case PacketBuilder.OPCODE_SEND_MESSAGE:
                            if (Settings.dumpMessages || Settings.dumpSleepWords || Settings.dumpRNGEvents) {
                                int type = packet.readUnsignedByte();
                                int infoContained = packet.readUnsignedByte();

                                String sendMessage = packet.readPaddedString();

                                if (Settings.dumpSleepWords) {
                                    if (sendMessage.equals("You are unexpectedly awoken! You still feel tired")) {
                                        interestingSleepPackets.add(packet);
                                    }
                                }

                                if (Settings.dumpRNGEvents) {
                                    try {
                                        // Dump cooking food chances
                                        Object[] foodResult =
                                        rng.identifyFood(rng.matchesCooking(sendMessage), sendMessage, keyCRC,
                                            packet.timestamp, playerCurStat[Game.STAT_COOKING],
                                            useItemWithSceneryItemID, useItemWithSceneryX, useItemWithSceneryY);

                                        if ((boolean) foodResult[0]) {
                                            thisReplaySqlStatements.add((String) foodResult[1]);
                                        }
                                    } catch (Exception e) {
                                        Logger.Error("Error identifying food! [" + fname + "] @ " + packet.timestamp);
                                        e.printStackTrace();
                                    }

                                    try {
                                        // Dump success rate at cutting trees

                                        int sceneryID = 60000;
                                        if (lastSceneryInteractX == -1) {
                                            // method assumes there is no spot in map where player is adjacent to
                                            // two fishing spots at the same time. Seems true.
                                            sceneryID = Scraper.worldManager.coordinateIsAdjacentScenery(playerX, playerY,
                                                new int[] { 0, 1, 70, 306, 307, 308, 309, 1086, 1091, 1092, 1099, 1100 });

                                        } else {
                                            sceneryID = Scraper.worldManager.getSceneryId(lastSceneryInteractX, lastSceneryInteractY);
                                            if (sceneryID == 60000) {
                                                sceneryID = Scraper.worldManager.coordinateIsAdjacentScenery(playerX, playerY,
                                                    new int[] { 0, 1, 70, 306, 307, 308, 309, 1086, 1091, 1092, 1099, 1100 });
                                            }
                                        }

                                        Object[] woodResult =
                                        rng.identifyWood(rng.matchesWoodcut(sendMessage), sendMessage, keyCRC,
                                            packet.timestamp, playerCurStat[Game.STAT_WOODCUTTING],
                                            Scraper.worldManager.getSceneryId(lastSceneryInteractX, lastSceneryInteractY),
                                            lastInteractOpcode);

                                        if ((boolean) woodResult[0]) {
                                            thisReplaySqlStatements.add((String) woodResult[1]);
                                        }
                                    } catch (Exception e) {
                                        Logger.Error("Error identifying wood! [" + fname + "] @ " + packet.timestamp);
                                        e.printStackTrace();
                                    }

                                    try {
                                        // Dump success rate at fishing
                                        int sceneryID = 60000;
                                        if (lastSceneryInteractX == -1) {
                                            // method assumes there is no spot in map where player is adjacent to
                                            // two fishing spots at the same time. Seems true.
                                            sceneryID = Scraper.worldManager.coordinateIsAdjacentScenery(playerX, playerY,
                                                new int[] { 192, 193, 194, 261, 271, 351, 352, 376 });

                                        } else {
                                            sceneryID = Scraper.worldManager.getSceneryId(lastSceneryInteractX, lastSceneryInteractY);
                                            if (sceneryID == 60000) {
                                                sceneryID = Scraper.worldManager.coordinateIsAdjacentScenery(playerX, playerY,
                                                    new int[] { 192, 193, 194, 261, 271, 351, 352, 376 });
                                            }
                                        }

                                        Object[] fishResult =
                                        rng.identifyFish(rng.matchesFish(sendMessage, type), sendMessage, keyCRC,
                                            packet.timestamp, playerCurStat[Game.STAT_FISHING], sceneryID, lastInteractOpcode);

                                        if ((boolean) fishResult[0]) {
                                            thisReplaySqlStatements.add((String) fishResult[1]);
                                        }
                                    } catch (Exception e) {
                                        Logger.Error("Error identifying fish! [" + fname + "] @ " + packet.timestamp);
                                        e.printStackTrace();
                                    }
                                }

                                if (Settings.dumpMessages) {
                                    String sender = "";
                                    String clan = "";
                                    String color = "";
                                    if ((infoContained & 1) != 0) {
                                        sender = packet.readPaddedString();
                                        clan = packet.readPaddedString();
                                    }

                                    if ((infoContained & 2) != 0) {
                                        color = packet.readPaddedString();
                                    }
                                    thisReplaySqlStatements.add(
                                        ScraperDatabase.newSQLStatement(
                                            Scraper.m_messageSQL,
                                            ScraperDatabase.getInsertStatement(
                                                sendMessageTable,
                                                new Object[] {
                                                    keyCRC,
                                                    packet.timestamp,
                                                    type,
                                                    infoContained,
                                                    sendMessage,
                                                    sender,
                                                    clan,
                                                    color
                                                }
                                            )
                                        )
                                    );
                                }
                            }
                            break;
                        case PacketBuilder.OPCODE_DIALOGUE_OPTIONS:
                            if (Settings.dumpMessages) {
                                int numberOfOptions = packet.readUnsignedByte();

                                String[] choices = new String[] { "", "", "", "", "" };
                                for (int i = 0; i < numberOfOptions; i++) {
                                    choices[i] = packet.readPaddedString();
                                }
                                thisReplaySqlStatements.add(
                                    ScraperDatabase.newSQLStatement(
                                        Scraper.m_messageSQL,
                                        ScraperDatabase.getInsertStatement(
                                            dialogueOptionTable,
                                            new Object[] {
                                                editor.getKeyCRC(),
                                                packet.timestamp,
                                                numberOfOptions,
                                                choices[0],
                                                choices[1],
                                                choices[2],
                                                choices[3],
                                                choices[4]
                                            }
                                        )
                                    )
                                );
                            }
                            break;
                        case PacketBuilder.OPCODE_APPEARANCE_KEEPALIVE: // 213
                            ++tickCounter;
                            break;
                        case PacketBuilder.OPCODE_CREATE_PLAYERS: // 191
                            ++tickCounter;
                            packet.startBitmask();
                            playerX = packet.readBitmask(11);
                            playerY = packet.readBitmask(13);
                            playerAnim = packet.readBitmask(4);
                            packet.readBitmask(4);
                            packet.endBitmask();
                            if (Settings.dumpScenery) {
                                fillView(playerX, playerY, sceneryLocs);
                            }
                            packet.skip(packet.data.length - 4);
                            break;
                        case PacketBuilder.OPCODE_UPDATE_PLAYERS: { // 234
                            int originalPlayerCount = packet.readUnsignedShort();
                            int playerCount = originalPlayerCount;
                            for (int i = 0; i < originalPlayerCount; i++) {
                                int startPosition = packet.tell();
                                int pid = packet.readUnsignedShort();
                                int updateType = packet.readUnsignedByte();
                                if (updateType == 0) { // bubble overhead
                                    packet.skip(2);
                                } else if (updateType == 1) { // chat
                                    packet.skip(1);
                                    String chatMessage = packet.readRSCString();
                                    if (Settings.dumpChat) {
                                        thisReplaySqlStatements.add(
                                            ScraperDatabase.newSQLStatement(
                                                Scraper.m_chatSQL,
                                                ScraperDatabase.getInsertStatement(
                                                    updatePlayersType1Table,
                                                    new Object[] {
                                                        keyCRC,
                                                        packet.timestamp,
                                                        pid,
                                                        pid == localPID ? op234type1EchoCount++ : -1,
                                                        chatMessage
                                                    }
                                                )
                                            )
                                        );
                                    }

                                    // Strip Chat
                                    if (Settings.sanitizePublicChat) {
                                        int trimSize = packet.tell() - startPosition;
                                        packet.skip(-trimSize);
                                        packet.trim(trimSize);
                                        playerCount--;
                                        continue;
                                    }
                                } else if (updateType == 2) { // damage taken
                                    packet.skip(3);
                                } else if (updateType == 3) { // show projectile towards an NPC
                                    int sprite = packet.readUnsignedShort();
                                    int shooterIndex = packet.readUnsignedShort();
                                    if (Settings.dumpNPCDamage) {
                                        if (npcsServer[shooterIndex] != null) {
                                            npcsServer[shooterIndex].attackingPlayerServerIndex = pid;
                                            npcsServer[shooterIndex].incomingProjectileSprite = sprite;
                                            npcsServer[shooterIndex].lastAttackerIndex = pid;
                                            npcsServer[shooterIndex].lastSprite = sprite;
                                        } else {
                                            // NPC possibly off screen & being shot by player closer to the NPC.
                                        }
                                    }
                                } else if (updateType == 4) { // show projectile towards a player
                                    int sprite = packet.readUnsignedShort();
                                    int shooterIndex = packet.readUnsignedShort();
                                    if (Settings.dumpNPCDamage) {
                                        if (sprite != 3) { // gnome ball
                                            Logger.Info("@|white [" + keyCRC + "]|@ " + pid + " shot at " + shooterIndex + " with " + sprite);
                                        }
                                    }
                                } else if (updateType == 5) { // equipment change
                                    packet.skip(2);
                                    packet.readPaddedString();
                                    packet.readPaddedString();
                                    int equipCount = packet.readUnsignedByte();
                                    packet.skip(equipCount);
                                    packet.skip(6);
                                } else if (updateType == 6) { // quest chat
                                    String message = packet.readRSCString();
                                    if (Settings.dumpMessages) {
                                        thisReplaySqlStatements.add(
                                            ScraperDatabase.newSQLStatement(
                                                Scraper.m_messageSQL,
                                                ScraperDatabase.getInsertStatement(
                                                    updatePlayersType6Table,
                                                    new Object[] {
                                                        keyCRC,
                                                        packet.timestamp,
                                                        pid,
                                                        message
                                                    }
                                                )
                                            )
                                        );
                                    }
                                } else {
                                    Logger.Info("@|white [" + keyCRC + "]|@ Hit unanticipated update type " + updateType);
                                    packet.skip(2);
                                    packet.readPaddedString();
                                    packet.readPaddedString();
                                    packet.skip(6 + packet.readUnsignedByte());
                                }
                            }

                            // Rewrite player count
                            if (originalPlayerCount != playerCount) {
                                if (playerCount == 0) {
                                    packet.opcode = ReplayEditor.VIRTUAL_OPCODE_NOP;
                                } else {
                                    packet.seek(0);
                                    packet.writeUnsignedShort(playerCount);
                                }
                            }
                            break;
                        }
                        case PacketBuilder.OPCODE_SET_IGNORE:
                            if (Settings.sanitizeFriendsIgnore)
                                packet.opcode = ReplayEditor.VIRTUAL_OPCODE_NOP;
                            break;
                        case PacketBuilder.OPCODE_UPDATE_IGNORE:
                            if (Settings.sanitizeFriendsIgnore)
                                packet.opcode = ReplayEditor.VIRTUAL_OPCODE_NOP;
                            break;
                        case PacketBuilder.OPCODE_UPDATE_FRIEND:
                            if (Settings.sanitizeFriendsIgnore)
                                packet.opcode = ReplayEditor.VIRTUAL_OPCODE_NOP;
                            try {
                                packet.readPaddedString(); // Friend's name
                                packet.readPaddedString(); // Friend's old name
                                int onlineStatus = packet.readByte();
                                if (onlineStatus > 1) { // the friend is online, offline can be 0 or 1 see fsnom2@aol.com2/08-03-2018 14.14.44 for 1
                                    String world = packet.readPaddedString();
                                    if (world.startsWith("Classic")) {
                                        if (onlineStatus == 6) { // same world
                                            int worldNum = Integer.parseInt(world.substring(world.length() - 1));
                                            editor.getReplayMetadata().IPAddress4 = worldNum;
                                        } else {
                                            int worldNumExcluded = Integer.parseInt(world.substring(world.length() - 1));
                                            if (worldNumExcluded <= 5) {
                                                editor.getReplayMetadata().world_num_excluded |= (int) Math.pow(2, worldNumExcluded - 1);
                                            } else {
                                                editor.foundInauthentic = true;
                                                Logger.Warn("@|white [" + keyCRC + "]|@ Inauthentic amount of worlds");
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Logger.Error("@|white [" + keyCRC + "]|@ " + String.format("error parsing opcode_update_friend, packet.timestamp: %d", packet.timestamp));
                            }
                            break;
                        case PacketBuilder.OPCODE_RECV_PM:
                            if (Settings.dumpChat) {
                                String recvPMSender1 = packet.readPaddedString(); //sender's name
                                String recvPMSender2 = packet.readPaddedString(); //sender's name again
                                Boolean recvPMSenderTwice = recvPMSender1.equals(recvPMSender2);
                                if (!recvPMSenderTwice) {
                                    Logger.Warn("@|white [" + keyCRC + "]|@ Sender1 != Sender2 in OPCODE_RECV_PM!");
                                }
                                int recvPMModeratorStatus = packet.readByte();
                                BigInteger recvPMMessageID = packet.readUnsignedLong();
                                String recvPMMessage = packet.readRSCString();

                                thisReplaySqlStatements.add(
                                    ScraperDatabase.newSQLStatement(
                                        Scraper.m_chatSQL,
                                        ScraperDatabase.getInsertStatement(
                                            receivePMTable,
                                            new Object[] {
                                                keyCRC,
                                                packet.timestamp,
                                                recvPMSenderTwice ? "1" : "0",
                                                recvPMModeratorStatus,
                                                recvPMMessageID.toString(),
                                                recvPMMessage
                                            }
                                        )
                                    )
                                );
                            }

                            if (Settings.sanitizePrivateChat)
                                packet.opcode = ReplayEditor.VIRTUAL_OPCODE_NOP;
                            break;
                        case PacketBuilder.OPCODE_SEND_PM:
                            if (Settings.dumpChat) {
                                packet.readPaddedString(); //recipient's name
                                String sendPMMessage = packet.readRSCString();
                                thisReplaySqlStatements.add(
                                    ScraperDatabase.newSQLStatement(
                                        Scraper.m_chatSQL,
                                        ScraperDatabase.getInsertStatement(
                                            sendPMServerEchoTable,
                                            new Object[] {
                                                keyCRC,
                                                packet.timestamp,
                                                sendPMEchoCount++,
                                                sendPMMessage
                                            }
                                        )
                                    )
                                );
                            }
                            if (Settings.sanitizePrivateChat)
                                packet.opcode = ReplayEditor.VIRTUAL_OPCODE_NOP;
                            break;
                        case PacketBuilder.OPCODE_CREATE_NPC: // 79
                            if (Settings.needNpcCreation) {
                                npcCacheCount = npcCount;
                                npcCount = 0;
                                for (int index = 0; index < npcCacheCount; ++index) {
                                    npcsCache[index] = npcs[index];
                                }

                                packet.startBitmask();
                                int createNpcCount = packet.readBitmask(8);
                                int animation;
                                for (int npcIndex = 0; npcIndex < createNpcCount; npcIndex++) {
                                    Character npc = npcsCache[npcIndex];
                                    int reqUpdate = packet.readBitmask(1);
                                    if (reqUpdate == 1) {
                                        int updateType = packet.readBitmask(1);
                                        if (updateType != 0) { // stationary animation update
                                            animation = packet.readBitmask(2);
                                            if (animation != 3) {
                                                animation <<= 2;
                                                animation |= packet.readBitmask(2);

                                                npc.animationNext = animation;
                                            } else {
                                                // npc is removed
                                                if (Settings.dumpNPCDamage) {
                                                    if (npc == null) continue;
                                                    if (removalIsKillshot(npc, localPID, playerX, playerY, keyCRC, packet.timestamp)) {
                                                        Logger.Info("@|white [" + keyCRC + "]|@ killshot!!! PID " + npc.lastAttackerIndex + " killed NPC Index " + npc.serverIndex + " @ timestamp:" + packet.timestamp);

                                                        if (npc.attackingPlayerServerIndex != localPID) {
                                                            npc.incomingProjectileSprite = npc.lastSprite;
                                                        }
                                                        // npc was under attack at time of death, this is a kill shot
                                                        switch (npc.incomingProjectileSprite) {
                                                            case -1:
                                                                Logger.Info("@|white [" + keyCRC + "]|@ incoming projectile sprite not set, but npc was damaged while not in melee combat");
                                                                break;
                                                            case 2: // ranged projectile

                                                                // best case scenario. We know who is attacking, that it is definitely ranged, and all stats

                                                                // determine bow wielded; might not exist if spear, dart, throwing knife
                                                                int bow = -1;
                                                                for (int slot = 0; slot < 30; slot++) {
                                                                    if (inventoryItemEquipped[slot] == 1) {
                                                                        switch (inventoryItems[slot]) {
                                                                            case 59:
                                                                            case 60:
                                                                            case 188:
                                                                            case 189:
                                                                            case 648:
                                                                            case 649:
                                                                            case 650:
                                                                            case 651:
                                                                            case 652:
                                                                            case 653:
                                                                            case 654:
                                                                            case 655:
                                                                            case 656:
                                                                            case 657:
                                                                                bow = inventoryItems[slot];
                                                                        }
                                                                    }
                                                                }

                                                                thisReplaySqlStatements.add(
                                                                    ScraperDatabase.newSQLStatement(
                                                                        Scraper.m_damageSQL,
                                                                        ScraperDatabase.getInsertStatement(
                                                                            unambiguousRangedTable,
                                                                            new Object[] {
                                                                                keyCRC,
                                                                                packet.timestamp,
                                                                                npc.npcId,
                                                                                npc.healthCurrent, // damage taken, assumed to be current health
                                                                                npc.healthCurrent,
                                                                                npc.healthMax,
                                                                                lastAmmo, // arrow/bolt/knife/dart/spear was determined in inventory updates
                                                                                bow,
                                                                                playerCurStat[Game.STAT_RANGED],
                                                                                1 // killshot
                                                                            }
                                                                        )
                                                                    )
                                                                );


                                                                break;
                                                            case 3: // gnomeball
                                                                Logger.Info("@|white [" + keyCRC + "]|@ What in tarnation? enemy killed by gnomeball??");
                                                                break;
                                                            case 1: // magic spell
                                                            case 4: // iban blast
                                                            case 6: // god spell
                                                                break;
                                                            case 5: // cannonball
                                                                break;
                                                            default:
                                                                Logger.Info("@|white [" + keyCRC + "]|@ unknown projectile, shouldn't be possible;; timestamp: " + packet.timestamp);
                                                                break;
                                                        }

                                                        npc.attackingPlayerServerIndex = -1;
                                                        npc.incomingProjectileSprite = -1;
                                                    }
                                                }

                                                continue;
                                            }
                                        } else { // npc is moving to another tile
                                            int nextAnim = packet.readBitmask(3); // animation
                                            int var11 = npc.waypointCurrent;
                                            int var12 = npc.waypointsX[var11];
                                            if (nextAnim == 2 || nextAnim == 1 || nextAnim == 3) {
                                                var12 += 128;
                                            }

                                            int var13 = npc.waypointsY[var11];
                                            if (nextAnim == 6 || nextAnim == 5 || nextAnim == 7) {
                                                var12 -= 128;
                                            }

                                            if (nextAnim == 4 || nextAnim == 3 || nextAnim == 5) {
                                                var13 += 128;
                                            }

                                            if (nextAnim == 0 || nextAnim == 1 || nextAnim == 7) {
                                                var13 -= 128;
                                            }

                                            npc.waypointCurrent = var11 = (var11 + 1) % 10;
                                            npc.animationNext = nextAnim;
                                            npc.waypointsX[var11] = var12;
                                            npc.waypointsY[var11] = var13;
                                        }
                                    }

                                    npcs[npcCount++] = npc;
                                }


                                while (packet.tellBitmask() + 34 < packet.data.length * 8) {
                                    int npcServerIndex = packet.readBitmask(12);
                                    int npcXCoordinate = packet.readBitmask(5);
                                    int npcYCoordinate = packet.readBitmask(5);
                                    int npcAnimation = packet.readBitmask(4);
                                    int npcId = packet.readBitmask(10);

                                    if (npcXCoordinate > 15) {
                                        npcXCoordinate -= 32;
                                    }
                                    if (npcYCoordinate > 15) {
                                        npcYCoordinate -= 32;
                                    }

                                    int x = npcXCoordinate + playerX;
                                    int y = npcYCoordinate + playerY;

                                    if (Settings.dumpNpcLocs) {
                                        thisReplaySqlStatements.add(
                                            ScraperDatabase.newSQLStatement(
                                                Scraper.m_npcLocsSQL,
                                                ScraperDatabase.getInsertStatement(
                                                    npcLocationsTable,
                                                    new Object[] {
                                                        keyCRC,
                                                        packet.timestamp,
                                                        npcId,
                                                        npcServerIndex,
                                                        x,
                                                        y
                                                    }
                                                )
                                            )
                                        );
                                    }

                                    createNpc(npcServerIndex, npcId, x, y, npcAnimation);
                                }

                                packet.endBitmask();
                            }

                            break;
                        case PacketBuilder.OPCODE_UPDATE_NPC: // 104
                            if (Settings.dumpMessages || Settings.dumpNPCDamage) {
                                int updateNpcCount = packet.readUnsignedShort();
                                for (int i = 0; i < updateNpcCount; i++) {
                                    int npcServerIndex = packet.readUnsignedShort();

                                    // There seems to be a bug in the server where it will (very rarely) send npc server index + 4096.
                                    // This can be detected & corrected so that we can still scrape data,
                                    // but we won't export the correction, to preserve the authentic behaviour of the server.
                                    if (npcServerIndex >= 5000) {
                                        Logger.Warn("Received NPC server index out of bounds...!! decrementing by 4096" + fname);
                                        npcServerIndex -= 4096;
                                    } else if (npcsServer[npcServerIndex] == null && npcServerIndex >= 4096) {
                                        Logger.Warn("Received NPC server index not defined...!! decrementing by 4096 " + fname);
                                        npcServerIndex -= 4096;
                                        if (npcsServer[npcServerIndex] == null) {
                                            // this seems to not actually happen. :-)
                                            Logger.Error("That didn't help. May be a problem in RSC-");
                                        }
                                    }

                                    int updateType = packet.readByte();
                                    if (updateType == 1) { // npc chat
                                        int pidTalkingTo = packet.readUnsignedShort();
                                        String updateNPCMessage = packet.readRSCString();
                                        if (Settings.dumpMessages) {
                                            thisReplaySqlStatements.add(
                                                ScraperDatabase.newSQLStatement(
                                                    Scraper.m_messageSQL,
                                                    ScraperDatabase.getInsertStatement(
                                                        updateNpcsType1Table,
                                                        new Object[] {
                                                            keyCRC,
                                                            packet.timestamp,
                                                            npcsServer[npcServerIndex].npcId,
                                                            pidTalkingTo,
                                                            updateNPCMessage
                                                        }
                                                    )
                                                )
                                            );
                                        }
                                    } else if (updateType == 2) { // combat update
                                        int npcDamageTaken = packet.readUnsignedByte();
                                        int npcCurrentHP = packet.readUnsignedByte();
                                        int npcMaxHP = packet.readUnsignedByte();
                                        npcsServer[npcServerIndex].healthCurrent = npcCurrentHP;
                                        npcsServer[npcServerIndex].healthMax = npcMaxHP;

                                        if (Settings.dumpNPCDamage) {
                                            // determine how the npc is being attacked
                                            if (npcsServer[npcServerIndex] != null) {

                                                if (npcsServer[npcServerIndex].animationNext >= 8) {
                                                    // in melee combat, but not necessarily have taken melee damage, could be spell
                                                    if (npcsServer[npcServerIndex].incomingProjectileSprite != -1) {
                                                        // TODO: can we assume that this means that this specific damage update was in fact ranged/mage damage?
                                                        npcsServer[npcServerIndex].attackingPlayerServerIndex = -1;
                                                        npcsServer[npcServerIndex].incomingProjectileSprite = -1;
                                                    }

                                                    // determine who is possibly attacking

                                                    // need to check if local player shares same X & Y coordinate as this NPC & has melee stance
                                                    if (playerX == npcsServer[npcServerIndex].currentX && playerY == npcsServer[npcServerIndex].currentY && playerAnim >= 8) {
                                                        // possible & likely that player is the same player attacking NPC, need to check there aren't 2 fights on the same tile
                                                    }
                                                } else {
                                                    // not possible for this to be melee damage :-)
                                                    switch (npcsServer[npcServerIndex].incomingProjectileSprite) {
                                                        case -1:
                                                            Logger.Info("@|white [" + keyCRC + "]|@ incoming projectile sprite not set, but npc was damaged while not in melee combat;; timestamp: " + packet.timestamp);
                                                            break;
                                                        case 1: // magic projectile
                                                            if (npcsServer[npcServerIndex].attackingPlayerServerIndex == localPID) {
                                                                // must determine the spell that was used
                                                                // must determine mage level & magic bonus
                                                            } else {
                                                                // useless, since we don't know what spell was used
                                                            }
                                                            break;
                                                        case 2: // ranged projectile
                                                            if (npcsServer[npcServerIndex].attackingPlayerServerIndex == localPID) {
                                                                // best case scenario. We know who is attacking, that it is definitely ranged, and all stats

                                                                // determine bow wielded; might not exist if spear, dart, throwing knife
                                                                int bow = -1;
                                                                for (int slot = 0; slot < 30; slot++) {
                                                                    if (inventoryItemEquipped[slot] == 1) {
                                                                        switch (inventoryItems[slot]) {
                                                                            case 59:
                                                                            case 60:
                                                                            case 188:
                                                                            case 189:
                                                                            case 648:
                                                                            case 649:
                                                                            case 650:
                                                                            case 651:
                                                                            case 652:
                                                                            case 653:
                                                                            case 654:
                                                                            case 655:
                                                                            case 656:
                                                                            case 657:
                                                                                bow = inventoryItems[slot];
                                                                        }
                                                                    }
                                                                }

                                                                thisReplaySqlStatements.add(
                                                                    ScraperDatabase.newSQLStatement(
                                                                        Scraper.m_damageSQL,
                                                                        ScraperDatabase.getInsertStatement(
                                                                            unambiguousRangedTable,
                                                                            new Object[] {
                                                                                keyCRC,
                                                                                packet.timestamp,
                                                                                npcsServer[npcServerIndex].npcId,
                                                                                npcDamageTaken,
                                                                                npcCurrentHP,
                                                                                npcMaxHP,
                                                                                lastAmmo, // arrow/bolt/knife/dart/spear was determined in inventory updates
                                                                                bow,
                                                                                playerCurStat[Game.STAT_RANGED],
                                                                                0 // not a kill shot
                                                                            }
                                                                        )
                                                                    )
                                                                );

                                                            } else {
                                                                // much less useful, since we don't know player's ranged level.
                                                                // arrow could still be sometimes determined if player doesn't pick it up
                                                                // and then we could maybe see the expected distribution of hits for some unknown level
                                                            }
                                                            break;
                                                        case 3: // gnomeball
                                                            break;
                                                        case 4: // iban blast
                                                            break;
                                                        case 5: // cannonball
                                                            break;
                                                        case 6: // god spell
                                                            break;
                                                        default:
                                                            Logger.Info("@|white [" + keyCRC + "]|@ unknown projectile, shouldn't be possible;; timestamp: " + packet.timestamp);
                                                            break;
                                                    }

                                                    npcsServer[npcServerIndex].attackingPlayerServerIndex = -1;
                                                    npcsServer[npcServerIndex].incomingProjectileSprite = -1;
                                                }
                                            } else {
                                                // some other player possibly shooting an an NPC off-screen that we don't know about?
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case PacketBuilder.OPCODE_SCENERY_HANDLER:
                            if (Settings.dumpScenery) {
                                length = packet.data.length;
                                while (length > 0) {
                                    if (packet.readUnsignedByte() == 255) {
                                        packet.skip(2);
                                        length -= 3;
                                    } else {
                                        packet.skip(-1);
                                        int type = handleSceneryIDConvert(packet.readUnsignedShort());
                                        int x = playerX + packet.readByte();
                                        int y = playerY + packet.readByte();
                                        length -= 4;

                                        if (planeX != Game.WORLD_PLANE_X || planeY != Game.WORLD_PLANE_Y || floorYOffset != Game.WORLD_Y_OFFSET || planeFloor > 3 || planeFloor < 0) {
                                            Logger.Error("@|white [" + keyCRC + "]|@ Invalid region or not logged in; Aborting");
                                            break;
                                        }

                                        if (!validCoordinates(x, y)) {
                                            Logger.Error("@|white [" + keyCRC + "]|@ Invalid coordinates " + x + ", " + y + "; Aborting");
                                            break;
                                        } else if (type != 60000 && !sceneryIDBlacklisted(type, x, y)) {
                                            if (type < 0 || type > 1188) {
                                                Logger.Error("@|white [" + keyCRC + "]|@ Scenery id " + type + " at " + x + ", " + y + " is invalid; Aborting");
                                                break;
                                            }

                                            int key = packCoordinate(x, y);
                                            //System.out.println("x: " + x + ", y: " + y);
                                            if (sceneryLocs.containsKey(key))
                                                type = handleSceneryIDConflict(sceneryLocs.get(key), type);
                                            sceneryLocs.put(key, type);
                                        }
                                    }
                                }
                            }
                            break;
                        case PacketBuilder.OPCODE_BOUNDARY_HANDLER:
                            if (Settings.dumpBoundaries) {
                                length = packet.data.length;
                                while (length > 0) {
                                    if (packet.readUnsignedByte() == 255) {
                                        packet.skip(2);
                                        length -= 3;
                                    } else {
                                        packet.skip(-1);
                                        int type = packet.readUnsignedShort();
                                        int x = playerX + packet.readByte();
                                        int y = playerY + packet.readByte();
                                        byte direction = packet.readByte();
                                        length -= 5;

                                        if (planeX != Game.WORLD_PLANE_X || planeY != Game.WORLD_PLANE_Y || floorYOffset != Game.WORLD_Y_OFFSET || planeFloor > 3 || planeFloor < 0) {
                                            Logger.Error("@|white [" + keyCRC + "]|@ Invalid region or not logged in; Aborting");
                                            break;
                                        }

                                        if (!validCoordinates(x, y)) {
                                            Logger.Error("@|white [" + keyCRC + "]|@ Invalid coordinates " + x + ", " + y + "; Aborting");
                                            break;
                                        } else if (type != 0xFFFF && !boundaryIDBlacklisted(type, x, y)) {
                                            if (type < 0 || type > 213) {
                                                Logger.Error("@|white [" + keyCRC + "]|@ Boundary id " + type + " at " + x + ", " + y + " is invalid; Aborting");
                                                break;
                                            }

                                            int key = packCoordinate(x, y);
                                            int value = handleBoundaryIDConvert(packCoordinate(type, direction));
                                            if (boundaryLocs.containsKey(key))
                                                value = handleBoundaryIDConflict(boundaryLocs.get(key), value);
                                            boundaryLocs.put(key, value);
                                        }
                                    }
                                }
                            } else if (Settings.checkBoundaryRemoval) {
                                while (length > 0) {
                                    if (packet.readUnsignedByte() == 255) {
                                        int x = playerX + packet.readByte();
                                        int y = playerY + packet.readByte();
                                        if (Scraper.worldManager.getViewArea(x, y).getBoundary(x, y) != null) {
                                            Logger.Info("@|white [" + keyCRC + "]|@ " + String.format("@|red BOUNDARY REMOVAL ACTUALLY DID SOMETHING @ %d,%d IN REPLAY %s AT TIMESTAMP %d|@", x, y, fname, packet.timestamp));
                                        }
                                        length -= 3;
                                    } else {
                                        length -= 5;
                                    }
                                }
                            }
                            break;
                        case PacketBuilder.OPCODE_SLEEP_WORD:
                            if (Settings.dumpSleepWords) {
                                interestingSleepPackets.add(packet);
                            }
                            break;
                        case PacketBuilder.OPCODE_WAKE_UP:
                            if (Settings.dumpSleepWords) {
                                interestingSleepPackets.add(packet);
                            }
                            break;
                        case PacketBuilder.OPCODE_CLOSE_CONNECTION_NOTIFY:
                            if (appendingToReplay) {
                                packet.opcode = ReplayEditor.VIRTUAL_OPCODE_NOP;
                            }
                            break;
                        case PacketBuilder.OPCODE_SHOW_SHOP: // 101
                            if (Settings.dumpShops) {



                                // 1. identify the shop
                                int shopIdx = getShopId(lastNPCTalkedTo, playerX, playerY, packet.timestamp, fname);

                                // 2. create shop if does not exist
                                if (shops[shopIdx] == null) {
                                    shops[shopIdx] = new Shop(shopIdx);
                                }

                                int shopItemCount = packet.readUnsignedByte();
                                byte shopType = packet.readByte();
                                int sellGenerosity = packet.readUnsignedByte();
                                int buyGenerosity = packet.readUnsignedByte();
                                int stockSensitivity = packet.readUnsignedByte();
                                for (int itemIdx = 0; itemIdx < shopItemCount; ++itemIdx) {
                                    int itemId = packet.readUnsignedShort();
                                    int amountInStock = packet.readUnsignedShort();
                                    int baseAmountInStock = packet.readUnsignedShort();

                                    // 3. add item to shop
                                    if (shops[shopIdx].items[itemId] == null) {
                                        shops[shopIdx].items[itemId] =
                                            new ShopItem(itemId, amountInStock, baseAmountInStock, packet.timestamp, tickCounter, ShopItem.EVENT_INITIAL);
                                    } else {
                                        if (shops[shopIdx].items[itemId].itemID == itemId) {
                                            int lastStock = shops[shopIdx].items[itemId].stock.get(shops[shopIdx].items[itemId].stock.size() - 1);
                                            int lastEvent = shops[shopIdx].items[itemId].eventType.get(shops[shopIdx].items[itemId].stock.size() - 1);
                                            if (amountInStock != lastStock || (shopWasClosed && lastEvent != ShopItem.EVENT_INITIAL)) {
                                                shops[shopIdx].items[itemId].stock.add(amountInStock);
                                                shops[shopIdx].items[itemId].baseStock.add(baseAmountInStock); // could probably be just an int, but will record
                                                shops[shopIdx].items[itemId].timestamps.add(packet.timestamp);
                                                shops[shopIdx].items[itemId].ticks.add(tickCounter);

                                                // 4. determine why the stock changed
                                                if (shopWasClosed) {
                                                    shops[shopIdx].items[itemId].eventType.add(ShopItem.EVENT_INITIAL);
                                                } else if (packet.timestamp - lastBuyTimestamp < 80 && itemId == lastBuyItem) {
                                                    shops[shopIdx].items[itemId].eventType.add(ShopItem.EVENT_BUY);
                                                    lastBuyTimestamp = -100;
                                                } else if (packet.timestamp - lastSellTimestamp < 80 && itemId == lastSellItem) {
                                                    shops[shopIdx].items[itemId].eventType.add(ShopItem.EVENT_SELL);
                                                    lastSellTimestamp = -100;
                                                } else if (amountInStock - lastStock == 1 && amountInStock <= baseAmountInStock) {
                                                    // could be a restock event, could still be another player
                                                    // for now, will just see how big of a deal this would be to manually disambiguate
                                                    // TODO: need player creation to see if there are any players around.
                                                    shops[shopIdx].items[itemId].eventType.add(ShopItem.EVENT_RESTOCK);
                                                    ++shops[shopIdx].items[itemId].restockCount;
                                                } else if (amountInStock - lastStock == -1 && amountInStock >= baseAmountInStock) {
                                                    // could be a destock event, could still be another player
                                                    // for now, will just see how big of a deal this would be to manually disambiguate
                                                    // TODO: need player creation to see if there are any players around.
                                                    shops[shopIdx].items[itemId].eventType.add(ShopItem.EVENT_DESTOCK);
                                                    ++shops[shopIdx].items[itemId].destockCount;
                                                } else {
                                                    // TODO:
                                                    // Can be discounted if there are not any players nearby.
                                                    // After that, it's a bit hard to detect, b/c e.g., some player could be nearby and sell 1 item to
                                                    // shop every half of a normal cycle, making it appear to restock twice as fast as normal.
                                                    shops[shopIdx].items[itemId].eventType.add(ShopItem.EVENT_OTHERPLAYER);
                                                }
                                            }
                                        } else {
                                            Logger.Error(String.format(
                                                    "itemId didn't match, %d != %d",
                                                    shops[shopIdx].items[itemId].itemID,
                                                    itemId
                                                )
                                            );
                                        }
                                    }

                                    thisReplaySqlStatements.add(
                                        ScraperDatabase.newSQLStatement(
                                            Scraper.m_shopsSQL,
                                            ScraperDatabase.getInsertStatement(
                                                shopsTable,
                                                new Object[] {
                                                    keyCRC,
                                                    packet.timestamp,
                                                    lastNPCTalkedTo,
                                                    playerX,
                                                    playerY,
                                                    shopType,
                                                    sellGenerosity,
                                                    buyGenerosity,
                                                    stockSensitivity,
                                                    itemId,
                                                    amountInStock,
                                                    baseAmountInStock
                                                }
                                            )
                                        )
                                    );
                                }
                                shopWasClosed = false;
                            }
                            break;
                        case PacketBuilder.OPCODE_SET_INVENTORY: // 53
                            if (Settings.dumpInventories || Settings.dumpNPCDamage || Settings.dumpRNGEvents) {
                                int inventoryItemCount = packet.readUnsignedByte();

                                inventoryItemsAllCount = new int[1290];
                                for (int i = 0; i < 1290; i++) {
                                    inventoryItemsAllCount[i] = 0;
                                }

                                for (int slot = 0; slot < inventoryItemCount; slot++) {
                                    int itemIDAndEquipped = packet.readUnsignedShort();
                                    int equipped = ((itemIDAndEquipped >> 15) & 0x1);
                                    int itemID = itemIDAndEquipped & 0x7FFF;
                                    long itemStack = 1;
                                    if (JGameData.itemStackable[itemID]) {
                                        itemStack = packet.readUnsignedInt3();
                                    }
                                    if (Settings.dumpInventories) {
                                        thisReplaySqlStatements.add(
                                            ScraperDatabase.newSQLStatement(
                                                Scraper.m_inventorySQL,
                                                ScraperDatabase.getInsertStatement(
                                                    inventoriesTable,
                                                    new Object[] {
                                                        keyCRC,
                                                        packet.timestamp,
                                                        slot,
                                                        equipped,
                                                        itemID,
                                                        itemStack,
                                                        packet.opcode
                                                    }
                                                )
                                            )
                                        );
                                    }

                                    inventoryItemsAllCount[itemID] += itemStack;

                                    inventoryItems[slot] = itemID;
                                    inventoryStackAmounts[slot] = itemStack;
                                    inventoryItemEquipped[slot] = equipped;
                                }
                            }
                            break;
                        case PacketBuilder.OPCODE_SET_INVENTORY_SLOT: // 90
                            if (packet.data == null) {
                                break;
                            }

                            if (Settings.dumpInventories || Settings.dumpNPCDamage || Settings.dumpRNGEvents) {
                                int slot = packet.readUnsignedByte();
                                int itemIDAndEquipped = packet.readUnsignedShort();
                                int equipped = ((itemIDAndEquipped >> 15) & 0x1);
                                int itemID = itemIDAndEquipped & 0x7FFF;
                                long itemStack = 1;
                                if (JGameData.itemStackable[itemID]) {
                                    itemStack = packet.readUnsignedInt3();
                                }


                                inventoryItems[slot] = itemID;
                                inventoryStackAmounts[slot] = itemStack;
                                inventoryItemEquipped[slot] = equipped;

                                if (Settings.dumpInventories) {
                                    thisReplaySqlStatements.add(
                                        ScraperDatabase.newSQLStatement(
                                            Scraper.m_inventorySQL,
                                            ScraperDatabase.getInsertStatement(
                                                inventoriesTable,
                                                new Object[] {
                                                    keyCRC,
                                                    packet.timestamp,
                                                    slot,
                                                    equipped,
                                                    itemID,
                                                    itemStack,
                                                    packet.opcode
                                                }
                                            )
                                        )
                                    );
                                }

                                if (Settings.dumpNPCDamage) {
                                    if (isAmmo(itemID)) {
                                        lastAmmo = itemID;
                                        lastAmmoTimestamp = packet.timestamp;
                                    }
                                }
                            }
                            break;

                        case PacketBuilder.OPCODE_REMOVE_INVENTORY_SLOT: // 123
                            if (Settings.dumpInventories || Settings.dumpNPCDamage || Settings.dumpRNGEvents) {
                                int slotRemoved = packet.readUnsignedByte();
                                int itemID = inventoryItems[slotRemoved];

                                if (itemID == -1) {
                                    Logger.Warn("Wasn't able to correctly track inventory for replay " + fname + "!!");
                                    break;
                                }

                                inventoryItemsAllCount[itemID] -= inventoryStackAmounts[slotRemoved];
                                for (int slot = slotRemoved; slot < 28; slot++) { // 30-1-1 for indexing & advancing
                                    inventoryItems[slot] = inventoryItems[slot + 1];
                                    inventoryStackAmounts[slot] = inventoryStackAmounts[slot + 1];
                                    inventoryItemEquipped[slot] = inventoryItemEquipped[slot + 1];
                                }

                                if (Settings.dumpNPCDamage) {
                                    if (isAmmo(itemID)) {
                                        lastAmmo = itemID;
                                        lastAmmoTimestamp = packet.timestamp;
                                    }
                                }
                            }

                            break;

                        case PacketBuilder.OPCODE_SET_STATS: // 156
                            for (int stat = 0; stat < 17; stat++) {
                                playerCurStat[stat] = packet.readByte();
                            }
                            for (int stat = 0; stat < 17; stat++) {
                                playerBaseStat[stat] = packet.readByte();
                            }
                            for (int stat = 0; stat < 17; stat++) {
                                playerXP[stat] = packet.readUnsignedInt();
                            }
                            packet.skip(1); // quest points
                            break;

                        case PacketBuilder.OPCODE_UPDATE_STAT: // 159
                            int stat = packet.readByte();
                            playerCurStat[stat] = packet.readByte();
                            playerBaseStat[stat] = packet.readByte();
                            playerXP[stat] = packet.readUnsignedInt();
                            break;

                        case PacketBuilder.OPCODE_PLAY_SOUND: // 204
                            lastSound = packet.readPaddedString();
                            lastSoundTimestamp = packet.timestamp;
                            break;

                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.Error(String.format("@|red Scraper.processReplays incomingPackets loop during replay %s on opcode %d at timestamp %d|@", fname, packet.opcode, packet.timestamp));
                }
            } else {
                // Process outgoing packets
                Logger.Debug("@|white [" + keyCRC + "]|@ " + String.format("outgoing opcode: %d",packet.opcode));
                try {

                    if (packet.opcode == ReplayEditor.VIRTUAL_OPCODE_ERROR) {
                    /* TODO:
                    // Try to determine if there is exactly one opcode that makes sense, given the data & context
                    // Obviously the client could send opcodes that don't make sense, but we will assume a good-faith recorder...
                    // for data where the client is sending nonsense data on purpose (don't think this happened), hopefully they
                    // had a functioning replay with valid out.bin. If they did, they would not reach this block.

                    // first we can filter by packet length. Some packets can't ever be a certain length.
                    int[] possibleOpcodes = ClientOpcodes.getPossibleOpcodesByLength(packet.data.length);

                    // next we can look at packets in the future to see what the server did in response to the data sent.
                    (requires restructure of packets loop)
                    */

                        // For now we can just mark some data invalid.
                        useItemWithSceneryX = -1;
                        useItemWithSceneryY = -1;
                        useItemWithSceneryItemID = -1;
                        interactWithSceneryOption2X = -1;
                        interactWithSceneryOption2Y = -1;
                        interactWithSceneryOption1X = -1;
                        interactWithSceneryOption1Y = -1;
                        lastSceneryInteractX = useItemWithSceneryX;
                        lastSceneryInteractY = useItemWithSceneryY;
                        lastInteractOpcode = -1;
                        lastNPCTalkedTo = -1;
                        lastBuyItem = -1;
                        lastBuyTimestamp = -1;
                        lastBuyAmount = -1;
                        lastSellItem = -1;
                        lastSellTimestamp = -1;
                        lastSellAmount = -1;
                    }

                    switch (packet.opcode) {
                        case ReplayEditor.VIRTUAL_OPCODE_CONNECT: // Login
                            Logger.Info("@|white [" + keyCRC + "]|@ outgoing login (timestamp: " + packet.timestamp + ")");
                            break;

                        case ClientOpcodes.CLIENT_OPCODE_SEND_CHAT_MESSAGE: // 216
                            if (Settings.dumpChat) {
                                String sendChatMessage = packet.readRSCString();
                                thisReplaySqlStatements.add(
                                    ScraperDatabase.newSQLStatement(
                                        Scraper.m_chatSQL,
                                        ScraperDatabase.getInsertStatement(
                                            sendChatTable,
                                            new Object[] {
                                                keyCRC,
                                                packet.timestamp,
                                                localPID,
                                                op216Count++,
                                                sendChatMessage
                                            }
                                        )
                                    )
                                );
                            }
                            if (Settings.sanitizePublicChat)
                                packet.opcode = ReplayEditor.VIRTUAL_OPCODE_NOP;
                            break;

                        case ClientOpcodes.CLIENT_OPCODE_SEND_PM: // 218
                            if (Settings.dumpChat) {
                                packet.readPaddedString(); // recipient
                                String sendPMMessage = packet.readRSCString();
                                thisReplaySqlStatements.add(
                                    ScraperDatabase.newSQLStatement(
                                        Scraper.m_chatSQL,
                                        ScraperDatabase.getInsertStatement(
                                            sendPMTable,
                                            new Object[] {
                                                keyCRC,
                                                packet.timestamp,
                                                sendPMCount++,
                                                sendPMMessage
                                            }
                                        )
                                    )
                                );
                            }
                            if (Settings.sanitizePrivateChat)
                                packet.opcode = ReplayEditor.VIRTUAL_OPCODE_NOP;
                            break;

                        case ClientOpcodes.CLIENT_OPCODE_REMOVE_FRIEND: // 167
                        case ClientOpcodes.CLIENT_OPCODE_ADD_FRIEND: // 195
                        case ClientOpcodes.CLIENT_OPCODE_REMOVE_IGNORED: // 241
                        case ClientOpcodes.CLIENT_OPCODE_ADD_IGNORE: // 132
                            if (Settings.sanitizeFriendsIgnore)
                                packet.opcode = ReplayEditor.VIRTUAL_OPCODE_NOP;
                            break;

                        case ClientOpcodes.CLIENT_OPCODE_SELECT_DIALOGUE_OPTION: // 116
                            if (Settings.dumpMessages) {
                                thisReplaySqlStatements.add(
                                    ScraperDatabase.newSQLStatement(
                                        Scraper.m_messageSQL,
                                        ScraperDatabase.getInsertStatement(
                                            chooseDialogueOptionTable,
                                            new Object[] {
                                                keyCRC,
                                                packet.timestamp,
                                                packet.readUnsignedByte() + 1
                                            }
                                        )
                                    )
                                );
                            }
                            break;
                        case ClientOpcodes.CLIENT_OPCODE_SEND_SLEEPWORD: // 45
                            if (Settings.dumpSleepWords) {
                                interestingSleepPackets.add(packet);
                            }
                            break;

                        case ClientOpcodes.CLIENT_OPCODE_SEND_APPEARANCE: // 235
                            if (Settings.dumpAppearances) {
                                System.out.print("Appearance Packet in " + fname + ": ");
                                try {
                                    for (int i = 0; i < 8; i++) {
                                        System.out.print(String.format("%d ", packet.readUnsignedByte()));
                                    }
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    System.out.print(" XXXXX");
                                }
                                System.out.println();
                            }
                            break;
                        case ClientOpcodes.CLIENT_OPCODE_USE_WITH_SCENERY: // 115
                            if (Settings.dumpRNGEvents || Settings.dumpInteractions) {
                                useItemWithSceneryX = packet.readUnsignedShort();
                                useItemWithSceneryY = packet.readUnsignedShort();
                                lastSceneryInteractX = useItemWithSceneryX;
                                lastSceneryInteractY = useItemWithSceneryY;
                                lastInteractOpcode = packet.opcode;
                                useItemWithSceneryItemID = inventoryItems[packet.readUnsignedShort()];
                            }
                            break;
                        case ClientOpcodes.CLIENT_OPCODE_INTERACT_WITH_SCENERY_OPTION_2: // 79
                            if (Settings.dumpRNGEvents || Settings.dumpInteractions) {
                                interactWithSceneryOption2X = packet.readUnsignedShort();
                                interactWithSceneryOption2Y = packet.readUnsignedShort();
                                lastSceneryInteractX = interactWithSceneryOption2X;
                                lastSceneryInteractY = interactWithSceneryOption2Y;
                                lastInteractOpcode = packet.opcode;
                            }
                            break;
                        case ClientOpcodes.CLIENT_OPCODE_INTERACT_WITH_SCENERY: // 136
                            if (Settings.dumpRNGEvents || Settings.dumpInteractions) {
                                interactWithSceneryOption1X = packet.readUnsignedShort();
                                interactWithSceneryOption1Y = packet.readUnsignedShort();
                                lastSceneryInteractX = interactWithSceneryOption1X;
                                lastSceneryInteractY = interactWithSceneryOption1Y;
                                lastInteractOpcode = packet.opcode;
                            }
                            if (Settings.dumpInteractions) {
                                
                            }
                            break;

                        case ClientOpcodes.CLIENT_OPCODE_TALK_TO_NPC: // 153
                            if (Settings.dumpShops) {
                                int npcServerIndex = packet.readUnsignedShort();
                                lastNPCTalkedTo = npcsServer[npcServerIndex].npcId;
                                shopWasClosed = true;
                            }
                            break;
                        case ClientOpcodes.CLIENT_OPCODE_SELL_TO_SHOP:
                            if (Settings.dumpShops) {
                                lastSellItem = packet.readUnsignedShort();
                                int shopAmount = packet.readUnsignedShort();
                                lastSellAmount = packet.readUnsignedShort();
                                lastSellTimestamp = packet.timestamp;
                            }
                            break;
                        case ClientOpcodes.CLIENT_OPCODE_BUY_FROM_SHOP:
                            if (Settings.dumpShops) {
                                lastBuyItem = packet.readUnsignedShort();
                                int shopAmount = packet.readUnsignedShort();
                                lastBuyAmount = packet.readUnsignedShort();;
                                lastBuyTimestamp = packet.timestamp;
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.Error(String.format("@|red Scraper.processReplays outgoingPackets loop during replay %s on opcode %d at timestamp %d|@", fname, packet.opcode, packet.timestamp));
                }
            }
        }

        if (Settings.dumpScenery) {
            for (HashMap.Entry<Integer, Integer> entry : sceneryLocs.entrySet()) {
                int key = entry.getKey();
                int id = entry.getValue();
                if (Scraper.m_sceneryLocs.containsKey(key)) {
                    int oldID = Scraper.m_sceneryLocs.get(key);
                    if (oldID == SCENERY_BLANK)
                        continue;
                    if (id == SCENERY_BLANK && oldID != SCENERY_BLANK && sceneryIDRemoveList(oldID, getPackedX(key), getPackedY(key))) {
                        Scraper.m_sceneryLocs.put(key, id);
                        continue;
                    }
                    if (id != SCENERY_BLANK) {
                        id = handleSceneryIDConflict(Scraper.m_sceneryLocs.get(key), id);
                        Scraper.m_sceneryLocs.put(key, id);
                    }
                } else {
                    Scraper.m_sceneryLocs.put(key, id);
                }
            }
        }
        if (Settings.dumpBoundaries) {
            for (HashMap.Entry<Integer, Integer> entry : boundaryLocs.entrySet()) {
                int key = entry.getKey();
                int value = entry.getValue();
                if (Scraper.m_boundaryLocs.containsKey(key))
                    value = handleBoundaryIDConflict(Scraper.m_boundaryLocs.get(key), value);
                Scraper.m_boundaryLocs.put(key, value);
            }
        }


        // go through both incoming & outgoing packets in chronological order, for ease of logic
        if (Settings.dumpSleepWords) {
            // TODO: could work this back into the main scraper now that it's been refactored
            // to go through both incoming & outgoing in chronological order too,
            // but also it still works and I'm reasonably sure we will never need to dump sleepwords again anyway.

            int numberOfPackets = interestingSleepPackets.size();
            ReplayPacket[] sleepPackets = new ReplayPacket[numberOfPackets];

            // Populate sleepPackets with sorted packets
            try {
                Collections.sort(interestingSleepPackets, new ReplayPacketComparator());
            } catch (Exception e) {
                e.printStackTrace();
            }
            int arrIndex = -1;
            for (Iterator<ReplayPacket> iterator = interestingSleepPackets.iterator(); iterator.hasNext();) {
                sleepPackets[++arrIndex] = iterator.next();
            }

            for (int cur = 0; cur < numberOfPackets; cur++) {
                Logger.Info("@|white [" + keyCRC + "]|@ " + String.format("Timestamp: %d; Opcode: %d;",
                    sleepPackets[cur].timestamp,
                    sleepPackets[cur].opcode));
                ReplayPacket packet = sleepPackets[cur];

                switch (sleepPackets[cur].opcode) {
                    case PacketBuilder.OPCODE_SLEEP_WORD:
                        String sleepWordGuess = "";
                        boolean guessCorrect = false;
                        boolean loggedOut = false;
                        boolean awokeEarly = false;

                        if (packet.data.length > 0) {

                            // Find event that ends this sleep session.
                            // This is the reason for putting everything interesting
                            // into an array in which we can freely hop between packets btw...
                            int sleepSessionEnd = -1;
                            for (int i = cur + 1; i < numberOfPackets && sleepSessionEnd == -1; ++i) {
                                switch(sleepPackets[i].opcode) {
                                    case ReplayEditor.VIRTUAL_OPCODE_CONNECT:
                                        loggedOut = true; // user disconnected while asleep & reconnects in awake state
                                        break;
                                    case 45: // CLIENT_SEND_SLEEPWORD_GUESS
                                        sleepPackets[i].readByte();
                                        sleepWordGuess = sleepPackets[i].readPaddedString();
                                        Logger.Info("@|white [" + keyCRC + "]|@ Found guess: " + sleepWordGuess);
                                        guessCorrect = true; // not known yet, just setting flag true so it can be set false later
                                        break;
                                    case PacketBuilder.OPCODE_SEND_MESSAGE:
                                        awokeEarly = true;
                                        break;
                                    case PacketBuilder.OPCODE_WAKE_UP:
                                        // unexpectedly woke up, even if there is a good guess
                                        if (i + 1 < numberOfPackets) {
                                            if (sleepPackets[i + 1].opcode == PacketBuilder.OPCODE_SEND_MESSAGE) {
                                                awokeEarly = true;
                                            }
                                        }
                                        sleepSessionEnd = cur;
                                        break;
                                    case PacketBuilder.OPCODE_SLEEP_WORD:
                                        guessCorrect = false;
                                        sleepSessionEnd = cur;
                                        break;
                                }
                            }

                            String sleepWordFilePath = String.format("%ssleepwords", Settings.scraperOutputPath);
                            String sleepWordFileName = "";

                            if (guessCorrect && !awokeEarly && !loggedOut) {
                                sleepWordFileName = String.format("sleep_%s%s_%d.",
                                    sleepWordGuess,
                                    fname.replaceFirst(Settings.sanitizePath, "").replaceAll("/", "_"),
                                    cur);
                            } else if (awokeEarly) {
                                sleepWordFileName = String.format("sleep_!SUDDENLY-AWOKE!%s_%d.",
                                    fname.replaceFirst(Settings.sanitizePath, "").replaceAll("/", "_"),
                                    cur);
                            } else if (!guessCorrect) {
                                sleepWordFileName = String.format("sleep_!INCORRECT!%s_%s_%d.",
                                    sleepWordGuess,
                                    fname.replaceFirst(Settings.sanitizePath, "").replaceAll("/", "_"),
                                    cur);
                            } else {
                                sleepWordFileName = String.format("sleep_!LOGGED-OUT!%s_%d.",
                                    fname.replaceFirst(Settings.sanitizePath, "").replaceAll("/", "_"),
                                    cur);
                            }

                            byte[] data = new byte[packet.data.length];

                            for (int i = 0; i < packet.data.length; i++) {
                                data[i] = packet.data[i];
                                System.out.print(String.format("%x", data[i]));
                            }
                            System.out.println();

                            // convert to BMP (mostly for fun)
                            try {
                                data = convertImage(data);
                                File fileName = new File(new File(sleepWordFilePath, "/images/"), sleepWordFileName + "bmp");
                                try (FileOutputStream fos = new FileOutputStream(fileName)) {
                                    fos.write(saveBitmap(data));
                                }
                            } catch (Exception e) {
                                //never happens btw
                                e.printStackTrace();
                            }

                            // export raw packet data
                            try {
                                File fileName = new File(new File(sleepWordFilePath, "/packetData/"), sleepWordFileName + "bin");
                                try (FileOutputStream fos = new FileOutputStream(fileName)) {
                                    fos.write(packet.data);
                                }
                            } catch (Exception e) {
                                //never happens btw
                                e.printStackTrace();
                            }

                            Logger.Info("@|white [" + keyCRC + "]|@ " + String.format("sleepword %d: %d length: %d", cur, packet.opcode, packet.data.length));

                        } else {
                            Logger.Warn("Zero length packet 117 in " + fname);
                        }
                        break;
                }

            }
        }

        if (Settings.sanitizeReplays) {
            // Set exported replay version
            if (Scraper.sanitizeVersion != -1)
                editor.getReplayVersion().version = Scraper.sanitizeVersion;

            String replayName = fname.substring(Settings.sanitizePath.length());

            String outDir = Settings.sanitizeOutputPath + replayName;
            outDir = new File(outDir).toPath().toAbsolutePath().toString();
            FileUtil.mkdir(outDir);
            if (false) {
                editor.justUpdateMetadata(outDir, Settings.sanitizePath);
            } else {
                editor.exportData(outDir, Settings.sanitizePath);
            }

            // outDir is the folder that everything goes into right now.
            // we would like the base dir, + strippedReplays + pcaps + directory structure + replayName.pcap
            outDir = outDir.replace(Settings.sanitizeBaseOutputPath,Settings.sanitizeBaseOutputPath + "/pcaps");
            FileUtil.mkdir(new File(outDir).getParent());
            editor.exportPCAP(outDir);
        }

        if (Settings.dumpShops) {
            for (Shop shop : shops) {
                if (shop != null) {
                    for (ShopItem item : shop.items) {
                        if (item != null) {
                            // from here there can be 2 methods to get shop restock time
                            // 1. a nice string of incrementing restock events (IDEAL)
                            // 2. a buy event (optional) followed by shop closing, coming back a while later, and stock still below baseStock
                            // Option 1 is only possible if restockCount > 1, but for option 2, we will log all events.
                            int events = item.eventType.size();
                            for (int idx = 0; idx < events; idx++) {
                                thisReplaySqlStatements.add(
                                    ScraperDatabase.newSQLStatement(
                                        Scraper.m_shopEventsSQL,
                                        ScraperDatabase.getInsertStatement(
                                            shopEventsTable,
                                            new Object[]{
                                                keyCRC,
                                                item.timestamps.get(idx),
                                                item.ticks.get(idx),
                                                shop.index,
                                                item.itemID,
                                                item.eventType.get(idx),
                                                item.stock.get(idx),
                                                item.baseStock.get(idx),
                                                item.restockCount,
                                                item.destockCount
                                            }
                                        )
                                    )
                                );
                            }
                        }
                    }
                }
            }
        }

        if (Scraper.validSQLCredentials) {
            int sqlStatementCount = thisReplaySqlStatements.size();
            int connectionNum = (int) Thread.currentThread().getId() % Settings.threads;
            Logger.Info("@|cyan Inserting " + sqlStatementCount + " statements on thread " + connectionNum + " for replay |@@|white [" + keyCRC + "]|@ aka " + fname);
            try {
                ScraperDatabase.batchInsert(thisReplaySqlStatements, connectionNum);
            } catch (Exception e) {
                e.printStackTrace();
                Logger.Error("@|red Discarded up to " + sqlStatementCount + " SQL statements!! :-( ... " + fname + "|@");
            }
            Logger.Info("@|cyan,intensity_bold Finished sanitizing |@@|white [" + keyCRC + "]|@ aka " + fname + " @|cyan Inserted " + sqlStatementCount + " statements on thread " + connectionNum + "|@");
        } else {
            Logger.Info("@|cyan,intensity_bold Finished sanitizing |@@|white [" + keyCRC + "]|@ aka " + fname );
        }

        Scraper.replaysProcessedCount += 1; // TODO: this isn't threadsafe

    }
}
