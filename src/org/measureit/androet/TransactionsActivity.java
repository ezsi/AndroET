package org.measureit.androet;

import org.measureit.androet.util.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import org.measureit.androet.db.Account;
import org.measureit.androet.db.Category;
import org.measureit.androet.db.Transaction;
import org.measureit.androet.ui.ActivitySwitch;
import org.measureit.androet.ui.TextViewBuilder;

//TODO: add category grouping

/**
 *
 * @author ezsi
 */
public class TransactionsActivity extends Activity {
    
    private final ArrayList<Transaction> listItems=new ArrayList<Transaction>();
    private ListView listView;
    private ArrayAdapter listAdapter;
    private Account account;
    private Transaction selectedTransaction;
    private DialogInterface.OnClickListener confirmDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which == DialogInterface.BUTTON_POSITIVE){
                Transaction.delete(selectedTransaction.getId());
                refreshTransactionList();
            }
        }
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listView = new ListView(this);
        listView.setBackgroundColor(Color.WHITE);
        listView.setCacheColorHint(Color.WHITE);
        registerForContextMenu(listView);
        setContentView(listView);
        listAdapter =  new TransactionAdapter(this,android.R.layout.simple_list_item_1 , listItems);
        listView.setAdapter(listAdapter);
    }
    
    @Override
    protected void onResume() {
        account = (Account) this.getIntent().getSerializableExtra("account");
        refreshTransactionList();
        super.onResume();
    }
    
    private void refreshTransactionList(){
        listItems.clear();       
        listItems.addAll(Transaction.list(account));
        if(listItems.isEmpty())
            listItems.add(new Transaction(0, account.getId(), new Category(-1, "Tap me long!", true), 0, "Tap me long!", Calendar.getInstance()));
        listAdapter.notifyDataSetChanged();
    }
    
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int listItemIndex = (int) ((AdapterContextMenuInfo)item.getMenuInfo()).id;
        selectedTransaction = (Transaction) listView.getItemAtPosition(listItemIndex);
        
        final String itemTitle = item.toString();
        if(Constants.TRANSACTION_DELETE.equals(itemTitle)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to delete this transaction?").setPositiveButton("Yes", confirmDialogClickListener)
                .setNegativeButton("No", confirmDialogClickListener).show();
            
        }else if(Constants.TRANSACTION_EDIT.equals(itemTitle)){
            ActivitySwitch.to(this, TransactionActivity.class)
                    .add("accountId", account.getId())
                    .add("transaction", selectedTransaction)
                    .execute();
        }else if(Constants.TRANSACTION_ADD.equals(itemTitle)){
            ActivitySwitch.to(this, TransactionActivity.class)
                    .add("accountId", account.getId())
                    .execute();
        }
        return super.onContextItemSelected(item);
    }

    private void showTransactionActivity(){
        Intent activity = new Intent(TransactionsActivity.this, TransactionActivity.class);
        activity.putExtra("accountId", account.getId());
        TransactionsActivity.this.startActivity(activity);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if(!account.isGroup())
            menu.add(Constants.TRANSACTION_ADD);
        menu.add(Constants.TRANSACTION_EDIT);
        menu.add(Constants.TRANSACTION_DELETE);
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    
    private class TransactionAdapter extends ArrayAdapter<Transaction> {

        private ArrayList<Transaction> items;

        public TransactionAdapter(Context context, int textViewResourceId, ArrayList<Transaction> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TableRow.LayoutParams cellLp = new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1.0f);
            cellLp.setMargins(2, 2, 2, 2);
            
            final TableLayout.LayoutParams rowParams = new TableLayout
                    .LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT);
            rowParams.setMargins(4, 2, 6, 2);
            
            Transaction tr = items.get(position);
            TableLayout tableLayout = new TableLayout(TransactionsActivity.this);
            
            TableRow row1 = new TableRow(TransactionsActivity.this);
            row1.setLayoutParams(rowParams);
            String dateText = DateFormat.format("MMM", tr.getDate()).toString(); // DateFormat.getDateFormat(TransactionsActivity.this).format(tr.getDate().getTime());
            row1.addView(TextViewBuilder.text(TransactionsActivity.this, dateText).gravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL).size(Constants.TEXT_SIZE).build());  
            row1.addView(TextViewBuilder.text(TransactionsActivity.this, "  "+tr.getCategory().getName()).gravity(Gravity.BOTTOM).size(Constants.HEADER_TEXT_SIZE).color(Color.DKGRAY).build());
            
            TextView amountTextView = TextViewBuilder.text(TransactionsActivity.this, account.getCurrency().getSymbol()
                    +" "+tr.getAmount()).size(Constants.TEXT_SIZE+2).color(Color.DKGRAY).gravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT).build();
            amountTextView.setLayoutParams(cellLp);
            row1.addView(amountTextView);
             
            TableRow row2 = new TableRow(TransactionsActivity.this);
            row2.setLayoutParams(rowParams);
            row2.addView(TextViewBuilder.text(TransactionsActivity.this, tr.getDate().get(Calendar.DATE)+".").gravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL).size(Constants.TEXT_SIZE).build());
            row2.addView(TextViewBuilder.text(TransactionsActivity.this, "   "+tr.getDescription()).size(Constants.TEXT_SIZE).color(Color.LTGRAY).build());
            row2.addView(TextViewBuilder.text(TransactionsActivity.this, "").build());
            
            tableLayout.addView(row1);
            tableLayout.addView(row2);
            return tableLayout;
        }
    }
    
}