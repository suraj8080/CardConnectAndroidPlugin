package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bolt.consumersdk.domain.CCConsumerAccount;
import com.bolt.consumersdk.domain.CCConsumerError;
import com.bolt.consumersdk.swiper.CCSwiperController;
import com.bolt.consumersdk.swiper.SwiperControllerListener;
import com.bolt.consumersdk.swiper.enums.BatteryState;
import com.bolt.consumersdk.swiper.enums.SwiperError;

public class SwiperTestFragment extends BaseFragment {
    public static final String TAG = SwiperTestFragment.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextView mConnectionStateTextView;
    private Switch mSwitchSwipeOrTap;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = null;
    private boolean bConnected = false;
    private SwiperControllerListener mSwiperControllerListener = null;

    public interface TokenListner{
        public void onTokenGenerated(CCConsumerAccount accountToken);
        public void onError(String errorMessage);
    }
    TokenListner tokenListner;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(getResourceId("fragment_swiper_test", "layout"), container, false);
        Activity activity = getActivity();
        try {
            tokenListner = (TokenListner)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
        setupListeners();
        mConnectionStateTextView = (TextView) v.findViewById(getResourceId("text_view_connection", "id"));
        mConnectionStateTextView.setText("Attempting to Connect .");
        mSwitchSwipeOrTap = (Switch) v.findViewById(getResourceId("fragment_swiper_test_switchSwipeORTap", "id"));
        mSwitchSwipeOrTap.setOnCheckedChangeListener(mOnCheckedChangeListener);
        requestRecordAudioPermission();
        updateConnectionProgress();

        return v;
    }

    private void setupListeners() {
        /*mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSwitchSwipeOrTap.setEnabled(false);
                mConnectionStateTextView.setText(mConnectionStateTextView.getText() + "\r\nSWITCHING MODES...");
                if (isChecked) {
                    SwiperControllerManager.getInstance().setSwiperCaptureMode(SwiperCaptureMode.SWIPE_TAP);
                } else {
                    SwiperControllerManager.getInstance().setSwiperCaptureMode(SwiperCaptureMode.SWIPE_INSERT);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSwitchText();
                    }
                }, 20000);
            }
        };*/

            mSwiperControllerListener = new SwiperControllerListener() {
                @Override
                public void onTokenGenerated(CCConsumerAccount account, CCConsumerError error) {
                    try {
                        Log.d(TAG, "onTokenGenerated");
                        dismissProgressDialog();
                        if (error == null) {
                            //showSnackBarMessage("Token Generated: " + account.getToken());
                            mConnectionStateTextView.setText(mConnectionStateTextView.getText() + "\r\n" + "Token Generated: ");
                            tokenListner.onTokenGenerated(account);
                        } else {
                            showErrorDialog(error.getResponseMessage());
                            tokenListner.onError(error.getResponseMessage());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(SwiperError swipeError) {
                    try {
                    Log.d(TAG, swipeError.toString());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSwiperReadyForCard() {
                    try {
                    Log.d(TAG, "Swiper ready for card");
                    if (getActivity() != null)
                        showSnackBarMessage(getString(getResourceId("ready_for_swipe", "string")));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSwiperConnected() {
                    try {
                    Log.d(TAG, "Swiper connected");
                    mConnectionStateTextView.setText(getResourceId("connected", "string"));
                    bConnected = true;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //updateSwitchText();
                            resetSwiper();
                        }
                    }, 2000);
                    mSwitchSwipeOrTap.setEnabled(true);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSwiperDisconnected() {
                    try {
                    Log.d(TAG, "Swiper disconnected");
                    mConnectionStateTextView.setText(getResourceId("disconnected", "string"));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onBatteryState(BatteryState batteryState) {
                    try{
                    Log.d(TAG, batteryState.toString());
                    switch (batteryState) {
                        case LOW:
                            showSnackBarMessage("Battery is low!");
                            break;
                        case CRITICALLY_LOW:
                            showSnackBarMessage("Battery is critically low!");
                            break;
                    }
                    }catch (Exception e){
                    e.printStackTrace();
                }
                }

                @Override
                public void onStartTokenGeneration() {
                    try{
                    if (getActivity() != null)
                        showProgressDialog();
                    Log.d(TAG, "Token Generation started.");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onLogUpdate(String strLogUpdate) {
                    try{
                    mConnectionStateTextView.setText(mConnectionStateTextView.getText() + "\r\n" + strLogUpdate);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDeviceConfigurationUpdate(String s) {

                }

                @Override
                public void onConfigurationProgressUpdate(double v) {

                }

                @Override
                public void onConfigurationComplete(boolean b) {

                }

                @Override
                public void onTimeout() {

                    //resetSwiper();
                }

                @Override
                public void onLCDDisplayUpdate(String str) {
                    try{
                    mConnectionStateTextView.setText(mConnectionStateTextView.getText() + "\r\n" + str);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onRemoveCardRequested() {
                    try {
                    if (getActivity() != null)
                        showRemoveCardDialog();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCardRemoved() {
                    try {
                        if (getActivity() != null)
                            hideRemoveCardDialog();
                        if (((CCSwiperController) SwiperControllerManager.getInstance().getSwiperController()) != null)
                            resetSwiper();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            try {
            SwiperControllerManager.getInstance().setSwiperControllerListener(mSwiperControllerListener);
            }catch (Exception e){
                e.printStackTrace();
            }
        }


    private void hideRemoveCardDialog() {
        Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).hideRemoveCardDialog();
        }
    }

    private void showRemoveCardDialog() {
        Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).showRemoveCardDialog();
        }
    }

    private void resetSwiper() {
        if(((CCSwiperController) SwiperControllerManager.getInstance().getSwiperController())!=null)
        ((CCSwiperController) SwiperControllerManager.getInstance().getSwiperController()).startReaders(SwiperControllerManager.getInstance().getSwiperCaptureMode());
    }

    private void updateSwitchText() {
        switch (SwiperControllerManager.getInstance().getSwiperCaptureMode()) {
            case SWIPE_INSERT:
                mSwitchSwipeOrTap.setText("Swipe/Dip Enabled");
                if (mSwitchSwipeOrTap.isChecked()) {
                    mSwitchSwipeOrTap.setChecked(false);
                }
                break;
            case SWIPE_TAP:
                mSwitchSwipeOrTap.setText("Tap Enabled");
                if (!mSwitchSwipeOrTap.isChecked()) {
                    mSwitchSwipeOrTap.setChecked(true);
                }
                break;
        }
        mSwitchSwipeOrTap.setEnabled(true);
    }

    private void requestRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            //initSwiperForTokenGeneration(MainApp.getInstance().getSwiperType());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //initSwiperForTokenGeneration(MainApp.getInstance().getSwiperType());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

            if (SwiperControllerManager.getInstance().isSwiperConnected()) {
                Log.d("swiperConnected ", "true");
                mSwiperControllerListener.onSwiperConnected();
            } else {
                SwiperControllerManager.getInstance().connectToDevice();
                updateConnectionProgress();
            }

            switch (SwiperControllerManager.getInstance().getSwiperType()) {
                case BBPosDevice:
                    Log.d("Type ", "BBPosDevice");
                   // mSwitchSwipeOrTap.setVisibility(View.GONE);
                    break;
                case IDTech:
                    //mSwitchSwipeOrTap.setVisibility(View.VISIBLE);
                    Log.d("Type ", "IDTech");
                    break;
            }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void updateConnectionProgress() {
        try {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!bConnected) {
                    mConnectionStateTextView.setText(mConnectionStateTextView.getText() + ".");
                    updateConnectionProgress();
                }
            }
        }, 2000);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}



