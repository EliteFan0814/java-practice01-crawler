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

public class Crawler {

    private CrawlerDao dao = new JdbcCrawlerDao();

    private static Boolean isInterestLink(String link) {
        return link.contains("sina.cn") && !link.contains("passport.sina.cn") && (link.contains("news.sina.cn") || "https://sina.cn".equals(link));
    }

    private static HttpGet generateATagRequest(String link) {
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
        return httpGet;
    }

    private void handleArticle(Document doc, String link) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                ArrayList<Element> paragraphs = articleTag.select("p");
                String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));
                dao.insertNewsIntoDatabase(link, title, content);
            }
        }
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private void handleNewLinkAndProcessedLink(String link) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = generateATagRequest(link);
        try (CloseableHttpResponse response1 = httpClient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            Document doc = Jsoup.parse(html);
            // 从当前链接获取新链接成功后再从数据库删除当前链接
            dao.handleUpdateDatabase(link, "delete from LINKS_TO_BE_PROCESSED where link = ?");
            parseALinkFromPageAndStoreIntoDatabase(doc);
            handleArticle(doc, link);
            dao.handleUpdateDatabase(link, "insert into LINKS_ALREADY_PROCESSED (link)values(?)");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseALinkFromPageAndStoreIntoDatabase(Document doc) {
        String[] banWordsList = new String[]{"#", "javascript"};
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            if (!isContainsBanWord(banWordsList, href)) {
                dao.handleUpdateDatabase(href, "insert into LINKS_TO_BE_PROCESSED (link)values(?)");
            }
        }
    }

    private Boolean isContainsBanWord(String[] banWordsList, String href) {
        List<String> tempList = Arrays.asList(banWordsList);
        for (String item :
                tempList) {
            if (href.toLowerCase().startsWith(item)) {
                return true;
            }
        }
        return false;
    }


    private boolean isANeededLink(String link) throws SQLException {
        return !dao.isProcessed(link) && isInterestLink(link);
    }

    public void run() throws SQLException {
        String link;
        while ((link = dao.getNextLink("select link from LINKS_TO_BE_PROCESSED limit 1")) != null) {
            if (isANeededLink(link)) {
                handleNewLinkAndProcessedLink(link);
            } else {
                //如果已经处理过此条链接，则从数据库删除
                dao.handleUpdateDatabase(link, "delete from LINKS_TO_BE_PROCESSED where link = ?");
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        Crawler crawler = new Crawler();
        crawler.run();
    }


}

