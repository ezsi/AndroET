package org.measureit.androet;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import org.measureit.androet.db.Account;
import org.measureit.androet.db.DatabaseHelper;
import org.measureit.androet.util.Cache;
import org.measureit.androet.ui.UIBuilder;
import org.measureit.androet.db.UpdateBuilder;
import org.measureit.androet.db.WhereBuilder;
import org.measureit.androet.util.Helper;

/**
 *
 * @author ezsi
 */
public class GroupActivity extends Activity {
    private final ArrayList<Currency> currencies = new ArrayList<Currency>();
    private final ArrayList<Account> accountListItems = new ArrayList<Account>();
    private Account account;
    private ArrayAdapter<Currency> currencyAdapter;
    private EditText accountNameEditBox;
    private EditText budgetEditBox;
    private Spinner currencySpinner;
    private ListView accountListView;
    private ArrayAdapter accountListAdapter;
    private OnClickListener okOnClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            
            //String accountName = (accountNameEditBox.getText() == null) ? "Untitled" : accountNameEditBox.getText().toString();
            String accountName = accountNameEditBox.getText().toString();
            if(accountName.isEmpty())
                accountName = "Untitled";
            String currencyCode = ((Currency)currencySpinner.getSelectedItem()).getCurrencyCode();
            double budget = Helper.parseDouble(budgetEditBox.getText().toString(), 0);
            if(account == null)
                Account.create(accountName, 0, budget, currencyCode, true);
            else{
                UpdateBuilder.table(Account.TABLE_NAME).column(Account.COL_NAME, accountName)
                        .column(Account.COL_CURRENCY, currencyCode).column(Account.COL_BUDGET, budget)
                    .where(WhereBuilder.get().where(Account.COL_ID).build(), Integer.toString(account.getId()))
                    .update();
                Account.clearGroup(account); // no complicated update logic just: drop + add again
            }
            final int groupId = (account == null) ? DatabaseHelper.getInstance().getLastInsertRowId() : account.getId();
            SparseBooleanArray checkedItems = accountListView.getCheckedItemPositions();    
            for(int i=0, n = accountListView.getCount(); i<n; i++)
                if(checkedItems.get(i))
                    Account.addGroup(groupId, accountListItems.get(i).getId());
            GroupActivity.this.finish();
        }
    };
    
    private OnClickListener cancelOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            GroupActivity.this.finish();
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        accountNameEditBox = new EditText(this);
        layout.addView( UIBuilder.createViewWithLabel(getBaseContext(), "Account name", accountNameEditBox) );

        currencySpinner = new Spinner(this);
        currencyAdapter = new ArrayAdapter<Currency>(this,android.R.layout.simple_spinner_item , currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
        currencySpinner.setAdapter(currencyAdapter);
        layout.addView( UIBuilder.createViewWithLabel(getBaseContext(), "Currency", currencySpinner) );
        
        budgetEditBox = new EditText(this);
        budgetEditBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView( UIBuilder.createViewWithLabel(getBaseContext(), "Monthly budget", budgetEditBox) );
        
        accountListView = new ListView(this);
        accountListAdapter =  new ArrayAdapter<Account>(this,android.R.layout.simple_list_item_multiple_choice , accountListItems);
        accountListView.setAdapter(accountListAdapter);
        accountListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        accountListView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f));
        layout.addView(accountListView);
        
        layout.addView( UIBuilder.createHorizontalView(this, UIBuilder.createButton(this, "OK", okOnClickListener), UIBuilder.createButton(this, "Cancel", cancelOnClickListener)));
        setContentView(layout);
    }
    
    @Override
    protected void onResume() {
        refreshCurrencyList();
        accountListItems.clear();
        List<Account> accounts = Account.list();
        for(Account acc : accounts)
            if(!acc.isGroup())
                accountListItems.add(acc);
        accountListAdapter.notifyDataSetChanged();
        
        account = (Account) this.getIntent().getSerializableExtra("account");
        if(account != null){
            accountNameEditBox.setText(account.getName());
            currencySpinner.setSelection(currencies.indexOf(account.getCurrency()));
            budgetEditBox.setText(Double.toString(account.getBudget()));
            List<Account> selectedAccounts = Account.list(account.getId());
            for(Account selectedAccount : selectedAccounts)
                accountListView.setItemChecked(accountListItems.indexOf(selectedAccount), true);
        }
        super.onResume();
    }
    
    private void refreshCurrencyList(){
        currencies.clear();       
        currencies.addAll(Cache.getCurrencies());
        currencyAdapter.notifyDataSetChanged();
    }
}
