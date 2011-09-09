package org.measureit.androet;

import org.measureit.androet.util.Helper;
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
import org.measureit.androet.db.Category;
import org.measureit.androet.db.Transaction;
import org.measureit.androet.ui.UIBuilder;
import org.measureit.androet.db.UpdateBuilder;
import org.measureit.androet.db.WhereBuilder;
import org.measureit.androet.util.Cache;
import org.measureit.androet.util.Constants;

/**
 *
 * @author ezsi
 */
public class TransactionActivity extends Activity {
    private final ArrayList<Category> categoryItems=new ArrayList<Category>();
    private ArrayAdapter categoryAdapter;
    private EditText amountEditBox;
    private EditText descriptionEditBox;
    private Button dateButton;
    private Button timeButton;
    private Calendar calendar;
    private Spinner categorySpinner;
    private int accountId;
    private Transaction transaction;
    
    private OnClickListener okOnClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            final Category selectedCategory = (Category) categorySpinner.getSelectedItem();
            final int timeInSec = Helper.calendarToSeconds(calendar);
            int sign = selectedCategory.isExpense() ? -1 : 1;
            if(transaction == null)
                Transaction.insert(accountId, selectedCategory.getId(), sign * Helper.parseDouble(amountEditBox.getText().toString(), 0), descriptionEditBox.getText().toString(), timeInSec);
            else
                UpdateBuilder.table(Transaction.TABLE_NAME)
                    .column(Transaction.COL_CATEGORY_ID, selectedCategory.getId())
                    .column(Transaction.COL_AMOUNT, sign * Helper.parseDouble(amountEditBox.getText().toString(), 0))
                    .column(Transaction.COL_DESCRIPTION, descriptionEditBox.getText().toString())
                    .column(Transaction.COL_DATE, timeInSec)
                    .where(WhereBuilder.get().where(Transaction.COL_ID).build(), Integer.toString(transaction.getId()))
                    .update();
            TransactionActivity.this.finish();
        }
    };
    
    private OnClickListener cancelOnClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            TransactionActivity.this.finish();
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
            DatePickerDialog dialog = new DatePickerDialog(TransactionActivity.this, onDateSetListener, 
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
            TimePickerDialog dialog = new TimePickerDialog(TransactionActivity.this, onTimeSetListener, 
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            dialog.show();
        }
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        amountEditBox = new EditText(this);
        amountEditBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView( UIBuilder.createViewWithLabel(this, "Amount", amountEditBox) );
        
        categorySpinner = new Spinner(this);
        categoryAdapter = new ArrayAdapter<Category>(this,android.R.layout.simple_spinner_item , categoryItems);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        layout.addView( UIBuilder.createViewWithLabel(this, "Category", categorySpinner) );
        
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
        layout.addView(UIBuilder.createHorizontalView(this, dateView, timeView));
        layout.addView( UIBuilder.createHorizontalView(this, UIBuilder.createButton(this, Constants.BUTTON_OK, okOnClickListener), UIBuilder.createButton(this, Constants.BUTTON_CANCEL, cancelOnClickListener)));

        setContentView(layout);
    }
    
    @Override
    protected void onResume() {
        refreshCategoryList();
        accountId = this.getIntent().getIntExtra("accountId", -1);
        int categoryId = this.getIntent().getIntExtra("categoryId", -1);
        if(categoryId > 0)
            categorySpinner.setSelection(categoryId-1);
        transaction = (Transaction) this.getIntent().getSerializableExtra("transaction");
        if(transaction != null){
            amountEditBox.setText(Double.toString(Math.abs(transaction.getAmount())));
            descriptionEditBox.setText(transaction.getDescription());
            calendar = transaction.getDate();
            updateDateTime();
            categorySpinner.setSelection(categoryItems.indexOf(transaction.getCategory()));
            setTitle("Edit transaction");
        }else
            setTitle("New transaction");
        
        super.onResume();
    }
    
    private void refreshCategoryList(){
        categoryItems.clear();       
        categoryItems.addAll(Cache.getCategories());
        categoryAdapter.notifyDataSetChanged();
    }
    
    private void updateDateTime(){
        final Date date = calendar.getTime();
        dateButton.setText(DateFormat.getMediumDateFormat(this).format(date));
        timeButton.setText(DateFormat.getTimeFormat(this).format(date));
    }

}
