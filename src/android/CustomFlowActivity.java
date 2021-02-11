package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bolt.consumersdk.CCConsumerTokenCallback;
import com.bolt.consumersdk.domain.CCConsumerAccount;
import com.bolt.consumersdk.domain.CCConsumerCardInfo;
import com.bolt.consumersdk.domain.CCConsumerError;
import com.bolt.consumersdk.enums.CCConsumerMaskFormat;
import com.bolt.consumersdk.swiper.SwiperControllerListener;
import com.bolt.consumersdk.views.CCConsumerCreditCardNumberEditText;
import com.bolt.consumersdk.views.CCConsumerCvvEditText;
import com.bolt.consumersdk.views.CCConsumerExpirationDateEditText;
import com.bolt.consumersdk.views.CCConsumerUiTextChangeListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomFlowActivity extends BaseActivity {
    private static final String TAG = CustomFlowActivity.class.getName();
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private CCConsumerCreditCardNumberEditText mCardNumberEditText;
    private CCConsumerExpirationDateEditText mExpirationDateEditText;
    private CCConsumerCvvEditText mCvvEditText;
    private EditText mPostalCodeEditText;
    private CCConsumerCardInfo mCCConsumerCardInfo;
    private TextView mConnectionStatus;
    private SwiperControllerListener mSwiperControllerListener = null;
    public static CallbackContext mCallbackContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResourceId("activity_custom_flow", "layout"));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setupToolBar();
        setupListeners();
        mCardNumberEditText = (CCConsumerCreditCardNumberEditText)findViewById(getResourceId("text_edit_credit_card_number","id"));
        mCardNumberEditText.setCCConsumerMaskFormat(CCConsumerMaskFormat.FIRST_LAST_FOUR);
        mExpirationDateEditText =
                (CCConsumerExpirationDateEditText)findViewById(getResourceId("text_edit_credit_card_expiration_date","id"));
        mCvvEditText = (CCConsumerCvvEditText)findViewById(getResourceId("text_edit_credit_card_cvv", "id"));
        mPostalCodeEditText = (EditText)findViewById(getResourceId("text_edit_credit_card_postal_code", "id"));
        mConnectionStatus = (TextView)findViewById(getResourceId("text_view_connection_status","id"));
        mCCConsumerCardInfo = new CCConsumerCardInfo();
        mCardNumberEditText.setCreditCardTextChangeListener(
                new CCConsumerUiTextChangeListener() {
                    @Override
                    public void onTextChanged() {
                        // This callback will be used for displaying custom UI showing validation completion
                        if (!mCardNumberEditText.isCardNumberValid() && mCardNumberEditText.getText().length() != 0) {
                            mCardNumberEditText.setError(getString(getResourceId("card_not_valid", "string")));
                        } else {
                            mCardNumberEditText.setError(null);
                        }
                    }
                });

        mExpirationDateEditText.setExpirationDateTextChangeListener(new CCConsumerUiTextChangeListener() {
            @Override
            public void onTextChanged() {
                // This callback will be used for displaying custom UI showing validation completion
                if (!mExpirationDateEditText.isExpirationDateValid() &&
                        mExpirationDateEditText.getText().length() != 0) {
                    mExpirationDateEditText.setError(getString(getResourceId("date_not_valid", "string")));
                } else {
                    mExpirationDateEditText.setError(null);
                }
            }
        });

        mCvvEditText.setCvvTextChangeListener(new CCConsumerUiTextChangeListener() {
            @Override
            public void onTextChanged() {
                // This callback will be used for displaying custom UI showing validation completion
                if (!mCvvEditText.isCvvCodeValid() && mCvvEditText.getText().length() != 0) {
                    mCvvEditText.setError(getString(getResourceId("cvv_not_valid", "string")));
                } else {
                    mCvvEditText.setError(null);
                }
            }
        });

        ImageView btn_close = (ImageView) findViewById(getResourceId("btn_close","id"));
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //setupTabMaskOptions();
        // Request android permissions for Swiper
        requestRecordAudioPermission();
    }

    private void setupListeners(){
        SwiperControllerManager.getInstance().setSwiperControllerListener(mSwiperControllerListener);
    }

    private void setupTabMaskOptions() {
        TabLayout maskOptionsTabLayout = (TabLayout)findViewById(getResourceId("tab_layout_mask_options", "id"));
        maskOptionsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        mCardNumberEditText.setCCConsumerMaskFormat(CCConsumerMaskFormat.LAST_FOUR);
                        break;
                    case 1:
                        mCardNumberEditText.setCCConsumerMaskFormat(CCConsumerMaskFormat.FIRST_LAST_FOUR);
                        break;
                    case 2:
                        mCardNumberEditText.setCCConsumerMaskFormat(CCConsumerMaskFormat.CARD_MASK_LAST_FOUR);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Unused
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Update default preselection
                if (tab.getPosition() == 0) {
                    mCardNumberEditText.setCCConsumerMaskFormat(CCConsumerMaskFormat.LAST_FOUR);
                }
            }
        });
        TabLayout.Tab selectedTab = maskOptionsTabLayout.getTabAt(0);
        if (selectedTab != null) {
            selectedTab.select();
        }
    }

    public void generateToken(View view) {
        // If using Custom UI Card object needs to be populated from within the component.
        mCardNumberEditText.setCardNumberOnCardInfo(mCCConsumerCardInfo);
        mExpirationDateEditText.setExpirationDateOnCardInfo(mCCConsumerCardInfo);
        mCvvEditText.setCvvCodeOnCardInfo(mCCConsumerCardInfo);
        if (!TextUtils.isEmpty(mPostalCodeEditText.getText())) {
            mCCConsumerCardInfo.setPostalCode(mPostalCodeEditText.getText().toString());
        }
        if (!mCCConsumerCardInfo.isCardValid()) {
            showErrorDialog(getString(getResourceId("card_invalid", "string")));
            return;
        }
        showProgressDialog();
        //MainApp.getConsumerApi().setEndPoint("https://fts.cardconnect.com:6443");
        MainApp.getConsumerApi().generateAccountForCard(mCCConsumerCardInfo, new CCConsumerTokenCallback() {
            @Override
            public void onCCConsumerTokenResponseError(CCConsumerError error) {
                dismissProgressDialog();
                showErrorDialog(error.getResponseMessage());
                JSONObject responseJObject = new JSONObject();
                try {
                    responseJObject.put("message", error.getResponseMessage());
                    responseJObject.put("errorcode", 1);
                    responseJObject.put("token", null);
                    //Log.d("responseJObject ", responseJObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                PluginResult result = new PluginResult(PluginResult.Status.ERROR,responseJObject.toString());
                result.setKeepCallback(true);
                mCallbackContext.sendPluginResult(result);
            }

            @Override
            public void onCCConsumerTokenResponse(CCConsumerAccount consumerAccount) {
                dismissProgressDialog();
                //showSnackBarMessage(consumerAccount.getToken());
                mCardNumberEditText.getText().clear();
                mCvvEditText.getText().clear();
                mExpirationDateEditText.getText().clear();
                mPostalCodeEditText.getText().clear();

                JSONObject responseJObject = new JSONObject();
                //Gson gson = new Gson();
                try {
                    //String tokenObject = gson.toJson(accountToken);
                    responseJObject.put("message", "No error");
                    responseJObject.put("errorcode", 0);
                    responseJObject.put("token", consumerAccount.getToken());
                    responseJObject.put("expirationDate", consumerAccount.getExpirationDate());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                PluginResult result = new PluginResult(PluginResult.Status.OK,responseJObject.toString());
                result.setKeepCallback(true);
                //Log.d("mCallbackContext Id ", mCallbackContext.getCallbackId());
                mCallbackContext.sendPluginResult(result);
                finish();
                setResult(RESULT_OK);
            }
        });
    }

    private void showSnackBarMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), getString(getResourceId("token_generated_format","string"),
                message), Snackbar.LENGTH_SHORT).show();
    }

    private void requestRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }
}


