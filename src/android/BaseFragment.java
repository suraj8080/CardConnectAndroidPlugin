package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public abstract class BaseFragment extends Fragment {

    protected void showErrorDialog(@NonNull String errorMessage) {
        Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).showErrorDialog(errorMessage);
        }
    }

    protected void showSnackBarMessage(@NonNull String message) {
        Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).showSnackMessage(message);
        }
    }

    protected void showProgressDialog() {
        Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).showProgressDialog();
        }
    }

    protected void dismissProgressDialog() {
        Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).dismissProgressDialog();
        }
    }

    public static int resId;
    public int getResourceId(String resourceName, String resourceType){
        // eg: getResources().getIdentifier("com.my.app:drawable/my_image", null, null); // another way
        String package_name = getContext().getPackageName();
        //Log.d("package_name ", package_name);
        resId = getResources().getIdentifier(resourceName, resourceType, package_name);
        return resId;
    }
}
