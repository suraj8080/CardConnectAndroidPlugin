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
import com.evontech.cardconnectdemo.R;
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
        setContentView(R.layout.activity_custom_flow);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setupToolBar();
        setupListeners();
        mCardNumberEditText = (CCConsumerCreditCardNumberEditText)findViewById(R.id.text_edit_credit_card_number);
        mCardNumberEditText.setCCConsumerMaskFormat(CCConsumerMaskFormat.FIRST_LAST_FOUR);
        mExpirationDateEditText =
                (CCConsumerExpirationDateEditText)findViewById(R.id.text_edit_credit_card_expiration_date);
        mCvvEditText = (CCConsumerCvvEditText)findViewById(R.id.text_edit_credit_card_cvv);
        mPostalCodeEditText = (EditText)findViewById(R.id.text_edit_credit_card_postal_code);
        mConnectionStatus = (TextView)findViewById(R.id.text_view_connection_status);
        mCCConsumerCardInfo = new CCConsumerCardInfo();
        mCardNumberEditText.setCreditCardTextChangeListener(
                new CCConsumerUiTextChangeListener() {
                    @Override
                    public void onTextChanged() {
                        // This callback will be used for displaying custom UI showing validation completion
                        if (!mCardNumberEditText.isCardNumberValid() && mCardNumberEditText.getText().length() != 0) {
                            mCardNumberEditText.setError(getString(R.string.card_not_valid));
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
                    mExpirationDateEditText.setError(getString(R.string.date_not_valid));
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
                    mCvvEditText.setError(getString(R.string.cvv_not_valid));
                } else {
                    mCvvEditText.setError(null);
                }
            }
        });

        ImageView btn_close = (ImageView) findViewById(R.id.btn_close);
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
        TabLayout maskOptionsTabLayout = (TabLayout)findViewById(R.id.tab_layout_mask_options);
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
            showErrorDialog(getString(R.string.card_invalid));
            return;
        }

        showProgressDialog();

        //MainApp.getConsumerApi().setEndPoint("https://fts.cardconnect.com:6443");
        MainApp.getConsumerApi().generateAccountForCard(mCCConsumerCardInfo, new CCConsumerTokenCallback() {
            @Override
            public void onCCConsumerTokenResponseError(CCConsumerError error) {
                dismissProgressDialog();
                showErrorDialog(error.getResponseMessage());
            }

            @Override
            public void onCCConsumerTokenResponse(CCConsumerAccount consumerAccount) {
                dismissProgressDialog();
                showSnackBarMessage(consumerAccount.getToken());
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                PluginResult result = new PluginResult(PluginResult.Status.OK,responseJObject.toString());
                result.setKeepCallback(true);
                //Log.d("mCallbackContext Id ", mCallbackContext.getCallbackId());
                mCallbackContext.sendPluginResult(result);
            }
        });
    }

    private void showSnackBarMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.token_generated_format,
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

