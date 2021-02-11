package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bolt.consumersdk.CCConsumer;

public class UrlFragmentDialog extends DialogFragment {
    public final static String TAG = UrlFragmentDialog.class.getName();

    public static UrlFragmentDialog newInstance() {
        return new UrlFragmentDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShowsDialog(true);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View dialogView = View.inflate(getActivity(), getResourceId("dialog_url", "layout"), null);
        final EditText urlEditText = (EditText)dialogView.findViewById(getResourceId("edit_text_url","id"));
        urlEditText.setText(CCConsumer.getInstance().getApi().getEndPoint());

        return new AlertDialog.Builder(getActivity()).setView(dialogView).setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CCConsumer.getInstance().getApi().setEndPoint(urlEditText.getText().toString());
                        dismiss();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        }).show();
    }

    public static int resId;
    public int getResourceId(String resourceName, String resourceType){
        // eg: getResources().getIdentifier("com.my.app:drawable/my_image", null, null); // another way
        String package_name = getContext().getPackageName();
        Log.d("package_name ", package_name);
        resId = getResources().getIdentifier(resourceName, resourceType, package_name);
        return resId;
    }
}
