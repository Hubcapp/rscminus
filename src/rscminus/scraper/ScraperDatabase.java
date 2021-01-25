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

import com.mchange.v2.c3p0.PooledDataSource;
import rscminus.common.Logger;
import rscminus.common.Settings;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public class ScraperDatabase {

    static String sqlIP = "localhost";
    static String sqlDatabaseName = "rscReplayData";
    static String sqlPort = "";
    static String sqlUsername = "";
    static String sqlPassword = "";

    private static ScraperDatabasePooledDataSource[] pooledDataSources;

    public static boolean init() throws Exception {
        pooledDataSources = new ScraperDatabasePooledDataSource[Settings.threads + 1];
        boolean connectionsSetUpOK = true;
        int datasource = 0;
        for (; datasource < Settings.threads + 1; datasource++) {
            pooledDataSources[datasource] = new ScraperDatabasePooledDataSource();
            if (!pooledDataSources[datasource].init()) {
                connectionsSetUpOK = false;
            }
        }
        if (!connectionsSetUpOK) {
            Logger.Warn("Could not establish Scraper Database connection " + datasource +". " + debugCredentialsString());
            if (!sqlIP.equals("localhost") && !sqlIP.equals("127.0.0.1") && !sqlIP.equals("::1")) {
                Logger.Warn("Try using an SSH tunnel with port forwarding instead of directly specifying hostname, to get around firewalling issues.");
            }
            return false;
        }

        Logger.Info("Establishing Scraper Database connection, " + debugCredentialsString());
        createDatabaseIfNotExists();
        ScraperDatabaseStructure.createTablesIfNotExists();
        return true;
    }
    private static String debugCredentialsString() {
        return  "IP: " + sqlIP
            + "; Port: " + sqlPort
            + "; Username: " + sqlUsername
            + "; Password Set?: " + (sqlPassword.length() > 0 ? "Yes" : "No")
            + "; Database Name: " + sqlDatabaseName
            ;
    }

    static void createDatabaseIfNotExists() {
        try {
            updateStructure("CREATE DATABASE IF NOT EXISTS `" + sqlDatabaseName + "`;\n");
        } catch (Exception e) {
            Logger.Error("Error creating database " + sqlDatabaseName);
            e.printStackTrace();
        }
    }

    static void createTableIfNotExists(ScraperDatabaseTable table) {
        StringBuilder s = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + sqlDatabaseName + "`.`"
            + table.getTableName() + "` (\n" + "  `index` int(11) NOT NULL AUTO_INCREMENT,\n");

        for (int i = 0; i < table.getTableRows().length; i++) {
            if (table.getVarCharLengths()[i] > 0) {
                // varchar type of length type[i]
                s.append("`").append(table.getTableRows()[i]).append("` varchar(").append(table.getVarCharLengths()[i]).append(") COLLATE utf8_bin NOT NULL DEFAULT '',\n");
            } else {
                // int
                s.append("`").append(table.getTableRows()[i]).append("` int(11) NOT NULL DEFAULT '-1',\n");
            }
        }

        s.append("  PRIMARY KEY (`index`)\n" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;\n");

        try {
            updateStructure(s.toString());
        } catch (Exception e) {
            Logger.Error("Error creating table " + table.getTableName());
            e.printStackTrace();
            return;
        }

        int rowsInTable = countRowsInTable(table);
        if (rowsInTable > 0) {
            Logger.Info("Deleting " + rowsInTable + " rows from table " + table.getTableName());
            emptyTable(table); // TODO: remove and put in proper spot, in command line parser
        }

        ScraperDatabaseStructure.createTableSQLCount++;
    }
    static void emptyTable(ScraperDatabaseTable table) {
        try {
            // TODO: check if rows will be deleted & make user run query with "truncate allowed" flag set
            updateStructure("TRUNCATE `" + sqlDatabaseName + "`.`" + table.getTableName() + "`");
        } catch (Exception e) {
            Logger.Error("Error creating table " + table.getTableName());
            e.printStackTrace();
        }
    }

    static int updateStructure(String sqlStatement) throws SQLException {
        PreparedStatement statement = null;
        Connection connection = null;
        try {
            DataSource ds = pooledDataSources[Settings.threads].getDataSource();
            connection = ds.getConnection();
            statement = connection.prepareStatement(sqlStatement);
            return statement.executeUpdate(sqlStatement);
        } finally {
            if (statement != null) try { statement.close(); } catch (SQLException ignore) {}
            if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
        }
    }

    public static void batchInsert(List<String> sqlStatements, int thread) throws SQLException {
        Statement statement = null;
        Connection connection = null;
        try {
            DataSource ds = pooledDataSources[thread].getDataSource();
            connection = ds.getConnection();
            statement = connection.createStatement();
            for (String sql : sqlStatements) {
                statement.addBatch(sql);
            }
            statement.executeBatch();

        } finally {
            if (statement != null) try { statement.close(); } catch (SQLException ignore) {}
            if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
        }
    }

    static String getInsertStatement(ScraperDatabaseTable table, Object[] values) {
        if (table.getTableRows().length != values.length) {
            Logger.Error("@|red bad \"values\" length for table " + table.getTableName() + "!!!|@");
            System.exit(1); // bad build, don't even want to continue.
        }

        StringBuilder insert = new StringBuilder("INSERT INTO `" + sqlDatabaseName + "`.`" + table.getTableName() + "` (`");
        for (int i = 0; i < table.getTableRows().length; i++) {
            if (i != table.getTableRows().length - 1) {
                insert.append(table.getTableRows()[i]).append("`, `");
            } else {
                insert.append(table.getTableRows()[i]).append("`) VALUES ('");
            }
        }
        for (int i = 0; i < values.length; i++) {
            if (table.getVarCharLengths()[i] > 0) {
                insert.append(((String)values[i]).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\"));

            } else {
                insert.append(values[i]);
            }
            if (i != values.length - 1) {
                insert.append("', '");
            } else {
                insert.append("');");
            }
        }

        return insert.toString();
    }

    static String newSQLStatement(List<String> sqlStatementList, String sqlStatement) {
        sqlStatementList.add(sqlStatement + "\n");
        return sqlStatement;
    }

    static int[] getShopBoundaries(String query, int thread) {
        Statement statement = null;
        Connection connection = null;
        try {
            DataSource ds = pooledDataSources[thread].getDataSource();
            connection = ds.getConnection();
            statement = connection.createStatement();
            ResultSet rs =  statement.executeQuery(query);
            int rowCount = 0;
            int[] shopInfo = new int[7];
            while (rs.next()) {
                ++rowCount;
                shopInfo[0] = rs.getInt("index");
                shopInfo[1] = rs.getInt("ownerID");
                shopInfo[2] = rs.getInt("assistantID");
                shopInfo[3] = rs.getInt("northEastX");
                shopInfo[4] = rs.getInt("northEastY");
                shopInfo[5] = rs.getInt("southWestX");
                shopInfo[6] = rs.getInt("southWestY");
            }
            if (rowCount == 1) {
                return shopInfo;
            } else {
                Logger.Error("More than one row when not expected!");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) try { statement.close(); } catch (SQLException ignore) {}
            if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
        }
        return null;
    }

    static int countRowsInTable(ScraperDatabaseTable table) {
        Statement statement = null;
        Connection connection = null;
        try {
            DataSource ds = pooledDataSources[Settings.threads].getDataSource();
            connection = ds.getConnection();
            String sql = "SELECT COUNT(*) from `" + sqlDatabaseName + "`.`" + table.getTableName() + "`";
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            rs.next();
            return rs.getInt("COUNT(*)");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) try { statement.close(); } catch (SQLException ignore) {}
            if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
        }
        return 0;
    }
}
