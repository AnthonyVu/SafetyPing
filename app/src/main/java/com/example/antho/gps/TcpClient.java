package com.example.antho.gps;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


/*---------------------------------------------------------------------------------------
--	Source File:    TcpClient.java - A simple Java TCP client
--
--	Methods:
--          sendMessage(fina String message)
--          stopClient()
--          run()
--
--	Date:			April 3, 2018
--
--	Revisions:		(Date and Description)
--                April 3, 2018
--                Initialize and Set up Project
--
--	Designer:	  Anthony Vu, Li-Yan Tong, Morgan Ariss, John Tee
--
--	Programmer:		Anthony Vu
--
--	Notes:
--  This class is responsible for holding the information necessary for connecting
--  to the server. It also contains a function for sending the clients’ location
--  information to the server
---------------------------------------------------------------------------------------*/
public class TcpClient {

    public String SERVER_IP; // = "192.168.0.24"; //server IP address
    public int SERVER_PORT; // = 7000;
    public static final String TAG = "CLIENT_MESSAGE";
    // message to send to the server
    private String mServerMessage;
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;

    /*------------------------------------------------------------------------------------
    -- FUNCTION: sendMessage(final String message)
    --
    -- DATE:  April 3, 2018
    --
    -- REVISIONS: April 3, 2018
    --							Initial file set up
    --
    -- DESIGNER: Anthony Vu
    --
    -- PROGRAMMER: Anthony Vu
    --
    -- INTERFACE: sendMessage(final String message)
    --             String message - message to send to the server
    --
    -- RETURNS: void
    --
    -- NOTES:
    -- Sends a message to the server by creating then utilizing a thread.
    ---------------------------------------------------------------------------------------*/
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

    /*------------------------------------------------------------------------------------
    -- FUNCTION: stopClient()
    --
    -- DATE:  April 3, 2018
    --
    -- REVISIONS: April 3, 2018
    --							Initial file set up
    --
    -- DESIGNER: Anthony Vu
    --
    -- PROGRAMMER: Anthony Vu
    --
    -- INTERFACE: stopClient()
    --
    -- RETURNS: void
    --
    -- NOTES:
    -- Flushes out the message buffer.
    ---------------------------------------------------------------------------------------*/
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mBufferOut = null;
        mServerMessage = null;
    }

    /*------------------------------------------------------------------------------------
    -- FUNCTION: run()
    --
    -- DATE:  April 3, 2018
    --
    -- REVISIONS: April 3, 2018
    --							Initial file set up
    --
    -- DESIGNER: Anthony Vu
    --
    -- PROGRAMMER: Anthony Vu
    --
    -- INTERFACE: run()
    --
    -- RETURNS: void
    --
    -- NOTES:
    -- Attempts to make a connection to a web server and calls functions required to send
    -- a string containing location data.
    ---------------------------------------------------------------------------------------*/
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