package com.SrivatsanPoddar.helpp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/*
 * ListAdapter for the Main Parent Company List
 */
public class ParentNodeListAdapter<T> extends ArrayAdapter<T>
{
    T[] nodes;

    public ParentNodeListAdapter(Context context, int resource, T[] myNodes) {
        super(context, R.layout.search_list_row, R.id.display_text, myNodes);
        nodes = myNodes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        TextView displayText = (TextView) row.findViewById(R.id.display_text);
        Style.toOpenSans(getContext(), displayText, "light");
        displayText.setText(displayText.getText());
        return(row);
    }
}
