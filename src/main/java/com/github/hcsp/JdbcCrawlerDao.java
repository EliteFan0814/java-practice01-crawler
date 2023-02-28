package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.ibatis.session.SqlSessionFactory;
import java.sql.*;

public class JdbcCrawlerDao implements MyBatisCrawlerDao {
    private SqlSessionFactory sqlSessionFactory;
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "123456";
    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        try {
            connection = DriverManager.getConnection("jdbc:h2:file:F:\\ideaMy\\java-practice01-crawler\\news", USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNextLink(String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                return result.getString(1);
            }
            return null;
        }
    }

    public void handleUpdateDatabase(String link, String sql) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertNewsIntoDatabase(String url, String title, String content) {
        try (PreparedStatement statement = connection.prepareStatement("insert into NEWS(url , title, content, created_at, modified_at) values ( ?,?,?,now(),now() )")) {
            statement.setString(1, url);
            statement.setString(2, title);
            statement.setString(3, content);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isProcessed(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }
}
