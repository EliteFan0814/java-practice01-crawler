package com.github.hcsp.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "123456";

    private static Boolean isInterestLink(String link) {
        return link.contains("sina.cn") && !link.contains("passport.sina.cn") && (link.contains("news.sina.cn") || "https://sina.cn".equals(link));
    }

    private static HttpGet generateATagRequest(String link) {
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
        return httpGet;
    }

    private static void handleArticle(Connection connection, Document doc, String link) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                ArrayList<Element> paragraphs = articleTag.select("p");
                String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));
                try (PreparedStatement statement = connection.prepareStatement("insert into NEWS(url , title, content, created_at, modified_at) values ( ?,?,?,now(),now() )")) {
                    statement.setString(1, link);
                    statement.setString(2, title);
                    statement.setString(3, content);
                    statement.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                System.out.println(link);
                System.out.println(title);
            }
        }
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private static void handleNewLinkAndProcessedLink(String link, Connection connection) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = generateATagRequest(link);
        try (CloseableHttpResponse response1 = httpClient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            Document doc = Jsoup.parse(html);
            // 从当前链接获取新链接成功后再从数据库删除当前链接
            handleUpdateDatabase(link, connection, "delete from LINKS_TO_BE_PROCESSED where link = ?");
            parseALinkFromPageAndStoreIntoDatabase(connection, doc);
            handleArticle(connection, doc, link);
            handleUpdateDatabase(link, connection, "insert into LINKS_ALREADY_PROCESSED (link)values(?)");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void parseALinkFromPageAndStoreIntoDatabase(Connection connection, Document doc) {
        String[] banWordsList = new String[]{"#", "javascript"};
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            if (!isContainsBanWord(banWordsList, href)) {
                handleUpdateDatabase(href, connection, "insert into LINKS_TO_BE_PROCESSED (link)values(?)");
            }
        }
    }

    private static Boolean isContainsBanWord(String[] banWordsList, String href) {
        List<String> tempList = Arrays.asList(banWordsList);
        for (String item :
                tempList) {
            if (href.toLowerCase().startsWith(item)) {
                return true;
            }
        }
        return false;
    }

    private static void handleUpdateDatabase(String link, Connection connection, String sql) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getNextLink(Connection connection, String sql) throws SQLException {
        List<String> list = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                return result.getString(1);
            }
            return null;
        }
    }

    private static boolean isProcessed(Connection connection, String link) throws SQLException {
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

    private static boolean isANeededLink(Connection connection, String link) throws SQLException {
        return !isProcessed(connection, link) && isInterestLink(link);
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:F:\\ideaMy\\java-practice01-crawler\\news", USER_NAME, PASSWORD);
        String link;
        while ((link = getNextLink(connection, "select link from LINKS_TO_BE_PROCESSED limit 1")) != null) {
            if (isANeededLink(connection, link)) {
                handleNewLinkAndProcessedLink(link, connection);
            } else {
                //如果已经处理过此条链接，则从数据库删除
                handleUpdateDatabase(link, connection, "delete from LINKS_TO_BE_PROCESSED where link = ?");
            }
        }
    }


}

