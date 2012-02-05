package org.measureit.androet.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import org.measureit.androet.util.Cache;
import org.measureit.androet.util.Constants;

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
    public static final String COL_ENABLED = "enabled";
    public static final String COL_CONFIDENT = "hidden";
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
            + COL_GROUP + " INTEGER, "
            + COL_ENABLED + " INTEGER, "
            + COL_CONFIDENT + " INTEGER"
            +");";

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
    private boolean enabled;
    private boolean confidant;

    public Account(int id, String name, double initialBalance, double budget, String currencyCode, boolean group) {
        this(id, name, initialBalance, budget, currencyCode, group, true, false);
    }
    
    public Account(int id, String name, double initialBalance, double budget, String currencyCode, boolean group, boolean enabled, boolean confidant) {
        this.id = id;
        this.name = name;
        this.initialBalance = (group) ? sumInitialBalance(id) : initialBalance;
        this.budget = budget;
        this.currency = Cache.getCurrency(currencyCode);
//        this.balance = (group) ? this.initialBalance + Transaction.sumGroup(id) : this.initialBalance + Transaction.sum(id);
        this.balance = this.initialBalance + Transaction.sum(id);
        this.group = group;
        this.enabled = enabled;
        this.confidant = confidant;
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
        return group ? Transaction.sumGroup(this) : balance;
    }
       
    public String getBalanceWithCurrency() {
        return currency.getSymbol() + " " + balance;
    }

    public boolean isGroup() {
        return group;
    }

    public boolean isConfidant() {
        return confidant;
    }

    public void setConfidant(boolean confidant) {
        this.confidant = confidant;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public static void insert(Account account) {
        insert(account.getName(), account.getInitialBalance(), account.getBudget(), account.getCurrency().getCurrencyCode(), account.isGroup(), account.isEnabled(), account.isConfidant());
    }
    
    public static void insert(String accountName, double initialBalance, double budget, String currency, boolean group) {
        insert(DatabaseHelper.getInstance().getWritableDatabase(), accountName, initialBalance, budget, currency, group, true, false);
    }
    
    public static void insert(String accountName, double initialBalance, double budget, String currency, boolean group, boolean enabled, boolean confidant) {
        insert(DatabaseHelper.getInstance().getWritableDatabase(), accountName, initialBalance, budget, currency, group, enabled, confidant);
    }
    
    public static void insert(SQLiteDatabase db, String accountName, double initialBalance, double budget, String currency, boolean group, boolean enabled, boolean confidant) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(COL_NAME, accountName);
        initialValues.put(COL_INITIAL_BALANCE, initialBalance);
        initialValues.put(COL_BUDGET, budget);
        initialValues.put(COL_CURRENCY, currency);
        initialValues.put(COL_GROUP, group);
        initialValues.put(COL_ENABLED, enabled);
        initialValues.put(COL_CONFIDENT, confidant);
        db.insert(TABLE_NAME, null, initialValues);
    }
    
    public static void insertGroup(int groupId, int accountId) {
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
    
    public static void deleteGroup(Account account){
        DatabaseHelper.getInstance().getWritableDatabase().delete(MAP_TABLE_NAME, COL_MAP_GROUP_ID + '=' + account.getId(), null);        
    }
    
    public static List<Account> list(Boolean enabled) {
        return list(DatabaseHelper.getInstance().getWritableDatabase(), enabled);
    }

    public static List<Account> list(SQLiteDatabase db, Boolean enabled) {
        List<Account> accounts = new ArrayList<Account>();
        String whereClause = null;
        String[] whereArgs = null;
        if( enabled != null ){
            whereClause = WhereBuilder.get().where(COL_ENABLED).build();
            whereArgs = new String[]{ enabled ? "1" : "0" };
        }
        Cursor cursor = db.query(TABLE_NAME, null, whereClause, whereArgs, 
                null, null, COL_CURRENCY + "," + COL_GROUP + ","+COL_NAME);
        while(cursor.moveToNext()) 
            accounts.add(readAccount(cursor));
        return accounts;
    }    
    
    private static Account readAccount(Cursor cursor){
        final int columnCount = cursor.getColumnCount();
        boolean enabled = (columnCount > 6) ? (cursor.getInt(6) == 1) : true; // for DB migration: applying defaults for missing columns
        boolean confidant = (columnCount > 7) ? (cursor.getInt(7) == 1) : false; // for DB migration: applying defaults for missing columns
        return new Account(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4), (cursor.getInt(5) == 1), enabled, confidant);
    }
    
    public static List<Account> list(int groupId, Boolean enabled) {
        return list(groupId, enabled, DatabaseHelper.getInstance().getWritableDatabase());
    }
    
    public static List<Account> list(int groupId, Boolean enabled, SQLiteDatabase db) {
        List<Account> accounts = new ArrayList<Account>();
        String enabledStr = "";
        if(enabled != null)
            enabledStr = " AND " + COL_ENABLED + " = " + (enabled ? "1" : "0") ;
        Cursor cursor = db.rawQuery(
            "SELECT a.* FROM " + TABLE_NAME + " AS a, " 
                + Account.MAP_TABLE_NAME + " AS aa WHERE a." + COL_ID 
                + " = aa." + Account.COL_MAP_ACCOUNT_ID + " AND aa." 
                + Account.COL_MAP_GROUP_ID + " = " + groupId + enabledStr, null);
        while(cursor.moveToNext())
            accounts.add(readAccount(cursor));
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
