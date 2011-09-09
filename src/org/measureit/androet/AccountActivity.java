package org.measureit.androet;

import org.measureit.androet.util.Helper;
import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.Currency;
import org.measureit.androet.db.Account;
import org.measureit.androet.util.Cache;
import org.measureit.androet.ui.UIBuilder;
import org.measureit.androet.db.UpdateBuilder;
import org.measureit.androet.db.WhereBuilder;
import org.measureit.androet.util.Constants;

/**
 *
 * @author ezsi
 */
public class AccountActivity extends Activity {
    private final ArrayList<Currency> currencies = new ArrayList<Currency>();
    private Account account;
    private ArrayAdapter<Currency> currencyAdapter;
    private EditText accountNameEditBox;
    private EditText initialBalanceEditBox;
    private EditText budgetEditBox;
    private Spinner currencySpinner;
    private OnClickListener okOnClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            String accountName = accountNameEditBox.getText().toString();
            if(accountName.isEmpty())
                accountName = "Untitled";
            double initialBalance = Helper.parseDouble(initialBalanceEditBox.getText().toString(), 0D);
            double budget = Helper.parseDouble(budgetEditBox.getText().toString(), 0D);
            String currencyCode = ((Currency)currencySpinner.getSelectedItem()).getCurrencyCode();
            if(account == null)
                Account.insert(accountName, initialBalance, budget, currencyCode, false);
            else  // edit => update
                UpdateBuilder.table(Account.TABLE_NAME).column(Account.COL_NAME, accountName)
                    .column(Account.COL_INITIAL_BALANCE, initialBalance).column(Account.COL_CURRENCY, currencyCode)
                    .column(Account.COL_BUDGET, budget)
                    .where(WhereBuilder.get().where(Account.COL_ID).build(), Integer.toString(account.getId()))
                    .update();
            AccountActivity.this.finish();
        }
    };
    
    private OnClickListener cancelOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            AccountActivity.this.finish();
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        accountNameEditBox = new EditText(this);
        layout.addView( UIBuilder.createViewWithLabel(getBaseContext(), "Account name", accountNameEditBox) );

        initialBalanceEditBox = new EditText(this);
        initialBalanceEditBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView( UIBuilder.createViewWithLabel(getBaseContext(), "Initial balance", initialBalanceEditBox) );
        budgetEditBox = new EditText(this);
        budgetEditBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView( UIBuilder.createViewWithLabel(getBaseContext(), "Monthly budget", budgetEditBox) );

        currencySpinner = new Spinner(this);
        currencyAdapter = new ArrayAdapter<Currency>(this,android.R.layout.simple_spinner_item , currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);
        layout.addView( UIBuilder.createViewWithLabel(getBaseContext(), "Currency", currencySpinner) );
        layout.addView( UIBuilder.createHorizontalView(this, UIBuilder.createButton(this, Constants.BUTTON_OK, okOnClickListener), UIBuilder.createButton(this, Constants.BUTTON_CANCEL, cancelOnClickListener)));

        setContentView(layout);
    }
    
    @Override
    protected void onResume() {
        account = (Account) this.getIntent().getSerializableExtra("account");
        refreshCurrencyList();
        if(account != null){
            accountNameEditBox.setText(account.getName());
            initialBalanceEditBox.setText(Double.toString(account.getInitialBalance()));
            budgetEditBox.setText(Double.toString(account.getBudget()));
            currencySpinner.setSelection(currencies.indexOf(account.getCurrency()));
        }
        super.onResume();
    }
    
    private void refreshCurrencyList(){
        currencies.clear();       
        currencies.addAll(Cache.getCurrencies());
        currencyAdapter.notifyDataSetChanged();
    }
}
