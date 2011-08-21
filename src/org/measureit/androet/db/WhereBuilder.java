package org.measureit.androet.db;

/**
 *
 * @author ezsi
 */
public class WhereBuilder {
    StringBuilder sb = new StringBuilder();
    
    protected WhereBuilder(){
    }
    
    public static WhereBuilder get(){
        return new WhereBuilder();
    }
    
    public WhereBuilder where(String columnName){
        sb.append(columnName).append("=?");
        return this;
    }
    
    public WhereBuilder and(){
        sb.append(" AND "); 
        return this;
    }
    
    public WhereBuilder or(){
        sb.append(" OR "); 
        return this;
    }
    
    public String build(){
        return sb.toString();
    }
}
