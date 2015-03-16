package com.example.wearlistener;

import android.content.Intent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.util.Log;
import com.google.android.gms.wearable.Node;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;


public class WearMessageListenerService extends WearableListenerService
{
    private static final String TAG = "WATCH-WEAR-LISTENER";

    @Override
    public void onCreate()
    {
        Log.i(TAG, "WearMessageListenerService onCreate()");
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {
        Log.i(TAG, "WearMessageListenerService onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onPeerConnected(Node peer)
    {
        Log.i(TAG, "WearMessageListenerService onPeerConnected()");
        super.onPeerConnected(peer);
    }

    @Override
    public void onPeerDisconnected(Node peer)
    {
        Log.i(TAG, "WearMessageListenerService onPeerDisconnected()");
        super.onPeerDisconnected(peer);
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        Log.i(TAG, "WearableListenerService onMessageReceived() with: " + messageEvent.getPath());

        if( messageEvent.getPath().equalsIgnoreCase( "CALL_STATE_RINGING" ))
        {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //WIll get this error message without NEW_TASK android.util.AndroidRuntimeException: Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?

            // FLAG_ACTIVITY_CLEAR_TOP: If set, and the activity being launched is already running in the current task,
            // then instead of launching a new instance of that activity, all of the other activities on top of it will be closed
            // and this Intent will be delivered to the (now on top) old activity as a new Intent.
            //  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            intent.putExtra("keep", true);
            intent.putExtra("callRinging", messageEvent.getPath());
            byte[] byteArray = messageEvent.getData();
            if (byteArray != null)
            {
                ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);
                DataInputStream dataStream = new DataInputStream(byteStream);
                try
                { // Compiler generates an error without wrapping in a try block
                    String number = dataStream.readUTF();
                    String name = dataStream.readUTF();
                    String firstName = dataStream.readUTF();
                    String lastName = dataStream.readUTF();
                    String contactsAppName = dataStream.readUTF();
                    String cityName = dataStream.readUTF();
                    String stateName = dataStream.readUTF();
                    String stateAbbr = dataStream.readUTF();
                    String countryName = dataStream.readUTF();
                    Boolean picturePresent = dataStream.readBoolean();
                    Boolean logoPresent = dataStream.readBoolean();

                 /*   Log.i(TAG, "number:             " + number);
                    Log.i(TAG, "name:               " + name);
                    Log.i(TAG, "first name:         " + firstName);
                    Log.i(TAG, "last name:          " + lastName);
                    Log.i(TAG, "contacts app name:  " + contactsAppName);
                    Log.i(TAG, "city name:          " + cityName);
                    Log.i(TAG, "state name:         " + stateName);
                    Log.i(TAG, "state abbr:         " + stateAbbr);
                    Log.i(TAG, "country name:       " + countryName);
                    Log.i(TAG, "picture present:    " + picturePresent.toString());
                    Log.i(TAG, "logo present:       " + logoPresent.toString()); */

                    intent.putExtra("number", number);
                    intent.putExtra("name", name);
                    intent.putExtra("firstName", firstName);
                    intent.putExtra("lastName", lastName);
                    intent.putExtra("contactsAppName", contactsAppName);
                    intent.putExtra("cityName", cityName);
                    intent.putExtra("stateName", stateName);
                    intent.putExtra("stateAbbr", stateAbbr);
                    intent.putExtra("countryName", countryName);


                    if (picturePresent || logoPresent)
                    {
                        Bitmap bitmap1 = BitmapFactory.decodeStream(byteStream);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream(); //todo put a check bitmap1 isn't null
                        bitmap1.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.flush();
                        stream.close();
                        byte[] byteArray2 = stream.toByteArray();
                        if (picturePresent)
                        {
                            Log.i(TAG, "adding picture as an intent extra");
                            intent.putExtra("picture", byteArray2);
                            if (logoPresent)
                            {
                                Log.i(TAG, "Adding logo in addition to picture as an intent extra");
                                Bitmap bitmap2 = BitmapFactory.decodeStream(byteStream);
                                ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                                bitmap1.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                stream2.flush();
                                stream2.close();
                                byte[] byteArray3 = stream2.toByteArray();
                                intent.putExtra("logo", byteArray2);
                            }
                        }
                        else
                        {
                            Log.i(TAG, "Adding just logo as an intent extra");
                            intent.putExtra("logo", byteArray2);
                        }
                    }

                } catch (Exception e)
                {
                    Log.i(TAG, " ************************************** EXCEPTION reading data stream");
                }

                Log.i(TAG, "Going to start activity");
                startActivity(intent);
            }
        }
        else if ( messageEvent.getPath().equalsIgnoreCase( "CALL_STATE_IDLE" ) ||
                    messageEvent.getPath().equalsIgnoreCase( "CALL_STATE_OFFHOOK" ))
        {
            Log.i(TAG, "Going to move activity to back by sending intent");
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("keep", false);

            startActivity(intent);
        } else
        {
            super.onMessageReceived(messageEvent);
        }
    }


}
