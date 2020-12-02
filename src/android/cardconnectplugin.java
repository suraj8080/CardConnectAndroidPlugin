package cordova.plugin.cardconnectplugin.cardconnectplugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bolt.consumersdk.CCConsumerTokenCallback;
import com.bolt.consumersdk.domain.CCConsumerAccount;
import com.bolt.consumersdk.domain.CCConsumerCardInfo;
import com.bolt.consumersdk.domain.CCConsumerError;
import com.bolt.consumersdk.swiper.SwiperControllerListener;

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
        if (action.equals("actionInitlisePayment")) {
            	String cardNumber = args.getString(0);
		String tokeniseUrl = args.getString(1)
	        String amount = args.getString(2);
	        String response = "Token for card number is "+cardNumber;
            	generateToken(tokensiseUrl, cardNumber, amount);
            return true;
        }
        return false;
    }

    public void generateToken(String tokeniseUrl, String cardNumber, String amount){
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
		this.responseInitlisePayment("Error in Generate token "+errorMessage, callbackContext);
            }

            @Override
            public void onCCConsumerTokenResponse(CCConsumerAccount consumerAccount) {
                //dismissProgressDialog();
                accountToken = consumerAccount.getToken();
                Log.d("accountToken ", accountToken);
		this.responseInitlisePayment("Account token is "+accountToken, callbackContext);
		
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

