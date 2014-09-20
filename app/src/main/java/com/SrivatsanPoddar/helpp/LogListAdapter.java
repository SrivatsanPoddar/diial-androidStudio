package com.SrivatsanPoddar.helpp;

import android.content.Context;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class LogListAdapter<T> extends ArrayAdapter<T>
{
    T[] nodes;
    public LogListAdapter(Context context, int resource, T[] myNodes) {
        super(context, R.layout.log_list_row, R.id.log_display_text, myNodes);
        // TODO Auto-generated constructor stub
        nodes = myNodes;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        TextView displayText = (TextView) row.findViewById(R.id.log_display_text);

        //Check if the URL is an image retrievable by Square Picasso, if so, then display that image...
        String[] imageFileExtensions =  new String[] {"jpg", "png", "gif","jpeg"};
        boolean isImage = false;
        for (String extension : imageFileExtensions)
        {
            if (displayText.getText().toString().toLowerCase().endsWith(extension))
            {
                isImage = true;
                break;
            }
        }
        ImageView image = (ImageView) row.findViewById(R.id.log_image_view);
        if (isImage) {
            Picasso.with(getContext()).load(displayText.getText().toString()).into(image);

        }
        else {
            image.setVisibility(View.GONE);
        }
//        TextView storeName = (TextView) row.findViewById(R.id.store_name);
//        storeName.setText(promotions[position].store_name);
//      TextView display_text = (TextView) row.findViewById(R.id.display_text);
//      display_text.setText(promotions[position].display_text);
//        TextView expiration = (TextView) row.findViewById(R.id.expiration_dates);


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

