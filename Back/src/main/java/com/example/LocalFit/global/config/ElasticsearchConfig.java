package com.example.LocalFit.global.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class ElasticsearchConfig {

    //@Value("${spring.elasticsearch.username}")
    private String username = "elastic";

    //@Value("${spring.elasticsearch.password}")
    private String password = "elastic";

    //@Value("${spring.elasticsearch.host}")
    private String host = "127.0.0.1";

    //@Value("${spring.elasticsearch.port:9200}")
    private int port = 9200;

    @Bean
    public ElasticsearchClient elasticsearchClient() throws Exception {
        RestClient restClient = restClient();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));

        return new ElasticsearchClient(transport);
    }

    @Bean
    public RestClient restClient() throws Exception {
        SSLContext sslContext = SSLContext.getDefault();

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, "https"))
                .setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder
                        .setSSLContext(sslContext)
                        .setDefaultCredentialsProvider(credentialsProvider()));

        return builder.build();
    }

    private CredentialsProvider credentialsProvider() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return credentialsProvider;
    }

}
