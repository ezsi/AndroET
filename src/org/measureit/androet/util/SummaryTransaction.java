/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.measureit.androet.util;

import org.measureit.androet.db.Transaction;

/**
 *
 * @author ezsi
 */
public class SummaryTransaction extends Transaction {

    private double expense;
    private double income;
    
    public SummaryTransaction(int accountId, int year, int month) {
        super(-1, accountId, null, 0, "", null, year, month);
    }

    public double getExpense() {
        return expense;
    }

    public void setExpense(double expense) {
        this.expense = expense;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

}
