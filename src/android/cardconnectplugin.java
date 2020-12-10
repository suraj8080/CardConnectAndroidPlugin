package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bolt.consumersdk.CCConsumerTokenCallback;
import com.bolt.consumersdk.domain.CCConsumerAccount;
import com.bolt.consumersdk.domain.CCConsumerCardInfo;
import com.bolt.consumersdk.domain.CCConsumerError;
import com.bolt.consumersdk.swiper.SwiperControllerListener;

import static android.app.Activity.RESULT_OK;

/**
 * This class echoes a string called from JavaScript.
 */
public class cardconnectplugin extends CordovaPlugin {

    private CCConsumerCardInfo mCCConsumerCardInfo;
    private SwiperControllerListener mSwiperControllerListener = null;
    private String accountToken;
    private String errorMessage;
    private CallbackContext PUBLIC_CALLBACKS = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        PUBLIC_CALLBACKS = callbackContext;
        if (action.equals("actionInitliseMannualPayment")) {
            String tokensiseUrl = args.getString(0);
		    String  cardNumber = args.getString(1);
	        String amount = args.getString(2);
	        String response = "Token for card number is "+cardNumber;
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    generateToken(callbackContext,tokensiseUrl, cardNumber, amount);
                }
            });
            return true;
        } 
	Context context = cordova.getActivity().getApplicationContext();
         if(action.equals("actionInitliseCardPayment")) {
            this.openMainActivity(context);
        }

        // Send no result, to execute the callbacks later
        PluginResult pluginResult = new  PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true); // Keep callback
        return true;

        //return false;
    }

     private void openMainActivity(Context context) {
         cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent("cordova.plugin.cardconnectplugin.cardconnectplugin.MainActivity");
                cordova.startActivityForResult((CordovaPlugin) cardconnectplugin.this, intent, 105);
            }
        });
    	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("requestCode "+requestCode, "resultCode: "+resultCode);
        if(resultCode == cordova.getActivity().RESULT_OK){
            accountToken = intent.getStringExtra("token");
                Log.d("onActivityResult ", "Account Token: " + accountToken);

            PluginResult resultado = new PluginResult(PluginResult.Status.OK, "this value will be sent to cordova");
            resultado.setKeepCallback(true);
           // PUBLIC_CALLBACKS.sendPluginResult(resultado);

             responseInitlisePayment("Account Token is: " + accountToken, PUBLIC_CALLBACKS);
                //cordova.getActivity().finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, intent);
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




