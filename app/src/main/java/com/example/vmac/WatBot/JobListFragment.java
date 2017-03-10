package com.example.vmac.WatBot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ibm.WatBot.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by norton on 3/10/17.
 */

public class JobListFragment extends Fragment {
    private List<Jobs> movieList = new ArrayList<Jobs>();
    private ListView listView;
    private CustomListAdapter adapter;
    private String user_id;
    private boolean displayChatIcon=true;

    public JobListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_one, container, false);
        // Inflate the layout for this fragment
        populateListItems();
        listView = (ListView) rootView.findViewById(R.id.list);
        adapter = new CustomListAdapter(getActivity(), movieList);
        listView.setAdapter(adapter);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);


        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        FloatingActionButton callfab = (FloatingActionButton) rootView.findViewById(R.id.fabcall);

        if (displayChatIcon) {
            fab.setVisibility(View.VISIBLE);
            callfab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.GONE);
            callfab.setVisibility(View.VISIBLE);
        }

        callfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(intent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });

        return rootView;

    }

    private void populateListItems(){
        Jobs jobs = new Jobs();
        jobs.setPolicyNumber("Acme LaserJet 400 M401");
        jobs.setStartDate("Repair");
        jobs.setEndDate("Paper load error");
        jobs.setTypeOfPolicy("2050 Bamako Place, WA 20521-2050");
        movieList.add(jobs);

        Jobs jobs1 = new Jobs();
        jobs1.setPolicyNumber("Acme Aficio CL3500N PS");
        jobs1.setStartDate("Maintenance");
        jobs1.setEndDate("Regular AMC service");
        jobs1.setTypeOfPolicy("7100 Athens Place, WA 20521-7100");
        movieList.add(jobs1);

        Jobs jobs2 = new Jobs();
        jobs2.setPolicyNumber("Acme Color LaserJet 3800");
        jobs2.setStartDate("General Service");
        jobs2.setEndDate("Monthly service");
        jobs2.setTypeOfPolicy("8400 London Place, WA 20521-8400");
        movieList.add(jobs2);

        Jobs jobs3 = new Jobs();
        jobs3.setPolicyNumber("Acme LaserJet 7200 Series");
        jobs3.setStartDate("Repair");
        jobs3.setEndDate("Power issues");
        jobs3.setTypeOfPolicy("5520 Quebec Place, WA 20521-5520");
        movieList.add(jobs3);

        Jobs jobs4 = new Jobs();
        jobs4.setPolicyNumber("Acme LaserJet 400 M401");
        jobs4.setStartDate("General Service");
        jobs4.setEndDate("Regular servicing");
        jobs4.setTypeOfPolicy("6170 Peshwar Place, WA 20521-6170");
        movieList.add(jobs4);
    }

}