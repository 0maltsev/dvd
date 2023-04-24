package edu.mipt.accounts;

import edu.mipt.accounts.dblock.Account;
import edu.mipt.accounts.dblock.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class DbSynchronizedAccountsTest {

    @Autowired
    private AccountRepository repository;
    @Autowired
    private Accounts accounts;

    private record Transfer(long from, long to, long value) {
    }

    @Test
    public void testTransfer() {
        //given
        var firstAccount = new Account(1, 100);
        var secondAccount = new Account(2, 100);
        repository.saveAllAndFlush(List.of(firstAccount, secondAccount));
        List<Transfer> transfers = createTransfers();
        //when
        executeTransfers(transfers);
        //then
        checkBalances();
    }

    private List<Transfer> createTransfers() {
        List<Transfer> transfers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            transfers.add(new Transfer(1L, 2L, 1));
            transfers.add(new Transfer(2L, 1L, 1));
        }
        return transfers;
    }

    private void executeTransfers(List<Transfer> transfers) {
        int availableProcessors = getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);
        List<CompletableFuture<Void>> futures = transfers.stream()
                .map(transfer -> runAsync(() ->
                        accounts.transfer(transfer.from(), transfer.to(), transfer.value()), executorService))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void checkBalances() {
        Account firstAccount = repository.findById(1L);
        Account secondAccount = repository.findById(2L);
        assertAll(
                () -> assertEquals(100, firstAccount.getBalance()),
                () -> assertEquals(100, secondAccount.getBalance())
        );
    }
}