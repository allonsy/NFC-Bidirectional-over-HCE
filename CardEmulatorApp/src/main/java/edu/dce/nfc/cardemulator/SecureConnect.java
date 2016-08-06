package edu.dce.nfc.cardemulator;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import edu.dce.nfc.libhce.common.ErrorStrings;

/**
 * Created by alec on 7/26/16.
 */
public class SecureConnect extends Thread {
    private String address;
    private int port;
    private InputStream keyStream;
    private SSLSocket socket;

    private String mSuccess;
    private String mCommandType;

    public SecureConnect(String addr, int p, InputStream ks, String commandType) {
        super();
        address = addr;
        port = p;
        keyStream = ks;
        mCommandType = commandType;
    }

    @Override
    public void run() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(keyStream, new char[]{});
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SSLContext context;
        try {
            context = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("no such algorithm: " + e);
            return;
        }
        try {
            TrustManager[] trustChain = {new CustomTrust()};
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(keyStore, new char[]{});
            KeyManager[] keys = kmf.getKeyManagers();
            context.init(keys, trustChain, null);
        } catch (KeyManagementException e) {
            System.out.println("key management error: " + e);
            mSuccess = ErrorStrings.ERROR_SECURE_CONNECT;
            return;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            mSuccess = ErrorStrings.ERROR_SECURE_CONNECT;
            return;
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        SocketFactory sf = context.getSocketFactory();
        try {
            socket = (SSLSocket) sf.createSocket(address, port);
            SSLSession s = socket.getSession();
            if(s.isValid()) {
                System.out.println("connected to server, writing command..");
                write(mCommandType.getBytes());
                byte[] received = new byte[1024];
                System.out.println("reading from server..");
                int writeLen = readSocket(received);
                if (writeLen != -1) {
                    String receivedString = new String(received, 0, writeLen, "UTF-8");
                    mSuccess = receivedString;
                } else {
                    System.out.println("failed to read..");
                    mSuccess = ErrorStrings.ERROR_SECURE_CONNECT;
                }
            }
            else {
                mSuccess = ErrorStrings.ERROR_SECURE_CONNECT;
                System.out.println("cannot connect to server");
            }
        } catch (IOException e) {
            mSuccess = ErrorStrings.ERROR_SECURE_CONNECT;
            e.printStackTrace();
            System.out.println("encountered error: " + e);
        }
        finally {
            closeSocket();
        }
    }

    public int readSocket(byte[] bytes) throws IOException {
        int writeLen = socket.getInputStream().read(bytes);
        if(writeLen == -1) { return writeLen; }
        bytes[writeLen-2] = 0;
        bytes[writeLen-1] = 0;
        return writeLen -2;
    }

    public void write(byte[] bytes) throws IOException {
        socket.getOutputStream().write(bytes);
        socket.getOutputStream().flush();
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String isSuccess() {
        return mSuccess;
    }


}