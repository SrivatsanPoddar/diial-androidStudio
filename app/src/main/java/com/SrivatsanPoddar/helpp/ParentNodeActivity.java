package com.SrivatsanPoddar.helpp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ParentNodeActivity extends Activity {

    public ParentNode[] parentNodes;
    Bundle state;
    private ActionBar actionBar;
    public ArrayList<Node> path = new ArrayList<Node>();
    public static ArrayList<ParentNode> favorites = new ArrayList<ParentNode>();
    private static final String TAG = "SearchActivity";
    private Call thisCall;
    private String device_id;
    private final String FILENAME = "Favorites.txt";
    EditText searchText;
    boolean favoritesVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_node);

        actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);

        state = savedInstanceState;

        //Retrieve components
        searchText = (EditText) findViewById(R.id.search_text_parent);

        //Style Components
        Style.toOpenSans(this, searchText, "light");

        device_id = Settings.Secure.getString(this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        //Create a new call
        thisCall = new Call();
        thisCall.device_id = this.device_id;
        parentNodes = Splash.parentNodes;

        if (parentNodes == null) {
            //RELOAD PARENT NODES -- NEED TO IMPLEMENT
        }

        loadFavorites();

        final Fragment cf = new CompanyFragment();
        final Fragment ff = new FavoritesFragment();

        if (state == null)
        {
            getFragmentManager().beginTransaction()
                    .add(R.id.favorites, ff).commit();
        }

        final TextView favText = (TextView) this.findViewById(R.id.favorites_text);
        favText.setOnClickListener(new View.OnClickListener() { //Toggle list of favorite companies on click
            @Override
            public void onClick(View view) {
                if(!favoritesVisible)
                {
                    getFragmentManager().beginTransaction()
                            .add(R.id.favorites, ff).commit();
                    getFragmentManager().beginTransaction()
                            .remove(cf).commit();
                    ((TextView) findViewById(R.id.favorites_text)).setText("Display All Companies");
                    favoritesVisible = true;
                }
                else
                {
                    getFragmentManager().beginTransaction()
                            .add(R.id.all_companies, cf).commit();
                    getFragmentManager().beginTransaction()
                            .remove(ff).commit();
                    ((TextView) findViewById(R.id.favorites_text)).setText("Display My Favorites");
                    favoritesVisible = false;
                }
            }
        });
    }

    /*
     * Load favorite from the file
     */
    private void loadFavorites()
    {
        FileInputStream fis = null;
        try {
            fis = openFileInput(FILENAME);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Scanner scan = new Scanner(fis);

        HashSet<String> favoriteNames = new HashSet<String>();
        while(scan.hasNextLine())
        {
            String line = scan.nextLine();
            Log.e("Reading from favorites", line);
            favoriteNames.add(line);
        }

        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(ParentNode n : parentNodes) //Add companies listed in file to favorites
        {
            if(favoriteNames.contains(n.toString()) && !favorites.contains(n))
            {
                Log.e("Adding to favorites", n.toString());
                favorites.add(n);
            }
        }
    }

    @Override
    public void onStop()
    {
        // Write the favorites to a file
        FileOutputStream fos = null;
        try
        {
            fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
        }
        catch (FileNotFoundException e)
        {
            Log.e("File not found", e.getMessage());
        }

        try
        {
            for (ParentNode n : favorites)
            {
                Log.e("Writing to favorites", n.toString());
                fos.write((n.toString() + "\n").getBytes());
            }
        }
        catch(IOException e)
        {
            Log.e("Error writing", e.getMessage());
        }

        try
        {
            fos.close();
        }
        catch(IOException e)
        {
            Log.e("Error closing file", e.getMessage());
        }
        super.onStop();
    }

    /**
     * A fragment containing the list of favorites
     */
    public static class FavoritesFragment extends ListFragment implements Callback<InstructionTree> {

        EditText searchText;
        ParentNodeListAdapter<ParentNode> listAdapter;
        ParentNode chosenNode;
        ParentNode[] fragNodes;
        boolean endActionInitiated = false; // Flag to denote that an 'end action' has been reached i.e.
                                            // phone call in order to trigger survey

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            final ParentNodeActivity act = (ParentNodeActivity) getActivity();

            // Concatenate favorites to top of list
            fragNodes = new ParentNode[act.favorites.size()];
            for(int i = 0; i < act.favorites.size(); i++)
            {
                fragNodes[i] = act.favorites.get(i);
            }

            listAdapter = new ParentNodeListAdapter<ParentNode>(getActivity(), android.R.layout.simple_list_item_1, fragNodes);
            setListAdapter(listAdapter);

            // Implement Search Functionality
            searchText = (EditText) getActivity()
                    .findViewById(R.id.search_text_parent);
            searchText.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable arg0) {
                    String text = searchText.getText().toString()
                            .toLowerCase(Locale.getDefault());
                    listAdapter.getFilter().filter(text);
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1,
                                              int arg2, int arg3) {
                    //Do nothing
                }

                @Override
                public void onTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                    //Do nothing
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            searchText.setText("");
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            chosenNode = (ParentNode) getListView().getItemAtPosition(position);

            //Retrieve instruction tree for this parent node
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setEndpoint("http://safe-hollows-9286.herokuapp.com")
                    .build();
            HerokuService nodeService = restAdapter.create(HerokuService.class);
            Map options = new HashMap<String, Integer>();
            options.put("company_id", chosenNode.company_id);
            nodeService.getInstructionTree(options, this);
        }

        public void failure(RetrofitError arg0) {
            // Print the error and close the application
            Log.e("Cant get instructions:", arg0.toString());
        }

        @Override
        public void success(InstructionTree myNodes, Response arg1) { //Successfully retrieved instruction tree for the given company

            Node rootNode = myNodes.instruction_tree[0];

            ParentNodeActivity act = (ParentNodeActivity) getActivity();
            act.path.add(rootNode); //Add the root to the call path

            //Set current time as start of call
            TimeZone UTC = TimeZone.getTimeZone("UTC");
            SimpleDateFormat dfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);
            dfUTC.setTimeZone(UTC);
            String start_time = dfUTC.format(new Date());

            //Initialize the fields for this call
            ((ParentNodeActivity) this.getActivity()).thisCall.company_id = chosenNode.company_id + "";
            ((ParentNodeActivity) this.getActivity()).thisCall.call_path = ((ParentNodeActivity)getActivity()).path;
            ((ParentNodeActivity) this.getActivity()).thisCall.start_time = start_time;
            ((ParentNodeActivity) this.getActivity()).thisCall.company_name = chosenNode.company_name;

            if (rootNode.getNodeType().equals("BRANCH")) //If this Node is not a leaf node
            {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("chosenNode", rootNode);
                intent.putExtra("thisCall", ((ParentNodeActivity) getActivity()).thisCall);
                this.startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in, R.anim.stay_still);
            }
            else if (rootNode.getNodeType().equals("PHONE") || rootNode.getNodeType().equals("TWILIO"))  //End action reached, add to path and handle call
            {
                endActionInitiated = true;

                // Add the node to favorites
                if(!act.favorites.contains(chosenNode)) {
                    act.favorites.add(chosenNode);
                    Log.e("Added to Favorites", chosenNode.toString());
                }

                //Create and set string representing the path of the chosen nodes
                String stringPath = "";
                for (Node n : ((ParentNodeActivity) getActivity()).path) {
                    stringPath = stringPath + " " + (getActivity()).getApplicationContext().getString(R.string.right_arrow) + " " + n.toString();
                }
                stringPath = stringPath.substring(2);  //Cut-off initial arrow from string display
                ((ParentNodeActivity) this.getActivity()).thisCall.call_path_string = stringPath;

                Intent intent;
                if (rootNode.getNodeType().equals("PHONE")) {   //Choose the right intent based on the type of end node reached
                    intent = new Intent(getActivity(), PhoneActivity.class);
                }
                else {
                    intent = new Intent(getActivity(), TwilioActivity.class);
                }
                intent.putExtra("thisCall", (Serializable) ((ParentNodeActivity) this.getActivity()).thisCall);
                String chosenPhoneNumber = PhoneNumberUtils.stripSeparators(rootNode.getPhoneNumber());
                intent.putExtra("phone_number", chosenPhoneNumber);

                startActivity(intent);
            }
        }
    }

    /**
     * A fragment containing all the companies
     */
    public static class CompanyFragment extends ListFragment implements Callback<InstructionTree> {

        ParentNode[] fragNodes;
        boolean endActionInitiated = false;
        EditText searchText;
        ParentNodeListAdapter<ParentNode> listAdapter;
        ParentNode chosenNode;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            final ParentNodeActivity act = (ParentNodeActivity) getActivity();

            fragNodes = act.parentNodes; //Set nodes
            listAdapter = new ParentNodeListAdapter<ParentNode>(getActivity(), android.R.layout.simple_list_item_1, fragNodes);
            setListAdapter(listAdapter);

            //Add company as favorite when long clicked
            getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
                     ParentNode temp = (ParentNode) getListView().getItemAtPosition(position);
                     listAdapter.nodes[position] = listAdapter.nodes[ParentNodeActivity.favorites.size()];
                     listAdapter.nodes[ParentNodeActivity.favorites.size()] = temp;

                    if(!ParentNodeActivity.favorites.contains(temp)) { // Add to favorites, change color
                        ParentNodeActivity.favorites.add(temp);
                        Style.makeToast(act, temp + " added to Favorites");
                        Log.e("Adding favorite", temp.toString());
                    }
                    return true;
                }
            });

            // Set up google search listener
            final Button button = (Button) getActivity().findViewById(R.id.button);
            button.setVisibility(View.GONE);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, searchText.getText().toString() + " customer support"); //Search for query when Google button pressed
                    startActivity(intent);
                }
            });

            // Implement Search Functionality
            searchText = (EditText) getActivity().findViewById(R.id.search_text_parent);
            searchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable arg0) {
                    String text = searchText.getText().toString()
                            .toLowerCase(Locale.getDefault());
                    listAdapter.getFilter().filter(text);

                    //Show or hide button
                    if (!searchText.getText().toString().equals("")) {
                        button.setVisibility(View.VISIBLE);
                        button.setText("Search Google for '" + searchText.getText().toString() + "'"); //Show button on typing
                    } else {
                        button.setVisibility(View.GONE);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1,
                                              int arg2, int arg3) {
                    //Do nothing
                }

                @Override
                public void onTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                    //Do nothing
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            searchText.setText("");
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            chosenNode = (ParentNode) getListView().getItemAtPosition(position);

            //Retrieve instruction tree for this parent node
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setEndpoint("http://safe-hollows-9286.herokuapp.com")
                    .build();
            HerokuService nodeService = restAdapter.create(HerokuService.class);
            Map options = new HashMap<String, Integer>();
            options.put("company_id", chosenNode.company_id);
            nodeService.getInstructionTree(options, this);
        }

        public void failure(RetrofitError arg0) { //Failed to get the instructions
            Log.e("Can't get instructions:", arg0.toString());
        }

        @Override
        public void success(InstructionTree myNodes, Response arg1) { //Retrieved instruction tree for the given company, add to path and transition

            endActionInitiated = true;

            Node rootNode = myNodes.instruction_tree[0];
            ParentNodeActivity act = (ParentNodeActivity) getActivity();
            act.path.add(rootNode);

            //Set current time as start of call
            TimeZone UTC = TimeZone.getTimeZone("UTC");
            SimpleDateFormat dfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);
            dfUTC.setTimeZone(UTC);
            String start_time = dfUTC.format(new Date());

            //Set call fields
            ((ParentNodeActivity) this.getActivity()).thisCall.company_id = chosenNode.company_id + "";
            ((ParentNodeActivity) this.getActivity()).thisCall.call_path = ((ParentNodeActivity)getActivity()).path;
            ((ParentNodeActivity) this.getActivity()).thisCall.start_time = start_time;
            ((ParentNodeActivity) this.getActivity()).thisCall.company_name = chosenNode.company_name;

            //Determine the type of the tapped node
            if (rootNode.getNodeType().equals("BRANCH"))
            {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("chosenNode", rootNode);
                intent.putExtra("thisCall", ((ParentNodeActivity) getActivity()).thisCall);
                this.startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in, R.anim.stay_still);
            }
            else if (rootNode.getNodeType().equals("PHONE") || rootNode.getNodeType().equals("TWILIO"))  //End action reached
            {
                // Add the node to favorites
                if(!act.favorites.contains(chosenNode)) {
                    act.favorites.add(chosenNode);
                    Log.e("Added to Favorites", chosenNode.toString());
                }

                //Create string representing the path of the chosen nodes
                String stringPath = "";
                for (Node n : ((ParentNodeActivity) getActivity()).path) {
                    stringPath = stringPath + " " + (getActivity()).getApplicationContext().getString(R.string.right_arrow) + " " + n.toString();
                }
                stringPath = stringPath.substring(2);  //Cut-off initial arrow from string display
                ((ParentNodeActivity) this.getActivity()).thisCall.call_path_string = stringPath;

                Intent intent;
                if (rootNode.getNodeType().equals("PHONE")) { //Choose the right intent based on the type of end node reached
                    intent = new Intent(getActivity(), PhoneActivity.class);
                }
                else {
                    intent = new Intent(getActivity(), TwilioActivity.class);
                }
                intent.putExtra("thisCall", (Serializable) ((ParentNodeActivity) this.getActivity()).thisCall);
                String chosenPhoneNumber = PhoneNumberUtils.stripSeparators(rootNode.getPhoneNumber());
                intent.putExtra("phone_number", chosenPhoneNumber);

                startActivity(intent);
            }

        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.parent_node, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.e("Menu Item Id", item.getItemId()+"");
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, ParentNodeActivity.class);
                startActivity(intent);
                return true;
            case R.id.call_log:
                intent = new Intent(this, LogListActivity.class);
                intent.putExtra("device_id", this.device_id);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
