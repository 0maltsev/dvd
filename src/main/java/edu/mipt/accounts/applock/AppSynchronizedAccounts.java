package edu.mipt.accounts.applock;

import edu.mipt.accounts.Account;
import edu.mipt.accounts.AccountRepository;
import edu.mipt.accounts.Accounts;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AppSynchronizedAccounts implements Accounts {
    private final AccountRepository accountRepository;
    private static final Object tieLock = new Object();

    @Override
    public void transfer(long fromAccountId, long toAccountId, long amount) throws Exception {
        var fromAccount = accountRepository.findById(fromAccountId);
        var toAccount = accountRepository.findById(toAccountId);

        int fromHash = System.identityHashCode(fromAccount);
        int toHash = System.identityHashCode(toAccount);

        if (fromHash < toHash) {
            synchronized (fromAccount) {
                synchronized (toAccount) {
                    doTransfer(fromAccount, toAccount, amount);
                }
            }
        } else if (fromHash > toHash) {
            synchronized (toAccount) {
                synchronized (fromAccount) {
                    doTransfer(fromAccount, toAccount, amount);
                }
            }
        } else {
            synchronized (tieLock) {
                synchronized (fromAccount) {
                    synchronized (toAccount) {
                        doTransfer(fromAccount, toAccount, amount);
                    }
                }
            }
        }
    }

    private void doTransfer(Account fromAccount, Account toAccount, long value) throws Exception {
        if (fromAccount.getBalance() - value < 0)
            throw new Exception();
        else {
            fromAccount.withdraw(value);
            toAccount.deposit(value);
        }
    }
}