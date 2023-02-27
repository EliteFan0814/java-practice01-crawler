package com.github.hcsp.io;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLink(String sql) throws SQLException;
    void handleUpdateDatabase(String link, String sql);
    void insertNewsIntoDatabase(String url, String title, String content);
    boolean isProcessed(String link) throws SQLException;
}
