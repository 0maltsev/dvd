package edu.mipt.accounts.dblock;

import edu.mipt.accounts.Accounts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DbSynchronizedAccounts implements Accounts {
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public void transfer(long fromAccountId, long toAccountId, long amount) {
        var fromAccount = accountRepository.findById(fromAccountId);
        var toAccount = accountRepository.findById(toAccountId);

        doTransfer(fromAccount, toAccount, amount);
    }

    private void doTransfer(Account fromAccount, Account toAccount, long value) {
        fromAccount.withdraw(value);
        toAccount.deposit(value);

        accountRepository.saveAllAndFlush(List.of(fromAccount, toAccount));
    }
}