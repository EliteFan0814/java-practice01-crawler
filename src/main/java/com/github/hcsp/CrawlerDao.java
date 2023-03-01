package com.github.hcsp;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLink();
    void handleUpdateDatabase(String link, String sql);
    void insertNewsIntoDatabase(String url, String title, String content);
    boolean isProcessed(String link) throws SQLException;

    void deleteLinkFromDatabase(String link);

    void insertProcessedLinkIntoDatabase(String link);

    void insertTobeProcessLinkIntoDatabase(String link);
}