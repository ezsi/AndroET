package org.measureit.androet.db;

import org.measureit.androet.AccountsActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 * @author ezsi
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper instance;
    private static final String DATABASE_NAME = "androet";
    public static final int DATABASE_VERSION = 32; 
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance(){
        if(instance == null)
            instance = new DatabaseHelper(AccountsActivity.getAppContext());
        return instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) { 
        db.execSQL(Category.TABLE_CREATE);
        db.execSQL(Account.TABLE_CREATE);        
        db.execSQL(Account.MAP_TABLE_CREATE);        
        db.execSQL(Transaction.TABLE_CREATE);
        db.execSQL(Transaction.VIEW_CREATE);
        
        Category.insert(db, "Accommodation", true);
        Category.insert(db, "Automobile", true);
        Category.insert(db, "Child Support", false);
        Category.insert(db, "Credit Card", true);
        Category.insert(db, "Donation", true);
        Category.insert(db, "Entertainment", true);
        Category.insert(db, "Food", true);
        Category.insert(db, "Gifts - Given", true);
        Category.insert(db, "Gifts - Received", false);
        Category.insert(db, "Groceries", true);
        Category.insert(db, "Household", true);
        Category.insert(db, "Income", false);
        Category.insert(db, "Insurance", true);
        Category.insert(db, "Investment", false);
        Category.insert(db, "Interest", false);
        Category.insert(db, "Medicare", true);
        Category.insert(db, "Personal Care", true);
        Category.insert(db, "Pets", true);
        Category.insert(db, "Salary", false);
        Category.insert(db, "Self Improvement", true);
        Category.insert(db, "Shopping", true);
        Category.insert(db, "Sport & Recreation", true);
        Category.insert(db, "Tax", true);
        Category.insert(db, "Transfer(Outward)", true);
        Category.insert(db, "Transfer(Inward)", false);
        Category.insert(db, "Transportation", true);
        Category.insert(db, "Utilities", true);
        Category.insert(db, "Vacation", true);
        Category.insert(db, "Other expense", true);
        Category.insert(db, "Other income", false);
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        dropDatabase(db);
        onCreate(db);
    }

    public void createDatabase(){
        onCreate(instance.getWritableDatabase());
    }
    
    public void dropDatabase(){
        dropDatabase(instance.getWritableDatabase());
    }
    
    private void dropDatabase(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + Account.MAP_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Account.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Transaction.TABLE_NAME);
        db.execSQL("DROP VIEW IF EXISTS " + Transaction.VIEW_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Category.TABLE_NAME);
    }
    
    public int getLastInsertRowId(){
        Cursor cursor = getWritableDatabase().rawQuery("select last_insert_rowid();", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }
}