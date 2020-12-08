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
import com.bolt.consumersdk.swiper.enums.SwiperCaptureMode;
import com.bolt.consumersdk.swiper.enums.SwiperError;
import com.evontech.cardconnectdemo.R;

public class SwiperTestFragment extends BaseFragment {
    public static final String TAG = SwiperTestFragment.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextView mConnectionStateTextView;
    private Switch mSwitchSwipeOrTap;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = null;
    private boolean bConnected = false;
    private SwiperControllerListener mSwiperControllerListener = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_swiper_test, container, false);

        setupListeners();

        mConnectionStateTextView = (TextView) v.findViewById(R.id.text_view_connection);
        mConnectionStateTextView.setText("Attempting to Connect .");

        mSwitchSwipeOrTap = (Switch) v.findViewById(R.id.fragment_swiper_test_switchSwipeORTap);
        mSwitchSwipeOrTap.setOnCheckedChangeListener(mOnCheckedChangeListener);


        requestRecordAudioPermission();
        updateConnectionProgress();



        return v;
    }

    private void setupListeners() {
        mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
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
        };

        mSwiperControllerListener = new SwiperControllerListener() {
            @Override
            public void onTokenGenerated(CCConsumerAccount account, CCConsumerError error) {
                Log.d(TAG, "onTokenGenerated");
                dismissProgressDialog();
                if (error == null) {
                    Log.d(TAG, "Token Generated");
                    showSnackBarMessage("Token Generated: " + account.getToken());
                    mConnectionStateTextView.setText(mConnectionStateTextView.getText() + "\r\n" + "Token Generated: " + account.getToken());
                } else {
                    showErrorDialog(error.getResponseMessage());
                }
            }

            @Override
            public void onError(SwiperError swipeError) {
                Log.d(TAG, swipeError.toString());
            }

            @Override
            public void onSwiperReadyForCard() {
                Log.d(TAG, "Swiper ready for card");
                showSnackBarMessage(getString(R.string.ready_for_swipe));
            }

            @Override
            public void onSwiperConnected() {
                Log.d(TAG, "Swiper connected");
                mConnectionStateTextView.setText(R.string.connected);
                bConnected = true;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSwitchText();
                        resetSwiper();
                    }
                }, 2000);

                mSwitchSwipeOrTap.setEnabled(true);
            }

            @Override
            public void onSwiperDisconnected() {
                Log.d(TAG, "Swiper disconnected");
                mConnectionStateTextView.setText(R.string.disconnected);
            }

            @Override
            public void onBatteryState(BatteryState batteryState) {
                Log.d(TAG, batteryState.toString());
                switch (batteryState){
                    case LOW:
                        showSnackBarMessage("Battery is low!");
                        break;
                    case CRITICALLY_LOW:
                        showSnackBarMessage("Battery is critically low!");
                        break;
                }
            }

            @Override
            public void onStartTokenGeneration() {
                showProgressDialog();
                Log.d(TAG, "Token Generation started.");
            }

            @Override
            public void onLogUpdate(String strLogUpdate) {
                mConnectionStateTextView.setText(mConnectionStateTextView.getText() + "\r\n" + strLogUpdate);
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
                mConnectionStateTextView.setText(mConnectionStateTextView.getText() + "\r\n" + str);
            }

            @Override
            public void onRemoveCardRequested() {
                showRemoveCardDialog();
            }

            @Override
            public void onCardRemoved() {
                hideRemoveCardDialog();
                resetSwiper();
            }
        };
        SwiperControllerManager.getInstance().setSwiperControllerListener(mSwiperControllerListener);
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
            mSwiperControllerListener.onSwiperConnected();
        } else {
            SwiperControllerManager.getInstance().connectToDevice();
            updateConnectionProgress();
        }

        switch (SwiperControllerManager.getInstance().getSwiperType()) {
            case BBPosDevice:
                Log.d("Type ", "BBPosDevice");
                mSwitchSwipeOrTap.setVisibility(View.GONE);
                break;
            case IDTech:
                mSwitchSwipeOrTap.setVisibility(View.VISIBLE);
                Log.d("Type ", "IDTech");
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateConnectionProgress() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!bConnected) {
                    mConnectionStateTextView.setText(mConnectionStateTextView.getText() + ".");
                    updateConnectionProgress();
                }
            }
        }, 2000);
    }
}