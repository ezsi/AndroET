package org.measureit.androet;

import org.measureit.androet.ui.UIBuilder;
import org.measureit.androet.util.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import java.util.ArrayList;
import org.measureit.androet.db.Account;
import org.measureit.androet.db.Backup;
import org.measureit.androet.db.Transaction;
import org.measureit.androet.ui.ActivitySwitch;
import org.measureit.androet.ui.TextViewBuilder;

//TODO: add date to transaction list
//TODO: add monthly expense/income sum
//TODO: show budget line

public class AccountsActivity extends Activity{
    private final ArrayList<Account> listItems = new ArrayList<Account>();
    private ListView listView;
    private ArrayAdapter listAdapter;    
    private static Context context;
    private Account selectedAccount;
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
                ActivitySwitch.to(AccountsActivity.this, TransactionsActivity.class).add("account", listItems.get(position)).execute();
            }
        });
        setContentView(listView);
        listAdapter =  new AccountAdapter(this,android.R.layout.simple_list_item_1 , listItems);
        listView.setAdapter(listAdapter);
    }
    
    public static Context getAppContext(){
        return context;
    }
    
//    private void switchToActivity(Context context, Class clazz, Account account){
//        Intent activity = new Intent(context, clazz);
//        activity.putExtra("account", account);
//        AccountsActivity.this.startActivity(activity);
//    }
    
    @Override
    protected void onResume() {
        refreshAccountList();
        super.onResume();
    }

    private void refreshAccountList(){
        listItems.clear(); 
        listItems.addAll(Account.list());
        if(listItems.isEmpty())
            listItems.add(new Account(0, "Tap me long!", 0, 0, "EUR", false));
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String itemTitle = item.toString();
        if(Constants.ACCOUNT_SAVE_DB.equals(itemTitle))
            Backup.save();
        else if(Constants.ACCOUNT_LOAD_DB.equals(itemTitle))
            Backup.load();
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Constants.ACCOUNT_SAVE_DB);
        menu.add(Constants.ACCOUNT_LOAD_DB);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int listItemIndex = (int) ((AdapterContextMenuInfo)item.getMenuInfo()).id;
        selectedAccount = (Account) listView.getItemAtPosition(listItemIndex);
        
        final String itemTitle = item.toString();
        if(Constants.ACCOUNT_CREATE_GROUP.equals(itemTitle))
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
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add(Constants.ACCOUNT_CREATE);
        menu.add(Constants.ACCOUNT_CREATE_GROUP);
        menu.add(Constants.ACCOUNT_EDIT);        
        menu.add(Constants.ACCOUNT_DELETE);
        menu.add(Constants.ACCOUNT_TRANSFER_MONEY);
        menu.add(Constants.ACCOUNT_SET_BUDGET);
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    
    
    private class AccountAdapter extends ArrayAdapter<Account> {
        private ArrayList<Account> items;

        public AccountAdapter(Context context, int textViewResourceId, ArrayList<Account> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout layout = new LinearLayout(AccountsActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            Account account = items.get(position);
            
            layout.addView(UIBuilder.createHorizontalView(AccountsActivity.this, 
                    TextViewBuilder.text(AccountsActivity.this, "").build() ));            
            int textColor = account.isGroup() ? Color.MAGENTA : Color.BLACK;
            layout.addView( UIBuilder.createHorizontalView(AccountsActivity.this, 
                    TextViewBuilder.text(AccountsActivity.this, account.getName()).size(Constants.TEXT_SIZE).color(textColor).build(),
                    TextViewBuilder.text(AccountsActivity.this, account.getBalanceWithCurrency()).size(Constants.TEXT_SIZE).color(textColor).gravity(Gravity.CENTER_HORIZONTAL | Gravity.RIGHT).build() ));
            layout.addView(UIBuilder.createHorizontalView(AccountsActivity.this, 
                    TextViewBuilder.text(AccountsActivity.this, "").build() ));
            return layout;
        }
    }
    
}
