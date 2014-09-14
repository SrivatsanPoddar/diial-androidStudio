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


public class TwilioPhone implements Twilio.InitListener, Callback<CallToken>
{
    String TAG = "TwilioPhone";
    private Device device;
    private Connection connection;
    private boolean toConnect = false;
    private String company_id;
    private Context context;

    public TwilioPhone(Context myContext, String mCompany_id) {
        if(Twilio.isInitialized()) {
            Twilio.shutdown();
            
        }
        context = myContext;
        Twilio.initialize(context,  this);
        company_id = mCompany_id;
    }
    
    @Override
    public void onError(Exception e)
    {
        // TODO Auto-generated method stub
        Log.e(TAG, "Twilio SDK couldn't start: " + e.getLocalizedMessage());
    }

    @Override
    public void onInitialized()
    {
        // TODO Auto-generated method stub
        Log.d(TAG, "Twilio SDK is ready");
        RestAdapter restAdapter = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL).setEndpoint("http://safe-hollows-9286.herokuapp.com").build();
        HerokuService phoneService = restAdapter.create(HerokuService.class);       
        phoneService.getCallToken(this);

    }

    @Override
    public void failure(RetrofitError arg0)
    {
        // TODO Auto-generated method stub
        Log.e("Failed to get call token", arg0.toString());
    }

    @Override
    public void success(CallToken token, Response arg1)
    {
        Log.d("Returned Call Token:", token.token);
        device = Twilio.createDevice(token.token, null);
        // TODO Auto-generated method stub
//        this.connect(company_id);
//        if (toConnect) {
//            connection = device.connect(null, null);
//            toConnect = false;
//            if (connection == null)
//                Log.e(TAG,"Failed to create a new connection");
//        }
        
    }
    
    @Override
    protected void finalize()
    {
        if (connection != null)
            connection.disconnect();
        if (device != null)
            device.release();
    }
    
    public void connect(String company_id) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("PhoneNumber", company_id);
        parameters.put("To", company_id);
        Log.e("Making call with parameters:", parameters.toString());
        connection = device.connect(parameters, null);
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
      if (connection == null)
          Log.e(TAG,"Failed to create a new connection");
//        if (device != null) {
//            connection = device.connect(parameters, null);
//            if (connection == null)
//                Log.e(TAG,"Failed to create a new connection");
//        }
//        else {
//            toConnect = true;
//        }

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
        
    public void sendDigit() {
        Log.e("Digits sent!","1");
        connection.sendDigits("1");
    }
}
