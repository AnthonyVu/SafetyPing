package com.example.antho.gps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


/*---------------------------------------------------------------------------------------
--	Source File:    MainActivity.java - An Android Application that connects and sends
--                                      device location data to a web server.
--
--	Classes:	MyLocationListener  - location listener class
--				ConnectTask - An Asynchronous task to manage the connection
--              TcpClient - TCP class
--
--	Methods:
--				onCreate 	        (Android Constructor)
--				onClick  	        (Android Button listener)
--              onStop              (Android in background function)
--
--
--	Date:			April 3, 2018
--
--	Revisions:	(Date and Description)
--                April 3, 2018
--                Initialize and Set up Project
--                April 5, 2018
--                Code comments and update send file data.
--
--	Designer:		Anthony Vu, Li-Yan Tong, Morgan Ariss, John Tee
--
--	Programmer:		Anthony Vu
--
--	Notes:
--	Entry point of the Android TCP Client Program.  This program sends the Android device's
--  Geographic location information via a mobile data/wifi internet connection using the
--  TCP/IP protocol to a remote Server application.
---------------------------------------------------------------------------------------*/
public class MainActivity extends AppCompatActivity {

    private TcpClient mTcpClient;
    private Button connect;
    private Button disconnect;
    private EditText ip;
    private EditText port;
    private EditText name;
    private String convertedIp;
    private String message;
    private String convertedName;
    private int convertedPort;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean set1, set2, set3, connected;

    /*------------------------------------------------------------------------------------
    -- FUNCTION: onCreate(Bundle savedInstanceState)
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
    -- INTERFACE: onCreate(Bundle savedInstanceState)
    --            savedInstanceState - Saved instance of this program
    --
    -- RETURNS: void
    --
    -- NOTES:
    -- First function in the activity life cycle, called immediately when the android
    -- application is started. Creates and forms the connection with the server and creates
    -- a location manager.
    --
    -- Once a connection is formed the client constantly communicates its location to the server,
    -- as a service in the background. This function also handles many of the UI options from
    -- the layout (button handling). Also handles requesting the necessary GPS and online permissions.
    ---------------------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);

        set1 = false;
        set2 = false;
        set3 = false;
        connected = false;
        connect = (Button) findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {

            /*------------------------------------------------------------------------------------
            -- FUNCTION: onClick(View v)
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
            -- INTERFACE: onClick(View v)
            --            v - Android GUI user interacts with
            --
            -- RETURNS: void
            --
            -- NOTES:
            -- Handles finding the device location and updating the location after the user clicks
            -- the connect button.
            ---------------------------------------------------------------------------------------*/
            @Override
            public void onClick(View v) {
                if(set1 && set2 && set3) {
                    new ConnectTask().execute("");
                    connected = true;
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    locationListener = new MyLocationListener();

                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainActivity.this, "fail permission", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);

                    //disable button once clicked or if no ip/port specified
                    //enable when close button is clicked
                } else {
                    Toast.makeText(MainActivity.this, "You left something blank", Toast.LENGTH_SHORT).show();
                    connected = false;
                }
            }
        });
        disconnect = (Button)findViewById(R.id.disconnect);
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    mTcpClient.sendMessage("quit");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mTcpClient.stopClient();
                    connected = false;
                } else {
                    Toast.makeText(MainActivity.this, "You're not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ip = (EditText)findViewById(R.id.ip);
        ip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0) {
                    set1 = false;
                    return;
                } else {
                    convertedIp = s.toString();
                    set1 = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        port = (EditText)findViewById(R.id.port);
        port.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0) {
                    set2 = false;
                    return;
                } else {
                    convertedPort = Integer.parseInt(s.toString());
                    set2 = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        name = (EditText)findViewById(R.id.name);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0) {
                    set3 = false;
                    return;
                } else {
                    convertedName = s.toString();
                    set3 = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /*------------------------------------------------------------------------------------
    -- FUNCTION: onStop()
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
    -- INTERFACE: onStop()
    --
    -- RETURNS: void
    --
    -- NOTES:
    -- Handles stopping the program and disconnecting from the server.
    ---------------------------------------------------------------------------------------*/
    @Override
    protected void onStop() {
        if(connected) {
            super.onStop();
            mTcpClient.sendMessage("disconnect");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mTcpClient.stopClient();
        }
    }

    /*---------- Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("Lat", "disable");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("Lat", "enable");
        }

        /*------------------------------------------------------------------------------------
        -- FUNCTION: onLocationChanged(Location location)
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
        -- INTERFACE: onLocationChanged(Location location)
        --              location - Location of device in latitude and longitude
        --
        -- RETURNS: void
        --
        -- NOTES:
        -- This function is responsible for sending the location and time of the client to
        -- the server to be displayed.
        ---------------------------------------------------------------------------------------*/
        @Override
        public void onLocationChanged(Location location) {
            Log.d("Lat", "Location");
            String latitude = Double.toString(location.getLatitude());
            String longitude = Double.toString(location.getLongitude());
            Log.e("Lat", latitude);
            Log.e("Long", longitude);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date());
            message = latitude + " " + longitude + " " + convertedName.replace(" ", "") + " " + currentDateTime;
            mTcpClient.sendMessage(message);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("Lat", "status");
        }
    }

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {

            mTcpClient = new TcpClient();
            mTcpClient.SERVER_IP = convertedIp;
            mTcpClient.SERVER_PORT = convertedPort;
            mTcpClient.run();
            return null;
        }
    }
}
