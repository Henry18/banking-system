package com.bank.cuentasmovimientos.handler;

public class SaldoNoDisponibleException extends RuntimeException {
    public SaldoNoDisponibleException(String m){ super(m); }
}
