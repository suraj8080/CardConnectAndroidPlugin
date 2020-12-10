package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bolt.ccconsumersdk.BuildConfig;
import com.bolt.consumersdk.CCConsumer;
import com.bolt.consumersdk.androidpay.CCConsumerAndroidPayActivity;
import com.bolt.consumersdk.androidpay.CCConsumerAndroidPayConfiguration;
import com.bolt.consumersdk.domain.CCConsumerAccount;
import com.bolt.consumersdk.domain.response.CCConsumerApiAndroidPayTokenResponse;
import com.bolt.consumersdk.listeners.BluetoothSearchResponseListener;
import com.bolt.consumersdk.network.CCConsumerApi;
import com.bolt.consumersdk.swiper.enums.SwiperType;
import com.bolt.consumersdk.views.payment.accounts.PaymentAccountsActivity;
import com.evontech.cardconnectdemo.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends BaseActivity implements View.OnClickListener, SwiperTestFragment.TokenListner {
    private int REQUEST_PERMISSIONS = 1000;
    private Button m_btnSelectDevice = null;
    private Button m_btnCustomFlow = null;
    private Button m_btnIntegratedFlow = null;
    private Button m_btnShowActivityWithFragment = null;
    private TextView m_txtvVersion = null;
    private LinearLayout m_llSearching = null;
    private ArrayAdapter<String> arrayAdapter = null;
    private ArrayAdapter<String> deviceListAdapter = null;
    private Map<String, BluetoothDevice> mapDevices = Collections.synchronizedMap(new HashMap<String, BluetoothDevice>());

    private View.OnClickListener mOnClickListener = null;
    private AdapterView.OnItemClickListener mOnItemClickListener = null;
    private BluetoothSearchResponseListener mBluetoothSearchResponseListener = null;
    private AlertDialog alertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            String package_name = getApplication().getPackageName();
            setContentView(getApplication().getResources().getIdentifier("activity_main", "layout", package_name));
            //setContentView(R.layout.activity_main);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            //setupToolBar();
            setupListeners();
            setupViews();

            ImageView btn_close = (ImageView) findViewById(R.id.btn_close);
            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("token", "123456789");
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                    finish();
                }
            });
    }

    private void setupListeners() {

        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.activity_main_btnSelectDevice:
                        showSelectDeviceDialog();
                        break;
                    case R.id.button_custom_flow:
                        startCustomFlowActivity();
                        break;
                    case R.id.button_integrated_flow:
                        startCustomFlowActivity();
                        break;
                    case R.id.button_show_activity_with_fragment:
                        Intent swiperActivity = new Intent(MainActivity.this, SwiperTestActivity.class);
                        startActivity(swiperActivity);
                        break;
                }
            }
        };

        mOnItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CCConsumerApi api = CCConsumer.getInstance().getApi();

                if (parent.getId() == R.id.dialog_select_device_lvDeviceType) {
                    switch (SwiperType.values()[position]) {
                        case BBPosDevice:
                            SwiperControllerManager.getInstance().setSwiperType(SwiperType.BBPosDevice);
                            updateDeviceButtonTitle();
                            alertDialog.dismiss();
                            break;
                        case IDTech:
                            mapDevices.clear();
                            m_llSearching.setVisibility(View.VISIBLE);
                            api.startBluetoothDeviceSearch(mBluetoothSearchResponseListener, MainActivity.this, false);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    m_llSearching.setVisibility(View.INVISIBLE);
                                }
                            }, 35000);
                            break;
                    }
                } else if (parent.getId() == R.id.dialog_select_device_lvDevicesFound) {
                    String str = deviceListAdapter.getItem(position);
                    BluetoothDevice ble = mapDevices.get(str);

                    if (ble == null){
                        for(BluetoothDevice bd: mapDevices.values()){
                            if (str.equalsIgnoreCase(bd.getName())){
                                ble = bd;
                                break;
                            }
                        }
                    }

                    SwiperControllerManager.getInstance().setSwiperType(SwiperType.IDTech);
                    SwiperControllerManager.getInstance().setMACAddress(ble.getAddress());
                    updateDeviceButtonTitle();
                    alertDialog.dismiss();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame_layout_container, new SwiperTestFragment(), SwiperTestFragment.TAG)
                            .addToBackStack(SwiperTestFragment.TAG).commit();
                }
            }
        };
    }

    private void setupViews() {
        m_btnSelectDevice = (Button) findViewById(R.id.activity_main_btnSelectDevice);
        m_btnSelectDevice.setOnClickListener(mOnClickListener);
        updateDeviceButtonTitle();

        m_btnCustomFlow = (Button) findViewById(R.id.button_custom_flow);
        m_btnCustomFlow.setOnClickListener(mOnClickListener);

        m_btnIntegratedFlow = (Button) findViewById(R.id.button_integrated_flow);
        m_btnIntegratedFlow.setOnClickListener(mOnClickListener);

        m_btnShowActivityWithFragment = (Button) findViewById(R.id.button_show_activity_with_fragment);
        m_btnShowActivityWithFragment.setOnClickListener(mOnClickListener);

        m_txtvVersion = (TextView)findViewById(R.id.activity_main_txtvVersion);
        m_txtvVersion.setText("v" + BuildConfig.VERSION_NAME);
    }

    private void showSelectDeviceDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        dialogBuilder.setTitle("Select Device Type");
        View view = null;
        LayoutInflater inflater = null;
        ListView listView = null;
        ListView deviceListView = null;

        if (!checkPermission()){
            requestPermission();
            return;
        }

        arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_item);
        for (SwiperType type : SwiperType.values()) {
            arrayAdapter.add(type.toString());
        }

        deviceListAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_item);

        dialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog.dismiss();
            }
        });

        inflater = getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_select_device, null);

        listView = view.findViewById(R.id.dialog_select_device_lvDeviceType);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(mOnItemClickListener);

        deviceListView = view.findViewById(R.id.dialog_select_device_lvDevicesFound);
        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setOnItemClickListener(mOnItemClickListener);

        m_llSearching = (LinearLayout) view.findViewById(R.id.dialog_select_device_llSearching);
        m_llSearching.setVisibility(View.INVISIBLE);

        dialogBuilder.setView(view);
        alertDialog = dialogBuilder.show();
    }

    public void startCustomFlowActivity() {
        Intent intent = new Intent(this, CustomFlowActivity.class);
        intent.putExtra("MAC", SwiperControllerManager.getInstance().getMACAddr());
        startActivity(intent);
    }

    public void startIntegratedFlowActivity() {
        ApiBridgeImpl apiBridgeImpl = new ApiBridgeImpl();
        Intent intent = new Intent(this, PaymentAccountsActivity.class);
        intent.putExtra("MAC", SwiperControllerManager.getInstance().getMACAddr());
        intent.putExtra(PaymentAccountsActivity.API_BRIDGE_IMPL_KEY, apiBridgeImpl);

        // Android Pay Integration
        CCConsumerAndroidPayConfiguration.getInstance().setAndroidPayUiEnabled(true);
        CCConsumerAndroidPayConfiguration.getInstance().setMerchantName("Merchant Test Name");
        CCConsumerAndroidPayConfiguration.getInstance().setMerchantTransactionId(UUID.randomUUID().toString());
        intent.putExtra(CCConsumerAndroidPayActivity.ANDROID_PAY_TOTAL_AMOUNT_KEY, "5.00");

        startActivityForResult(intent, PaymentAccountsActivity.PAYMENT_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_custom_flow:
                startCustomFlowActivity();
                break;
            case R.id.button_integrated_flow:
                startIntegratedFlowActivity();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Get Account selected by integrated UI flow
        if (requestCode == PaymentAccountsActivity.PAYMENT_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            // Check for Android Pay selected Payment
            if (data.hasExtra(PaymentAccountsActivity.ANDROID_PAY_TOKEN_RESPONSE_KEY)) {
                CCConsumerApiAndroidPayTokenResponse response =
                        data.getParcelableExtra(PaymentAccountsActivity.ANDROID_PAY_TOKEN_RESPONSE_KEY);
                Toast.makeText(this, getString(R.string.selected_android_pay_account_text_format,
                        response.getGoogleTransactionId(), response.getToken()), Toast.LENGTH_SHORT).show();
            } else {
                // Example of displaying account selected
                CCConsumerAccount selectedAccount =
                        data.getParcelableExtra(PaymentAccountsActivity.PAYMENT_ACTIVITY_ACCOUNT_SELECTED);
                Toast.makeText(this, getString(R.string.selected_account_text_format, selectedAccount.getAccountType()
                        .toString(), selectedAccount.getLast4()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBluetoothSearchResponseListener = new BluetoothSearchResponseListener() {
            @Override
            public void onDeviceFound(BluetoothDevice device) {
                synchronized (mapDevices) {


                    mapDevices.put(device.getAddress(), device);

                    deviceListAdapter.clear();

                    for (BluetoothDevice dev : mapDevices.values()) {
                        if (TextUtils.isEmpty(dev.getName())) {
                            deviceListAdapter.add(dev.getAddress());
                        } else {
                            deviceListAdapter.add(dev.getName());
                        }
                    }

                    deviceListAdapter.notifyDataSetChanged();

                }
            }
        };
    }

    @Override
    protected void onPause() {
        CCConsumer.getInstance().getApi().removeBluetoothListener();

        super.onPause();
    }

    @Override
    protected void onStop() {
        CCConsumer.getInstance().getApi().removeBluetoothListener();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                    Toast.makeText(this, getPermissionDeniedString(permissions[i]), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public String getPermissionDeniedString(String str){
        String strResult = "";

        if (str.equals(Manifest.permission.ACCESS_FINE_LOCATION)){
            strResult = "Bluetooth permission denied, Unable to connect to bluetooth device.";
        } else if (str.equals(Manifest.permission.RECORD_AUDIO)){
            strResult = "Record Audio permission denied, Unable to connect to audio jack device.";
        }

        return strResult;
    }

    private void updateDeviceButtonTitle() {
        m_btnSelectDevice.setText("Current Device (" + SwiperControllerManager.getInstance().getSwiperType() + ")");
    }

    private Boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
    }


    @Override
    public void onTokenGenerated(String token) {
        SwiperControllerManager.getInstance().disconnectFromDevice();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("token", token);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}

