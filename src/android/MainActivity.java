package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
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
import com.bolt.consumersdk.domain.CCConsumerAccount;
import com.bolt.consumersdk.listeners.BluetoothSearchResponseListener;
import com.bolt.consumersdk.network.CCConsumerApi;
import com.bolt.consumersdk.swiper.enums.SwiperType;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity implements View.OnClickListener, SwiperTestFragment.TokenListner {
    private int REQUEST_PERMISSIONS = 1000;
    private Button m_btnSelectDevice = null;
    private Button m_btnCustomFlow = null;
    private Button m_btnSwiperInitilization = null;
    private TextView m_txtvVersion = null;
    private LinearLayout m_llSearching = null;
    private ArrayAdapter<String> arrayAdapter = null;
    private ArrayAdapter<String> deviceListAdapter = null;
    private Map<String, BluetoothDevice> mapDevices = Collections.synchronizedMap(new HashMap<String, BluetoothDevice>());

    private View.OnClickListener mOnClickListener = null;
    private AdapterView.OnItemClickListener mOnItemClickListener = null;
    private BluetoothSearchResponseListener mBluetoothSearchResponseListener = null;
    private AlertDialog alertDialog = null;
    public static CallbackContext mCallbackContext;
    private String package_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResourceId("activity_main", "layout"));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setupListeners();
            setupViews();
            ImageView btn_close = (ImageView) findViewById(getResourceId("btn_close", "id"));

            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closePaymentView();
                }
            });
    }

    private void setupListeners() {

        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==getResourceId("activity_main_btnSelectDevice", "id")){
                    showSelectDeviceDialog();
                }else if(v.getId()==getResourceId("button_custom_flow", "id")){
                    startCustomFlowActivity();
                }else if(v.getId()==getResourceId("button_swiper_initialization", "id")){
                    if(!TextUtils.isEmpty(SwiperControllerManager.getInstance().getMACAddr())) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(getResourceId("frame_layout_container","id"), new SwiperTestFragment(), SwiperTestFragment.TAG)
                                .addToBackStack(SwiperTestFragment.TAG).commit();
                    }else showSelectDeviceTypeDialog();
                }

                /*switch (v.getId()) {
                    case R.id.activity_main_btnSelectDevice:
                        showSelectDeviceDialog();
                        break;
                    case R.id.button_custom_flow:
                        startCustomFlowActivity();
                        break;
                    case R.id.button_swiper_initialization:
                        if(!TextUtils.isEmpty(SwiperControllerManager.getInstance().getMACAddr())) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(getResourceId("frame_layout_container","id"), new SwiperTestFragment(), SwiperTestFragment.TAG)
                                    .addToBackStack(SwiperTestFragment.TAG).commit();
                        }else showSelectDeviceTypeDialog();
                        break;
                }*/
            }
        };

        mOnItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CCConsumerApi api = CCConsumer.getInstance().getApi();

                if (parent.getId() == getResourceId("dialog_select_device_lvDeviceType","id")) {
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
                } else if (parent.getId() == getResourceId("dialog_select_device_lvDevicesFound", "id")) {
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
                }
            }
        };
    }

    private void setupViews() {
        m_btnSelectDevice = (Button) findViewById(getResourceId("activity_main_btnSelectDevice", "id"));
        m_btnSelectDevice.setOnClickListener(mOnClickListener);

        m_btnCustomFlow = (Button) findViewById(getResourceId("button_custom_flow", "id"));
        m_btnCustomFlow.setOnClickListener(mOnClickListener);


        m_btnSwiperInitilization = (Button) findViewById(getResourceId("button_swiper_initialization", "id"));
        m_btnSwiperInitilization.setOnClickListener(mOnClickListener);

        m_txtvVersion = (TextView)findViewById(getResourceId("activity_main_txtvVersion", "id"));
        m_txtvVersion.setText("v" + BuildConfig.VERSION_NAME);

        updateDeviceButtonTitle();
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
        view = inflater.inflate(getResourceId("dialog_select_device", "layout"), null);

        listView = view.findViewById(getResourceId("dialog_select_device_lvDeviceType", "id"));
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(mOnItemClickListener);

        deviceListView = view.findViewById(getResourceId("dialog_select_device_lvDevicesFound", "id"));
        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setOnItemClickListener(mOnItemClickListener);

        m_llSearching = (LinearLayout) view.findViewById(getResourceId("dialog_select_device_llSearching", "id"));
        m_llSearching.setVisibility(View.INVISIBLE);

        dialogBuilder.setView(view);
        alertDialog = dialogBuilder.show();
    }

    public void startCustomFlowActivity() {
        CustomFlowActivity.mCallbackContext = mCallbackContext;
        Intent intent = new Intent(this, CustomFlowActivity.class);
        intent.putExtra("MAC", SwiperControllerManager.getInstance().getMACAddr());
        startActivityForResult(intent,111);
    }

    @Override
    public void onClick(View v) {
        final int customFlowView = getResourceId("button_custom_flow", "id");
            if(v.getId() == customFlowView){
                startCustomFlowActivity();
            }
            /*switch (v.getId()) {
            case R.id.button_custom_flow:
                startCustomFlowActivity();
                break;
            }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("requestCode "+requestCode, "resultCode "+resultCode);
        if (requestCode == 111 ) {
            closePaymentView();
        }
        super.onActivityResult(requestCode, resultCode, data);
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

        updateDeviceButtonTitle();
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
        if(!TextUtils.isEmpty(SwiperControllerManager.getInstance().getMACAddr())) {
            //Log.d("MacAddr ", SwiperControllerManager.getInstance().getMACAddr());
            m_btnSelectDevice.setText("Current Device (" + SwiperControllerManager.getInstance().getSwiperType() + ")");
            m_btnSwiperInitilization.setVisibility(View.VISIBLE);
        }
        else {
            m_btnSelectDevice.setText("Select Device ");
            m_btnSwiperInitilization.setVisibility(View.GONE);
        }
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
    public void onTokenGenerated(CCConsumerAccount accountToken) {
        JSONObject responseJObject = new JSONObject();
        //Gson gson = new Gson();
        try {
            //String tokenObject = gson.toJson(accountToken);
            responseJObject.put("message", "No error");
            responseJObject.put("errorcode", 0);
            responseJObject.put("token", accountToken.getToken());
            responseJObject.put("expirationDate", accountToken.getExpirationDate());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PluginResult result = new PluginResult(PluginResult.Status.OK,responseJObject.toString());
        result.setKeepCallback(true);
        //Log.d("mCallbackContext Id ", mCallbackContext.getCallbackId());
        mCallbackContext.sendPluginResult(result);
        closePaymentView();
    }

    @Override
    public void onError(String errorMessage) {
        JSONObject responseJObject = new JSONObject();
        try {
            responseJObject.put("message", errorMessage);
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

    public void closePaymentView(){
        Log.d("closePaymentView ", "Called");
        CCConsumer.getInstance().getApi().removeBluetoothListener();
        SwiperControllerManager.getInstance().disconnectFromDevice();
        mBluetoothSearchResponseListener = null;
        try {
            if(getParent()!=null && getParent().getCurrentFocus()!=null)
                getParent().getCurrentFocus().clearFocus();
            finishAndRemoveTask();
        }catch (Exception e){
            finishAndRemoveTask();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onBackPressed() {
        closePaymentView();
    }

    @Override
    protected void onDestroy() {
        closePaymentView();
        super.onDestroy();
    }

}



