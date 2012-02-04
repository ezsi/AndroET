package org.measureit.androet.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.measureit.androet.db.Account;
import org.measureit.androet.db.Category;

/**
 *
 * @author ezsi
 */
public class Cache {
    private static final Map<Integer, Account> ACCOUNT_CACHE = new HashMap<Integer, Account>();
    
    private static final HashMap<Integer, Category> CATEGORY_CACHE = new HashMap<Integer, Category>();
    
    private static final List<Category> CATEGORY_LIST = new ArrayList<Category>();
    
    private static final Map<String, Currency> CURRENCY_CACHE = new HashMap<String, Currency>();
    
    private static final Comparator<Currency> CURRENCY_COMPARATOR = new Comparator<Currency>(){

        public int compare(Currency arg0, Currency arg1) {
            return arg0.getCurrencyCode().compareTo(arg1.getCurrencyCode());
        }
        
    };
    
    private static final String[] CURRENCY_CODES = { "JPY", "CNY", "SDG", "RON", "MKD", "MXN", "CAD",
    "ZAR", "AUD", "NOK", "ILS", "ISK", "SYP", "LYD", "UYU", "YER", "CSD",
    "EEK", "THB", "IDR", "LBP", "AED", "BOB", "QAR", "BHD", "HNL", "HRK",
    "COP", "ALL", "DKK", "MYR", "SEK", "RSD", "BGN", "DOP", "KRW", "LVL",
    "VEF", "CZK", "TND", "KWD", "VND", "JOD", "NZD", "PAB", "CLP", "PEN",
    "GBP", "DZD", "CHF", "RUB", "UAH", "ARS", "SAR", "EGP", "INR", "PYG",
    "TWD", "TRY", "BAM", "OMR", "SGD", "MAD", "BYR", "NIO", "HKD", "LTL",
    "SKK", "GTQ", "BRL", "EUR", "HUF", "IQD", "CRC", "PHP", "SVC", "PLN",
    "USD", // available end here
    "XBB", "XBC", "XBD", "UGX", "MOP", 
    "SHP", "TTD", "UYI", "KGS", "DJF", "BTN", "XBA", "HTG", "BBD", "XAU",
    "FKP", "MWK", "PGK", "XCD", "COU", "RWF", "NGN", "BSD", "XTS", "TMT",
    "GEL", "VUV", "FJD", "MVR", "AZN", "MNT", "MGA", "WST", "KMF", "GNF",
    "SBD", "BDT", "MMK", "TJS", "CVE", "MDL", "KES", "SRD", "LRD", "MUR",
    "CDF", "BMD", "USN", "CUP", "USS", "GMD", "UZS", "CUC", "ZMK", "NPR",
    "NAD", "LAK", "SZL", "XDR", "BND", "TZS", "MXV", "LSL", "KYD", "LKR",
    "ANG", "PKR", "SLL", "SCR", "GHS", "ERN", "BOV", "GIP", "IRR", "XPT",
    "BWP", "XFU", "CLF", "ETB", "STD", "XXX", "XPD", "AMD", "XPF", "JMD",
    "MRO", "BIF", "CHW", "ZWL", "AWG", "MZN", "CHE", "XOF", "KZT", "BZD",
    "XAG", "KHR", "XAF", "GYD", "AFN", "SOS", "TOP", "AOA", "KPW" };
    
    public static Account getAccount(int id){
        if(!ACCOUNT_CACHE.containsKey(id))
            reloadAccountCache();
        return ACCOUNT_CACHE.get(id);
    }
    
    private static void reloadAccountCache(){
        ACCOUNT_CACHE.clear();
        List<Account> accounts = Account.list(null);
        for(Account account : accounts)
            ACCOUNT_CACHE.put(account.getId(), account);
    }
    
    public static List<Category> getCategories(){
        if(CATEGORY_LIST.isEmpty())
            reloadCategoryCache();
        return CATEGORY_LIST;
    }
    
    public static Category getCategory(int id){
        if(!CATEGORY_CACHE.containsKey(id))
            reloadCategoryCache();
        return CATEGORY_CACHE.get(id);
    }
    
    public static Category getCategoryByName(String categoryName){
        List<Category> categories = Cache.getCategories();
        for(Category category : categories)
            if(category.getName().equals(categoryName))
                return category;
        return null;
    }
    
    private static void reloadCategoryCache(){
        CATEGORY_LIST.clear();
        CATEGORY_LIST.addAll(Category.list());
        for(Category category : CATEGORY_LIST)
            CATEGORY_CACHE.put(category.getId(), category);
    }
    
    public static Currency getCurrency(String currencyCode){
        if(!CURRENCY_CACHE.containsKey(currencyCode))
            reloadCurrencyCache();
        return CURRENCY_CACHE.get(currencyCode);
    }
    
    public static List<Currency> getCurrencies(){
        if(CURRENCY_CACHE.isEmpty())
            reloadCurrencyCache();
        List<Currency> currencies = new ArrayList<Currency>();
        currencies.addAll(CURRENCY_CACHE.values());
        Collections.sort(currencies, CURRENCY_COMPARATOR);
        return currencies;
    }
    
    private static void reloadCurrencyCache(){
        for(String currencyCode : CURRENCY_CODES)
            CURRENCY_CACHE.put(currencyCode, Currency.getInstance(currencyCode));
    }
       
}

