package edu.mipt.accounts;

public interface Accounts {
    void transfer(long fromAccountId, long toAccountId, long amount);
}
