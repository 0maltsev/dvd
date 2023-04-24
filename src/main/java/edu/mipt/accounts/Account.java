package edu.mipt.accounts;

import lombok.Data;

@Data
public class Account {
    private long id;
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