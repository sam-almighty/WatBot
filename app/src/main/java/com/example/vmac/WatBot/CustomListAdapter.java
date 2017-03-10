package com.example.vmac.WatBot;

/**
 * Created by norton on 3/7/17.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibm.WatBot.R;

import java.util.List;

public class CustomListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Jobs> jobsItems;


    public CustomListAdapter(Activity activity, List<Jobs> jobsItems) {
        this.activity = activity;
        this.jobsItems = jobsItems;
    }

    @Override
    public int getCount() {
        return jobsItems.size();
    }

    @Override
    public Object getItem(int location) {
        return jobsItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.listrow, null);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.priority);
        TextView policyNumberText = (TextView) convertView.findViewById(R.id.policynumbertext);
        TextView startDateText = (TextView) convertView.findViewById(R.id.startdatetext);
        TextView endDateText = (TextView) convertView.findViewById(R.id.enddatetext);
        TextView policyTypeText = (TextView) convertView.findViewById(R.id.policytypetext);



        // getting movie data for the row
        Jobs jobs = jobsItems.get(position);

        if("Repair".equals(jobs.getStartDate())){
            imageView.setImageResource(R.drawable.red);
        }else if("Maintenance".equals(jobs.getStartDate())){
            imageView.setImageResource(R.drawable.green);
        }else{
            imageView.setImageResource(R.drawable.yellow);
        }

        // title
        policyNumberText.setText(jobs.getPolicyNumber());

        // rating
        startDateText.setText(jobs.getStartDate());

        endDateText.setText(jobs.getEndDate());

        // release year
        policyTypeText.setText(jobs.getTypeOfPolicy());

        return convertView;
    }

}