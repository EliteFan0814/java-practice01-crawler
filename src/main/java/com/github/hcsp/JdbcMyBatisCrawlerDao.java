package com.github.hcsp;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class JdbcMyBatisCrawlerDao implements MyBatisCrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

    public JdbcMyBatisCrawlerDao() {
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNextLink() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            String url = (String) session.selectOne("com.github.hcsp.MyMapper.getNextLink");
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    @Override
    public void handleUpdateDatabase(String link, String sql) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.delete("com.github.hcsp.MyMapper.deleteLink");
        }
    }

    @Override
    public void deleteLinkFromDatabase(String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.delete("com.github.hcsp.MyMapper.deleteLink", link);
        }
    }

    @Override
    public void insertProcessedLinkIntoDatabase(String link) {
        Map<String, String> param = new HashMap<>();
        param.put("tableName", "links_already_processed");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.delete("com.github.hcsp.MyMapper.insertLink", param);
        }
    }

    @Override
    public void insertTobeProcessLinkIntoDatabase(String link) {
        Map<String, String> param = new HashMap<>();
        param.put("tableName", "links_to_be_processed");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.delete("com.github.hcsp.MyMapper.insertLink", param);
        }
    }

    @Override
    public void insertNewsIntoDatabase(String url, String title, String content) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.MyMapper.insertNews", new News(url, title, content));
        }
    }

    @Override
    public boolean isProcessed(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("com.github.hcsp.MyMapper.countLink", link);
            return count != 0;
        }
    }
}
