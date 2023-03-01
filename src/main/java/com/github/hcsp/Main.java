package com.github.hcsp;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        CrawlerDao dao = new JdbcMyBatisCrawlerDao();
        for (int i = 0; i < 8; i++) {
            new Crawler(dao).start();
        }
    }
}
