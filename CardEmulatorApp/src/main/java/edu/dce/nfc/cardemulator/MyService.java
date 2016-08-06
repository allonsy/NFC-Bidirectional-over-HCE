package edu.dce.nfc.cardemulator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.Date;

import edu.dce.nfc.libhce.EmulatorService;
import edu.dce.nfc.libhce.common.ErrorStrings;

/**
 * Created by championswimmer on 6/10/14.
 */
public class MyService extends EmulatorService {
    public static final String TAG = "libHCEEmulator";


    @Override
    public String onReceiveCommand(String command) {
        String actualCommand = command;
        Log.i(TAG, "actual command = " + actualCommand);
        String result, jsonString;
        boolean jsonResult;
        try {
            if (actualCommand.equals("OPENDOOR")) {
                SharedPreferences settings = getSharedPreferences("My_Prefs", 0);
                int room = settings.getInt("ROOM", -1);
                int floor = settings.getInt("FLOOR", -1);
                if (room == -1 || floor == -1) {
                    return ErrorStrings.ERROR_NO_ROOM;
                }
                jsonString = "{\"command\": \"OPENDOOR\", \"floor\":" + floor + "\"room\":" + room + "}";
                result = initializeConnection("192.168.1.46", 3000, getResources().openRawResource(R.raw.client), jsonString);
                System.out.println("SecureConnect returned a val of " + result);
                if (result.equals(ErrorStrings.ERROR_SECURE_CONNECT)) {
                    return ErrorStrings.ERROR_SECURE_CONNECT;
                }
                JSONObject jsonObject = new JSONObject(result);
                if (parseJSON(jsonObject)){
                    return ErrorStrings.SUCCESS_OPEN_ROOM;
                } else {
                    return ErrorStrings.ERROR_OPEN_ROOM;
                }
            } else if (actualCommand.equals("CHECKIN")) {
                jsonString = "{\"command\": \"CHECKIN\"}";
                result = initializeConnection("192.168.1.46", 3000, getResources().openRawResource(R.raw.client), jsonString);
                System.out.println("SecureConnect returned a val of " + result);
                //Need to save room to shared preferences
                if (result.equals(ErrorStrings.ERROR_SECURE_CONNECT)) {
                    return ErrorStrings.ERROR_SECURE_CONNECT;
                }
                JSONObject jsonObject = new JSONObject(result);
                if (parseJSON(jsonObject)) {
                    SharedPreferences settings = getSharedPreferences("My_Prefs", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("FLOOR", jsonObject.getInt("floor"));
                    editor.putInt("ROOM", jsonObject.getInt("room"));
                    editor.commit();
                    return ErrorStrings.SUCCESS_CHECK_IN;
                } else {
                    return ErrorStrings.ERROR_CHECK_IN;
                }
                } else if (actualCommand.equals("ROOMCHARGE")) {
                jsonString = "{\"command\": \"ROOMCHARGE\", \"amount\": 1}";
                result = initializeConnection("192.168.1.46", 3000, getResources().openRawResource(R.raw.client), jsonString);
                System.out.println("SecureConnect returned a val of " + result);
                return "emulator sends back: " + result;
            } else {
                System.out.println("got unknown command");
                //need to return error
                return ErrorStrings.ERROR_UNKNOWN_COMMAND;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return ErrorStrings.ERROR_SECURE_CONNECT;
        }
    }

    @Override
    public String onCardSelect(String command) {
        Log.i(TAG, "onCardSelect called with command = " + command);
        return "PATIENT_ID_HERE";
    }

    private String initializeConnection(String addr, int port, InputStream stream, String commandType) {
        try {
            SecureConnect secureConnect = new SecureConnect(addr, port, stream, commandType);
            secureConnect.start();
            secureConnect.join();
            return secureConnect.isSuccess();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return ErrorStrings.ERROR_SECURE_CONNECT;
        }
    }


    private boolean parseJSON(JSONObject obj) {
        try {
            if (obj.getBoolean("success") == true) {
                return true;
            } else {
                System.out.println(obj.get("error"));
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }


}
