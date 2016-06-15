package com.packtpub.masteringopencvandroid.objectdetect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends Activity {

    public static final int MODE_SIFT   = 1;
    public static final int MODE_SURF   = 2;
    public static final int MODE_ORB    = 3;
    public static final int MODE_BRISK  = 4;
    public static final int MODE_FREAK  = 5;

    public static final String TAG = "ObjectDetect::HomeActivity";

    public static final String ACTION_MOE = "Action_Mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate is called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Button bSIFT, bSURF, bORB, bBRISK, bFREAK;
        bSIFT = (Button) findViewById(R.id.bSIFT);
        bSURF = (Button) findViewById(R.id.bSURF);
        bORB = (Button) findViewById(R.id.bORB);
        bBRISK = (Button) findViewById(R.id.bBRISK);
        bFREAK = (Button) findViewById(R.id.bFREAK);
        bSIFT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra(ACTION_MOE, MODE_SIFT);
                startActivity(i);
            }
        });
        bSURF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra(ACTION_MOE, MODE_SURF);
                startActivity(i);
            }
        });
        bORB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra(ACTION_MOE, MODE_ORB);
                startActivity(i);
            }
        });
        bBRISK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra(ACTION_MOE, MODE_BRISK);
                startActivity(i);
            }
        });
        bFREAK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra(ACTION_MOE, MODE_FREAK);
                startActivity(i);
            }
        });
    }
}
