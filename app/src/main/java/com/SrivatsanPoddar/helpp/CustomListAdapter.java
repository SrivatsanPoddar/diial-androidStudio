package com.SrivatsanPoddar.helpp;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

//ListAdapter for the list of companies shown
public class CustomListAdapter<T> extends ArrayAdapter<T> 
{
    T[] nodes;
    
    public CustomListAdapter(Context context, int resource, T[] myNodes) {
        super(context, R.layout.search_list_row, R.id.display_text, myNodes);
        nodes = myNodes;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        TextView displayText = (TextView) row.findViewById(R.id.display_text);
        
        if(SearchActivity.favorites.contains(nodes[position])) //Set the background if the company is in the favorites
        {
            Log.e("In favorites", nodes[position].toString());
            row.setBackgroundResource(R.color.light_blue);
        }
        else
        {
            row.setBackgroundResource(0);
        }

        Style.toOpenSans(getContext(), displayText, "light"); //Style font
        displayText.setText(displayText.getText());
        return(row);
    }
    

}

