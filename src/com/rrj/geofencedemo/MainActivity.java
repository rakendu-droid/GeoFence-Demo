package com.rrj.geofencedemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener,
OnAddGeofencesResultListener
{

	GoogleMap mMap;
	private LocationClient mLocationClient;
	BroadcastReceiver mGeofenceBroadcastReceiver;
	IntentFilter mIntentFilter;
	String mGeofenceState;
	PendingIntent mGeofencePendingIntent=null;
	Geofence myGeofence;
	private List<Geofence> mCurrentGeofences;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.myMap)).getMap();
//		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CHANDLER, ZOOM_LEVEL));
		mLocationClient = new LocationClient(this, this, this);
		mIntentFilter = new IntentFilter();
		
		// Action for broadcast Intents that report successful addition of geofences
			        mIntentFilter.addAction("ACTION_GEOFENCES_ADDED");
		 
			        // Action for broadcast Intents that report successful removal of geofences
			        mIntentFilter.addAction("ACTION_GEOFENCES_REMOVED");
			 
			        // Action for broadcast Intents containing various types of geofencing errors
			        mIntentFilter.addAction("ACTION_GEOFENCE_ERROR");
			 
			        // All Location Services sample apps use this category
			        mIntentFilter.addCategory("CATEGORY_LOCATION_SERVICES");
			 
			       // createGeofences();
			        //mGeofenceState = "CAN_START_GEOFENCE";
			        
			        Boolean isConnected =servicesConnected();
			        if(isConnected)
			        	createGeofences();
			 
	}
	
	private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            // In debug mode, log the status
        	Log.d("APPTAG","Play services available");
            // Continue
            return true;

        // Google Play services was not available for some reason
        } else {

            // Display an error dialog
//            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
//            if (dialog != null) {
//                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
//                errorFragment.setDialog(dialog);
//                errorFragment.show(getSupportFragmentManager(), GeofenceUtils.APPTAG);
//            }
        	Log.d("APPTAG","Play services not available");
            return false;
        }
	
	}
    private void createGeofences() {
		// TODO Auto-generated method stub
    	//12.93014,77.587732
    	
    	Log.d("APPTAG", "Create Geofence");
    	myGeofence=new Geofence.Builder()
        .setRequestId("1")
        .setTransitionTypes(	Geofence.GEOFENCE_TRANSITION_ENTER)
        .setCircularRegion(
                12.93014,
                77.587732,
                2000)
        .setExpirationDuration(12*DateUtils.HOUR_IN_MILLIS)
        .build();
    	mCurrentGeofences = new ArrayList<Geofence>();
    	
    	mCurrentGeofences.add(myGeofence);
    	Log.d("APPTAG", " Geofence  "+myGeofence);
    	//mLocationClient = new LocationClient(this, this, this);
    	requestConnection();
    	
    	
	}
    private void requestConnection() {
        getLocationClient().connect();
       
    }

    /**
     * Get the current location client, or create a new one if necessary.
     *
     * @return A LocationClient object
     */
    private GooglePlayServicesClient getLocationClient() {
        if (mLocationClient == null) {

            mLocationClient = new LocationClient(this, this, this);
        }
        return mLocationClient;

    }
	//AIzaSyAfAkZZAhNQQIuUOGmL_44upjmLfZmV1bE
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		Log.d("APPTAG", "mActivity.getString(R.string.connected)");
		mGeofencePendingIntent = createRequestPendingIntent();
		mLocationClient.addGeofences(mCurrentGeofences, mGeofencePendingIntent, this);
		//mGeofencePendingIntent = createRequestPendingIntent();
		
	}
	private PendingIntent createRequestPendingIntent() {
		Log.d("APPTAG", "Requester ");
        // If the PendingIntent already exists
        if (null != mGeofencePendingIntent) {

            // Return the existing intent
            return mGeofencePendingIntent;

        // If no PendingIntent exists
        } else {

            // Create an Intent pointing to the IntentService
            Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);
            /*
             * Return a PendingIntent to start the IntentService.
             * Always create a PendingIntent sent to Location Services
             * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
             * again updates the original. Otherwise, Location Services
             * can't match the PendingIntent to requests made with it.
             */
            return PendingIntent.getService(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }
	

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
//	public static class PlaceholderFragment extends Fragment {
//
//		public PlaceholderFragment() {
//		}
//
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
//				Bundle savedInstanceState) {
//			View rootView = inflater.inflate(R.layout.fragment_main, container,
//					false);
//			return rootView;
//		}
//	}
	
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		// TODO Auto-generated method stub
		Log.d("APPTAG", "Geofence Result");
		Intent broadcastIntent = new Intent();
		 //Temp storage for messages
	        String msg;

	        // If adding the geocodes was successful
	        if (LocationStatusCodes.SUCCESS == statusCode) {

	            // Create a message containing all the geofence IDs added.
	            msg = this.getString(R.string.add_geofences_result_success,
	                    Arrays.toString(geofenceRequestIds));

	            // In debug mode, log the result
	            Log.d(GeofenceUtils.APPTAG, msg);

	            // Create an Intent to broadcast to the app
	            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCES_ADDED)
	                           .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
	                           .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
	        // If adding the geofences failed
	        } else {

	            /*
	             * Create a message containing the error code and the list
	             * of geofence IDs you tried to add
	             */
	            msg = this.getString(
	                    R.string.add_geofences_result_failure,
	                    statusCode,
	                    Arrays.toString(geofenceRequestIds)
	            );

	            // Log an error
	            Log.e(GeofenceUtils.APPTAG, msg);

	            // Create an Intent to broadcast to the app
	            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR)
	                           .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
	                           .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
	        }

	        // Broadcast whichever result occurred
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

	        // Disconnect the location client
	        //requestDisconnection();
	}
	public class GeofenceSampleReceiver extends BroadcastReceiver {
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Check the action code and determine what to do
            String action = intent.getAction(); 

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);

            // Intent contains information about successful addition or removal of geofences
            } else if (
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
                    ||
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

                handleGeofenceStatus(context, intent);

            // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

                handleGeofenceTransition(context, intent);

            // The Intent contained an invalid action
            } else {
                Log.e(GeofenceUtils.APPTAG, "getString(R.string.invalid_action_detail, action)");
                Toast.makeText(context, "R.string.invalid_action", Toast.LENGTH_LONG).show();
            }
        }

        /**
         * If you want to display a UI message about adding or removing geofences, put it here.
         *
         * @param context A Context for this component
         * @param intent The received broadcast Intent
         */
        private void handleGeofenceStatus(Context context, Intent intent) {

        }

        /**
         * Report geofence transitions to the UI
         *
         * @param context A Context for this component
         * @param intent The Intent containing the transition
         */
        private void handleGeofenceTransition(Context context, Intent intent) {
            /*
             * If you want to change the UI when a transition occurs, put the code
             * here. The current design of the app uses a notification to inform the
             * user that a transition has occurred.
             */
        }

        /**
         * Report addition or removal errors to the UI, using a Toast
         *
         * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
         */
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Log.e(GeofenceUtils.APPTAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
	}
}
