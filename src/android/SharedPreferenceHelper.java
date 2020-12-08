package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.content.Context;
import android.content.SharedPreferences;


import androidx.annotation.NonNull;

import com.bolt.consumersdk.domain.CCConsumerAccount;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Handles the local user information saved in the application
 */
public class SharedPreferenceHelper {
    public static final String TAG = SharedPreferenceHelper.class.getSimpleName();

    public static final String USER_LOCAL_SHARED_PREFERENCES = SharedPreferenceHelper.class.getSimpleName();

    private static final String USER_LOCAL_ACCOUNTS = "user_local_accounts";
    private static final String NOT_FOUND_DEFAULT_VALUE = "notfounddefaultvalue";

    private static SharedPreferences getSharedPreference() {
        return MainApp.getInstance().getSharedPreferences(USER_LOCAL_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static ArrayList<CCConsumerAccount> getConsumerAccounts() {
        SharedPreferences sharedPreferences = getSharedPreference();
        String result = sharedPreferences.getString(USER_LOCAL_ACCOUNTS, NOT_FOUND_DEFAULT_VALUE);

        if (result.equals(NOT_FOUND_DEFAULT_VALUE)) {
            return new ArrayList<>();
        } else {
            Type listType = new TypeToken<ArrayList<CCConsumerAccount>>() {
            }.getType();
            return (ArrayList<CCConsumerAccount>) GsonUtils.getInstance().fromJson(result, listType);
        }
    }

    public static void saveAccount(@NonNull CCConsumerAccount account) {
        ArrayList<CCConsumerAccount> accounts = getConsumerAccounts();
        SharedPreferences.Editor editor = getSharedPreference().edit();
        accounts.add(account);
        editor.putString(USER_LOCAL_ACCOUNTS, GsonUtils.getInstance().toJson(accounts));
        editor.apply();
    }

    public static void deleteAccount(@NonNull CCConsumerAccount account) {
        ArrayList<CCConsumerAccount> accounts = getConsumerAccounts();
        SharedPreferences.Editor editor = getSharedPreference().edit();
        Iterator<CCConsumerAccount> accountIterator = accounts.iterator();
        while (accountIterator.hasNext()) {
            if (accountIterator.next().getAccountId().equals(account.getAccountId())) {
                accountIterator.remove();
            }
        }
        editor.putString(USER_LOCAL_ACCOUNTS, GsonUtils.getInstance().toJson(accounts));
        editor.apply();
    }

    public static void updateAccount(@NonNull CCConsumerAccount account) {
        ArrayList<CCConsumerAccount> accounts = getConsumerAccounts();
        SharedPreferences.Editor editor = getSharedPreference().edit();

        for (CCConsumerAccount accountItem : accounts) {
            if (accountItem.getAccountId().equals(account.getAccountId())) {
                accountItem.setExpirationDate(account.getExpirationDate());
                accountItem.setDefaultAccount(account.isDefaultAccount());
            }
        }
        //Updating default account
        if (account.isDefaultAccount()) {
            for (CCConsumerAccount accountItem : accounts) {
                if (!account.getAccountId().equals(accountItem.getAccountId()) && accountItem.isDefaultAccount()) {
                    accountItem.setDefaultAccount(false);
                }
            }
        }
        editor.putString(USER_LOCAL_ACCOUNTS, GsonUtils.getInstance().toJson(accounts));
        editor.apply();
    }
}
