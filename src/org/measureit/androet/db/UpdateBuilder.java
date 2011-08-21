package org.measureit.androet.db;

import android.content.ContentValues;

/**
 *
 * @author ezsi
 */
public class UpdateBuilder {
    ContentValues values = new ContentValues();
    String tableName;
    String where;
    String[] whereArgs;
    
    protected UpdateBuilder(String tableName){
        this.tableName = tableName;
    }
    
    public static UpdateBuilder table(String tableName){
        return new UpdateBuilder(tableName);
    }
    
    public void update(){
        DatabaseHelper.getInstance().getWritableDatabase().update(tableName, values, where, whereArgs);
    }
    
    public UpdateBuilder where(String where, String ... whereArgs){
        this.where = where;
        this.whereArgs = whereArgs;
        return this;
    }
    
    public UpdateBuilder column(String columnName, Double updateValue){
        values.put(columnName, updateValue);
        return this;
    }
    
    public UpdateBuilder column(String columnName, Integer updateValue){
        values.put(columnName, updateValue);
        return this;
    }
    
    public UpdateBuilder column(String columnName, String updateValue){
        values.put(columnName, updateValue);
        return this;
    }
}
