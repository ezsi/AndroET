package org.measureit.androet.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ezsi
 */
public class Backup {

    public static void save() {
        FileOutputStream out = null;
        File root = Environment.getExternalStorageDirectory();
        if (!root.canWrite()) 
            return;
        try {
            File dir = new File(root.getAbsolutePath() + "/AndroET");
            if(!dir.exists())
                dir.mkdirs();
            Calendar cal = Calendar.getInstance();
            final String fileName = "backup"+cal.get(Calendar.YEAR)+"-"+(cal.get(Calendar.MONTH)+1)+"-"
                    +cal.get(Calendar.DATE)+"-"+cal.get(Calendar.HOUR_OF_DAY)+"-"+cal.get(Calendar.MINUTE) +".db";
            File file = new File(dir, fileName);
            out = new FileOutputStream(file);
            InputStream input = new FileInputStream("/data/data/org.measureit.androet/databases/androet");
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = input.read(buffer)) > 0) 
                    out.write(buffer, 0, bytesRead);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Backup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Backup.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(Backup.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
    public static void load(){
        DatabaseHelper.getInstance().dropDatabase();
        DatabaseHelper.getInstance().createDatabase();
        
        File root = Environment.getExternalStorageDirectory();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(root.getAbsolutePath() + "/AndroET/backup.db", null, 0);
        List<Account> accounts = Account.list(db, null);
        Map<Integer, Integer> accountIdMap = new HashMap<Integer, Integer>();
        for(Account account : accounts){
            Account.insert(account);
            int accountId = DatabaseHelper.getInstance().getLastInsertRowId();
            accountIdMap.put(account.getId(), accountId);
            if(account.isGroup()) // group doesn't have real transactions
                continue;
            List<Transaction> transactions = Transaction.list(account, db);
            for(Transaction transaction : transactions){
                transaction.setAccountId(accountId); // needed for proper auto id handling
                Transaction.insert(transaction);
            }
        }
        for(Account account : accounts){
            List<Account> groupAccounts = Account.list(account.getId(), null, db);
            int accountId = accountIdMap.get(account.getId());
            for(Account groupAccount : groupAccounts){
                Account.insertGroup(accountId, accountIdMap.get(groupAccount.getId()));
            }
        }
        db.close();
    }
    
}
