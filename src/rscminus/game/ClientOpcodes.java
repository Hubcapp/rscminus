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


package rscminus.game;

public class ClientOpcodes {
    // Note: RSC235 opcodes.

    public static final int CLIENT_OPCODE_CONNECT = 0;
    public static final int CLIENT_OPCODE_SEND_DEBUG_INFO = 3; // 204 opcode (and possibly earlier) removed in 233, used to send various debug info to jagex if the client was erroring. unlikely that any information was sent back from server in response.
    public static final int CLIENT_OPCODE_CAST_ON_INVENTORY_ITEM = 4;
    public static final int CLIENT_OPCODE_SEND_DUEL_SETTINGS = 8;
    public static final int CLIENT_OPCODE_INTERACT_WITH_BOUNDARY = 14;
    public static final int CLIENT_OPCODE_WALK_AND_PERFORM_ACTION = 16;
    public static final int CLIENT_OPCODE_BANK_WITHDRAW = 22;
    public static final int CLIENT_OPCODE_BANK_DEPOSIT = 23;
    public static final int CLIENT_OPCODE_SEND_COMBAT_STYLE = 29;
    public static final int CLIENT_OPCODE_CLOSE_CONNECTION_REPLY = 31;
    public static final int CLIENT_OPCODE_SESSION = 32; // 204 opcode (and possibly earlier) removed in 233, used to be able to detect if Login Server was offline
    public static final int CLIENT_OPCODE_SEND_STAKED_ITEMS = 33;
    public static final int CLIENT_OPCODE_SEND_COMMAND_STRING = 38;
    public static final int CLIENT_OPCODE_SEND_SLEEPWORD = 45;
    public static final int CLIENT_OPCODE_OFFER_TRADE_ITEM = 46;
    public static final int CLIENT_OPCODE_CAST_NPC = 50;
    public static final int CLIENT_OPCODE_USE_ON_GROUND_ITEM = 53;
    public static final int CLIENT_OPCODE_SET_TRADE_ACCEPTED_TRUE = 55;
    public static final int CLIENT_OPCODE_ADMIN_TELEPORT_TO_TILE = 59;
    public static final int CLIENT_OPCODE_ENABLE_PRAYER = 60;
    public static final int CLIENT_OPCODE_SEND_PRIVACY_SETTINGS = 64;
    public static final int CLIENT_OPCODE_HEARTBEAT = 67;
    public static final int CLIENT_OPCODE_DUEL_CONFIRM_ACCEPT = 77;
    public static final int CLIENT_OPCODE_INTERACT_WITH_SCENERY_OPTION_2 = 79;
    public static final int CLIENT_OPCODE_SKIP_TUTORIAL = 84;
    public static final int CLIENT_OPCODE_ACTIVATE_INVENTORY_ITEM = 90;
    public static final int CLIENT_OPCODE_COMBINE_INVENTORY_ITEMS = 91;
    public static final int CLIENT_OPCODE_CAST_ON_SCENERY = 99;
    public static final int CLIENT_OPCODE_REQUEST_LOGOUT = 102;
    public static final int CLIENT_OPCODE_SEND_DUEL_REQUEST = 103;
    public static final int CLIENT_OPCODE_CONFIRM_ACCEPT_TRADE = 104;
    public static final int CLIENT_OPCODE_SEND_CLIENT_SETTINGS = 111;
    public static final int CLIENT_OPCODE_USE_WITH_PLAYER = 113;
    public static final int CLIENT_OPCODE_USE_WITH_SCENERY = 115;
    public static final int CLIENT_OPCODE_SELECT_DIALOGUE_OPTION = 116;
    public static final int CLIENT_OPCODE_INTERACT_WITH_BOUNDARY_OPTION_2 = 127;
    public static final int CLIENT_OPCODE_ADD_IGNORE = 132;
    public static final int CLIENT_OPCODE_USE_ON_NPC = 135;
    public static final int CLIENT_OPCODE_INTERACT_WITH_SCENERY = 136;
    public static final int CLIENT_OPCODE_CAST_ON_SELF = 137;
    public static final int CLIENT_OPCODE_AGREE_TO_TRADE = 142;
    public static final int CLIENT_OPCODE_TALK_TO_NPC = 153;
    public static final int CLIENT_OPCODE_CAST_ON_GROUND = 158;
    public static final int CLIENT_OPCODE_USE_WITH_BOUNDARY = 161;
    public static final int CLIENT_OPCODE_KNOWN_PLAYERS = 163; // This opcode was in RSC in all revisions between MC40 (early 2001) and RSC204 (2006). It was removed in RSC233 (2011). The client used to send this opcode as a reply for every PLAYER_COORDS packet with the client's known-players array!
    public static final int CLIENT_OPCODE_FOLLOW_PLAYER = 165;
    public static final int CLIENT_OPCODE_CLOSE_SHOP = 166;
    public static final int CLIENT_OPCODE_REMOVE_FRIEND = 167;
    public static final int CLIENT_OPCODE_EQUIP_ITEM = 169;
    public static final int CLIENT_OPCODE_UNEQUIP_ITEM = 170;
    public static final int CLIENT_OPCODE_ATTACK_PLAYER = 171;
    public static final int CLIENT_OPCODE_ACCEPT_DUEL = 176;
    public static final int CLIENT_OPCODE_CAST_ON_BOUNDARY = 180;
    public static final int CLIENT_OPCODE_WALK = 187;
    public static final int CLIENT_OPCODE_ATTACK_NPC = 190;
    public static final int CLIENT_OPCODE_ADD_FRIEND = 195;
    public static final int CLIENT_OPCODE_DECLINE_DUEL = 197;
    public static final int CLIENT_OPCODE_INTERACT_NPC = 202;
    public static final int CLIENT_OPCODE_SEND_REPORT = 206;
    public static final int CLIENT_OPCODE_BANK_CLOSE = 212;
    public static final int CLIENT_OPCODE_SEND_CHAT_MESSAGE = 216;
    public static final int CLIENT_OPCODE_SEND_PM = 218;
    public static final int CLIENT_OPCODE_SELL_TO_SHOP = 221;
    public static final int CLIENT_OPCODE_CAST_PVP = 229;
    public static final int CLIENT_OPCODE_ABORT_DIALOGUE = 230;
    public static final int CLIENT_OPCODE_SEND_APPEARANCE = 235;
    public static final int CLIENT_OPCODE_BUY_FROM_SHOP = 236;
    public static final int CLIENT_OPCODE_REMOVE_IGNORED = 241;
    public static final int CLIENT_OPCODE_DROP_ITEM = 246;
    public static final int CLIENT_OPCODE_TAKE_GROUND_ITEM = 247;
    public static final int CLIENT_OPCODE_CAST_ON_GROUND_ITEM = 249;
    public static final int CLIENT_OPCODE_DISABLE_PRAYER = 254;


    public static String[] CLIENT_OPCODES = new String[255];
    static {
        CLIENT_OPCODES[0] = "CONNECT";
        // CLIENT_OPCODES[3] = "SEND_DEBUG_INFO"; // 204 opcode (and possibly earlier) removed in 233, used to send various debug info to jagex if the client was erroring. unlikely that any information was sent back from server in response.
        CLIENT_OPCODES[4] = "CAST_ON_INVENTORY_ITEM";
        CLIENT_OPCODES[8] = "SEND_DUEL_SETTINGS";
        CLIENT_OPCODES[14] = "INTERACT_WITH_BOUNDARY";
        CLIENT_OPCODES[16] = "WALK_AND_PERFORM_ACTION";
        CLIENT_OPCODES[22] = "BANK_WITHDRAW";
        CLIENT_OPCODES[23] = "BANK_DEPOSIT";
        CLIENT_OPCODES[29] = "SEND_COMBAT_STYLE";
        CLIENT_OPCODES[31] = "CLOSE_CONNECTION_REPLY";
        // CLIENT_OPCODES[32] = "SESSION"; // 204 opcode (and possibly earlier) removed in 233, used to be able to detect if Login Server was offline
        CLIENT_OPCODES[33] = "SEND_STAKED_ITEMS";
        CLIENT_OPCODES[38] = "SEND_COMMAND_STRING";
        CLIENT_OPCODES[45] = "SEND_SLEEPWORD";
        CLIENT_OPCODES[46] = "OFFER_TRADE_ITEM";
        CLIENT_OPCODES[50] = "CAST_NPC";
        CLIENT_OPCODES[53] = "USE_ON_GROUND_ITEM";
        CLIENT_OPCODES[55] = "SET_TRADE_ACCEPTED_TRUE";
        CLIENT_OPCODES[59] = "ADMIN_TELEPORT_TO_TILE";
        CLIENT_OPCODES[60] = "ENABLE_PRAYER";
        CLIENT_OPCODES[64] = "SEND_PRIVACY_SETTINGS";
        CLIENT_OPCODES[67] = "HEARTBEAT";
        CLIENT_OPCODES[77] = "DUEL_CONFIRM_ACCEPT";
        CLIENT_OPCODES[79] = "INTERACT_WITH_SCENERY_OPTION_2";
        CLIENT_OPCODES[84] = "SKIP_TUTORIAL";
        CLIENT_OPCODES[90] = "ACTIVATE_INVENTORY_ITEM";
        CLIENT_OPCODES[91] = "COMBINE_INVENTORY_ITEMS";
        CLIENT_OPCODES[99] = "CAST_ON_SCENERY";
        CLIENT_OPCODES[102] = "REQUEST_LOGOUT";
        CLIENT_OPCODES[103] = "SEND_DUEL_REQUEST";
        CLIENT_OPCODES[104] = "CONFIRM_ACCEPT_TRADE";
        CLIENT_OPCODES[111] = "SEND_CLIENT_SETTINGS";
        CLIENT_OPCODES[113] = "USE_WITH_PLAYER";
        CLIENT_OPCODES[115] = "USE_WITH_SCENERY";
        CLIENT_OPCODES[116] = "SELECT_DIALOGUE_OPTION";
        CLIENT_OPCODES[127] = "INTERACT_WITH_BOUNDARY_OPTION_2";
        CLIENT_OPCODES[132] = "ADD_IGNORE";
        CLIENT_OPCODES[135] = "USE_ON_NPC";
        CLIENT_OPCODES[136] = "INTERACT_WITH_SCENERY";
        CLIENT_OPCODES[137] = "CAST_ON_SELF";
        CLIENT_OPCODES[142] = "AGREE_TO_TRADE";
        CLIENT_OPCODES[153] = "TALK_TO_NPC";
        CLIENT_OPCODES[158] = "CAST_ON_GROUND";
        CLIENT_OPCODES[161] = "USE_WITH_BOUNDARY";
        // CLIENT_OPCODES[163] = "KNOWN_PLAYERS"; // This opcode was in RSC in all revisions between MC40 (early 2001) and RSC204 (2006). It was removed in RSC233 (2011). The client used to send this opcode as a reply for every PLAYER_COORDS packet with the client's known-players array!
        CLIENT_OPCODES[165] = "FOLLOW_PLAYER";
        CLIENT_OPCODES[166] = "CLOSE_SHOP";
        CLIENT_OPCODES[167] = "REMOVE_FRIEND";
        CLIENT_OPCODES[169] = "EQUIP_ITEM";
        CLIENT_OPCODES[170] = "UNEQUIP_ITEM";
        CLIENT_OPCODES[171] = "ATTACK_PLAYER";
        CLIENT_OPCODES[176] = "ACCEPT_DUEL";
        CLIENT_OPCODES[180] = "CAST_ON_BOUNDARY";
        CLIENT_OPCODES[187] = "WALK";
        CLIENT_OPCODES[190] = "ATTACK_NPC";
        CLIENT_OPCODES[195] = "ADD_FRIEND";
        CLIENT_OPCODES[197] = "DECLINE_DUEL";
        CLIENT_OPCODES[202] = "INTERACT_NPC";
        CLIENT_OPCODES[206] = "SEND_REPORT";
        CLIENT_OPCODES[212] = "BANK_CLOSE";
        CLIENT_OPCODES[216] = "SEND_CHAT_MESSAGE";
        CLIENT_OPCODES[218] = "SEND_PM";
        CLIENT_OPCODES[221] = "SELL_TO_SHOP";
        CLIENT_OPCODES[229] = "CAST_PVP";
        CLIENT_OPCODES[230] = "ABORT_DIALOGUE";
        CLIENT_OPCODES[235] = "SEND_APPEARANCE";
        CLIENT_OPCODES[236] = "BUY_FROM_SHOP";
        CLIENT_OPCODES[241] = "REMOVE_IGNORED";
        CLIENT_OPCODES[246] = "DROP_ITEM";
        CLIENT_OPCODES[247] = "TAKE_GROUND_ITEM";
        CLIENT_OPCODES[249] = "CAST_ON_GROUND_ITEM";
        CLIENT_OPCODES[254] = "DISABLE_PRAYER";
    }

    // a basic check can be done on authentic opcodes against their possible lengths
    public static boolean isPossiblyValid(int opcode, int length, int protocolVer) {
        if (protocolVer < 127 || (protocolVer > 175 && protocolVer != 235)) {
            return true;
        }
        if (protocolVer <= 175) {
            switch (opcode) {
                // CHANGE_RECOVERY_REQUEST
                case 197:
                    return length == 0;
                // CHANGE_DETAILS_REQUEST
                case 247:
                    return length == 0;
                // CHANGE_PASS
                case 25:
                    return length > 0;
                // SET_RECOVERY
                case 208:
                    return length >= 15; // 5 sets of at least 3 for question-answer
                // SET_DETAILS
                case 253:
                    return length >= 8; // 4 sets of at least 2 per each
                // CANCEL_RECOVERY_REQUEST
                case 196:
                    return length == 0;

                // Unknown OPCODE
                default:
                    return false;
            }
        }
        if (protocolVer == 235) {
            switch (opcode) {
                // HEARTBEAT
                case 67:
                    return length == 0;
                // WALK_TO_ENTITY
                case 16:
                    return length >= 4;
                // WALK_TO_POINT
                case 187:
                    return length >= 4;
                // CONFIRM_LOGOUT
                case 31:
                    return length == 0;
                // LOGOUT
                case 102:
                    return length == 0;
                // ADMIN_TELEPORT
                case 59:
                    return length == 4;
                // COMBAT_STYLE_CHANGE
                case 29:
                    return length == 1;
                // QUESTION_DIALOG_ANSWER
                case 116:
                    return length == 1;

                // PLAYER-APPEARANCE_CHANGE
                case 235:
                    return length == 8;
                // SOCIAL_ADD_IGNORE
                case 132:
                    return length >= 3 && length <=22;
                // SOCIAL_ADD_FRIEND
                case 195:
                    return length >= 3 && length <=22;
                // SOCIAL_SEND_PRIVATE_MESSAGE
                case 218:
                    return length >= 6;
                // SOCIAL_REMOVE_FRIEND
                case 167:
                    return length >= 3 && length <=22;
                // SOCIAL_REMOVE_IGNORE
                case 241:
                    return length >= 3 && length <=22;

                // DUEL_FIRST_SETTINGS_CHANGED
                case 8:
                    return length == 4;
                // DUEL_FIRST_ACCEPTED
                case 176:
                    return length == 0;
                // DUEL_DECLINED
                case 197:
                    return length == 0;
                // DUEL_OFFER_ITEM
                case 33:
                    return length >= 1;
                // DUEL_SECOND_ACCEPTED
                case 77:
                    return length == 0;

                // INTERACT_WITH_BOUNDARY
                case 14:
                    return length == 5;
                // INTERACT_WITH_BOUNDARY2
                case 127:
                    return length == 5;
                // CAST_ON_BOUNDARY
                case 180:
                    return length == 7;
                // USE_WITH_BOUNDARY
                case 161:
                    return length == 7;

                // NPC_TALK_TO
                case 153:
                    return length == 2;
                // NPC_COMMAND1
                case 202:
                    return length == 2;
                // NPC_ATTACK1
                case 190:
                    return length == 2;
                // CAST_ON_NPC
                case 50:
                    return length == 4;
                // NPC_USE_ITEM
                case 135:
                    return length == 4;

                // PLAYER_CAST_PVP
                case 229:
                    return length == 4;
                // PLAYER_USE_ITEM
                case 113:
                    return length == 4;
                // PLAYER_ATTACK
                case 171:
                    return length == 2;
                // PLAYER_DUEL
                case 103:
                    return length == 2;
                // PLAYER_INIT_TRADE_REQUEST
                case 142:
                    return length == 2;
                // PLAYER_FOLLOW
                case 165:
                    return length == 2;

                // CAST_ON_GROUND_ITEM
                case 249:
                    return length == 8;
                // GROUND_ITEM_USE_ITEM
                case 53:
                    return length == 8;
                // GROUND_ITEM_TAKE
                case 247:
                    return length == 6;

                // CAST_ON_INVENTORY_ITEM
                case 4:
                    return length == 4;
                // ITEM_USE_ITEM
                case 91:
                    return length == 4;
                // ITEM_UNEQUIP_FROM_INVENTORY
                case 170:
                    return length == 2;
                // ITEM_EQUIP_FROM_INVENTORY
                case 169:
                    return length == 2;
                // ITEM_COMMAND
                case 90:
                    return length == 2;
                // ITEM_DROP
                case 246:
                    return length == 2;

                // CAST_ON_SELF
                case 137:
                    return length == 2;
                // CAST_ON_LAND
                case 158:
                    return length == 6;

                // OBJECT_COMMAND1
                case 136:
                    return length == 4;
                // OBJECT_COMMAND2
                case 79:
                    return length == 4;
                // CAST_ON_SCENERY
                case 99:
                    return length == 6;
                // USE_ITEM_ON_SCENERY
                case 115:
                    return length == 6;

                // SHOP_CLOSE
                case 166:
                    return length == 0;
                // SHOP_BUY
                case 236:
                    return length == 6;
                // SHOP_SELL
                case 221:
                    return length == 6;

                // PLAYER_ACCEPTED_INIT_TRADE_REQUEST
                case 55:
                    return length == 0;
                // PLAYER_DECLINED_TRADE
                case 230:
                    return length == 0;
                // PLAYER_ADDED_ITEMS_TO_TRADE_OFFER
                case 46:
                    return length >= 1;
                // PLAYER_ACCEPTED_TRADE
                case 104:
                    return length == 0;

                // PRAYER_ACTIVATED
                case 60:
                    return length == 1;
                // PRAYER_DEACTIVATED
                case 254:
                    return length == 1;

                // GAME_SETTINGS_CHANGED
                case 111:
                    return length == 2;
                // CHAT_MESSAGE
                case 216:
                    return length >= 2;
                // COMMAND
                case 38:
                    return length >= 3;
                // PRIVACY_SETTINGS_CHANGED
                case 64:
                    return length == 4;
                // REPORT_ABUSE
                case 206:
                    return length >= 5 && length <= 24;
                // BANK_CLOSE
                case 212:
                    return length == 0;
                // BANK_WITHDRAW
                case 22:
                    return length == 10;
                // BANK_DEPOSIT
                case 23:
                    return length == 10;

                // SLEEPWORD_ENTERED
                case 45:
                    return length >= 3;

                // SKIP_TUTORIAL
                case 84:
                    return length == 0;

                // Unknown OPCODE
                default:
                    return false;
            }
        }
        return false;
    }


    public static int[] getPossibleOpcodesByLength (int length) {
        int[] possibleOpcodes;
        if (length == 0) {
            possibleOpcodes = new int[] {
                CLIENT_OPCODE_HEARTBEAT, // exactly 0 bytes long block
                CLIENT_OPCODE_CLOSE_CONNECTION_REPLY,
                CLIENT_OPCODE_REQUEST_LOGOUT,
                CLIENT_OPCODE_ACCEPT_DUEL,
                CLIENT_OPCODE_DECLINE_DUEL,
                CLIENT_OPCODE_DUEL_CONFIRM_ACCEPT,
                CLIENT_OPCODE_CLOSE_SHOP,
                CLIENT_OPCODE_SET_TRADE_ACCEPTED_TRUE,
                CLIENT_OPCODE_CONFIRM_ACCEPT_TRADE,
                CLIENT_OPCODE_BANK_CLOSE,
                CLIENT_OPCODE_SKIP_TUTORIAL,
                CLIENT_OPCODE_ABORT_DIALOGUE
            };
        } else if (length == 1) {
            possibleOpcodes = new int[] {
                CLIENT_OPCODE_SEND_STAKED_ITEMS, // byte must be 0
                CLIENT_OPCODE_OFFER_TRADE_ITEM, // byte must be 0
                CLIENT_OPCODE_SELECT_DIALOGUE_OPTION, // == 1 block
                CLIENT_OPCODE_SEND_COMBAT_STYLE,
                CLIENT_OPCODE_ENABLE_PRAYER,
                CLIENT_OPCODE_DISABLE_PRAYER
            };
        } else if (length == 2) {
            possibleOpcodes = new int[] {
                CLIENT_OPCODE_OFFER_TRADE_ITEM, // TODO: not sure this is possible actually.
                CLIENT_OPCODE_SEND_STAKED_ITEMS, // TODO: not sure this is possible actually.
                CLIENT_OPCODE_SEND_CHAT_MESSAGE, // >= 2 block
                CLIENT_OPCODE_TALK_TO_NPC, // == 2 block
                CLIENT_OPCODE_INTERACT_NPC,
                CLIENT_OPCODE_ATTACK_NPC,
                CLIENT_OPCODE_ATTACK_PLAYER,
                CLIENT_OPCODE_SEND_DUEL_REQUEST,
                CLIENT_OPCODE_AGREE_TO_TRADE,
                CLIENT_OPCODE_FOLLOW_PLAYER,
                CLIENT_OPCODE_UNEQUIP_ITEM,
                CLIENT_OPCODE_EQUIP_ITEM,
                CLIENT_OPCODE_ACTIVATE_INVENTORY_ITEM,
                CLIENT_OPCODE_DROP_ITEM,
                CLIENT_OPCODE_CAST_ON_SELF,
                CLIENT_OPCODE_SEND_CLIENT_SETTINGS
            };
        } else if (length == 3) {
            possibleOpcodes = new int[] {
                CLIENT_OPCODE_OFFER_TRADE_ITEM, // >= 1 block
                CLIENT_OPCODE_SEND_STAKED_ITEMS,
                CLIENT_OPCODE_SEND_CHAT_MESSAGE, // >= 2 block
                CLIENT_OPCODE_SEND_SLEEPWORD, // >= 3 block
                CLIENT_OPCODE_SEND_COMMAND_STRING,
                CLIENT_OPCODE_REMOVE_FRIEND,
                CLIENT_OPCODE_REMOVE_IGNORED,
                CLIENT_OPCODE_ADD_FRIEND,
                CLIENT_OPCODE_REMOVE_FRIEND
                // no client opcode is specifically 3 bytes long.
            };
        } else if (length == 4) {
            possibleOpcodes = new int[] {
                CLIENT_OPCODE_OFFER_TRADE_ITEM, // >= 1 block
                CLIENT_OPCODE_SEND_STAKED_ITEMS,
                CLIENT_OPCODE_SEND_CHAT_MESSAGE, // >= 2 block
                CLIENT_OPCODE_SEND_SLEEPWORD, // >= 3 block
                CLIENT_OPCODE_SEND_COMMAND_STRING,
                CLIENT_OPCODE_REMOVE_FRIEND,
                CLIENT_OPCODE_REMOVE_IGNORED,
                CLIENT_OPCODE_ADD_FRIEND,
                CLIENT_OPCODE_REMOVE_FRIEND,
                CLIENT_OPCODE_WALK_AND_PERFORM_ACTION, // >= 4 block
                CLIENT_OPCODE_WALK,
                CLIENT_OPCODE_ADMIN_TELEPORT_TO_TILE, // == 4 block
                CLIENT_OPCODE_SEND_DUEL_SETTINGS,
                CLIENT_OPCODE_CAST_NPC,
                CLIENT_OPCODE_USE_ON_NPC,
                CLIENT_OPCODE_CAST_PVP,
                CLIENT_OPCODE_USE_WITH_PLAYER,
                CLIENT_OPCODE_CAST_ON_INVENTORY_ITEM,
                CLIENT_OPCODE_COMBINE_INVENTORY_ITEMS,
                CLIENT_OPCODE_INTERACT_WITH_SCENERY,
                CLIENT_OPCODE_INTERACT_WITH_SCENERY_OPTION_2,
                CLIENT_OPCODE_SEND_PRIVACY_SETTINGS
            };
        } else if (length == 5) {
            possibleOpcodes = new int[]{
                CLIENT_OPCODE_OFFER_TRADE_ITEM, // >= 1 block
                CLIENT_OPCODE_SEND_STAKED_ITEMS,
                CLIENT_OPCODE_SEND_CHAT_MESSAGE, // >= 2 block
                CLIENT_OPCODE_SEND_SLEEPWORD, // >= 3 block
                CLIENT_OPCODE_SEND_COMMAND_STRING,
                CLIENT_OPCODE_REMOVE_FRIEND,
                CLIENT_OPCODE_REMOVE_IGNORED,
                CLIENT_OPCODE_ADD_FRIEND,
                CLIENT_OPCODE_REMOVE_FRIEND,
                CLIENT_OPCODE_WALK_AND_PERFORM_ACTION, // >= 4 block
                CLIENT_OPCODE_WALK,
                CLIENT_OPCODE_SEND_REPORT, // >= 5 block
                CLIENT_OPCODE_INTERACT_WITH_BOUNDARY, // == 5 block
                CLIENT_OPCODE_INTERACT_WITH_BOUNDARY_OPTION_2
            };
        } else if (length == 6) {
            possibleOpcodes = new int[]{
                CLIENT_OPCODE_OFFER_TRADE_ITEM, // >= 1 block
                CLIENT_OPCODE_SEND_STAKED_ITEMS,
                CLIENT_OPCODE_SEND_CHAT_MESSAGE, // >= 2 block
                CLIENT_OPCODE_SEND_SLEEPWORD, // >= 3 block
                CLIENT_OPCODE_SEND_COMMAND_STRING,
                CLIENT_OPCODE_REMOVE_FRIEND,
                CLIENT_OPCODE_REMOVE_IGNORED,
                CLIENT_OPCODE_ADD_FRIEND,
                CLIENT_OPCODE_REMOVE_FRIEND,
                CLIENT_OPCODE_WALK_AND_PERFORM_ACTION, // >= 4 block
                CLIENT_OPCODE_WALK,
                CLIENT_OPCODE_SEND_REPORT, // >= 5 block
                CLIENT_OPCODE_SEND_PM, // >= 6 block
                CLIENT_OPCODE_TAKE_GROUND_ITEM, // == 6 block
                CLIENT_OPCODE_CAST_ON_GROUND,
                CLIENT_OPCODE_CAST_ON_SCENERY,
                CLIENT_OPCODE_USE_WITH_SCENERY,
                CLIENT_OPCODE_BUY_FROM_SHOP,
                CLIENT_OPCODE_SELL_TO_SHOP
            };
        } else if (length == 7) {
            possibleOpcodes = new int[]{
                CLIENT_OPCODE_OFFER_TRADE_ITEM, // >= 1 block
                CLIENT_OPCODE_SEND_STAKED_ITEMS,
                CLIENT_OPCODE_SEND_CHAT_MESSAGE, // >= 2 block
                CLIENT_OPCODE_SEND_SLEEPWORD, // >= 3 block
                CLIENT_OPCODE_SEND_COMMAND_STRING,
                CLIENT_OPCODE_REMOVE_FRIEND,
                CLIENT_OPCODE_REMOVE_IGNORED,
                CLIENT_OPCODE_ADD_FRIEND,
                CLIENT_OPCODE_REMOVE_FRIEND,
                CLIENT_OPCODE_WALK_AND_PERFORM_ACTION, // >= 4 block
                CLIENT_OPCODE_WALK,
                CLIENT_OPCODE_SEND_REPORT, // >= 5 block
                CLIENT_OPCODE_SEND_PM, // >= 6 block
                CLIENT_OPCODE_CAST_ON_BOUNDARY, // == 7 block
                CLIENT_OPCODE_USE_WITH_BOUNDARY
            };
        } else if (length == 8) {
            possibleOpcodes = new int[]{
                CLIENT_OPCODE_OFFER_TRADE_ITEM, // >= 1 block
                CLIENT_OPCODE_SEND_STAKED_ITEMS,
                CLIENT_OPCODE_SEND_CHAT_MESSAGE, // >= 2 block
                CLIENT_OPCODE_SEND_SLEEPWORD, // >= 3 block
                CLIENT_OPCODE_SEND_COMMAND_STRING,
                CLIENT_OPCODE_REMOVE_FRIEND,
                CLIENT_OPCODE_REMOVE_IGNORED,
                CLIENT_OPCODE_ADD_FRIEND,
                CLIENT_OPCODE_REMOVE_FRIEND,
                CLIENT_OPCODE_WALK_AND_PERFORM_ACTION, // >= 4 block
                CLIENT_OPCODE_WALK,
                CLIENT_OPCODE_SEND_REPORT, // >= 5 block
                CLIENT_OPCODE_SEND_PM, // >= 6 block
                CLIENT_OPCODE_SEND_APPEARANCE, // == 8 block
                CLIENT_OPCODE_CAST_ON_GROUND_ITEM,
                CLIENT_OPCODE_USE_ON_GROUND_ITEM
            };
        } else if (length == 10) {
            possibleOpcodes = new int[]{
                CLIENT_OPCODE_OFFER_TRADE_ITEM, // >= 1 block
                CLIENT_OPCODE_SEND_STAKED_ITEMS,
                CLIENT_OPCODE_SEND_CHAT_MESSAGE, // >= 2 block
                CLIENT_OPCODE_SEND_SLEEPWORD, // >= 3 block
                CLIENT_OPCODE_SEND_COMMAND_STRING,
                CLIENT_OPCODE_REMOVE_FRIEND,
                CLIENT_OPCODE_REMOVE_IGNORED,
                CLIENT_OPCODE_ADD_FRIEND,
                CLIENT_OPCODE_REMOVE_FRIEND,
                CLIENT_OPCODE_WALK_AND_PERFORM_ACTION, // >= 4 block
                CLIENT_OPCODE_WALK,
                CLIENT_OPCODE_SEND_REPORT, // >= 5 block
                CLIENT_OPCODE_SEND_PM, // >= 6 block
                CLIENT_OPCODE_BANK_WITHDRAW, // == 10 block
                CLIENT_OPCODE_BANK_DEPOSIT
            };
        } else {
            // opcode is not of a length that any constant-length opcode has.
            possibleOpcodes = new int[]{
                CLIENT_OPCODE_OFFER_TRADE_ITEM, // >= 1 block
                CLIENT_OPCODE_SEND_STAKED_ITEMS,
                CLIENT_OPCODE_SEND_CHAT_MESSAGE, // >= 2 block
                CLIENT_OPCODE_SEND_SLEEPWORD, // >= 3 block
                CLIENT_OPCODE_SEND_COMMAND_STRING,
                CLIENT_OPCODE_REMOVE_FRIEND, // <= 22
                CLIENT_OPCODE_REMOVE_IGNORED, // <= 22
                CLIENT_OPCODE_ADD_FRIEND, // <= 22
                CLIENT_OPCODE_REMOVE_FRIEND, // <= 22
                CLIENT_OPCODE_WALK_AND_PERFORM_ACTION, // >= 4 block
                CLIENT_OPCODE_WALK,
                CLIENT_OPCODE_SEND_REPORT, // >= 5  && <= 24
                CLIENT_OPCODE_SEND_PM // >= 6
            };
        }
        return possibleOpcodes;
    }
}
