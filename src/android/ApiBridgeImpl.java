package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.bolt.consumersdk.CCConsumerApiBridge;
import com.bolt.consumersdk.CCConsumerApiBridgeCallbacks;
import com.bolt.consumersdk.domain.CCConsumerAccount;
import com.bolt.consumersdk.domain.response.CCConsumerApiBridgeDeleteAccountResponse;
import com.bolt.consumersdk.domain.response.CCConsumerApiBridgeGetAccountsResponse;
import com.bolt.consumersdk.domain.response.CCConsumerApiBridgeSaveAccountResponse;
import com.bolt.consumersdk.domain.response.CCConsumerApiBridgeUpdateAccountResponse;

/**
 * Provides an example of {@link CCConsumerApiBridge} implementation
 */
public class ApiBridgeImpl implements CCConsumerApiBridge, Parcelable {
    // Parcelable implementation required fo passing this object to Consumer SDK
    public static final Creator<ApiBridgeImpl> CREATOR = new Creator<ApiBridgeImpl>() {
        @Override
        public ApiBridgeImpl createFromParcel(Parcel in) {
            return new ApiBridgeImpl(in);
        }

        @Override
        public ApiBridgeImpl[] newArray(int size) {
            return new ApiBridgeImpl[size];
        }
    };

    public ApiBridgeImpl() {
    }

    protected ApiBridgeImpl(Parcel in) {
        // Unused
    }

    @Override
    public void getAccounts(@NonNull final CCConsumerApiBridgeCallbacks apiBridgeCallbacks) {

        final CCConsumerApiBridgeGetAccountsResponse response = new CCConsumerApiBridgeGetAccountsResponse();
        // TODO: Implement getAccounts from Third party server here, this is just an example implementation
        response.setCCConsumerAccounts(SharedPreferenceHelper.getConsumerAccounts());
        // TODO: provide result through apiBridgeCallbacks object
        apiBridgeCallbacks.onApiResponse(response);
    }

    @Override
    public void saveAccountToCustomer(@NonNull final CCConsumerAccount account,
            @NonNull final CCConsumerApiBridgeCallbacks apiBridgeCallbacks) {

        final CCConsumerApiBridgeSaveAccountResponse response = new CCConsumerApiBridgeSaveAccountResponse();
        // TODO: Implement addAccount to Profile from Third party server here, this is just an example implementation
        account.setAccountId(String.valueOf(System.currentTimeMillis()));
        if (SharedPreferenceHelper.getConsumerAccounts().size() == 0) {
            account.setDefaultAccount(true);
        }
        SharedPreferenceHelper.saveAccount(account);
        // TODO: provide result through apiBridgeCallbacks object
        response.setCCConsumerAccount(account);
        apiBridgeCallbacks.onApiResponse(response);
    }

    @Override
    public void deleteCustomerAccount(@NonNull CCConsumerAccount accountToDelete,
            @NonNull final CCConsumerApiBridgeCallbacks apiBridgeCallbacks) {
        final CCConsumerApiBridgeDeleteAccountResponse response = new CCConsumerApiBridgeDeleteAccountResponse();
        // TODO: Implement removeAccount to Profile from Third party server here, this is just an example implementation
        SharedPreferenceHelper.deleteAccount(accountToDelete);
        // TODO: provide result through apiBridgeCallbacks object
        apiBridgeCallbacks.onApiResponse(response);
    }

    @Override
    public void updateAccount(@NonNull CCConsumerAccount account,
            @NonNull final CCConsumerApiBridgeCallbacks apiBridgeCallbacks) {

        final CCConsumerApiBridgeUpdateAccountResponse response = new CCConsumerApiBridgeUpdateAccountResponse();
        // TODO: Implement updateAccount to Profile from Third party server here, this is just an example implementation
        SharedPreferenceHelper.updateAccount(account);
        // TODO: provide result through apiBridgeCallbacks object
        response.setCCConsumerAccount(account);
        apiBridgeCallbacks.onApiResponse(response);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Unused
    }
}
