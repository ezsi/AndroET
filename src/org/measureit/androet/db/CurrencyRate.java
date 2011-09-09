/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.measureit.androet.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.measureit.androet.util.Constants;
import org.measureit.androet.util.Helper;

/**
 *
 * @author ezsi
 */
public class CurrencyRate {
    public static final String COL_PAIR = "pair";
    public static final String COL_RATE = "rate";
    public static final String TABLE_NAME = "currencyrate";
    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " ("
            + COL_PAIR+" TEXT primary key ,"
            + COL_RATE + " REAL);";
    
    private String pair;
    private double rate;

    public CurrencyRate(String pair, double rate) {
        this.pair = pair;
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "CurrencyRate{" + "pair=" + pair + ", rate=" + rate + '}';
    }
    
    public static void insertOrUpdate(String pair, double rate) {
        final SQLiteDatabase db = DatabaseHelper.getInstance().getWritableDatabase();
        if(getRate(pair) != null){ // update rate
            UpdateBuilder.table(TABLE_NAME)
                    .column(COL_RATE, rate)
                    .where(WhereBuilder.get().where(COL_PAIR).build(), pair)
                    .update();
        } else { // insert the first time
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_PAIR, pair);
            initialValues.put(COL_RATE, rate);
            db.insert(TABLE_NAME, null, initialValues);
        }
    }
    
    public static Double getRate(String pair){
        Cursor cursor = DatabaseHelper.getInstance().getWritableDatabase()
                .query(TABLE_NAME, null, COL_PAIR+" = \""+pair +"\"", null, null, null, null);
        return cursor.moveToNext() ? cursor.getDouble(1) : null;
    }
    
    public static void download() {
        Set<String> currencyPairs = new HashSet<String>();
        List<Account> accounts = Account.list();
        for(Account account : accounts){
            if(!account.isGroup())
                continue;
            String termCurrency = account.getCurrency().getCurrencyCode();
            List<Account> groupAccounts = Account.list(account.getId());
            for(Account groupAccount : groupAccounts){
                String baseCurrency = groupAccount.getCurrency().getCurrencyCode();
                if(!baseCurrency.equals(termCurrency))
                    currencyPairs.add(baseCurrency+termCurrency);
            }
        }
        if(currencyPairs.isEmpty())
            return;
        StringBuilder urlString = new StringBuilder("http://finance.yahoo.com/d/quotes.csv?e=.csv&f=sl1d1t1&s=");
        for(String currencyPair : currencyPairs)
            urlString.append(currencyPair).append("=X%20");
        
        BufferedReader br = null;
        try {
            URL url = new URL(urlString.toString());
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = br.readLine()) != null){
                StringTokenizer st = new StringTokenizer(inputLine, ",");
                String pair = st.nextToken().substring(1, 7);
                Double rate = Helper.parseDouble(st.nextToken(), null);
                if(rate == null) // wrong date date so we'll keep the old one
                    continue;
                insertOrUpdate(pair, rate);
            }
        } catch (IOException ex) {
            Log.e(Constants.LOG_NAME, ex.getMessage());
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Log.e(Constants.LOG_NAME, ex.getMessage());
            }
        }
        
        

    }
    
}
