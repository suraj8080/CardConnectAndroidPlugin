package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bolt.consumersdk.CCConsumer;
import com.bolt.consumersdk.CCConsumerTokenCallback;
import com.bolt.consumersdk.domain.CCConsumerAccount;
import com.bolt.consumersdk.domain.CCConsumerCardInfo;
import com.bolt.consumersdk.domain.CCConsumerError;
import com.bolt.consumersdk.swiper.SwiperControllerListener;
import com.evontech.cardconnectdemo.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import static cordova.plugin.cardconnectplugin.cardconnectplugin.MainActivity.mCallbackContext;

/**
 * This class echoes a string called from JavaScript.
 */
public class cardconnectplugin extends CordovaPlugin {

    private CCConsumerCardInfo mCCConsumerCardInfo;
    private SwiperControllerListener mSwiperControllerListener = null;
    private String accountToken;
    private String errorMessage;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        MainApp.getInstance().setCallbackContext(callbackContext);
        mCallbackContext = callbackContext;
        Log.d("callbackContext ",  "  "+MainApp.getInstance().getCallbackContext());
        if (action.equals("actionInitliseMannualPayment")) {
            String tokensiseUrl = args.getString(0);
		    String  cardNumber = args.getString(1);
	        String amount = args.getString(2);
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    generateToken(callbackContext,tokensiseUrl, cardNumber, amount);
                }
            });
            return true;
        } else if(action.equals("actionInitliseCardPayment")) {
             String tokensiseUrl = args.getString(0);
             MainApp.getConsumerApi().setEndPoint(tokensiseUrl);
             Intent intent = new Intent("cordova.plugin.cardconnectplugin.cardconnectplugin.MainActivity");
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
             //cordova.startActivityForResult((CordovaPlugin) cardconnectplugin.this, intent, 105);
             cordova.getActivity().startActivity(intent);

            // Send no result, to execute the callbacks later
            PluginResult pluginResult = new  PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true); // Keep callback
            return true;
        } else if (action.equals("closePluginView")) {
            try {
                Activity activity = this.cordova.getActivity();
                activity.finish();
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 0));
            } catch (Exception e) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, 1));
            }
            return true;
        }

        return false;
    }

    public void generateToken(CallbackContext callbackContext, String tokeniseUrl, String cardNumber, String amount){
        SwiperControllerManager.getInstance().setSwiperControllerListener(mSwiperControllerListener);
        mCCConsumerCardInfo = new CCConsumerCardInfo();
        mCCConsumerCardInfo.setCardNumber(cardNumber);
        mCCConsumerCardInfo.setCvv("123");
        mCCConsumerCardInfo.setExpirationDate("09/23");
        //showProgressDialog();

        MainApp.getConsumerApi().setEndPoint(tokeniseUrl);
        MainApp.getConsumerApi().generateAccountForCard(mCCConsumerCardInfo, new CCConsumerTokenCallback() {
            @Override
            public void onCCConsumerTokenResponseError(CCConsumerError error) {
                errorMessage = error.getResponseMessage();
                Log.d("accountToken ", errorMessage);
		  responseInitlisePayment("Error in Generate token "+errorMessage, callbackContext);
            }

            @Override
            public void onCCConsumerTokenResponse(CCConsumerAccount consumerAccount) {
                //dismissProgressDialog();
                accountToken = consumerAccount.getToken();
                Log.d("accountToken ", accountToken);
		    responseInitlisePayment("Account token is "+accountToken, callbackContext);
		
            }
        });
    }	

    private void responseInitlisePayment(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}





