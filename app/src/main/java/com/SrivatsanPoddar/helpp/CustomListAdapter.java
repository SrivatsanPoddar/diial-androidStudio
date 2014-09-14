package com.SrivatsanPoddar.helpp;

import android.content.Context;
import android.text.util.Linkify;
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

        Style.toOpenSans(getContext(), displayText, "light");
        displayText.setAutoLinkMask(Linkify.ALL);
        //displayText.setText(nodes[position].toString());
        displayText.setText(displayText.getText());
//        Style.toOpenSans(getContext(), expiration, "light");
//        Style.toOpenSans(getContext(), storeName, "light");
        return(row);
    }
    

}

