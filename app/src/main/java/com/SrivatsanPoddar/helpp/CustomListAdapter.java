package com.SrivatsanPoddar.helpp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomListAdapter<T> extends ArrayAdapter<T> 
{
    T[] nodes;
    
    public CustomListAdapter(Context context, int resource, T[] myNodes) {
        super(context, R.layout.search_list_row, R.id.display_text, myNodes);
        // TODO Auto-generated constructor stub
        nodes = myNodes;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
//        TextView storeName = (TextView) row.findViewById(R.id.store_name);
//        storeName.setText(promotions[position].store_name);
//      TextView display_text = (TextView) row.findViewById(R.id.display_text);
//      display_text.setText(promotions[position].display_text);
//        TextView expiration = (TextView) row.findViewById(R.id.expiration_dates);
        TextView displayText = (TextView) row.findViewById(R.id.display_text);

        //expiration.setText(("valid " + convertDate(promotions[position].start_date) + "-" + convertDate(promotions[position].end_date)));

        // Set color if item in favorites
        /** SharedPreferences prefs = getContext().getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        int numFavorites = prefs.getInt("numFavorites", 0);
        Log.e("position", position + "");
        if(numFavorites > position) {
            row.setBackgroundResource(R.drawable.abc_list_selector_background_transition_holo_light);
        } **/
        
        if(SearchActivity.favorites.contains(nodes[position]))
        {
            Log.e("In favorites", nodes[position].toString());
<<<<<<< HEAD
            row.setBackgroundResource(R.color.light_blue);
=======
            row.setBackgroundResource(R.drawable.favorites_color);
>>>>>>> 4aca2ef7ac9a26bb9c6a09d5c9bc050977cca448
        }
        else
        {
            row.setBackgroundResource(0);
        }

        Style.toOpenSans(getContext(), displayText, "light");
        displayText.setAutoLinkMask(Linkify.ALL);
        //displayText.setText(nodes[position].toString());
        displayText.setText(displayText.getText());
//        Style.toOpenSans(getContext(), expiration, "light");
//        Style.toOpenSans(getContext(), storeName, "light");
        return(row);
    }
    

}

