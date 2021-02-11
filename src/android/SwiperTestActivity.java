package cordova.plugin.cardconnectplugin.cardconnectplugin;

import android.os.Bundle;
import android.view.View;


public class SwiperTestActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResourceId("activity_swiper_test", "layout"));
        //setupToolBar();
        findViewById(getResourceId("button_show_fragment_by_action", "id")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSupportFragmentManager().findFragmentByTag(SwiperTestFragment.TAG) == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(getResourceId("frame_layout_container", "id"), new SwiperTestFragment(), SwiperTestFragment.TAG)
                            .addToBackStack(SwiperTestFragment.TAG).commit();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
