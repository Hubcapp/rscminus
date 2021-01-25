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

import static rscminus.scraper.ScraperDatabaseStructure.cookingTable;
import static rscminus.scraper.ScraperDatabaseStructure.fishingTable;
import static rscminus.scraper.ScraperDatabaseStructure.woodcutTable;

public class RNGEventIdentifiers {
    private final static int NOMATCH = -1;
    private final static int ATTEMPT = 0;
    private final static int SUCCESS = 1;
    private final static int FAILURE = 2;
    private final static int INVALID = 3;

    //-----------------------------------------------------------
    //-----------------------------------------------------------
    // (ATTEMPT :: SUCCESS :: FAILURE) MESSAGES
    //-----------------------------------------------------------
    //-----------------------------------------------------------

    // Cooking
    //-----------------------------------------------------------

    private String[] stoves = { "Fire", "Range", "Cook's Range", "Tutorial Island Range", "Unknown" };
    private int lastStove = 4; // indexes stoves[]
    private int lastFoodID = -1; // item ID of food used on cooking implement

    int matchesCooking(String message) {
        String cookingAttemptMessage = "You cook the"; // 4864 times
        String cookingSuccessMessage = "now nicely cooked";
        String cookingFailureMessage = "You accidentally burn the";
        String cookingAttemptInvalidMessage = "you burn the meat"; // purposely using cooked meat on cooking source, 2 times

        if (message.endsWith(cookingSuccessMessage) || message.equals("You cook the Ugthanki meat perfectly.")) {
            return SUCCESS;
        }
        if (message.startsWith(cookingAttemptMessage)) {
            return ATTEMPT;
        }
        if (message.startsWith(cookingFailureMessage)) {
            return FAILURE;
        }
        if (message.equals(cookingAttemptInvalidMessage)) {
            return INVALID;
        }
        return NOMATCH;
    }

    private int identifyStove(int sceneryX, int sceneryY, String message) {
        // Mostly we can just look at the message to tell if it's a fire or stove, but must also handle special stoves

        if (message.contains("fire")) {
            return 0; // fire, could also be fireplace which has the same logic based on mod tweets
        }

        if (message.contains("stove") || message.contains("oven") || message.contains("range")) {
            if (sceneryX == 131 && sceneryY == 660) {
                return 2; // Special Lumbridge Range
            }
            if (sceneryX == 216 && sceneryY == 731) {
                return 3; // Tutorial Island Range
            }
            return 1; // regular stove
        }

        return 4; // unknown
    }

    private int fallbackToStringParseFoodID(String message) {
        // this function is necessary if out.bin is corrupt.

        if (message.contains("lobster")) {
            return 372;
        }
        if (message.contains("salmon")) {
            return 356;
        }
        if (message.contains("trout")) {
            return 358;
        }
        if (message.contains("tuna")) {
            return 366;
        }
        if (message.contains("shrimp")) {
            return 349;
        }
        if (message.contains("meat")) {
            if (lastStove != 4) {
                return 133;
            } else {
                return 1280;
            }
        }
        return -1;
    }

    Object[] identifyFood(int cookingStage, String message, int keyCRC, int timestamp, int cookingLevel, int foodItemID, int sceneryX, int sceneryY) {
        if (cookingStage == NOMATCH) {
            return new Object[] { false, "" };
        }
        switch(cookingStage) {
            case ATTEMPT:
                lastStove = identifyStove(sceneryX, sceneryY, message);
                lastFoodID = foodItemID;
                return new Object[] { true, addCookingSQL(keyCRC, timestamp, message, cookingStage, stoves[lastStove], lastFoodID, cookingLevel) };
            case SUCCESS:
            case FAILURE:
            case INVALID:
                if (lastFoodID == -1) {
                    lastFoodID = fallbackToStringParseFoodID(message);
                }

                Object[] result = new Object[] { true, addCookingSQL(keyCRC, timestamp, message, cookingStage, stoves[lastStove], lastFoodID, cookingLevel) };
                lastFoodID = -1;
                lastStove = 4;
                return result;
            default:
                lastFoodID = -1;
                lastStove = 4;
                Logger.Error("programmer error while identifying food, stage " + cookingStage);
                return new Object[] { false, "" };
        }
    }

    private String addCookingSQL(int keyCRC, int timestamp, String message, int stage, String stove, int itemIDCooked, int cookingLevel) {
        return ScraperDatabase.newSQLStatement(
            Scraper.m_cookingSQL,
            ScraperDatabase.getInsertStatement(
                cookingTable,
                new Object[] {
                    keyCRC,
                    timestamp,
                    message,
                    stage,
                    stove,
                    itemIDCooked,
                    cookingLevel
                }
            )
        );
    }

    // Woodcutting
    //-----------------------------------------------------------

    // want message, keyCRC, timestamp, want woodcut level, axe type, tree type

    private String lastAxe = "";
    private int lastWood = -1;

    int matchesWoodcut(String message) {
        String woodcutAttemptMessage = "axe at the tree..."; // 8005 times
        String woodcutSuccessMessage = "You get some wood";
        String woodcutFailureMessage = "You slip and fail to hit the tree";

        if (message.endsWith(woodcutAttemptMessage)) {
            int prefixLen = "You swing your ".length();
            lastAxe = message.substring(prefixLen, message.indexOf(" ", prefixLen));
            return ATTEMPT;
        }
        if (message.equals(woodcutSuccessMessage)) {
            return SUCCESS;
        }
        if (message.equals(woodcutFailureMessage)) {
            return FAILURE;
        }
        return NOMATCH;
    }

    Object[] identifyWood(int woodcuttingStage, String message, int keyCRC, int timestamp, int woodcuttingLevel, int treeType, int lastInteractOpcode) {
        if (woodcuttingStage == NOMATCH) {
            return new Object[] { false, "" };
        }
        switch(woodcuttingStage) {
            case ATTEMPT:
                lastAxe = message.substring(15, message.indexOf(" ", 19));
                lastWood = treeType;
                return new Object[] { true, addWoodcutSQL(keyCRC, timestamp, message, woodcuttingStage, woodcuttingLevel, lastInteractOpcode) };
            case SUCCESS:
            case FAILURE:
                return new Object[] { true, addWoodcutSQL(keyCRC, timestamp, message, woodcuttingStage, woodcuttingLevel, lastInteractOpcode) };
            default:
                Logger.Error("programmer error while identifying wood, stage " + woodcuttingStage);
                return new Object[] { false, "" };
        }
    }

    private String addWoodcutSQL(int keyCRC, int timestamp, String message, int stage, int woodcuttingLevel, int lastInteractOpcode) {
        return ScraperDatabase.newSQLStatement(
            Scraper.m_woodcutSQL,
            ScraperDatabase.getInsertStatement(
                woodcutTable,
                new Object[] {
                    keyCRC,
                    timestamp,
                    message,
                    stage,
                    lastAxe,
                    lastWood,
                    woodcuttingLevel,
                    lastInteractOpcode
                }
            )
        );
    }

    // Fishing
    //-----------------------------------------------------------

    // want message, keyCRC, timestamp, want fishing level, fishing spot type
    int matchesFish(String message, int messageType) {
        String fishingAttemptMessage = "You attempt to catch"; // 13,319 times
        String fishingSuccessMessage = "You catch"; // must also check that messageType == 3, else get gnomeball & scorpion
        String fishingFailureMessage = "You fail to catch anything";

        if (message.startsWith(fishingAttemptMessage)) {
            return ATTEMPT;
        }
        if (message.startsWith(fishingSuccessMessage) && messageType == 3) {
            return SUCCESS;
        }
        if (message.equals(fishingFailureMessage)) {
            return FAILURE;
        }
        return NOMATCH;
    }

    Object[] identifyFish(int fishingStage, String message, int keyCRC, int timestamp, int fishingLevel, int fishingSpotType, int lastInteractOpcode) {
        if (fishingStage == NOMATCH) {
            return new Object[] { false, "" };
        }
        switch(fishingStage) {
            case ATTEMPT:
                return new Object[] { true, addFishSQL(keyCRC, timestamp, message, fishingStage, fishingLevel, fishingSpotType, lastInteractOpcode) };
            case SUCCESS:
            case FAILURE:
                return new Object[] { true, addFishSQL(keyCRC, timestamp, message, fishingStage, fishingLevel, fishingSpotType, lastInteractOpcode) };
            default:
                Logger.Error("programmer error while identifying fish, stage " + fishingStage);
                return new Object[] { false, "" };
        }
    }

    private String addFishSQL(int keyCRC, int timestamp, String message, int stage, int fishingLevel, int fishingSpotType, int lastInteractOpcode) {
        return ScraperDatabase.newSQLStatement(
            Scraper.m_fishingSQL,
            ScraperDatabase.getInsertStatement(
                fishingTable,
                new Object[] {
                    keyCRC,
                    timestamp,
                    message,
                    stage,
                    fishingSpotType,
                    fishingLevel,
                    lastInteractOpcode
                }
            )
        );
    }

    // Mining
    //-----------------------------------------------------------

    // want message, keyCRC, timestamp, want mining level, probably need X & Y of rock attempted on or else would need more complicated message parsing logic
    public static String miningAttemptMessage = "You swing your pick at the rock..."; // 19,493 times
    public static String miningSuccessMessage = "You manage to obtain some";
    public static String[] miningFailureMessages = { "You only succeed in scratching the rock", "You slip and fail to hit the rock", "You fail to make any real impact on the rock" }; // last is for tutorial island only

    // Firemaking
    //-----------------------------------------------------------

    //want message, keyCRC, timestamp, want firemaking level, success
    public static String firemakingAttemptMessage = "You attempt to light the logs"; // 619 times
    public static String firemakingSuccessMessage = "The fire catches and the logs begin to burn"; // 572 times
    public static String firemakingFailureMessage = "You fail to light a fire"; // 47 times

    // Pickpocketing
    //-----------------------------------------------------------

    // want message, keyCRC, timestamp, want thieving level, type of monster, success
    public static String[] pickpocketAttemptNecessarySubstrings = { "You attempt to pick", "'s pocket" }; //59,588 times, message must contain both
    public static String[] pickpocketSuccessNecessarySubstrings = { "You pick the", "'s pocket" }; // message must contain both
    public static String[] pickpocketFailureNecessarySubstrings = { "You fail to pick", "'s pocket" }; // message must contain both
    // TODO: "You attempt to steal" from stalls
    // TODO: Lock picking

    // Crafting
    //-----------------------------------------------------------

    // want message, keyCRC, timestamp, want crafting level, type of gem, success
    public static String[] semiPreciousGemCutMessages = { "You cut the Opal", "You cut the Jade", "You cut the Red Topaz" };
    public static String semiPreciousGemSmashMessage = "You miss hit the chisel and smash the ";

    // Gnomeball
    //-----------------------------------------------------------

    // want message, keyCRC, timestamp,  X coordinate, Y coordinate. Note that the goal is @ (729, 450)
    public static String gnomeballShootAttemptMessage = "you throw the ball at the goal"; // 153 times
    public static String[] gnomeballShootResultMessages = { "it flys through the net...", "the ball just misses the net", "the ball flys way over the net",  "you miss by a mile!" }; // 66, 16, 14, 57 times
    // "you need to be nearer the goal!" happens when not even close, e.g. while standing just outside the gate, but "you throw the ball at the goal" doesn't appear. // 7 times

    //-----------------------------------------------------------
    //-----------------------------------------------------------
    //-----------------------------------------------------------

    // RNG Events that we won't bother specifically scraping
    // Either because they are not suspected to be dependent on any condition other than RNG,
    // Or because the sample size is too low in known replays.

    // only did it enough times to get the messages. Nice of Jagex to reveal that it does depend on woodcutting skill somehow
    public static String waterskinAttemptMessage = "You use your woodcutting skill to extract some water from the cactus."; // 6 times
    public static String waterskinSuccessMessage = "You collect some precious water in your waterskin."; // 4 times
    public static String waterskinFailureMessage = "You make a mistake and fail to fill your waterskin."; // 1 time
    public static String waterskinAttemptInvalidMessage = "You need to have a non-full waterskin to contain the fluid."; // 1 time

    // this is probably not dependant on any level
    // looks about 50/50, 50.9% actual odds in replays, could be 130/256, but more likely 50/50. not high enough sample size to say
    public static String gnomeTackleAttemptMessage = "you attempt to tackle the gnome"; // 338 times
    public static String gnomeTackleSuccessMessage = "and push the gnome to the floor"; // 172 times
    public static String gnomeTackleFailureMessage = "You're pushed away by the gnome"; // 166 times

    // this is probably not dependent on any level, looks like a 2 in 3 chance of pushing them away
    public static String gnomeTacklesYouAttemptMessage = "the gnome trys to tackle you"; // 525 times
    public static String gnomeTacklesYouSuccessMessage = "You manage to push him away"; // 350 times
    public static String gnomeTacklesYouFailureMessage = "and pushes you to the floor"; // 175 times

    // low sample size, but pretty commonly known to be 50/50
    public static String ironOreSmeltAttemptMessage = "You smelt the iron in the furnace"; // only 20 times, not significant sample size, steel message is different
    public static String ironOreSmeltSuccessMessage = "You retrieve a bar of iron"; // 7 times
    public static String ironOreSmeltFailureMessage = "The ore is too impure and you fail to refine it"; // 13 times

    // doesn't add up to 100%, probably someone logged out while cutting a web. (might have been me in temple of ikov, with lag issues)
    // wiki says it's 50/50, data here is 48.25 / 51.75. Could be 125/256, but not really high enough sample size to say
    public static String webAttemptMessage = "You try to destroy the web..."; // 774 times
    public static String webSuccessMessage = "You slice through the web"; // 373 times
    public static String webFailureMessage = "You fail to cut through it"; // 400 times

    // Could possibly be dependent on woodcutting level?
    // ignoring that possibility (not enough variance in wc levels or sufficient sample size to say),
    // it's about a 155/256 chance of succeeding, 60.68% chance
    public static String vineAttemptMessage = "You swing your machette at the jungle vines..."; // 646 times
    public static String vineSuccessMessage = "You hack your way through the jungle."; // 392 times
    public static String vineFailureMessage = "You slip and fail to hit the jungle vines"; // 254 times

    // probably is dependent on mining level somehow,
    // but sample here indicates roughly 1/3rd chance of failing one time before succeeding.
    public static String legendsBoulderAttemptMessage = "You take a good swing at the rock with your pick..."; // 173 times
    public static String legendsBoulderSuccessMessage = "...and smash it into smaller pieces."; // 173 times
    public static String legendsBoulderFailureMessage = "You fail to make a mark on the rocks."; // 58 times

    // Golden bowl is actually really weird and seems to sometimes send both the failure & success messages for a successful bowl.
    // They are probably jumping to the wrong runescript label & intended to remove 2 bars of gold, but actually jumped to success.
    // This was fixed in RS2.
    // Mod Ash says that on OSRS, it is a 135 / 256 chance of pouring molten gold on the ground if "You make a mistake forging the bowl" appears
    // I also tried making a bunch of gold bowls on OSRS with 83 smithing & it's actually really hard to fail at all,
    // so the success rate is most likely based on smithing level...
    public static String goldenBowlAttemptMessage = "You hammer the metal..."; // 19 times
    public static String goldenBowlSuccessMessage = "You forge a beautiful bowl made out of solid gold."; // 14 times
    public static String goldenBowlFailureMessage = "You make a mistake forging the bowl.."; // 9 times
    public static String goldenBowlRealFailureMessage = "You pour molten gold all over the floor.."; // 3 times

    // 271 Opals, 94 Jades, 62 Topaz. The rocks also have regular gems, but would need specific scraper for that.
    // compared to OSRS wiki rates, seems like Opal is less likely & Jade is more likely.
    public static String semiPreciousGemObtainMessage = "You just mined a"; // "n Opal"; " piece of Jade"; " Red Topaz!"

    //-----------------------------------------------------------
}
