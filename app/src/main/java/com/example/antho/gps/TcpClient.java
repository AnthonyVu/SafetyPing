package com.example.antho.gps;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClient {

    public String SERVER_IP; // = "192.168.0.24"; //server IP address
    public int SERVER_PORT; // = 7000;
    public static final String TAG = "CLIENT_MESSAGE";
    // message to send to the server
    private String mServerMessage;
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    Log.d(TAG, "Sending: " + message);
                    mBufferOut.println(message + "\r\n");
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {

        mRun = true;

        try {
            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);

            try {
                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

}