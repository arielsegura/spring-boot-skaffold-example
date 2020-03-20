package com.example.demo;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.String.format;

@Service
public class DemoService {

    private static final Logger logger = LoggerFactory.getLogger(DemoService.class);
    private final JdbcTemplate jdbcTemplate;

    private final PubSubTemplate pubSubTemplate;

    @Autowired
    public DemoService(JdbcTemplate jdbcTemplate, PubSubTemplate pubSubTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.pubSubTemplate = pubSubTemplate;
    }

    @Setter
    @Getter
    public static class Account {
        private String userId;
        private String username;
    }

    public void bla() {
        List<Account> accounts = jdbcTemplate.query("select * from account",
                (resultSet, i) -> {
                    String userId = resultSet.getString(1);
                    String username = resultSet.getString(2);
                    logger.info("User id {} username {} ", userId, username);
                    Account account = new Account();
                    account.setUserId(userId);
                    account.setUsername(username);
                    return account;
                }
        );


        accounts.forEach( account -> {
            ListenableFuture<String> publish = pubSubTemplate.publish("TOPIC1", format("%s-%s", account.userId, account.username));
            publish
                    .addCallback(s -> {
                        logger.info("Success! {}", s);
                    }, Throwable::printStackTrace);

            try {
                publish.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        });


        String textFromPubSub = pubSubTemplate.pullNext("SUBSCRIPTION1")
                .getData().toStringUtf8();
        logger.info(textFromPubSub);
    }
}
