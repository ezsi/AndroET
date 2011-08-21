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
    public static final int DATABASE_VERSION = 28;
    
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
        
        Category.create(db, "Accommodation", true);
        Category.create(db, "Automobile", true);
        Category.create(db, "Child Support", false);
        Category.create(db, "Credit Card", true);
        Category.create(db, "Donation", true);
        Category.create(db, "Entertainment", true);
        Category.create(db, "Food", true);
        Category.create(db, "Gifts - Given", true);
        Category.create(db, "Gifts - Received", false);
        Category.create(db, "Groceries", true);
        Category.create(db, "Household", true);
        Category.create(db, "Income", false);
        Category.create(db, "Insurance", true);
        Category.create(db, "Investment", false);
        Category.create(db, "Interest", false);
        Category.create(db, "Medicare", true);
        Category.create(db, "Personal Care", true);
        Category.create(db, "Pets", true);
        Category.create(db, "Salary", false);
        Category.create(db, "Self Improvement", true);
        Category.create(db, "Shopping", true);
        Category.create(db, "Sport & Recreation", true);
        Category.create(db, "Tax", true);
        Category.create(db, "Transfer(Outward)", true);
        Category.create(db, "Transfer(Inward)", false);
        Category.create(db, "Transportation", true);
        Category.create(db, "Utilities", true);
        Category.create(db, "Vacation", true);
        Category.create(db, "Other expense", true);
        Category.create(db, "Other income", false);
        
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
        db.execSQL("DROP TABLE IF EXISTS " + Category.TABLE_NAME);
    }
    
    public int getLastInsertRowId(){
        Cursor cursor = getWritableDatabase().rawQuery("select last_insert_rowid();", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }
}