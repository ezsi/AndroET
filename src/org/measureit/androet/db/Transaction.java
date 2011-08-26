package org.measureit.androet.db;

import org.measureit.androet.util.Cache;
import java.util.Calendar;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.measureit.androet.util.Helper;

/**
 *
 * @author ezsi
 */
public class Transaction implements Serializable{
    public static final String TABLE_NAME = "transactions";
    public static final String COL_ID = "id";
    public static final String COL_CATEGORY_ID = "categoryId";
    public static final String COL_ACCOUNT_ID = "accountId";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_DATE = "date";
    public static final String COL_DESCRIPTION = "description";
    
    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " ("
            + COL_ID+" integer primary key autoincrement,"
            + COL_ACCOUNT_ID + " INTEGER, "
            + COL_CATEGORY_ID + " INTEGER, "
            + COL_AMOUNT + " REAL, "
            + COL_DESCRIPTION + " TEXT, "
            + COL_DATE + " INTEGER, "
            + "FOREIGN KEY("+COL_ACCOUNT_ID+") REFERENCES "+Account.TABLE_NAME+"("+Account.COL_ID+"), "
            + "FOREIGN KEY("+COL_CATEGORY_ID+") REFERENCES "+Category.TABLE_NAME+"("+Category.COL_ID+"));";
    
    
    private int id;
    private int accountId;
    private Category category;
    private double amount;
    private String description;
    private Calendar date;

    public Transaction(int id, int accountId, Category category, double amount, String description, Calendar date) {
        this.id = id;
        this.accountId = accountId;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    @Override
    public String toString() {
        return "Transaction{" + "id=" + id + ", accountId=" + accountId + ", category=" + category + ", amount=" + amount + ", description=" + description + ", date=" + date.getTime().toString() + '}';
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }

    public double getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public int getId() {
        return id;
    }

    public Calendar getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }
    
    public static void create(Transaction transaction) {
        create(transaction.getAccountId(), transaction.getCategory().getId(), transaction.getAmount(), transaction.getDescription(), Helper.calendarToSeconds(transaction.getDate()));
    }
    
    public static void create(int accountId, int categoryId, double amount, String description, int date) {
        create(DatabaseHelper.getInstance().getWritableDatabase(), accountId, categoryId, amount, description, date);
    }
    
    public static void create(SQLiteDatabase db, int accountId, int categoryId, double amount, String description, int date) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(COL_ACCOUNT_ID, accountId);
        initialValues.put(COL_CATEGORY_ID, categoryId);
        initialValues.put(COL_AMOUNT, amount);
        initialValues.put(COL_DESCRIPTION, description);
        initialValues.put(COL_DATE, date);
        db.insert(TABLE_NAME, null, initialValues);
    }
    
    public static void delete(int transactionId){
        DatabaseHelper.getInstance().getWritableDatabase().delete(TABLE_NAME, COL_ID + "="+transactionId, null);
    }
    
    public static void delete(Account account){
        DatabaseHelper.getInstance().getWritableDatabase().delete(TABLE_NAME, COL_ACCOUNT_ID + "="+account.getId(), null);
    }
    
    
    public static double sum(int accountId){
        Cursor cursor = DatabaseHelper.getInstance().getWritableDatabase().rawQuery(
            "SELECT SUM("+COL_AMOUNT+") FROM "+TABLE_NAME+" WHERE "+COL_ACCOUNT_ID+"="+accountId, null);
        if(cursor.moveToFirst()) 
            return cursor.getDouble(0);
        return -1;
    }
    
    /**
     * Summarize transaction values for the current month.
     */
    public static double sum(Account account, int month){
        Calendar currentMonth = Calendar.getInstance();
        Helper.resetDate(currentMonth);
        currentMonth.set(Calendar.MONTH, month-1);
        
        Cursor cursor = account.isGroup() ? 
            DatabaseHelper.getInstance().getWritableDatabase().rawQuery(
                "SELECT SUM(t." + COL_AMOUNT + ") FROM " + TABLE_NAME + " AS t," + Account.MAP_TABLE_NAME
                + " AS a WHERE t." + COL_ACCOUNT_ID + " = a." + Account.COL_MAP_ACCOUNT_ID 
                + " AND a." + Account.COL_MAP_GROUP_ID + " = " + account.getId() 
                + " AND t." + COL_DATE + " > " + Helper.calendarToSeconds(currentMonth), null)
            : DatabaseHelper.getInstance().getWritableDatabase().rawQuery(
                "SELECT SUM("+COL_AMOUNT+") FROM "+TABLE_NAME+" WHERE "+COL_ACCOUNT_ID+"="+account.getId() + " AND " + COL_DATE + " > " + Helper.calendarToSeconds(currentMonth), null);
        
        
        if(cursor.moveToFirst()) 
            return cursor.getDouble(0);
        return -1;
    }

    
    
    public static double sumGroup(int groupId){
        Cursor cursor = DatabaseHelper.getInstance().getWritableDatabase().rawQuery(
            "SELECT SUM(t." + COL_AMOUNT + ") FROM " + TABLE_NAME + " AS t," + Account.MAP_TABLE_NAME
                + " AS a WHERE t." + COL_ACCOUNT_ID + " = a." + Account.COL_MAP_ACCOUNT_ID 
                + " AND a." + Account.COL_MAP_GROUP_ID + " = " + groupId, null);
        if(cursor.moveToFirst()) 
            return cursor.getDouble(0);
        return -1;
    }

    
    public static List<Transaction> list(Account account){
        return list(account, DatabaseHelper.getInstance().getWritableDatabase());
    }
    
    public static List<Transaction> list(Account account, SQLiteDatabase db){
        final int accountId = account.getId();
        List<Transaction> transactions = new ArrayList<Transaction>();
        Cursor cursor = (account.isGroup()) ? db.rawQuery(
            "SELECT t.* FROM " + TABLE_NAME + " AS t," + Account.MAP_TABLE_NAME
                + " AS a WHERE t." + COL_ACCOUNT_ID + " = a." + Account.COL_MAP_ACCOUNT_ID 
                + " AND a." + Account.COL_MAP_GROUP_ID + " = " + account.getId() + " ORDER by t." + COL_DATE, null)
                : db.query(TABLE_NAME, null, COL_ACCOUNT_ID+" = "+accountId, null, null, null, COL_DATE);
        while(cursor.moveToNext()){
            Calendar calendar = Calendar.getInstance();
            final int timeInSec = cursor.getInt(5);
            calendar.setTimeInMillis(timeInSec * 1000L);
            final int categoryId = cursor.getInt(2);
            Transaction transaction = new Transaction(cursor.getInt(0), cursor.getInt(1), Cache.getCategory(categoryId)
                    , cursor.getDouble(3), cursor.getString(4), calendar);
            transactions.add(transaction);
        }
        return transactions;
    }
    
}