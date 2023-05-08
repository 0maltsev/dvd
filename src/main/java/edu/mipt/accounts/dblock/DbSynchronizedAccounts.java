package edu.mipt.accounts.dblock;

import edu.mipt.accounts.Accounts;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DbSynchronizedAccounts implements Accounts {
    private final AccountRepository accountRepository;
    private static final Object tieLock = new Object();
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @Retryable(maxAttempts = 50)
    public void transfer(long fromAccountId, long toAccountId, long amount) throws Exception {
        var fromAccount = accountRepository.findById(fromAccountId);
        var toAccount = accountRepository.findById(toAccountId);

        int fromHash = System.identityHashCode(fromAccount);
        int toHash = System.identityHashCode(toAccount);

        if (fromHash < toHash) {
            finishTransfer(fromAccount, toAccount, fromAccount, toAccount, amount);

        } else if (fromHash > toHash) {
            finishTransfer(toAccount, fromAccount, fromAccount, toAccount, amount);

        } else {
            synchronized (tieLock) {
                finishTransfer(fromAccount, toAccount, fromAccount, toAccount, amount);
            }
        }
    }

    private void finishTransfer(Account firstSynch, Account secondSynch, Account fromAccount, Account toAccount, long amount) throws Exception {
        synchronized (firstSynch) {
            synchronized (secondSynch) {
                doTransfer(fromAccount, toAccount, amount);
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