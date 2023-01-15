package net.typeblog.socks;

import android.app.Activity;
import android.os.Bundle;

import net.typeblog.socks.util.Utility;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(String.format("%s v%s", getTitle(), BuildConfig.VERSION_NAME));

        ProfileFragment fragment = new ProfileFragment();
        fragment.setContext(getApplicationContext());
        this.getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }
}
