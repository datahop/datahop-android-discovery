package network.datahop.datahoptest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;
import java.util.UUID;

import datahop.AdvertisementNotifier;
import datahop.DiscoveryNotifier;


import datahop.WifiConnectionNotifier;
import datahop.WifiHotspotNotifier;
import network.datahop.blediscovery.BLEAdvertising;
import network.datahop.blediscovery.BLEServiceDiscovery;
import network.datahop.wifidirect.WifiDirectHotSpot;
import network.datahop.wifidirect.WifiLink;

public class MainActivity extends AppCompatActivity implements AdvertisementNotifier, DiscoveryNotifier, WifiHotspotNotifier, WifiConnectionNotifier {


    private Button startButton,stopButton,refreshButton;

    private TextView status, peerId;
    private BLEAdvertising advertisingDriver;
    private BLEServiceDiscovery discoveryDriver;

    private WifiDirectHotSpot hotspot;
    private WifiLink connection;

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_WIFI_STATE = 2;

    private static final String TAG = "DatahopTest";

    private String stat;

    private int counter;
    private boolean stopping;

    private String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        counter=0;
        stopping=false;
        advertisingDriver = BLEAdvertising.getInstance(getApplicationContext());
        discoveryDriver = BLEServiceDiscovery.getInstance(getApplicationContext());
        advertisingDriver.setNotifier(this);
        discoveryDriver.setNotifier(this);

        hotspot = WifiDirectHotSpot.getInstance(getApplicationContext());
        connection = WifiLink.getInstance(getApplicationContext());
        hotspot.setNotifier(this);
        connection.setNotifier(this);

        peerId = (TextView) findViewById(R.id.textview_id);

        this.id = randomString();
        peerId.setText("PeerId: "+id);

        startButton = (Button) findViewById(R.id.start_button);

        stopButton = (Button) findViewById(R.id.stop_button);
        refreshButton = (Button) findViewById(R.id.add_content);

        status = (TextView) findViewById(R.id.textview_status);

        requestForPermissions();
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stat = randomString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status.setText("Status: "+stat);
                    }
                });
                //advertisingDriver.addAdvertisingInfo("bledemo",stat);
                discoveryDriver.addAdvertisingInfo("datahoptest",stat);
                //advertisingDriver.start(TAG,"peerId");
                discoveryDriver.start(TAG,id,2000,30000);

                advertisingDriver.addAdvertisingInfo("datahoptest",stat);
                //discoveryDriver.addAdvertisingInfo("bledemo",stat);
                advertisingDriver.start(TAG,id);
            }
        });


        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                advertisingDriver.stop();
                discoveryDriver.stop();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //advertisingDriver.addAdvertisingInfo();

                stat = randomString();
                status.setText("Status: "+stat);

                advertisingDriver.addAdvertisingInfo("datahoptest",stat);
                discoveryDriver.addAdvertisingInfo("datahoptest",stat);
                advertisingDriver.stop();
                advertisingDriver.start(TAG,id);
                discoveryDriver.stop();
                discoveryDriver.start(TAG,id,2000,30000);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        advertisingDriver.stop();
        discoveryDriver.stop();
        super.onDestroy();
    }


    private void requestForPermissions() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                }
            });
            builder.show();
        }
        if (this.checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.CHANGE_WIFI_STATE}, PERMISSION_WIFI_STATE);
                }
            });
            builder.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "Permissions " + requestCode + " " + permissions + " " + grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG, "Location accepted");
                    //timers.setLocationPermission(true);
                    //if(timers.getStoragePermission())startService();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d(TAG, "Location not accepted");

                }
                break;
            }

        }

        // other 'case' lines to check for other
        // permissions this app might request.

    }

    @Override
    public void advertiserPeerDifferentStatus(String topic, byte[] bytes, String peerinfo) {
        Log.d(TAG,"differentStatusDiscovered "+topic+" "+peerinfo);
        //advertisingDriver.notifyNetworkInformation(stat,stat);
        discoveryDriver.stop();
        hotspot.start();
    }

    @Override
    public void advertiserPeerSameStatus() {
        Log.d(TAG,"sameStatusDiscovered");
        advertisingDriver.notifyEmptyValue();
        //discoveryDriver.stop();
        //discoveryDriver.start(TAG,"peerId",2000,30000);

    }

    @Override
    public void discoveryPeerDifferentStatus(String device, String topic, String network, String pass, String info) {
        Log.d(TAG,"peerDifferentStatusDiscovered "+device+" "+topic+" "+network+" "+pass+" "+info);
        /*stat = network;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText("Status: "+stat);
            }
        });*/
         /*   advertisingDriver.addAdvertisingInfo("bledemo",stat);
            advertisingDriver.stop();
            advertisingDriver.start(TAG,"peerId");
            discoveryDriver.addAdvertisingInfo("bledemo",stat);*/
        String password = pass.split("/")[0];
        stat = pass.split("/")[1];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText("Status: "+stat);
            }
        });
        discoveryDriver.stop();
        discoveryDriver.addAdvertisingInfo("datahoptest",stat);
        advertisingDriver.addAdvertisingInfo("datahoptest",stat);
        advertisingDriver.stop();
        connection.connect(network, password, "192.168.49.2", "");



    }

    /*@Override
    public void peerDiscovered(String s) {
        Log.d(TAG,"peerDiscovered "+s);
    }*/

    @Override
    public void discoveryPeerSameStatus(String device, String topic) {
        Log.d(TAG,"peerSameStatusDiscovered "+device+" "+topic);

    }


    public String randomString() {

        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();

    }

    @Override
    public void clientsConnected(long l) {
        Log.d(TAG,"Clients connected "+l);
        if(counter>0&&l==0&&!stopping){
            Log.d(TAG,"Discovery restart");
            stopping=true;
            discoveryDriver.start(TAG,id,2000,30000);
            hotspot.stop();
        }
        counter= (int) l;
        stopping=false;
    }

    @Override
    public void networkInfo(String net, String pass) {
        Log.d(TAG,"SSID: "+net);
        Log.d(TAG,"Pass: "+pass);

        advertisingDriver.notifyNetworkInformation(net,pass+"/"+stat);
    }

    @Override
    public void onFailure(long l) {
        Log.d(TAG,"onFailure");

    }

    @Override
    public void onSuccess() {
        Log.d(TAG,"onSuccess");

    }

    @Override
    public void onDisconnect() {
        Log.d(TAG,"onDisconnect");
        discoveryDriver.start(TAG,id,2000,30000);
        advertisingDriver.start(TAG,id);
    }

    @Override
    public void onConnectionFailure(long code, long started, long failed) {
        Log.d(TAG,"onFailure "+code);

    }

    @Override
    public void onConnectionSuccess(long started, long completed, long rssi , long speed ,long freq) {
        Log.d(TAG,"onSuccess");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        connection.disconnect();
        discoveryDriver.start(TAG,id,2000,30000);
        advertisingDriver.start(TAG,id);

    }

    @Override
    public void stopOnFailure(long l) {
        Log.d(TAG,"stopOnFailure "+l);
    }

    @Override
    public void stopOnSuccess() {
        Log.d(TAG,"stopOnSuccess");

    }


}