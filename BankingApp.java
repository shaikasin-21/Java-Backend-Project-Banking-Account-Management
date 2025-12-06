import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

// Custom Exceptions
class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}

class InvalidTransactionException extends Exception {
    public InvalidTransactionException(String message) {
        super(message);
    }
}

// Base Account Class (OOP: Inheritance & Polymorphism)
abstract class Account {
    protected String accountNumber;
    protected String holderName;
    protected double balance;

    public Account(String accountNumber, String holderName, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = balance;
    }

    public abstract void deposit(double amount) throws InvalidTransactionException;
    public abstract void withdraw(double amount) throws InsufficientBalanceException, InvalidTransactionException;

    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @Override
    public String toString() {
        return holderName + " (" + accountNumber + ") Balance: " + balance;
    }
}

// Savings Account
class SavingsAccount extends Account {
    public SavingsAccount(String accountNumber, String holderName, double balance) {
        super(accountNumber, holderName, balance);
    }

    @Override
    public void deposit(double amount) throws InvalidTransactionException {
        if (amount <= 0) throw new InvalidTransactionException("Deposit amount must be positive!");
        balance += amount;
    }

    @Override
    public void withdraw(double amount) throws InsufficientBalanceException, InvalidTransactionException {
        if (amount <= 0) throw new InvalidTransactionException("Withdrawal amount must be positive!");
        if (balance < amount) throw new InsufficientBalanceException("Insufficient balance!");
        balance -= amount;
    }
}

// Current Account
class CurrentAccount extends Account {
    private double overdraftLimit = 5000;

    public CurrentAccount(String accountNumber, String holderName, double balance) {
        super(accountNumber, holderName, balance);
    }

    @Override
    public void deposit(double amount) throws InvalidTransactionException {
        if (amount <= 0) throw new InvalidTransactionException("Deposit amount must be positive!");
        balance += amount;
    }

    @Override
    public void withdraw(double amount) throws InsufficientBalanceException, InvalidTransactionException {
        if (amount <= 0) throw new InvalidTransactionException("Withdrawal amount must be positive!");
        if (balance + overdraftLimit < amount) throw new InsufficientBalanceException("Overdraft limit exceeded!");
        balance -= amount;
    }
}

// Banking System (Collections + Multithreading)
class BankingSystem {
    private Map<String, Account> accounts = new HashMap<>();
    private ReentrantLock lock = new ReentrantLock();

    public void addAccount(Account account) {
        accounts.put(account.getAccountNumber(), account);
    }

    public void performTransaction(String accountNumber, String type, double amount) {
        lock.lock();
        try {
            Account acc = accounts.get(accountNumber);
            if (acc == null) {
                System.out.println("Account not found!");
                return;
            }
            switch (type.toLowerCase()) {
                case "deposit":
                    acc.deposit(amount);
                    System.out.println("Deposited " + amount + " into " + acc);
                    break;
                case "withdraw":
                    acc.withdraw(amount);
                    System.out.println("Withdrew " + amount + " from " + acc);
                    break;
                default:
                    System.out.println("Invalid transaction type!");
            }
        } catch (Exception e) {
            System.out.println("Transaction failed: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void showAllAccounts() {
        for (Account acc : accounts.values()) {
            System.out.println(acc);
        }
    }
}

// Multithreading Simulation
class TransactionThread extends Thread {
    private BankingSystem system;
    private String accountNumber;
    private String type;
    private double amount;

    public TransactionThread(BankingSystem system, String accountNumber, String type, double amount) {
        this.system = system;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
    }

    @Override
    public void run() {
        system.performTransaction(accountNumber, type, amount);
    }
}

// Main Class
public class BankingApp {
    public static void main(String[] args) {
        BankingSystem system = new BankingSystem();

        // Create accounts
        Account acc1 = new SavingsAccount("A1001", "Shaik Asin", 10000);
        Account acc2 = new CurrentAccount("A1002", "John Doe", 5000);

        system.addAccount(acc1);
        system.addAccount(acc2);

        // Multithreaded transactions
        Thread t1 = new TransactionThread(system, "A1001", "deposit", 2000);
        Thread t2 = new TransactionThread(system, "A1001", "withdraw", 3000);
        Thread t3 = new TransactionThread(system, "A1002", "withdraw", 7000);

        t1.start();
        t2.start();
        t3.start();

        // Wait for threads to finish
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Show final account states
        System.out.println("\nFinal Account States:");
        system.showAllAccounts();
    }
}
