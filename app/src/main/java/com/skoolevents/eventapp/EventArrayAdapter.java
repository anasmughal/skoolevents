package com.skoolevents.eventapp;

import android.app.Activity;
import android.content.Context;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.models.nosql.SkooleventsDO;
import com.skoolevents.eventapp.R;

import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */

public class EventArrayAdapter extends ArrayAdapter {

    private Context context;
    private List<SkooleventsDO> eventList;


    /**
     * Constructor
     *
     * @param context
     * @param resource
     * @param objects
     */
    public EventArrayAdapter(Context context, int resource, ArrayList<SkooleventsDO> objects) {
        super(context, resource, objects);

        this.context = context;
        this.eventList = objects;
    }



    /**
     * called when rendering the list
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        //get the property we are displaying
        SkooleventsDO event = eventList.get(position);

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.event_layout, null);

        TextView description = (TextView) view.findViewById(R.id.description);
        TextView date = (TextView) view.findViewById(R.id.date);
        TextView title = (TextView) view.findViewById(R.id.title);


        //set address and description
        String descriptionShortened = this.shortenString(event.getDescription());
        description.setText(descriptionShortened);

        title.setText(event.getTitle());

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM, yyyy 'at' HH:mm");
        long dateAsLong = event.getDate().longValue();
        String dateString = sdf.format(new Date( dateAsLong ));
        date.setText(dateString);

        return view;
    }


    /**
     *
     * @param origString
     * @return
     */
    private String shortenString(String origString) {

        if (origString == null) return "";

        //display trimmed excerpt for description
        int descriptionLength = origString.length();
        if(descriptionLength >= 100){
            String descriptionTrim = origString.substring(0, 100) + "...";
            return descriptionTrim;
        }else{
            return origString;
        }
    }



}
