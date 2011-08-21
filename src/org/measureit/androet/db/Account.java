package org.measureit.androet.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import org.measureit.androet.util.Cache;

/**
 *
 * @author ezsi
 */
public class Account implements Serializable{
    public static final String TABLE_NAME = "account";
    public static final String MAP_TABLE_NAME = "account2account";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_INITIAL_BALANCE = "initialBalance";
    public static final String COL_BUDGET = "budget";
    public static final String COL_CURRENCY = "currency";
    public static final String COL_GROUP = "isgroup";
    public static final String COL_MAP_GROUP_ID = "groupId";
    public static final String COL_MAP_ACCOUNT_ID = "accountId";
    public static final String WHERE_ID = COL_ID+"=?";
    
    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " ("
            + COL_ID+" integer primary key autoincrement,"
            + COL_NAME + " TEXT, "
            + COL_INITIAL_BALANCE + " REAL, "
            + COL_BUDGET + " REAL, "
            + COL_CURRENCY + " TEXT, "
            + COL_GROUP + " INTEGER);";

    public static final String MAP_TABLE_CREATE =
            "CREATE TABLE " + MAP_TABLE_NAME + " ("
            + COL_MAP_GROUP_ID + " INTEGER, "
            + COL_MAP_ACCOUNT_ID + " INTEGER, "
            + "FOREIGN KEY("+COL_MAP_GROUP_ID+") REFERENCES "+Account.TABLE_NAME+"("+Account.COL_ID+"), "
            + "FOREIGN KEY("+COL_MAP_ACCOUNT_ID+") REFERENCES "+Account.TABLE_NAME+"("+Account.COL_ID+"));";
    
    
    private int id;
    private String name;
    private double initialBalance;
    private double balance;
    private double budget;
    private Currency currency;
    private boolean group;

    public Account(int id, String name, double initialBalance, double budget, String currencyCode, boolean group) {
        this.id = id;
        this.name = name;
        this.initialBalance = (group) ? sumInitialBalance(id) : initialBalance;
        this.budget = budget;
        this.currency = Cache.getCurrency(currencyCode);
        this.balance = (group) ? this.initialBalance + Transaction.sumGroup(id) : this.initialBalance + Transaction.sum(id);
        this.group = group;
    }

    @Override
    public String toString() {
        return name+" ("+currency.getCurrencyCode()+")";
    }

    public int getId() {
        return id;
    }
    
    public double getBudget() {
        return budget;
    }

    public Currency getCurrency() {
        return currency;
    }
    
    public double getInitialBalance() {
        return initialBalance;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }
       
    public String getBalanceWithCurrency() {
        return currency.getSymbol() + " " + balance;
    }

    public boolean isGroup() {
        return group;
    }
    
    public static void create(Account account) {
        create(account.getName(), account.getInitialBalance(), account.getCurrency().getCurrencyCode(), account.isGroup());
    }
    
    public static void create(String accountName, double initialBalance, String currency, boolean group) {
        create(DatabaseHelper.getInstance().getWritableDatabase(), accountName, initialBalance, currency, group);
    }
    
    public static void create(SQLiteDatabase db, String accountName, double initialBalance, String currency, boolean group) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(COL_NAME, accountName);
        initialValues.put(COL_INITIAL_BALANCE, initialBalance);
        initialValues.put(COL_BUDGET, 0);
        initialValues.put(COL_CURRENCY, currency);
        initialValues.put(COL_GROUP, group);
        db.insert(TABLE_NAME, null, initialValues);
    }
    
    public static void addGroup(int groupId, int accountId) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(COL_MAP_GROUP_ID, groupId);
        initialValues.put(COL_MAP_ACCOUNT_ID, accountId);
        DatabaseHelper.getInstance().getWritableDatabase().insert(MAP_TABLE_NAME, null, initialValues);
    }

    public static double sumInitialBalance(int groupId){
        Cursor cursor = DatabaseHelper.getInstance().getWritableDatabase().rawQuery(
            "SELECT SUM(a." + COL_INITIAL_BALANCE + ") FROM " + TABLE_NAME + " AS a, " 
                + Account.MAP_TABLE_NAME + " AS aa WHERE a." + COL_ID 
                + " = aa." + Account.COL_MAP_ACCOUNT_ID + " AND aa." 
                + Account.COL_MAP_GROUP_ID + " = " + groupId, null);
        if(cursor.moveToFirst()) 
            return cursor.getDouble(0);
        return -1;
    }
    
    
    public static void delete(Account account){
        if(account.isGroup())
            DatabaseHelper.getInstance().getWritableDatabase().delete(MAP_TABLE_NAME, COL_MAP_GROUP_ID + '=' + account.getId(), null);
        else
            DatabaseHelper.getInstance().getWritableDatabase().delete(MAP_TABLE_NAME, COL_MAP_ACCOUNT_ID + '=' + account.getId(), null);
        DatabaseHelper.getInstance().getWritableDatabase().delete(TABLE_NAME, COL_ID + '=' + account.getId(), null);
    }
    
    public static void clearGroup(Account account){
        DatabaseHelper.getInstance().getWritableDatabase().delete(MAP_TABLE_NAME, COL_MAP_GROUP_ID + '=' + account.getId(), null);        
    }
    
    public static List<Account> list() {
        return list(DatabaseHelper.getInstance().getWritableDatabase());
    }
    
    public static List<Account> list(SQLiteDatabase db) {
        List<Account> accounts = new ArrayList<Account>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COL_CURRENCY + "," + COL_GROUP + ","+COL_NAME);
        while(cursor.moveToNext())
            accounts.add(new Account(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4), (cursor.getInt(5) == 1)));
        return accounts;
    }    
    
    public static List<Account> list(int groupId) {
        return list(groupId, DatabaseHelper.getInstance().getWritableDatabase());
    }
    
    public static List<Account> list(int groupId, SQLiteDatabase db) {
        List<Account> accounts = new ArrayList<Account>();
        Cursor cursor = db.rawQuery(
            "SELECT a.* FROM " + TABLE_NAME + " AS a, " 
                + Account.MAP_TABLE_NAME + " AS aa WHERE a." + COL_ID 
                + " = aa." + Account.COL_MAP_ACCOUNT_ID + " AND aa." 
                + Account.COL_MAP_GROUP_ID + " = " + groupId, null);
        while(cursor.moveToNext())
            accounts.add(new Account(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4), (cursor.getInt(5) == 1)));
        return accounts;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Account other = (Account) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.id;
        return hash;
    }
    
}
