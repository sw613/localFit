package com.example.LocalFit.global.config;

//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

//@Configuration
//@EnableMongoRepositories(basePackages = {
//        "com.example.LocalFit.community.repository"
//})
//@EnableReactiveMongoRepositories(basePackages = {
//        "com.example.LocalFit.search.repository"
//})
//public class MongoConfig {
//    private static final String MONGO_URI = "mongodb://localhost:27017";
//    private static final String DB_NAME = "chatdb";
//
//    private static final String ELASTIC_DB_NAME = "elastic";
//
//    @Bean
//    public MongoClient mongoClient() {
//        return MongoClients.create(MONGO_URI);
//    }
//
//    @Bean(name = "mongoTemplate")
//    public MongoTemplate mongoTemplate() {
//        return new MongoTemplate(mongoClient(), DB_NAME);
//    }
//
//    @Bean
//    public com.mongodb.reactivestreams.client.MongoClient reactiveMongoClient() {
//        return com.mongodb.reactivestreams.client.MongoClients.create(MONGO_URI);
//    }
//
//    @Bean(name = "reactiveMongoTemplate")
//    public ReactiveMongoTemplate reactiveMongoTemplate() {
//        return new ReactiveMongoTemplate(reactiveMongoClient(), ELASTIC_DB_NAME);
//    }
//}
@Configuration
@EnableReactiveMongoRepositories(basePackages = {
      "com.example.LocalFit.community.repository", 
      "com.example.LocalFit.search.repository"
})
public class MongoConfig {

  private static final String MONGO_URI = "mongodb://localhost:27017";
  private static final String DB_NAME = "chatdb";

  private static final String ELASTIC_DB_NAME = "elastic";

  @Primary
  @Bean
  public com.mongodb.reactivestreams.client.MongoClient chatReactiveMongoClient() {
      return com.mongodb.reactivestreams.client.MongoClients.create(MONGO_URI);
  }

  @Bean(name = "reactiveMongoChatTemplate")
  public ReactiveMongoTemplate reactiveMongoChatTemplate() {
      return new ReactiveMongoTemplate(chatReactiveMongoClient(), DB_NAME);
  }

  @Bean
  public com.mongodb.reactivestreams.client.MongoClient reactiveMongoClient() {
      return com.mongodb.reactivestreams.client.MongoClients.create(MONGO_URI);
  }

  @Bean(name = "reactiveMongoTemplate")
  public ReactiveMongoTemplate reactiveMongoTemplate() {
      return new ReactiveMongoTemplate(reactiveMongoClient(), ELASTIC_DB_NAME);
  }
}


