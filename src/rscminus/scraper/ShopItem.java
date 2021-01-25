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

import java.util.ArrayList;

public class ShopItem {

    int itemID;
    int restockCount = 0;
    int destockCount = 0;

    ArrayList<Integer> stock = new ArrayList<>();
    ArrayList<Integer> baseStock = new ArrayList<>();
    ArrayList<Integer> timestamps = new ArrayList<>();
    ArrayList<Integer> ticks = new ArrayList<>();
    ArrayList<Integer> eventType = new ArrayList<>();

    static final int EVENT_INITIAL = 0;
    static final int EVENT_RESTOCK = 1;
    static final int EVENT_DESTOCK = 2;
    static final int EVENT_BUY = 3;
    static final int EVENT_SELL = 4;
    static final int EVENT_OTHERPLAYER = 5;
    static final int EVENT_UNKNOWN = 6;

    public ShopItem (int itemID, int stock, int baseStock, int timestamp, int ticks, int eventType) {
        this.itemID = itemID;
        this.stock.add(stock);
        this.baseStock.add(baseStock);
        this.timestamps.add(timestamp);
        this.ticks.add(ticks);
        this.eventType.add(eventType);
    }


}
