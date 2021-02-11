package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class GsonUtils {
    public static final String TAG = GsonUtils.class.getSimpleName();
    private static GsonUtils sInstance;
    private Gson mGson;

    private GsonUtils() {
    }

    @NonNull
    public static GsonUtils getInstance() {
        if (sInstance == null) {
            synchronized (GsonUtils.class) {
                if (sInstance == null) {
                    sInstance = new GsonUtils();
                    Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy HH:mm:ss")
                            .setPrettyPrinting().disableHtmlEscaping().create();
                    sInstance.setGson(gson);
                }
            }
        }
        return sInstance;
    }

    @NonNull
    public String toJson(@Nullable Object object) {
        if (object == null) {
            return "";
        }
        try {
            return mGson.toJson(object);
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
            return "";
        }
    }

    @Nullable
    public <T> T fromJson(String jsonString, Class<T> classOfT) {
        try {
            return mGson.fromJson(jsonString, classOfT);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            return null;
        }
    }

    @Nullable
    public <T> T fromJson(String jsonString, Type type) {
        try {
            return mGson.fromJson(jsonString, type);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            return null;
        }
    }

    private void setGson(Gson gson) {
        mGson = gson;
    }
}
