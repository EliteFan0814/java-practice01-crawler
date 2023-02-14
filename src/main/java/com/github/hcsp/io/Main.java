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

import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> linkPool = new ArrayList<>();
        Set<String> processedLinks = new HashSet<>();
        linkPool.add("https://sina.cn");
        while (true) {
            if (linkPool.isEmpty()) {
                break;
            }
            String link = linkPool.remove(linkPool.size() - 1);
            if (processedLinks.contains(link)) {
                continue;
            }
            if (link.contains("sina.cn") && !link.contains("passport.sina.cn") && (link.contains("news.sina.cn") || "https://sina.cn".equals(link))) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                if (link.startsWith("//")) {
                    link = "https:" + link;
                }
                HttpGet httpGet = new HttpGet(link);
                httpGet.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
                System.out.println(link);
                try (CloseableHttpResponse response1 = httpClient.execute(httpGet)) {
                    HttpEntity entity1 = response1.getEntity();
                    String html = EntityUtils.toString(entity1);
                    Document doc = Jsoup.parse(html);
                    ArrayList<Element> links = doc.select("a");
                    for (Element aTag : links) {
                        linkPool.add(aTag.attr("href"));
                    }
                    ArrayList<Element> articleTags = doc.select("article");
                    if (!articleTags.isEmpty()) {
                        for (Element articleTag : articleTags) {
                            String title = articleTag.child(0).text();
                            System.out.println(title);
                        }
                    }
                    processedLinks.add(link);
                }
            }
        }
    }
}
