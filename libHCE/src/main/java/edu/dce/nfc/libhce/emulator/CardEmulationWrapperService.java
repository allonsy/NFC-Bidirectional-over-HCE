package edu.dce.nfc.libhce.emulator;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.List;

import edu.dce.nfc.libhce.common.Headers;
import edu.dce.nfc.libhce.common.Utils;

/**
 * Created by championswimmer on 5/9/14.
 */
public abstract class CardEmulationWrapperService extends HostApduService {

    public static final String TAG = "libHCEEmulator";
    int sendCounter = 0;
    String[] results;
    String result;
    String command = "";

    @Override
    public byte[] processCommandApdu(byte[] bytes, Bundle bundle) {
        String s = Utils.ByteArrayToHexString(bytes);
        String utf = null;
        try {
            utf = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        Log.d(TAG, "processCommandApdu : " + s);
        String commandClass = s.substring(0, Math.min(s.length(), 8));
        String utfCmd = utf.substring(Math.min(utf.length(), 6));
//        System.out.println(utfCmd);
//        Log.d(TAG, "commandClass = " + commandClass);

        if (commandClass.equalsIgnoreCase(Headers.HEADER_SELECT)) {
            // This is select command
            return Utils.ConcatArrays(onCardSelect(s).getBytes(), Headers.RESPONSE_SELECT_OK);
        }

        else if (commandClass.equalsIgnoreCase(Headers.HEADER_SENDCOMMAND)) {
            try {
                Log.d(TAG, "catenated command  = " + new String(Utils.HexStringToByteArray(command)));
            } catch (Exception e) {

            }

            if (utf.contains("END_OF_COMMAND")) {
                Log.d(TAG, "received END_OF_COMMAND");
                results = Utils.StringSplit255(onReceiveCommand(command));
                command = "";
                return Utils.ConcatArrays("RESPONSE_SENDCOMMAND_PROCESSED".getBytes(), Headers.RESPONSE_SENDCOMMAND_PROCESSED);
            }
            command += utfCmd;

            return Utils.ConcatArrays("RESPONSE_SENDCOMMAND_OK".getBytes(), Headers.RESPONSE_SENDCOMMAND_OK);
        }

        else if (commandClass.equalsIgnoreCase(Headers.HEADER_GETDATA)) {

            result = sendResultPart(results);
            if (result.equalsIgnoreCase("END_OF_DATA")) {
                return Utils.ConcatArrays(result.getBytes(), Headers.RESPONSE_GETDATA_FINAL);
            }
            return Utils.ConcatArrays(result.getBytes(), Headers.RESPONSE_GETDATA_INTERMEDIATE);

        }




        return Utils.ConcatArrays(onReceiveCommand(s).getBytes(), Headers.RESPONSE_SELECT_OK);
    }

    @Override
    public void onDeactivated(int i) {

    }

    public String sendResultPart (String[] results) {
        int targetLength = 0;
        if (results != null) {
            targetLength = results.length;
        }
        if (sendCounter < targetLength) {
            sendCounter += 1;
            return results[sendCounter-1];
        } else {
            sendCounter = 0;
            return "END_OF_DATA";
        }

    }

    public abstract String onReceiveCommand(String command);
    public abstract String onCardSelect(String command);

}
