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

class ScraperDatabaseTable {
    private String name_;
    private String[] rows_;
    private int[] varcharLengths_;

    ScraperDatabaseTable (String name, String[] rows, int[] varcharLengths) {
        this.name_ = name;
        this.rows_ = rows;
        this.varcharLengths_ = varcharLengths;

        if (rows.length != varcharLengths.length) {
            Logger.Error("@|red Error defining table " + name + "; rows length " + rows.length
                + " doesn't equal varcharlengths length " + varcharLengths.length + "!!!|@");
            System.exit(1); // don't even want to continue. this build is broken.
        }

        ScraperDatabaseStructure.createTableJavaCount++;
    }

    String getTableName() {
        return name_;
    }

    String[] getTableRows() {
        return rows_;
    }

    int[] getVarCharLengths() {
        return varcharLengths_;
    }
}
