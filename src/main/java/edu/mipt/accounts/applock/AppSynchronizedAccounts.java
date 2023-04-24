package edu.mipt.accounts.applock;

import edu.mipt.accounts.Account;
import edu.mipt.accounts.AccountRepository;
import edu.mipt.accounts.Accounts;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AppSynchronizedAccounts implements Accounts {
    private final AccountRepository accountRepository;

    @Override
    public void transfer(long fromAccountId, long toAccountId, long amount) {
        var fromAccount = accountRepository.findById(fromAccountId);
        var toAccount = accountRepository.findById(toAccountId);

        doTransfer(fromAccount, toAccount, amount);
    }

    private void doTransfer(Account fromAccount, Account toAccount, long value) {
        fromAccount.withdraw(value);
        toAccount.deposit(value);
    }
}