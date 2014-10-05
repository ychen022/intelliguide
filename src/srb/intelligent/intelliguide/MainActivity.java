package srb.intelligent.intelliguide;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gn.intelligentheadset.IHS;
import com.gn.intelligentheadset.IHSDevice;
import com.gn.intelligentheadset.IHSListener;
import com.gn.intelligentheadset.IHSDevice.IHSDeviceConnectionState;
import com.gn.intelligentheadset.IHSDevice.IHSDeviceListener;
import com.gn.intelligentheadset.IHSListener.APIStatus;
import com.gn.intelligentheadset.subsys.IHSSensorPack;
import com.gn.intelligentheadset.subsys.IHSSensorPack.IHSSensorsListener;



public class MainActivity extends ActionBarActivity {
	private IHS mIHS;

    // The currently selected device
    private IHSDevice mMyDevice = null;

    // If you change the package name, visit developer.intelligentheadset.com and obtain a matching API key.
    private final static String apikey    = "4zeUfJXKppXb5nomDNGsaNV2fiU5ATUW/sQCihynXAvN22fzO0YHXwbZfesD+IEg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        mIHS = new IHS(this, apikey, mIHSListener);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
    
    private IHSListener mIHSListener = new IHSListener() {

        @Override
        public void onAPIstatus(APIStatus apiStatus) {
        	System.out.println("yes");
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

            // Add the device sensor listener to the sensorpack of the IHS device
            // to be able to notified on events. See declaration of mDeviceSensorListener
            // below.
            //mMyDevice.getSensorPack().addListener(mDeviceSensorListener);
        }
    };

// Listener for sensorpack-level events
//private IHSSensorsListener mDeviceSensorListener = new IHSSensorsListener() {
//        @Override
//        public void yawChanged(IHSSensorPack ihs, float yaw) {
//            // Rotate the 'arrow' image representing the current yaw
//            imageYaw.setRotation(yaw);
//            // Display the current yaw in text
//            tvYaw.setText(yaw + "�");
//        }
//
//        @Override
//        public void gyroCalibrationChanged(IHSSensorPack ihs, boolean isCalibrated) {
//            if (isCalibrated) {
//                Toast.makeText(MainActivity.this, "Gyro is calibrated!", Toast.LENGTH_SHORT).show();
//            }
//            // Update 'isGyroUncalibrated' checkbox
//            cbGyroCalib.setChecked(isCalibrated);
//        }
//
//        @Override
//        public void magneticDisturbanceDetetionChanged(IHSSensorPack arg0, boolean disturbed) {
//            cbMagDisturb.setChecked(disturbed);
//        };
//
//        @Override
//        public void fusedHeadingChanged(IHSSensorPack ihs, float heading) {
//            // Rotate the 'arrow' image representing the current fused heading
//            imageFused.setRotation(heading);
//            // Display the current fused heading in text
//            tvFused.setText(heading + "�");
//        }
//
//        @Override
//        public void compassHeadingChanged(IHSSensorPack ihs, float heading) {
//            // Rotate the 'arrow' image representing the current compass heading
//            imageCompass.setRotation(heading);
//            // Display the current compass heading in text
//            tvCompass.setText(heading + "�");
//        }
//    };
//
// Listener for device-level events
private IHSDeviceListener  mDeviceInfoListener   = new IHSDeviceListener() {

        @Override
        public void connectedStateChanged(IHSDevice device, IHSDeviceConnectionState connectionState) {
            super.connectedStateChanged(device, connectionState);
            System.out.println("Headset Connected");

            switch (connectionState) {
                case IHSDeviceConnectionStateConnected:
                    // When we are fully connected, we will show an opaque logo
                    //imageLogo.setAlpha(1f);
                	System.out.println("Headset Connected");
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
}
