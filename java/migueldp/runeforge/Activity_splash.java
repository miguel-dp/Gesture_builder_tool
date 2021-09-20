package migueldp.runeforge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Activity_splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(Activity_splash.this, Activity_GesturesPanel.class));
        finish();

    }
}