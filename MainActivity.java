package com.example.wearlistener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.support.wearable.view.WatchViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.WindowManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;


public class MainActivity extends Activity
{
    private static final String TAG = "WATCH-WEAR-ACTIVITY";

    private TextView            mNameOrNumberTextView;
    private TextView            mCityAndStateTextView;
    private ImageView           mImageView;

    private Boolean             mLayoutInflated = false;
    private String              mNumber;
    private String              mName;
    private String              mFirstName;
    private String              mLastName;
    private String              mContactsAppName;
    private String              mCityName;
    private String              mStateName;
    private String              mStateAbbr;
    private String              mCountryName;
    private Bitmap              mPicture;
    private Bitmap              mLogo;
    private String              mCallState;



    public void onWindowFocusChanged(boolean hasFocus)
    // Very very occassionally the activity is losing focus, it looks like when this happens it is being displayed a fraction of
    // a second before the OS is displaying the incoming call screen, which results in the activity losing the foreground.
    // This method is here to track the focus,  setContent() moves the task back to the front if necessary
    {
        super.onWindowFocusChanged(hasFocus);

        if(hasFocus)
        {
            Log.i(TAG, "HAS FOCUS");
        }
        else
        {
            Log.i(TAG, "DOESN'T HAVE FOCUS");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "************************************** ACTIVITY onCreate() ****************************************");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        Intent intent = this.getIntent();
        boolean keep = intent.getExtras().getBoolean("keep");
        if(keep==true)
        {
            Log.i(TAG, "onCreate keep is true");
            final String callState = intent.getExtras().getString("callRinging");
            Log.i(TAG, "CALL STATE: " + callState);

            mCallState = callState;
            if (callState.equalsIgnoreCase("CALL_STATE_RINGING" ))
                getCallDataFromIntent(intent);

            Log.i(TAG, "Inflating layout and setting content");
            setContentView(R.layout.activity_main);
            WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);


            Log.i(TAG, "requesting INSETS");

            stub.requestApplyInsets();
            stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener()
            {
                @Override
                public void onLayoutInflated(WatchViewStub stub)
                {
                    Log.i(TAG, "Layout inflated");
                    mLayoutInflated = true;
                    if (mCallState.equalsIgnoreCase("CALL_STATE_RINGING" ))
                    {
                        Log.i(TAG, "CALL_STATE is RINGING - Going to call setContent from within closure");
                        setContent();
                    }
                    else
                    {
                        Log.i(TAG, "CALL_STATE is NOT RINGING - Not Going to call setContent from within closure");
                    }
                }
            });
        }
        else
        {
            Log.i(TAG, "  onCreate keep is false. Calling moveTaskToBack()");

            Log.i(TAG, "Inflating layout");
            setContentView(R.layout.activity_main);
            WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
            stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
                @Override
                public void onLayoutInflated(WatchViewStub stub) {
                    Log.i(TAG, "Layout inflated");
                    mLayoutInflated = true;
                    Log.i(TAG, "Going to call setContent from within closure");
                    setContent();
                }
            });

            //moveTaskToBack(true);
            finish();
        }
    }


    private void getCallDataFromIntent(Intent intent)
    {
        mNumber             = intent.getExtras().getString("number");
        mName               = intent.getExtras().getString("name");
        mFirstName          = intent.getExtras().getString("firstName");
        mLastName           = intent.getExtras().getString("lastName");
        mContactsAppName    = intent.getExtras().getString("contactsAppName");
        mCityName           = intent.getExtras().getString("cityName");
        mStateName          = intent.getExtras().getString("stateName");
        mStateAbbr          = intent.getExtras().getString("stateAbbr");
        mCountryName        = intent.getExtras().getString("countryName");

        byte[] byteArray = intent.getExtras().getByteArray("picture");
        if (byteArray != null)
            mPicture = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        else
            Log.i(TAG, "no picture found");

        byteArray = intent.getExtras().getByteArray("logo");
        if (byteArray != null)
            mLogo = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        else
            Log.i(TAG, "no logo found");
    }


    private void setContent()
    {
        Log.i(TAG, "setContent");
        WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        mNameOrNumberTextView = (TextView) stub.findViewById(R.id.nameOrNumberTextView);
        mCityAndStateTextView = (TextView) stub.findViewById(R.id.cityAndStateTextView);
        mImageView = (ImageView) stub.findViewById(R.id.imageView);
        if (mPicture != null)
            mImageView.setImageBitmap(mPicture);
        else if (mLogo != null)
            mImageView.setImageBitmap(mLogo);

        if (mNameOrNumberTextView == null || mCityAndStateTextView == null || mImageView == null)
            Log.i(TAG, "Unable to load all resources");
        else
        {
            // mContactsAppName is the name of a contact entry, if any, for the incoming number.
            // If there is a contact entry then the Android Wear will display that name, otherwise
            // it will display the number. Therefore the overlay will display the opposite
            if (mContactsAppName.length() > 0)
            {
                // Display the number
                mNameOrNumberTextView.setText(mNumber);
            }
            else
            {
                // display the name
                if (mName.length() == 0 && mFirstName.length() == 0 && mLastName.length() == 0)
                {
                    // No name information available
                    // Behaviour TBD
                    Log.i(TAG, "No Name info");
                    mNameOrNumberTextView.setText("No name info!");
                }
                else
                {
                    if (mName.length() > 0 )
                    {
                        Log.i(TAG, "Setting the name to: " + mName);
                        mNameOrNumberTextView.setText(mName);
                    }
                    else
                    {
                        String name = mFirstName + mLastName;
                        Log.i(TAG, "Setting name to: " + name);
                        mNameOrNumberTextView.setText(name);
                    }
                }
            }

            // TODO format city,state, country
            String location = "";
            if (mCityName != null && mCityName.length() > 0 && mStateAbbr != null && mStateAbbr.length() > 0)
                location = mCityName + ", " + mStateAbbr;
            mCityAndStateTextView.setText(location);
        }
    }


    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        boolean keep = intent.getExtras().getBoolean("keep");
        Log.i(TAG, "  ACTIVITY onNewIntent");

        if(keep==false)
        {
            Log.i(TAG, "onNewIntent keep is false. Going to move task to back");
            //moveTaskToBack(true);
            finish();
        }
        else
        {
            Log.i(TAG, "onNewIntent keep is true");
            getCallDataFromIntent(intent);
            Log.i(TAG, "Calling setContent from onNewIntent");
            setContent();
        }
    }


    @Override
    protected  void onStop()
    {
        Log.i(TAG, "  ACTIVITY onStop(). Identity: " +  toString());
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Log.i(TAG, "  ACTIVITY onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onRestart()
    {

        Log.i(TAG, "  ACTIVITY onRestart()");
        super.onRestart();
    }

    @Override
    protected void onStart()
    {
        Log.i(TAG, "  ACTIVITY onStart()");
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        Log.i(TAG, "  ACTIVITY onResume()");
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        Log.i(TAG, "  ACTIVITY onPause()");
        super.onPause();
    }
}
