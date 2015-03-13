package com.SrivatsanPoddar.helpp;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.twilio.client.Connection;
import com.twilio.client.Device;
import com.twilio.client.Twilio;

/**
 * Handles connecting and disconnecting the Twilio Call
 */
public class TwilioPhone implements Twilio.InitListener, Callback<CallToken>
{
    String TAG = "TwilioPhone";
    private Device device;
    private Connection connection;
    private Context context;

    public TwilioPhone(Context myContext, String mCompany_id) {
        if(Twilio.isInitialized()) { //Shut-down if already started
            Twilio.shutdown();
        }
        context = myContext;
        Twilio.initialize(context,  this);
    }
    
    @Override
    public void onError(Exception e)
    {
        Log.e(TAG, "Twilio SDK couldn't start: " + e.getLocalizedMessage());
    }

    @Override
    public void onInitialized()
    {
        Log.d(TAG, "Twilio SDK is ready");
        RestAdapter restAdapter = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL).setEndpoint("http://safe-hollows-9286.herokuapp.com").build();
        HerokuService phoneService = restAdapter.create(HerokuService.class);       
        phoneService.getCallToken(this);
    }

    @Override
    public void failure(RetrofitError arg0)
    {
        Log.e("Failed to get callToken", arg0.toString());
    }

    @Override
    public void success(CallToken token, Response arg1)
    {
        Log.d("Returned Call Token:", token.token);
        device = Twilio.createDevice(token.token, null);
    }
    
    @Override
    protected void finalize()
    {
        if (connection != null)
            connection.disconnect();
        if (device != null)
            device.release();
    }

    /**
     * Connect the call with the company ID as a parameter
     * @param company_id The ID of the company trying to be reached
     */
    public void connect(String company_id) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("PhoneNumber", company_id);
        parameters.put("To", company_id);
        Log.e("Making call w. params", parameters.toString());
        connection = device.connect(parameters, null);
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
      if (connection == null)
          Log.e(TAG,"Failed to create a new connection");
    }
    
    public void disconnect()
    {
        if (connection != null) {
            connection.disconnect();
            connection = null;
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
    }

    /**
     * Sends a digit
     */
    public void sendDigit() {
        Log.e("Digits sent!","1");
        connection.sendDigits("1");
    }
}
