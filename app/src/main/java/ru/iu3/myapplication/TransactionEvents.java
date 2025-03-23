package ru.iu3.myapplication;

public interface TransactionEvents {
    String enterPin(int ptc, String amount);
    void transactionResult(boolean result);

}
