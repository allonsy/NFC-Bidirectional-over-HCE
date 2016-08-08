package edu.dce.nfc.cardemulator;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;

import edu.dce.nfc.libhce.EmulatorService;
import edu.dce.nfc.libhce.common.ReturnStrings;
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
        String result, jsonString;
        int commandLength = 10;
        if (actualCommand.length()<10) {
            commandLength = actualCommand.length();
        }
        try {
            if (actualCommand.equals("CHECKIN")) {
                jsonString = "{\"command\": \"CHECKIN\"}";
                result = initializeConnection(Utils.SERVER_IP_ADDRESS, Utils.SERVER_PORT, getResources().openRawResource(R.raw.client), jsonString);
                System.out.println("SecureConnect returned a val of " + result);
                if (result.equals(ReturnStrings.ERROR_SECURE_CONNECT)) {
                    return ReturnStrings.ERROR_SECURE_CONNECT;
                }
                JSONObject jsonObject = new JSONObject(result);
                if (parseJSON(jsonObject)) {
                    SharedPreferences settings = getSharedPreferences("My_Prefs", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("FLOOR", jsonObject.getInt("floor"));
                    editor.putInt("ROOM", jsonObject.getInt("room"));
                    editor.commit();
                    return ReturnStrings.SUCCESS_CHECK_IN;
                }
                return getJSONError(jsonObject);
            } else if (actualCommand.equals("OPENDOOR")) {
                SharedPreferences settings = getSharedPreferences("My_Prefs", 0);
                int room = settings.getInt("ROOM", -1);
                int floor = settings.getInt("FLOOR", -1);
                if (room == -1 || floor == -1) {
                    return ReturnStrings.ERROR_NO_ROOM;
                }
                jsonString = "{\"command\": \"OPENDOOR\", \"floor\":" + floor + ",\"room\":" + room + "}";
                result = initializeConnection(Utils.SERVER_IP_ADDRESS, Utils.SERVER_PORT, getResources().openRawResource(R.raw.client), jsonString);
                System.out.println("SecureConnect returned a val of " + result);
                if (result.equals(ReturnStrings.ERROR_SECURE_CONNECT)) {
                    return ReturnStrings.ERROR_SECURE_CONNECT;
                }
                JSONObject jsonObject = new JSONObject(result);
                if (parseJSON(jsonObject)){
                    return ReturnStrings.SUCCESS_OPEN_ROOM;
                }
                return getJSONError(jsonObject);
            } else if (actualCommand.substring(0, commandLength).equals("ROOMCHARGE")) {
                SharedPreferences settings = getSharedPreferences("My_Prefs", 0);
                int amount = 0;
                try {
                    amount = Integer.parseInt(actualCommand.substring(10));
                }
                catch(NumberFormatException e) {
                    return ReturnStrings.ERROR_INVALID_AMOUNT;
                }
                jsonString = "{\"command\": \"ROOMCHARGE\", \"amount\": " + amount + "}";
                result = initializeConnection(Utils.SERVER_IP_ADDRESS, Utils.SERVER_PORT, getResources().openRawResource(R.raw.client), jsonString);
                System.out.println("SecureConnect returned a val of " + result);
                if (result.equals(ReturnStrings.ERROR_SECURE_CONNECT)) {
                    return ReturnStrings.ERROR_SECURE_CONNECT;
                }
                JSONObject jsonObject = new JSONObject(result);
                if (parseJSON(jsonObject)) {
                    return ReturnStrings.SUCCESS_ROOM_CHARGE;
                }
                return getJSONError(jsonObject);
            } else if (actualCommand.equals("CHECKOUT")) {
                jsonString = "{\"command\": \"CHECKOUT\"}";
                result = initializeConnection(Utils.SERVER_IP_ADDRESS, Utils.SERVER_PORT, getResources().openRawResource(R.raw.client), jsonString);
                if (result.equals((ReturnStrings.ERROR_SECURE_CONNECT))) {
                    return ReturnStrings.ERROR_SECURE_CONNECT;
                }
                JSONObject jsonObject = new JSONObject(result);
                if (parseJSON(jsonObject)) {
                    return ReturnStrings.SUCCESS_CHECKOUT;
                }
                return getJSONError(jsonObject);
            } else {
                System.out.println("got unknown command");
                return ReturnStrings.ERROR_UNKNOWN_COMMAND;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return ReturnStrings.ERROR_SECURE_CONNECT;
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
            return ReturnStrings.ERROR_SECURE_CONNECT;
        }
    }


    private boolean parseJSON(JSONObject obj) throws JSONException {
        if (obj.getBoolean("success") == true) {
            return true;
        } else {
            System.out.println("ERROR: " + obj.get("error"));
            return false;
        }
    }

    private String getJSONError(JSONObject obj) throws JSONException{
        return obj.getString("error");
    }


}
