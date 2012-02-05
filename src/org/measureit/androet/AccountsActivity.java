package org.measureit.androet;

import android.app.Dialog;
import org.measureit.androet.util.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.measureit.androet.db.Account;
import org.measureit.androet.db.Backup;
import org.measureit.androet.db.CurrencyRate;
import org.measureit.androet.db.Transaction;
import org.measureit.androet.db.UpdateBuilder;
import org.measureit.androet.db.WhereBuilder;
import org.measureit.androet.ui.ActivitySwitch;
import org.measureit.androet.ui.TextViewBuilder;
import org.measureit.androet.util.Helper;
import org.measureit.androet.util.ProgressTask;
 
//TODO: test with different size config

//TODO: rate access exception
//TODO: rate download exception

public class AccountsActivity extends Activity{
    private final ArrayList<Account> listItems = new ArrayList<Account>();
    private ListView listView;
    private ArrayAdapter listAdapter;    
    private static Context context;
    private Account selectedAccount;
    private EditText passwordEditBox; 
    private static final int DIALOG_PASSWORD = 1;
    private boolean authorized = false;
    private boolean showEnabledAccounts = true;
    
    private DialogInterface.OnClickListener confirmDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which == DialogInterface.BUTTON_POSITIVE){
                if(!selectedAccount.isGroup()) // only non group accounts have real trasactions
                    Transaction.delete(selectedAccount);
                Account.delete(selectedAccount);
                refreshAccountList();
            }
        }
    };
        
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        listView = new ListView(this);
        listView.setBackgroundColor(Color.WHITE);
        listView.setCacheColorHint(Color.WHITE);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ActivitySwitch.to(AccountsActivity.this, SummaryActivity.class).add("account", listItems.get(position)).execute();
            }
        });
        setContentView(listView);
        listAdapter =  new AccountAdapter(this,android.R.layout.simple_list_item_1 , listItems);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);         
        alertDialogBuilder.setTitle("Please enter PIN");  
        passwordEditBox = new EditText(this);
        passwordEditBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        passwordEditBox.setTransformationMethod(PasswordTransformationMethod.getInstance());
        alertDialogBuilder.setView(passwordEditBox);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(Constants.BUTTON_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int width) {
                removeDialog(DIALOG_PASSWORD);
                String pin = PreferenceManager.getDefaultSharedPreferences(AccountsActivity.this).getString("pin", "");
                if(!passwordEditBox.getText().toString().equals(pin))
                    showDialog(DIALOG_PASSWORD);
                else{
                    authorized = true;
                    refreshAccountList();
                }
            }
        });
        
        alertDialogBuilder.setNegativeButton(Constants.BUTTON_EXIT, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int width) {
                AccountsActivity.this.finish();
            }
        });
        
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    public static Context getAppContext(){
        return context;
    }
        
    @Override
    protected void onResume() {
        String pin = PreferenceManager.getDefaultSharedPreferences(this).getString("pin", "");
        if(pin.isEmpty())
            authorized = true;
        if(authorized)
            refreshAccountList();
        else 
            showDialog(DIALOG_PASSWORD);
        super.onResume();
    }

    private void refreshAccountList(){
        listItems.clear(); 
        List<Account> accounts = Account.list(showEnabledAccounts);
        if(accounts.isEmpty() && !showEnabledAccounts){ // if disabled list is empty we switch back to enabled accounts
            showEnabledAccounts = true;
            accounts = Account.list(showEnabledAccounts);
        }
        listItems.addAll(accounts);
        if(listItems.isEmpty())
            listItems.add(new Account(0, "Tap me long!", 0, 0, "EUR", false));
        listAdapter.notifyDataSetChanged();
        setTitle( showEnabledAccounts ? "Accounts" : "Disabled accounts");
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String itemTitle = item.toString();
        if(Constants.ACCOUNT_SAVE_DB.equals(itemTitle))
            new SaveDatabaseProgressTask().execute();
        else if(Constants.ACCOUNT_LOAD_DB.equals(itemTitle))
            new LoadDatabaseProgressTask().execute();
        else if(Constants.ACCOUNT_SETTINGS.equals(itemTitle))
            ActivitySwitch.to(this, Preferences.class).execute();
        else if(Constants.ACCOUNT_REFRESH_CURRENCY_RATES.equals(itemTitle)){
            new DownloadRatesProgressTask().execute();
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Constants.ACCOUNT_SAVE_DB);
        menu.add(Constants.ACCOUNT_LOAD_DB);
        menu.add(Constants.ACCOUNT_REFRESH_CURRENCY_RATES);
        menu.add(Constants.ACCOUNT_SETTINGS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int listItemIndex = (int) ((AdapterContextMenuInfo)item.getMenuInfo()).id;
        selectedAccount = (Account) listView.getItemAtPosition(listItemIndex);
        
        final String itemTitle = item.toString();
        if(Constants.TRANSACTION_ADD.equals(itemTitle))
            ActivitySwitch.to(this, TransactionActivity.class).add("accountId", selectedAccount.getId()).execute();
        else if(Constants.ACCOUNT_CREATE_GROUP.equals(itemTitle))
            ActivitySwitch.to(AccountsActivity.this, GroupActivity.class).execute();
        else if(Constants.ACCOUNT_CREATE.equals(itemTitle))
            ActivitySwitch.to(AccountsActivity.this, AccountActivity.class).execute();
        else if(Constants.ACCOUNT_EDIT.equals(itemTitle)){
            if(selectedAccount.isGroup())
                ActivitySwitch.to(AccountsActivity.this, GroupActivity.class).add("account", selectedAccount).execute();
            else
                ActivitySwitch.to(AccountsActivity.this, AccountActivity.class).add("account", selectedAccount).execute();
        }else if(Constants.ACCOUNT_DELETE.equals(itemTitle)){
            (new AlertDialog.Builder(this))
                .setMessage("Are you sure you want to delete \""+selectedAccount.getName()+"\" account?").setPositiveButton("Yes", confirmDialogClickListener)
                .setNegativeButton("No", confirmDialogClickListener).show();
        }else if(Constants.ACCOUNT_TRANSFER_MONEY.equals(itemTitle))
            ActivitySwitch.to(AccountsActivity.this, TransferActivity.class).add("account", selectedAccount).execute();
        else if(Constants.ACCOUNT_CORRECT_BALANCE.equals(itemTitle)) 
            ActivitySwitch.to(AccountsActivity.this, CorrectBalanceActivity.class).add("account", selectedAccount).execute();
        else if(Constants.ACCOUNT_DISABLE.equals(itemTitle)) {
            setEnabled(false);
        } else if(Constants.ACCOUNT_ENABLE.equals(itemTitle)) {
            setEnabled(true);
        } else if(Constants.ACCOUNT_SHOW_ENABLED.equals(itemTitle)) {
            showEnabledAccounts = true;
            refreshAccountList();
        } else if(Constants.ACCOUNT_SHOW_DISABLED.equals(itemTitle)) {
            showEnabledAccounts = false;
            refreshAccountList();
        }
        return super.onContextItemSelected(item);
    }
    
    private void setEnabled(Boolean enabled){
        selectedAccount.setEnabled(enabled);
        UpdateBuilder.table(Account.TABLE_NAME).column(Account.COL_ENABLED, enabled ? 1 : 0)
                .where(WhereBuilder.get().where(Account.COL_ID).build(), 
                Integer.toString(selectedAccount.getId())).update();
        refreshAccountList();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if(showEnabledAccounts){
            menu.add(Constants.TRANSACTION_ADD);
            menu.add(Constants.ACCOUNT_TRANSFER_MONEY);
            menu.add(Constants.ACCOUNT_CORRECT_BALANCE);
            menu.add(Constants.ACCOUNT_CREATE);
            menu.add(Constants.ACCOUNT_CREATE_GROUP);
            menu.add(Constants.ACCOUNT_DISABLE); 
            if(!Account.list(false).isEmpty())
                menu.add(Constants.ACCOUNT_SHOW_DISABLED); 
        }else {
            menu.add(Constants.ACCOUNT_ENABLE); 
            menu.add(Constants.ACCOUNT_SHOW_ENABLED); 
        }
        menu.add(Constants.ACCOUNT_EDIT);        
        menu.add(Constants.ACCOUNT_DELETE);        
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    
    private void runDialog(final int seconds)

{
        Log.e(Constants.LOG_NAME, "rundialog");
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Please wait....", "Here your message");

 
        Log.e(Constants.LOG_NAME, "dialog shown");
        new Thread(new Runnable(){
            public void run(){
                try {
                    Log.e(Constants.LOG_NAME, "start thread");
                    Thread.sleep(seconds * 1000);
                    Log.e(Constants.LOG_NAME, "done thread");
                    progressDialog.dismiss();
                    Log.e(Constants.LOG_NAME, "dismiss");

                } catch (InterruptedException e) {

                   e.printStackTrace();

                }

            }

        }).start();
        Log.e(Constants.LOG_NAME, "start done");
}

    
    private class DownloadRatesProgressTask extends ProgressTask {

        public DownloadRatesProgressTask() {
            super(AccountsActivity.this, "Updating currency rates.");
        }
        
        @Override
        protected Boolean doInBackground(String... arg0) {
            CurrencyRate.download();
            return true;
        }
        
    }
    
    private class SaveDatabaseProgressTask extends ProgressTask {

        public SaveDatabaseProgressTask() {
            super(AccountsActivity.this, "Saving database ...");
        }
        
        @Override
        protected Boolean doInBackground(String... arg0) {
            Backup.save();
            return true;
        }
        
    }
    
    private class LoadDatabaseProgressTask extends ProgressTask {

        public LoadDatabaseProgressTask() {
            super(AccountsActivity.this, "Loading database ...");
        }
        
        @Override
        protected Boolean doInBackground(String... arg0) {
            Backup.load();
            return true;
        }
        
    }
    
    private class AccountAdapter extends ArrayAdapter<Account> {
        private ArrayList<Account> items;

        public AccountAdapter(Context context, int textViewResourceId, ArrayList<Account> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TableRow.LayoutParams cellParams = new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1.0f);
            cellParams.setMargins(2, 2, 2, 2);
            
            final TableLayout.LayoutParams rowParams = new TableLayout
                    .LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT);
            rowParams.setMargins(4, 2, 6, 2);
            
            Account account = items.get(position);
            TableLayout tableLayout = new TableLayout(AccountsActivity.this);
            final int textColor = account.isGroup() ? Constants.HIGHLIGHT_COLOR : Constants.HEADER_TEXT_COLOR;            
            TableRow row1 = new TableRow(AccountsActivity.this);
            row1.setLayoutParams(rowParams);
            row1.addView(TextViewBuilder.text(AccountsActivity.this, account.getName())
                    .size(Constants.HEADER_TEXT_SIZE).color(textColor).build());
            
            TextView amountTextView = TextViewBuilder.text(AccountsActivity.this, 
                    String.format("%s %.2f", account.getCurrency().getSymbol(), account.getBalance()))
                    .size(Constants.TEXT_SIZE+2).gravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT)
                    .color(textColor).build();
            amountTextView.setLayoutParams(cellParams);
            row1.addView(amountTextView);

            TableRow row2 = new TableRow(AccountsActivity.this);
            row2.setLayoutParams(rowParams);
            double expense = Transaction.sum(account, Calendar.getInstance().get(Calendar.MONTH)+1);
            StringBuilder expenseSb = new StringBuilder();
            if(expense < 0)
                expenseSb.append(Constants.ACCOUNT_EXPENSE).append(Helper.formatNumber(Math.abs(expense)));
            else if (expense > 0)
                expenseSb.append(Constants.ACCOUNT_INCOME).append(Helper.formatNumber(Math.abs(expense)));
            
            int expenseColor = Constants.TEXT_COLOR;
            if(expense < 0 && account.getBudget() > 0){
                double availableBudget = account.getBudget()+expense;
                expenseSb.append(" / ").append(Helper.formatNumber(availableBudget));
                if(availableBudget < 0)
                    expenseColor = Constants.WARNING_COLOR;
            }
            row2.addView(TextViewBuilder.text(AccountsActivity.this, expenseSb.toString()).color(expenseColor).build());
            
            tableLayout.addView(row1);
            tableLayout.addView(row2);
            return tableLayout;
        }
    }
    
}
