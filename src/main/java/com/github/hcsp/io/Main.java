package com.github.hcsp.io;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    private static Boolean isInterestLink(String link) {
        return link.contains("sina.cn") && !link.contains("passport.sina.cn") && (link.contains("news.sina.cn") || "https://sina.cn".equals(link));
    }

    private static HttpGet generateATagRequest(String link) {
        if (link.startsWith("//")) {
            link = "https:" + link;
        }
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
        return httpGet;
    }

    private static void handleArticle(Document doc) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                System.out.println(title);
            }
        }
    }

    private static void handleNewLinkAndProcessedLink(String link, Connection connection) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = generateATagRequest(link);
        try (CloseableHttpResponse response1 = httpClient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            Document doc = Jsoup.parse(html);
            //            从当前链接获取新链接成功后再从数据库删除当前链接
            handleLinkInDatabase(link, connection, "delete from LINKS_TO_BE_PROCESSED where link = ?");
            for (Element aTag : doc.select("a")) {
                String href = aTag.attr("href");
                handleLinkInDatabase(href, connection, "insert into LINKS_TO_BE_PROCESSED (link)values(?)");
            }
            handleArticle(doc);
            handleLinkInDatabase(link, connection, "insert into LINKS_ALREADY_PROCESSED (link)values(?)");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleLinkInDatabase(String link, Connection connection, String sql) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> list = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                list.add(result.getString(1));
            }
            return list;
        }
    }

    private static boolean isProcessed(Connection connection, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            statement.setString(1, link);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:F:\\ideaMy\\java-practice01-crawler\\news", "root", "123456");
        while (true) {
            List<String> linkPool = loadUrlsFromDatabase(connection, "select link from LINKS_TO_BE_PROCESSED");
            String link = linkPool.remove(linkPool.size() - 1);
            if (!isProcessed(connection, link)&&isInterestLink(link)) {
                handleNewLinkAndProcessedLink(link, connection);
            } else {
                //如果已经处理过此条链接，则从数据库删除
                handleLinkInDatabase(link, connection, "delete from LINKS_TO_BE_PROCESSED where link = ?");
            }
        }
    }
}

