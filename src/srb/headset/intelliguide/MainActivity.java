package srb.headset.intelliguide;


import java.util.ArrayList;
import java.util.List;

import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.gn.intelligentheadset.IHS;
import com.gn.intelligentheadset.IHSDevice;
import com.gn.intelligentheadset.IHSDevice.IHSDeviceConnectionState;
import com.gn.intelligentheadset.IHSDevice.IHSDeviceListener;
import com.gn.intelligentheadset.IHSListener;
import com.gn.intelligentheadset.subsys.IHSButtonHandler;
import com.gn.intelligentheadset.subsys.IHSButtonHandler.IHSButton;
import com.gn.intelligentheadset.subsys.IHSButtonHandler.IHSButtonEvent;
import com.gn.intelligentheadset.subsys.IHSButtonHandler.IHSButtonListener;
import com.gn.intelligentheadset.subsys.IHSSensorPack;
import com.gn.intelligentheadset.subsys.IHSSensorPack.IHSSensorsListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends ActionBarActivity implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{
	
	public static MapFragment mapFragment;
	public static TextView infoView;
	public static int MAP_ZOOM = 15;
	public GoogleMap map;
	public Location location;
	public LatLng myLocation = new LatLng(0,0 );
	public List<GuidePortal> guidePortals;
	private IHS mIHS;
	private boolean mapConnected = false;
	private boolean headsetConnected = false;

// The currently selected device
	public IHSDevice mMyDevice = null;
// entry to sensor data
	public IHSSensorPack sensorPack = null;

// If you change the package name, visit developer.intelligentheadset.com and obtain a matching API key.
	private final static String apikey    = "k8XipEfLXKx7hUIzaN+K5GErDoFRD5JhckYAT03t1Ok=";
	public LocationClient mLocationClient;
	private final static int
    CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
//	public static View myView;
	
	private UpdateHandler mHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        guidePortals = new ArrayList<GuidePortal>();
        GuidePortal kresge = new GuidePortal(new LatLng(42.358139, -71.095030), "Kresge", "kresge.wav");
        GuidePortal simmons = new GuidePortal(new LatLng(42.357267, -71.101194), "Simmons", "simmons.wav");
        GuidePortal flour = new GuidePortal(new LatLng(42.360938, -71.096640), "Flour", "flour.wav");
        GuidePortal medialab = new GuidePortal(new LatLng(42.360986, -71.087756), "Media Lab", "medialab.wav");
        guidePortals.add(kresge);
        guidePortals.add(simmons);
        guidePortals.add(flour);
        guidePortals.add(medialab);
        
        // Add guideportals
        
        mIHS = new IHS(this, apikey, mIHSListener);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();
        map.setMyLocationEnabled(true);
        for (GuidePortal g:guidePortals){
        	map.addMarker(new MarkerOptions()
            .position(g.getCordinate())
            .title(g.getName()));
        }
        
        infoView = (TextView) findViewById(R.id.info);
        infoView.setText("No points of interests nearby");

        mLocationClient = new LocationClient(this, this, this);
        // Connect the client.
        mLocationClient.connect();
        mHandler = new UpdateHandler(this);
    }

    @Override
    public void onPause(){
    	super.onPause();
    	mHandler.stopTask();
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	if (mHandler!=null && headsetConnected && mapConnected){
    		mHandler.startTask();
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        //Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        centerMapOnMyLocation();
        mapConnected = true;
        if (headsetConnected){
	        mHandler.startTask();
        }
    }
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
       Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
        mHandler.stopTask();
    }
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
        	Toast.makeText(this, "Failed to connect", connectionResult.getErrorCode()).show();
        }
    }
    
    public void changeInfoText(String info){
    	infoView.setText(info);
    }
    
    public void centerMapOnMyLocation() {

        //location = map.getMyLocation();
    	location = mLocationClient.getLastLocation();

        if (location != null) {
            myLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                MAP_ZOOM));
    }
    
    public void buttonPressed(){
    	if (mMyDevice.getAudio3DPlayer().isPlaying()){
    		mMyDevice.getAudio3DPlayer().stop();
    	}
    }
    
    private IHSListener mIHSListener = new IHSListener() {

        @Override
        public void onAPIstatus(APIStatus apiStatus) {
        	Toast.makeText(MainActivity.this, "API Connect", Toast.LENGTH_LONG).show();
            if (apiStatus == APIStatus.READY) {
                // We want it to stay alive until explicitly stopped (in onBackPressed)
                mIHS.enableBackgroundOperation();
                mIHS.connectHeadset(); // May already be connected, but that doesn't matter
            }
        }

        @Override
        public void onIHSDeviceSelected(IHSDevice device) {
            mMyDevice = device;

            // Add the device info listener to the selected device
            // to be able to notified on events. See declaration of mDeviceInfoListener
            // below.
            mMyDevice.addListener(mDeviceInfoListener);
            mMyDevice.getButtonHandler().addListener(mButtonListener);
            sensorPack = mMyDevice.getSensorPack();
          
            if (sensorPack!=null){
            	sensorPack.addListener(mSensorpackListener);
	            headsetConnected = true;
	            if (mapConnected){
	    	        mHandler.startTask();
	            }
            }

            // Add the device sensor listener to the sensorpack of the IHS device
            // to be able to notified on events. See declaration of mDeviceSensorListener
            // below.
            //mMyDevice.getSensorPack().addListener(mDeviceSensorListener);
        }
    };

// Listener for device-level events
private IHSDeviceListener  mDeviceInfoListener   = new IHSDeviceListener() {

        @Override
        public void connectedStateChanged(IHSDevice device, IHSDeviceConnectionState connectionState) {
            super.connectedStateChanged(device, connectionState);
            //Toast.makeText(MainActivity.this, "Connectivity changed", Toast.LENGTH_SHORT).show();

            switch (connectionState) {
                case IHSDeviceConnectionStateConnected:
                    // When we are fully connected, we will show an opaque logo
                    //imageLogo.setAlpha(1f);
                	Toast.makeText(MainActivity.this, "Device Connected", Toast.LENGTH_SHORT).show();
                    break;
                case IHSDeviceConnectionStateConnecting:
                	break;
                case IHSDeviceConnectionStateLingering:
                    // We will show a transparent logo while in one of these states.
                    //imageLogo.setAlpha(0.25f);
                    break;
                case IHSDeviceConnectionStateDeviceNotFound:
                    // This is where you decide what to do if the device you expect to connect to is
                    // not available.
                    // Note that you may get this several times with short intervals.
                    // It's up to you to decide how much patience you have.
                    // When out of patience, call device.disconnect();

                    break;
                case IHSDeviceConnectionStateDisconnected:
                    //resetUI();

                    // In this example, we decide that we want automatic reconnection in case of
                    // disconnects.
                    // Other apps may choose differently.

                    mIHS.connectHeadset(); // generic (re)connect - beware that this may end up connecting to a
                    // different device!
                    break;
                default:
                    // In any other state, we will hide the logo.
                    //imageLogo.setAlpha(0f);
                    break;
            }
        }
    };
    private IHSButtonListener mButtonListener = new IHSButtonListener() {

        @Override
        public void didPressIHSButton(IHSButtonHandler handler, IHSButton button, IHSButtonEvent event) {
        	buttonPressed();
        	Toast.makeText(MainActivity.this, "Button", 0).show();
//            if (button == IHSButton.IHSButtonRight){
//            }

        }
    };
    
    private IHSSensorsListener mSensorpackListener = new IHSSensorsListener() {
        @Override
        public void compassHeadingChanged(IHSSensorPack ihs, float heading){
        	mMyDevice.getAudio3DPlayer().setHeading(heading);
        }

    };
    
    public IHS getIHS(){
    	return mIHS;
    }

}


