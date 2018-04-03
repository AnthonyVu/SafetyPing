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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

        set1 = false;
        set2 = false;
        set3 = false;
        connected = false;
        connect = (Button) findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(set1 && set2 && set3) {
                    new ConnectTask().execute("");
                    connected = true;
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    locationListener = new MyLocationListener();

                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        Toast.makeText(MainActivity.this, "fail permission", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

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

        @Override
        public void onLocationChanged(Location location) {
            Log.d("Lat", "Location");
            String latitude = Double.toString(location.getLatitude());
            String longitude = Double.toString(location.getLongitude());
            Log.e("Lat", latitude);
            Log.e("Long", longitude);
            message = latitude + " " + longitude + " " + convertedName.replace(" ", "");
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
