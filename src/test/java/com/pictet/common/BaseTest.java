package com.pictet.common;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.jeasy.random.EasyRandom;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import tools.jackson.databind.ObjectMapper;

public abstract class BaseTest {

    protected EasyRandom generator;
    protected ObjectMapper parser;

    @TestConfiguration
    public static class InMemoryDatabase {

        @Bean
        public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory) {
            return new MongoTemplate(mongoDbFactory);
        }

        @Bean
        public MongoDatabaseFactory mongoDbFactory(MongoServer mongoServer) {
            String connectionString = mongoServer.getConnectionString();
            return new SimpleMongoClientDatabaseFactory(connectionString + "/test");
        }

        @Bean(destroyMethod = "shutdown")
        public MongoServer mongoServer() {
            MongoServer mongoServer = new MongoServer(new MemoryBackend());
            mongoServer.bind();
            return mongoServer;
        }
    }
}
