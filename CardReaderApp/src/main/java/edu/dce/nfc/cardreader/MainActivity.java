package edu.dce.nfc.cardreader;

import android.graphics.Color;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Handler;

import java.io.IOException;

import edu.dce.nfc.libhce.ReaderActivity;
import edu.dce.nfc.libhce.common.ReturnStrings;


public class MainActivity extends ReaderActivity {

    public static int mMaxTransceiveLength;
    public static boolean mExtendedApduSupported;
    public static int mTimeout;
    LinearLayout mLayout;


    @Override
    public void onHceStarted(IsoDep isoDep) {
        Log.d(TAG, "onHCEStarted");

        //Check out support for extended APDU here if you want to
        mExtendedApduSupported = isoDep.isExtendedLengthApduSupported();
        mMaxTransceiveLength = isoDep.getMaxTransceiveLength();
        // Set and check timeout here if you want to
        mTimeout = isoDep.getTimeout();
        isoDep.setTimeout(3600);
        mTimeout = isoDep.getTimeout();

        Log.i(TAG,
                "  Extended APDU Supported = " + mExtendedApduSupported +
                "  Max Transceive Length = " + mMaxTransceiveLength +
                "  Timeout = " + mTimeout);

        try {
            String command, result, successString;

//            command = "CHECKIN";
//            successString = ReturnStrings.SUCCESS_CHECK_IN;

//            command = "OPENDOOR";
//            successString = ReturnStrings.SUCCESS_OPEN_ROOM;

//            command = "ROOMCHARGE";
//            successString = ReturnStrings.SUCCESS_ROOM_CHARGE;

            command = "CHECKOUT";
            successString = ReturnStrings.SUCCESS_CHECKOUT;


            result = transactNfc(isoDep, command);
            if (result.equals(successString)) {
                handleSuccess(command);
                System.out.println("Successful " + command);
            } else {
                handleError(result);
            }
            System.out.println("result is: " + result);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = (LinearLayout)findViewById(R.id.text_view_container);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "starting reader");
        enableReaderMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableReaderMode();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleError(String error) {
        this.findViewById(android.R.id.content).setBackgroundColor(Color.RED);
        final TextView t = new TextView(this);
        t.setGravity(Gravity.CENTER);
        t.setText(error);
        t.setTextColor(Color.WHITE);
        t.setTextSize(35);
        mLayout.addView(t);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.findViewById(android.R.id.content).setBackgroundColor(Color.WHITE);
                mLayout.removeView(t);
            }
        }, 2000);
    }

    private void handleSuccess(String command) {
        this.findViewById(android.R.id.content).setBackgroundColor(Color.GREEN);
        final TextView t = new TextView(this);
        t.setGravity(Gravity.CENTER);
        t.setText("SUCCESSFUL " + command);
        t.setTextColor(Color.BLACK);
        t.setTextSize(35);
        mLayout.addView(t);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.findViewById(android.R.id.content).setBackgroundColor(Color.WHITE);
                mLayout.removeView(t);
            }
        }, 2000);

    }
}
