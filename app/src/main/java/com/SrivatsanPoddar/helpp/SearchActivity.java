package com.SrivatsanPoddar.helpp;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/*
 * Activity used to search through instruction nodes before reaching the call end point
 */
@SuppressWarnings("unused")
public class SearchActivity extends Activity
{
    //Activity Variables
    ActionBar actionBar;
    Node[] nodes;
    Bundle state;
    ArrayList<Node> path = new ArrayList<Node>();
    static ArrayList<Node> favorites = new ArrayList<Node>();
    static final String TAG = "SearchActivity";
    Call thisCall;
    String device_id;
    final String FILENAME = "Favorites.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        state = savedInstanceState;

        actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);

        //Get intent data
        Bundle extras = this.getIntent().getExtras();
        Node chosenNode = (Node) extras.getSerializable("chosenNode");
        thisCall = (Call) extras.getSerializable("thisCall");
        path = thisCall.call_path;
        device_id = thisCall.device_id;
        nodes = chosenNode.getChildren();

        if (state == null)
        {
            getFragmentManager().beginTransaction()
                    .add(R.id.frame_layout, new PlaceholderFragment()).commit();
        }
    }

    /**
     * A fragment housing the list of instructions
     */
    public static class PlaceholderFragment extends ListFragment
    {

        Node[] fragNodes;
        boolean endActionInitiated = false; // Flag to denote that an 'end action' has been reached i.e.
                                            // phone call in order to trigger survey

        CustomListAdapter<Node> adapter;
        Node chosenNode;
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            super.onActivityCreated(savedInstanceState);

            final SearchActivity act = (SearchActivity) getActivity();
            fragNodes = act.nodes;
            adapter = new CustomListAdapter<Node>(getActivity(),android.R.layout.simple_list_item_1,fragNodes);
            setListAdapter(adapter);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) //Go to next intstruction or make the call
        {
            super.onListItemClick(l, v, position, id);

            chosenNode = (Node) getListView().getItemAtPosition(position);
            ((SearchActivity)getActivity()).path.add(chosenNode);  //Add chosen node to path of traveled nodes
            ((SearchActivity) this.getActivity()).thisCall.call_path = ((SearchActivity)getActivity()).path;
            Log.e("Reached", position + "");

            if (chosenNode.getNodeType().equals("BRANCH")) //If the tapped node is a branch
            {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("chosenNode", chosenNode);
                intent.putExtra("thisCall", ((SearchActivity) getActivity()).thisCall);
                this.startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in,R.anim.stay_still);
            }
            else if (chosenNode.getNodeType().equals("PHONE") || chosenNode.getNodeType().equals("TWILIO"))  //End action reached
            {
                endActionInitiated = true;

                //Create string representing the path of the chosen nodes
                String stringPath = "";
                for (Node n : ((SearchActivity) getActivity()).path) {
                    stringPath = stringPath + " " + ((SearchActivity) getActivity()).getApplicationContext().getString(R.string.right_arrow) + " " + n.toString();
                }
                stringPath = stringPath.substring(2);  //Cut-off initial arrow from string display
                ((SearchActivity) this.getActivity()).thisCall.call_path_string = stringPath;

                Intent intent;
                if (chosenNode.getNodeType().equals("PHONE")) { //Choose the right intent based on the type of end node reached
                    intent = new Intent(getActivity(), PhoneActivity.class);
                }
                else {
                    intent = new Intent(getActivity(), TwilioActivity.class);
                }
                intent.putExtra("thisCall", (Serializable) ((SearchActivity) this.getActivity()).thisCall);
                String chosenPhoneNumber = PhoneNumberUtils.stripSeparators(chosenNode.getPhoneNumber());
                intent.putExtra("phone_number", chosenPhoneNumber);
                startActivity(intent);
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_activity_actions, menu);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.slide_out);
    }
}
