package com.github.hcsp;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {

    private static void mockData(int howMany) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> currentNews = session.selectList("com.github.hcsp.MockMapper.selectNews");
            int count = howMany - currentNews.size();
            Random random = new Random();
            try {
                while (count-- > 0) {
                    int index = random.nextInt(currentNews.size());
                    News newsToBeInsert = new News(currentNews.get(index));
                    Instant currentTime = newsToBeInsert.getCreatedAt();
                    currentTime = currentTime.minusSeconds(random.nextInt(3600 * 24 * 365));
                    newsToBeInsert.setCreatedAt(currentTime);
                    newsToBeInsert.setModifiedAt(currentTime);
                    session.insert("com.github.hcsp.MockMapper.insertNews", newsToBeInsert);
                    System.out.println(count);
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        mockData(1000);
    }
}
