package com.SrivatsanPoddar.helpp;

import android.graphics.Color;

import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.*;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

@SuppressWarnings("unused")
public class SearchActivity extends Activity
{
    public Node[] nodes;
    Bundle state;
    private ActionBar actionBar;
    private ArrayList<Node> path = new ArrayList<Node>();
    private static final String TAG = "SearchActivity";
    private Call thisCall;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        state = savedInstanceState;
        Bundle extras = this.getIntent().getExtras();
        
        setContentView(R.layout.activity_search);
        actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);        
        EditText searchText = (EditText) findViewById(R.id.search_text);
        Style.toOpenSans(this, searchText, "light");
        
        // Check if this activity was started clicking of non-root node. If so,
        // find and display children of that node
        if (extras != null)
        {            
            // Hide search bar since we're in a tree
            searchText.setVisibility(View.GONE);
            
            Node chosenNode = (Node) extras.getSerializable("chosenNode");
            path = (ArrayList<Node>) extras.getSerializable("path");
            thisCall = (Call) extras.getSerializable("thisCall");
            
            nodes = chosenNode.getChildren();
            //path.add(chosenNode);  //Add chosen node to path of traveled nodes
            if (state == null)
            {
                getFragmentManager().beginTransaction()
                        .add(R.id.frame_layout, new PlaceholderFragment()).commit();
            }
        }
        // Otherwise we are coming from splash loader
        else
        {
            thisCall = new Call();
            thisCall.device_id = Secure.getString(this.getApplicationContext().getContentResolver(),Secure.ANDROID_ID);
            nodes = Splash.loadedNodes;
            if (state == null)
            {
                getFragmentManager().beginTransaction()
                        .add(R.id.frame_layout, new PlaceholderFragment()).commit();
            }
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        Log.e("Menu Item Id", item.getItemId()+"");
        
        switch (item.getItemId()) {
            case android.R.id.home:
                //Do stuff
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    
    
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends ListFragment
    {

        Node[] fragNodes;
        boolean endActionInitiated = false; // Flag to denote that an 'end
                                            // action' has been reached i.e.
                                            // phone call in order to trigger
                                            // survey
        EditText searchText;
        CustomListAdapter<Node> adapter;
        Node chosenNode;
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            super.onActivityCreated(savedInstanceState);
            SearchActivity act = (SearchActivity) getActivity();
            fragNodes = act.nodes;
            
            adapter = new CustomListAdapter<Node>(getActivity(),android.R.layout.simple_list_item_1,fragNodes);
            //aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //promotionsList.setAdapter(aa);
            setListAdapter(adapter);
            
            // Set up google search listener
            final Button button = (Button) getActivity().findViewById(R.id.search_button);
            button.setVisibility(View.GONE);
            button.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);   
                    intent.putExtra(SearchManager.QUERY, searchText.getText().toString() + " customer support");    
                    startActivity(intent);
                }
            });

            // Implement Search Functionality
            searchText = (EditText) getActivity()
                    .findViewById(R.id.search_text);
            searchText.addTextChangedListener(new TextWatcher()
            {

                @Override
                public void afterTextChanged(Editable arg0)
                {
                    String text = searchText.getText().toString()
                            .toLowerCase(Locale.getDefault());
                    adapter.getFilter().filter(text);
                    
                    //Show or hide button
                    if(!searchText.getText().toString().equals(""))
                    {
                        button.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        button.setVisibility(View.GONE);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1,
                        int arg2, int arg3)
                {
                }

                @Override
                public void onTextChanged(CharSequence arg0, int arg1,
                        int arg2, int arg3)
                {
                }
            });
        }

        @Override
        public void onResume()
        {
            super.onResume();

            // If the fragment restarts after an end action was performed,
            // then start the survey activity
            if (endActionInitiated)
            {
                
                Intent intent = new Intent(getActivity(), SurveyActivity.class);
                
                intent.putExtra("company_id", chosenNode.getCompanyId());
                intent.putExtra("thisCall", ((SearchActivity) this.getActivity()).thisCall);
                
                this.startActivity(intent);
            }
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id)
        {
            super.onListItemClick(l, v, position, id);
            chosenNode = (Node) getListView().getItemAtPosition(position);
            ((SearchActivity)getActivity()).path.add(chosenNode);  //Add chosen node to path of traveled nodes
            //chosenNode = fragNodes[position];
            Log.e("Reached", position + "");
            // Node[] childrenOfChosenNode = chosenNode.childrenNodes;


            if (chosenNode.getNodeType().equals("BRANCH"))
            {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("chosenNode", chosenNode);
                intent.putExtra("path", ((SearchActivity)getActivity()).path);
                intent.putExtra("thisCall", ((SearchActivity) getActivity()).thisCall);
                this.startActivity(intent);
            }
            else if (chosenNode.getNodeType().equals("PHONE") || chosenNode.getNodeType().equals("TWILIO"))  //End action reached
            {
                String stringPath = "";

                //Create string representing the path of the chosen nodes
                for (Node n : ((SearchActivity) getActivity()).path) {
                    stringPath = stringPath + " --> " + n.toString();
                }
                //stringPath = stringPath + " --> " + chosenNode.toString();
                stringPath = stringPath.substring(4);  //Cut-off initial arrow from string display

                //Set current time as start of call
                TimeZone UTC = TimeZone.getTimeZone("UTC");
                SimpleDateFormat dfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);
                dfUTC.setTimeZone(UTC);
                String start_time = dfUTC.format(new Date());

                ((SearchActivity) this.getActivity()).thisCall.company_id = chosenNode.getCompanyId();
                ((SearchActivity) this.getActivity()).thisCall.call_path = ((SearchActivity)getActivity()).path.toArray(new Node[0]);
                ((SearchActivity) this.getActivity()).thisCall.start_time = start_time;
                ((SearchActivity) this.getActivity()).thisCall.call_path_string = stringPath;

                Intent intent;

                //Choose the right intent based on the type of end node reached
                if (chosenNode.getNodeType().equals("PHONE")) {
                    intent = new Intent(getActivity(), PhoneActivity.class);
                }
                else {
                    intent = new Intent(getActivity(), TwilioActivity.class);
                }

                intent.putExtra("company_id", chosenNode.getCompanyId());
                intent.putExtra("string_path", stringPath);
                intent.putExtra("thisCall", (Serializable) ((SearchActivity) this.getActivity()).thisCall);

                String chosenPhoneNumber = PhoneNumberUtils.stripSeparators(chosenNode.getPhoneNumber());
                intent.putExtra("phone_number", chosenPhoneNumber);

                endActionInitiated = true;
                startActivity(intent);
            }
        }
    }
}
