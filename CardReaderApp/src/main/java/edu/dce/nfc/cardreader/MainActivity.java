package edu.dce.nfc.cardreader;

import android.graphics.Color;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
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
    public ReceiverState mReceiverState;
    private EditText mTextView;
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
            if (mReceiverState == ReceiverState.OPENDOOR) {
                System.out.println("SENDING OPENDOOR SETTINGS");
                command = "OPENDOOR";
                successString = ReturnStrings.SUCCESS_OPEN_ROOM;
            }
            else if (mReceiverState == ReceiverState.ROOMCHARGE) {
                System.out.println("SENDING ROOMCHARGE SETTINGS");
                command = "ROOMCHARGE";
                String amount = mTextView.getText().toString();
                command = command + amount;
                successString = ReturnStrings.SUCCESS_ROOM_CHARGE;
            }
            else if (mReceiverState == ReceiverState.CHECKOUT) {
                System.out.println("SENDING CHECKOUT SETTINGS");
                command = "CHECKOUT";
                successString = ReturnStrings.SUCCESS_CHECKOUT;
            }
            else {
                System.out.println("going default");
                command = "CHECKIN";
                successString = ReturnStrings.SUCCESS_CHECK_IN;
            }


            result = transactNfc(isoDep, command);
            if (result.equals(successString)) {
                handleSuccess(command);
                System.out.println("Successful " + command);
            } else {
                System.out.println("Errored");
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
        mTextView = (EditText) findViewById(R.id.amountText);
        mTextView.setVisibility(View.INVISIBLE);
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
        if (id == R.id.check_in) {
            mReceiverState = ReceiverState.CHECKIN;
            mTextView.setVisibility(View.INVISIBLE);
        }
        else if (id == R.id.open_door) {
            mReceiverState = ReceiverState.OPENDOOR;
            mTextView.setVisibility(View.INVISIBLE);
        }
        else if (id == R.id.room_charge) {
            mReceiverState = ReceiverState.ROOMCHARGE;
            mTextView.setVisibility(View.VISIBLE);
        }
        else {
            mReceiverState = ReceiverState.CHECKOUT;
            mTextView.setVisibility(View.INVISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleError(String error) {
        final String finalErr = error;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.setVisibility(View.INVISIBLE);
                MainActivity.this.findViewById(android.R.id.content).setBackgroundColor(Color.RED);
                final TextView t = new TextView(MainActivity.this);
                t.setGravity(Gravity.CENTER);
                t.setText(finalErr);
                t.setTextColor(Color.WHITE);
                t.setTextSize(35);
                mLayout.addView(t);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.findViewById(android.R.id.content).setBackgroundColor(Color.WHITE);
                        mLayout.removeView(t);
                        if (mReceiverState == ReceiverState.ROOMCHARGE) {
                            mTextView.setVisibility(View.VISIBLE);
                        }
                    }
                }, 2000);

            }
        });
    }

    private void handleSuccess(String command) {
        final String finalCmd = command;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.findViewById(android.R.id.content).setBackgroundColor(Color.GREEN);
                final TextView t = new TextView(MainActivity.this);
                t.setGravity(Gravity.CENTER);
                t.setText("SUCCESSFUL " + finalCmd);
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
        });
    }

    public enum ReceiverState {
        CHECKIN,
        OPENDOOR,
        ROOMCHARGE,
        CHECKOUT
    }
}
