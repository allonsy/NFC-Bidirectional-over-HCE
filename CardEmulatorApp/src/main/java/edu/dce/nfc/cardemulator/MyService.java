package edu.dce.nfc.cardemulator;

import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import edu.dce.nfc.libhce.EmulatorService;
import edu.dce.nfc.libhce.common.Utils;

/**
 * Created by championswimmer on 6/10/14.
 */
public class MyService extends EmulatorService {
    public static final String TAG = "libHCEEmulator";

    @Override
    public String onReceiveCommand(String command) {
        String actualCommand = command;
        Log.i(TAG, "actual command = " + actualCommand);
        if (actualCommand.equals("OPENDOOR")) {
            String jsonString = "{\"command\": \"OPENDOOR\", \"room\": 1}";
            boolean result = initializeConnection("192.168.1.46", 3000, getResources().openRawResource(R.raw.client), jsonString);
            System.out.println ("SecureConnect returned a val of " + result);
            return "emulator sends back: " + result;
        } else {
            System.out.println("got unknown command");
            //need to return error
            return "DATA_BASED_ON_COMMAND";
        }
    }

    @Override
    public String onCardSelect(String command) {
        Log.i(TAG, "onCardSelect called with command = " + command);
        return "PATIENT_ID_HERE";
    }

    private boolean initializeConnection(String addr, int port, InputStream stream, String commandType) {
        try {
            SecureConnect secureConnect = new SecureConnect(addr, port, stream, commandType);
            secureConnect.start();
            secureConnect.join();
            return secureConnect.isSuccess();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }


}
