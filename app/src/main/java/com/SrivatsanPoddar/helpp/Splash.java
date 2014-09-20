package com.SrivatsanPoddar.helpp;

import java.util.Arrays;
import java.util.Hashtable;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class Splash extends Activity implements Callback<Node[]>
{
    private long startTime;
    private HerokuService nodeService;
    private Node[] tempNodes;
    public static Node[] loadedNodes;
    private final int SPLASH_DISPLAY_LENGTH = 2000;
    private final int ERROR_DISPLAY_LENGTH = 5000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        //Calculate passage of time to ensure splash screen displayed for 2s
        startTime = System.currentTimeMillis();
        
        // Get the nodes from Heroku
        RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint("http://safe-hollows-9286.herokuapp.com")
            .build();
        nodeService = restAdapter.create(HerokuService.class);       
        nodeService.nodes(this);
    }
    
    @Override
    public void failure(RetrofitError arg0)
    {
        // Print the error and close the application
        Log.e("Error retreiving nodes from database:", arg0.toString());
        setContentView(R.layout.error);
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {               
                System.exit(0);
            }
        }, ERROR_DISPLAY_LENGTH);
    }

    @Override
    public void success(Node[] arg0, Response arg1)
    {        
        Log.e("Success retrieving nodes from database:", Arrays.toString(arg0));
        tempNodes = arg0;
        
        // We use a hashtable as well to avoid indexing issues
        Hashtable<Integer, Node> nodeHash = new Hashtable<Integer, Node>();
        for (Node n : tempNodes)
        {
            // Initialize child lists
            n.initChildren();
            // Insert into hashtable
            nodeHash.put(n.getNodeId(), n);
        }

        // Create our tree
        Node root = new Node(0, 0, "Root", null,null, null);
        for (Node n : tempNodes)
        {
            if (n.getParentNodeId() == 0)
            {
                root.addChild(n);
            }
            else
            {
                // Use the hashtable to get the parents
                try {
                    nodeHash.get(n.getParentNodeId()).addChild(n);
                }
                catch (Exception e) {
                    Log.e(e.getLocalizedMessage(), "AH");
                }
            }
        }

        // Start with the topmost children
        loadedNodes = root.getChildren();
        
        //See how much longer to show splash screen
        long loadTime = System.currentTimeMillis() - startTime;
        long displayTime = 0;
        if(loadTime < SPLASH_DISPLAY_LENGTH)
        {
            displayTime = SPLASH_DISPLAY_LENGTH - loadTime;
        }
        
        /* New Handler to start the Menu-Activity 
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {               
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(Splash.this, SearchActivity.class);
                Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }
        }, displayTime);
    }
}