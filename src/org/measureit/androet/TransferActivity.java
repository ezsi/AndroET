package org.measureit.androet;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.measureit.androet.db.Account;
import org.measureit.androet.db.Category;
import org.measureit.androet.db.Transaction;
import org.measureit.androet.util.Helper;
import org.measureit.androet.ui.UIBuilder;
import org.measureit.androet.util.Cache;
import org.measureit.androet.util.Constants;

/**
 *
 * @author ezsi
 */
public class TransferActivity extends Activity {
    private final ArrayList<Account> accounts = new ArrayList<Account>();
    private Account account;
    private ArrayAdapter<Account> accountAdapter;
    private EditText amountEditBox;
    private Spinner accountSpinner;
    private EditText descriptionEditBox;
    private Button dateButton;
    private Button timeButton;
    private Calendar calendar;
    
    private OnClickListener okOnClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            Account selectedAccount = (Account)accountSpinner.getSelectedItem();
            if(selectedAccount == null) // the list was empty
                return;
            final int timeInSec = Helper.calendarToSeconds(calendar);
            double amount = Helper.parseDouble(amountEditBox.getText().toString(), 0D);
            Category outTransactionCategory = Cache.getCategoryByName("Transfer(Outward)");
            Category inTransactionCategory = Cache.getCategoryByName("Transfer(Inward)");
            Transaction.insert(account.getId(), outTransactionCategory.getId(), - 1 * amount, descriptionEditBox.getText().toString(), timeInSec);
            Transaction.insert(selectedAccount.getId(), inTransactionCategory.getId(), amount, descriptionEditBox.getText().toString(), timeInSec);
            TransferActivity.this.finish();
        }
    };
    
    private OnClickListener cancelOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            TransferActivity.this.finish();
        }
    };
    
    private final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateDateTime();
        }
    };
 
    private OnClickListener dateOnClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            DatePickerDialog dialog = new DatePickerDialog(TransferActivity.this, onDateSetListener, 
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
            dialog.show();
        }
    };
    
    private final TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

        public void onTimeSet(TimePicker view, int hour, int min) {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, min);
            updateDateTime();
        }
    };
    
    private OnClickListener timeOnClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            TimePickerDialog dialog = new TimePickerDialog(TransferActivity.this, onTimeSetListener, 
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            dialog.show();
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        amountEditBox = new EditText(this);
        amountEditBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        accountSpinner = new Spinner(this);
        accountAdapter = new ArrayAdapter<Account>(this,android.R.layout.simple_spinner_item , accounts);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountSpinner.setAdapter(accountAdapter);

        layout.addView( UIBuilder.createHorizontalView(this, 
                UIBuilder.createViewWithLabel(getBaseContext(), "Amount", amountEditBox), 
                UIBuilder.createViewWithLabel(getBaseContext(), "Destination account", accountSpinner)));
        
        descriptionEditBox = new EditText(this);
        layout.addView( UIBuilder.createViewWithLabel(this, "Description", descriptionEditBox) );
        
        calendar = Calendar.getInstance();
        dateButton = new Button(this);
        dateButton.setText(DateFormat.getMediumDateFormat(this).format(calendar.getTime()));
        dateButton.setOnClickListener(dateOnClickListener);
        LinearLayout dateView = UIBuilder.createViewWithLabel(this, "Date", dateButton);
        
        timeButton = new Button(this);
        timeButton.setText(DateFormat.getTimeFormat(this).format(calendar.getTime()));
        timeButton.setOnClickListener(timeOnClickListener);
        LinearLayout timeView = UIBuilder.createViewWithLabel(this, "Time", timeButton);
        layout.addView( UIBuilder.createHorizontalView(this, dateView, timeView));
        layout.addView( UIBuilder.createHorizontalView(this, 
                UIBuilder.createButton(this, Constants.BUTTON_OK, okOnClickListener), 
                UIBuilder.createButton(this, Constants.BUTTON_CANCEL, cancelOnClickListener)));

        setContentView(layout);
    }
    
    @Override
    protected void onResume() {
        account = (Account) this.getIntent().getSerializableExtra("account");
        refreshAccountList();        
        super.onResume();
    }
    
    private void refreshAccountList(){
        accounts.clear();       
        List<Account> allAccounts = Account.list(true);
        for(Account acc : allAccounts){
            if(!acc.equals(account) && !acc.isGroup() 
                    && acc.getCurrency().equals(account.getCurrency()))
                accounts.add(acc);
        }
        accountAdapter.notifyDataSetChanged();
    }
    
    private void updateDateTime(){
        final Date date = calendar.getTime();
        dateButton.setText(DateFormat.getMediumDateFormat(this).format(date));
        timeButton.setText(DateFormat.getTimeFormat(this).format(date));
    }
}
