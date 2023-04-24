package edu.mipt.accounts;

import edu.mipt.accounts.applock.AppSynchronizedAccounts;
import edu.mipt.accounts.applock.InMemoryAccountRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppSynchronizedAccountsTest {
    private final Account firstAccount = new Account(1, 100);
    private final Account secondAccount = new Account(2, 100);
    private final AccountRepository repository = new InMemoryAccountRepository(List.of(firstAccount, secondAccount));
    private final Accounts accounts = new AppSynchronizedAccounts(repository);

    private record Transfer(long from, long to, long amount) {
    }

    @Test
    public void parallelTransfer() {
        //given
        List<Transfer> transfers = createTransfers();
        //when
        executeTransfers(transfers);
        //then
        assertAll(
                () -> assertEquals(100, firstAccount.getBalance()),
                () -> assertEquals(100, secondAccount.getBalance())
        );
    }

    private List<Transfer> createTransfers() {
        List<Transfer> transfers = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            transfers.add(new Transfer(1L, 2L, 1));
            transfers.add(new Transfer(2L, 1L, 1));
        }
        return transfers;
    }

    private void executeTransfers(List<Transfer> transfers) {
        ExecutorService executorService = getExecutorService();
        List<CompletableFuture<Void>> futures = createTransferFutures(transfers, executorService);
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private ExecutorService getExecutorService() {
        int availableProcessors = getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(availableProcessors);
    }

    @NotNull
    private List<CompletableFuture<Void>> createTransferFutures(List<Transfer> transfers, ExecutorService executorService) {
        return transfers.stream()
                .map(transfer -> runAsync(() ->
                        accounts.transfer(transfer.from(), transfer.to(), transfer.amount()), executorService))
                .toList();
    }
}