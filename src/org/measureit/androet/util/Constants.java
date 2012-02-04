package org.measureit.androet.util;

import android.graphics.Color;

/**
 *
 * @author ezsi
 */
public interface Constants {
    static final String LOG_NAME = "AndroET";
    
    static final String ACCOUNT_INCOME = "Income: ";
    static final String ACCOUNT_EXPENSE = "Expense: ";
    static final String ACCOUNT_CREATE_GROUP = "New group";
    static final String ACCOUNT_CREATE = "New account";
    static final String ACCOUNT_EDIT = "Edit account";
    static final String ACCOUNT_DISABLE = "Disable account";
    static final String ACCOUNT_SHOW_DISABLED = "Show disabled accounts";
    static final String ACCOUNT_ENABLE = "Enable account";
    static final String ACCOUNT_SHOW_ENABLED = "Show enabled accounts";
    static final String ACCOUNT_DELETE = "Delete account";
    static final String ACCOUNT_TRANSFER_MONEY = "Transfer money";
    static final String ACCOUNT_SETTINGS = "Settings";
    static final String ACCOUNT_SAVE_DB = "Save Database";
    static final String ACCOUNT_LOAD_DB = "Load Database";
    static final String ACCOUNT_REFRESH_CURRENCY_RATES = "Refresh rates";
    static final String ACCOUNT_CORRECT_BALANCE = "Correct balance";
    static final String TRANSACTION_ADD = "Add transaction";
    static final String TRANSACTION_DELETE = "Delete transaction";
    static final String BUTTON_OK = "OK";
    static final String BUTTON_CANCEL = "Cancel";
    static final String BUTTON_EXIT = "Exit";
    
    static final int TEXT_SIZE = 12;
    static final int HEADER_TEXT_SIZE = 18;
    static final int HEADER_TEXT_COLOR = Color.DKGRAY;
    static final int TEXT_COLOR = Color.LTGRAY;
    static final int HIGHLIGHT_COLOR = Color.rgb(255, 175, 0);
    static final int WARNING_COLOR = Color.RED;
}
