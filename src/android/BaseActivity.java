package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;


public abstract class BaseActivity extends AppCompatActivity {
    AlertDialog mDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (this instanceof CustomFlowActivity && getResources().getBoolean(R.bool.is_phone)) {
        if (this instanceof CustomFlowActivity && getResources().getBoolean(getResourceId("is_phone","bool"))) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    protected void showProgressDialog() {
        if (getSupportFragmentManager().findFragmentByTag(SpinningDialogFragment.TAG) == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(SpinningDialogFragment.newInstance(), SpinningDialogFragment.TAG);
            transaction.commitAllowingStateLoss();
        }
    }

    protected void setupToolBar() {
        //Toolbar toolbar = (Toolbar)findViewById(R.id.tool_bar);
        Toolbar toolbar = (Toolbar)findViewById(getResourceId("tool_bar","id"));
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (!(this instanceof MainActivity) && getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            setChangeUrlAction();
        }
    }

    protected void showSnackMessage(@NonNull String message) {
        Snackbar.make(this.findViewById(android.R.id.content), message,
                Snackbar.LENGTH_SHORT).show();
    }

    protected void dismissProgressDialog() {
        SpinningDialogFragment fragment =
                (SpinningDialogFragment)getSupportFragmentManager().findFragmentByTag(SpinningDialogFragment.TAG);

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }

    protected void showErrorDialog(@NonNull String errorMessage) {
        new AlertDialog.Builder(this).setTitle(getString(getResourceId("error", "string"))).setMessage(errorMessage)  //R.string.error
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    private void setChangeUrlAction() {
        //TextView changeUrlTextView = (TextView)findViewById(R.id.text_view_change_url);
        TextView changeUrlTextView = (TextView)findViewById(getResourceId("text_view_change_url", "id"));
        if (changeUrlTextView != null) {
            changeUrlTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangeUrlDialog();
                }
            });
        }
    }

    private void showChangeUrlDialog() {
        UrlFragmentDialog urlFragmentDialog =
                (UrlFragmentDialog)getSupportFragmentManager().findFragmentByTag(UrlFragmentDialog.TAG);

        if (urlFragmentDialog != null) {
            getSupportFragmentManager().beginTransaction().remove(urlFragmentDialog).commit();
        }

        UrlFragmentDialog.newInstance().show(getSupportFragmentManager(), UrlFragmentDialog.TAG);
    }

    public void showRemoveCardDialog() {
        Log.d("showRemoveCardDialog", "called");
        if (mDialog == null) {
            mDialog = new AlertDialog.Builder(this).setTitle("Please Remove Card").setMessage("Please Remove Card")
                    .setCancelable(false)
                    .create();
            mDialog.show();
        }
    }

    public void showSelectDeviceTypeDialog() {
        if (mDialog == null) {
            mDialog = new AlertDialog.Builder(this).setTitle("Please Select Device ").setMessage("Select device to connect.")
                    .setCancelable(false)
                    .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDialog.dismiss();
                            mDialog = null;
                        }
                    })
                    .create();
            mDialog.show();
        }
    }

    public void hideRemoveCardDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    protected void onPause() {
       // Log.d("BaseActivity ", "onPause: ");
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
       // Log.d("BaseActivity ", "onDestroy: ");
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        super.onDestroy();
    }


    /*getResources().getIdentifier("btn_close", "id", package_name);  // String in strings.xml or an identifier of a UI element
    getResources().getIdentifier("activity_main", "layout", package_name); // for layout id
    getResources().getIdentifier("nameOfDrawable", "drawable", this.getPackageName()); //for drawable id*/
    public static int resId;
    public int getResourceId(String resourceName, String resourceType){
        // eg: getResources().getIdentifier("com.my.app:drawable/my_image", null, null); // another way
        String package_name = getApplication().getPackageName();
        //Log.d("package_name ", package_name);
        resId = getResources().getIdentifier(resourceName, resourceType, package_name);
        return resId;
    }
}


