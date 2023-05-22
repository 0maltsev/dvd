package edu.mipt.accounts;

import jakarta.persistence.LockModeType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.retry.annotation.EnableRetry;

import static org.springframework.boot.SpringApplication.run;

@EnableRetry
@SpringBootApplication
public class AccountsApplication {
    public static void main(String[] args) {
        run(AccountsApplication.class, args);
    }
}