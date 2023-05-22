package edu.mipt.accounts.dblock;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;

@Data
@Entity
public class Account {
    @Id
    private long id;

    @Version
    private Long version;

    private long balance;

    public Account() {
    }

    public Account(long id, long balance) {
        this.id = id;
        this.balance = balance;
    }

    public void deposit(long value) {
        balance += value;
    }

    public void withdraw(long value) {
        balance -= value;
    }
}