package com.github.hcsp;

import java.sql.SQLException;

public interface MyBatisCrawlerDao {
    String getNextLink(String sql) throws SQLException;
    void handleUpdateDatabase(String link, String sql);
    void insertNewsIntoDatabase(String url, String title, String content);
    boolean isProcessed(String link) throws SQLException;

    void deleteLinkFromDatabase(String link);

    void insertLinkIntoDatabase(String link);
}
