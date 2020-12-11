package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.app.Application;

import com.bolt.consumersdk.CCConsumer;
import com.bolt.consumersdk.network.CCConsumerApi;
import com.bolt.consumersdk.swiper.enums.SwiperType;

import org.apache.cordova.CallbackContext;

public class MainApp extends Application {

    private static String TAG = cordova.plugin.cardconnectplugin.cardconnectplugin.MainApp.class.getSimpleName();
    private static cordova.plugin.cardconnectplugin.cardconnectplugin.MainApp sAppContext;

    public static CCConsumerApi getConsumerApi() {
        return CCConsumer.getInstance().getApi();
    }

    public static cordova.plugin.cardconnectplugin.cardconnectplugin.MainApp getInstance() {
        return sAppContext;
    }

    public CallbackContext getCallbackContext() {
        return callbackContext;
    }

    public void setCallbackContext(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    private CallbackContext callbackContext;



    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = (cordova.plugin.cardconnectplugin.cardconnectplugin.MainApp) getApplicationContext();

        SwiperControllerManager.getInstance().setContext(sAppContext);
        SwiperControllerManager.getInstance().setSwiperType(SwiperType.BBPosDevice);
    }

    @Override
    public void onTerminate() {
        SwiperControllerManager.getInstance().disconnectFromDevice();
        super.onTerminate();
    }


}


