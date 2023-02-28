package com.github.hcsp;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class JdbcMyBatisCrawlerDao implements MyBatisCrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

    public JdbcMyBatisCrawlerDao() {
        try {
            String resource = "db/mybatis/MyMapper.xml";
            InputStream inputStream = null;
            inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNextLink(String sql) throws SQLException {
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
        try (SqlSession session = sqlSessionFactory.openSession()) {
            session.delete("com.github.hcsp.MyMapper.deleteLink");
        }
    }

    @Override
    public void deleteLinkFromDatabase(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            session.delete("com.github.hcsp.MyMapper.deleteLink", link);
        }
    }

    @Override
    public void insertLinkIntoDatabase(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            session.delete("com.github.hcsp.MyMapper.insertLink", link);
        }
    }

    @Override
    public void insertNewsIntoDatabase(String url, String title, String content) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
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