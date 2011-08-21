package org.measureit.androet.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ezsi
 */
public class Category implements Serializable{
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_EXPENSE = "expense";
    public static final String TABLE_NAME = "category";
    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " ("
            + COL_ID+" integer primary key autoincrement,"
            + COL_NAME + " TEXT, "
            + COL_EXPENSE + " INTEGER);";
    private int id;
    private String name;  
    private boolean expense;

    public Category(int id, String name, boolean expense) {
        this.id = id;
        this.name = name;
        this.expense = expense;
    }
       
    @Override
    public String toString() {
        return name;
    }
    
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isExpense() {
        return expense;
    }
    
    public static void create(SQLiteDatabase db, String categoryName, boolean expense) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(COL_NAME, categoryName);
        initialValues.put(COL_EXPENSE, expense);
        db.insert(TABLE_NAME, null, initialValues);
    }
    
    public static List<Category> list(){
        List<Category> categories = new ArrayList<Category>();
        Cursor cursor = DatabaseHelper.getInstance().getWritableDatabase()
                .query(TABLE_NAME, null, null, null, null, null, null);
        while(cursor.moveToNext())
            categories.add(new Category(cursor.getInt(0), cursor.getString(1), cursor.getInt(2) == 1));
        return categories;
    }
    
    //TODO: may improve this later
    public static Category getByName(String categoryName){
        List<Category> categories = list();
        for(Category category : categories)
            if(category.getName().equals(categoryName))
                return category;
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Category other = (Category) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.id;
        return hash;
    }
}