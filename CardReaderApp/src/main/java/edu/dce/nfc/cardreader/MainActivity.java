package edu.dce.nfc.cardreader;

import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;

import java.io.IOException;

import edu.dce.nfc.libhce.ReaderActivity;
import edu.dce.nfc.libhce.common.ReturnStrings;


public class MainActivity extends ReaderActivity {

    public static int mMaxTransceiveLength;
    public static boolean mExtendedApduSupported;
    public static int mTimeout;


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
/*            String result = transactNfc(isoDep, "CHECKIN");
            if (result.equals(ReturnStrings.SUCCESS_CHECK_IN)) {
                //TODO: display green
                System.out.println("Successful checkin");
            } else {
                handleError(result);
            }
*/
/*            String result = transactNfc(isoDep, "OPENDOOR");
            if (result.equals(ReturnStrings.SUCCESS_OPEN_ROOM)) {
                //TODO: display green
                System.out.println("Successful open door");
            } else {
                handleError(result);
            }
*/
/*            String result = transactNfc(isoDep, "ROOMCHARGE");
            if (result.equals(ReturnStrings.SUCCESS_ROOM_CHARGE)) {
                //TODO: display green
                System.out.println("Successful room charge");
            } else {
                handleError(result);
            }
*/
            String result = transactNfc(isoDep, "CHECKOUT");
            if (result.equals(ReturnStrings.SUCCESS_CHECKOUT)) {
                //TODO: display green
                System.out.println("Successful check out");
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
        //TODO: turn screen red
        System.out.println("ERROR: " + error);
    }
}
