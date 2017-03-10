package com.example.vmac.WatBot;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.ibm.WatBot.R;
import com.worklight.jsonstore.api.JSONStoreAddOptions;
import com.worklight.jsonstore.api.JSONStoreCollection;
import com.worklight.jsonstore.api.JSONStoreInitOptions;
import com.worklight.jsonstore.api.WLJSONStore;
import com.worklight.jsonstore.database.SearchFieldType;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLResourceRequest;
import com.worklight.wlclient.api.WLResponse;
import com.worklight.wlclient.api.WLResponseListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private List<Jobs> movieList = new ArrayList<Jobs>();
    private ListView listView;
    private CustomListAdapter adapter;
    private String user_id;
    private boolean displayChatIcon=true;
    private static final String JOBS_COLLECTION_NAME = "printerjobs";
    private static final String DEFAULT_USER = "jsonstore";

    private ProgressDialog mProgressDialog;
    private JSONStoreCollection jobs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Bundle bundle = getIntent().getExtras();
        user_id = bundle.getString("user_id");
        listView = (ListView) findViewById(R.id.list);
        adapter = new CustomListAdapter(this, movieList);
        listView.setAdapter(adapter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        FloatingActionButton callfab = (FloatingActionButton) findViewById(R.id.fabcall);

        if (displayChatIcon) {
            fab.setVisibility(View.VISIBLE);
            callfab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.GONE);
            callfab.setVisibility(View.VISIBLE);
        }
        try {

            List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
            jobs = new JSONStoreCollection(JOBS_COLLECTION_NAME);
            jobs.setSearchField("printername", SearchFieldType.STRING);
            jobs.setSearchField("repairtype", SearchFieldType.STRING);
            collections.add(jobs);
            JSONStoreInitOptions initOptions = new JSONStoreInitOptions();
            initOptions.setUsername(DEFAULT_USER);
            WLJSONStore.getInstance(this).openCollections(collections, initOptions);
        } catch (final Exception e) {
            Log.e("JobsError",e.getMessage());
        }

        try {
            showProgressDialog();
            WLResourceRequest request = new WLResourceRequest(new URI("/adapters/JSONStoreAdapter/getPeople"), WLResourceRequest.GET);
            request.send(responseListener);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            // handle error
        }



        callfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private   WLResponseListener responseListener = new WLResponseListener() {
        @Override
        public void onFailure(final WLFailResponse response) {
            // handle failure
            Log.e("JobsError", response.getErrorMsg());
            hideProgressDialog();
        }

        @Override
        public void onSuccess(WLResponse response) {
            try {
                JSONObject responseJson = new JSONObject(response.getResponseText());
                String joblist= responseJson.getString("joblist");
                final JSONArray loadedDocuments = new JSONArray(joblist);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        populateListItems(loadedDocuments);
                        adapter.notifyDataSetInvalidated();
                        hideProgressDialog();
                    }
                });


            } catch(Exception e) {
                e.printStackTrace();
                // error decoding JSON data
            }
        }
    };
    private void populateListItems(JSONArray loadedDocuments){
        JSONStoreAddOptions options = new JSONStoreAddOptions();
        options.setMarkDirty(true);
        try {
            for(int index=0;index<loadedDocuments.length();index++){
                Jobs jobObject = new Jobs();
                JSONObject jsonObject = (JSONObject) loadedDocuments.get(index);
                jobObject.setPolicyNumber(jsonObject.getString("printername"));
                jobObject.setStartDate(jsonObject.getString("repairtype"));
                jobObject.setEndDate(jsonObject.getString("errordescription"));
                jobObject.setTypeOfPolicy(jsonObject.getString("address"));
                this.jobs.addData(jobObject.getJson(),options);
                movieList.add(jobObject);
            }

        } catch (Exception e) {
           Log.e("JSONStore",e.getMessage());
        }


       /* Insurance insurance = new Insurance();
        insurance.setPolicyNumber("Acme LaserJet 400 M401");
        insurance.setStartDate("Repair");
        insurance.setEndDate("Paper load error");
        insurance.setTypeOfPolicy("2050 Bamako Place, WA 20521-2050");
        movieList.add(insurance);

        Insurance insurance1 = new Insurance();
        insurance1.setPolicyNumber("Acme Aficio CL3500N PS");
        insurance1.setStartDate("Maintenance");
        insurance1.setEndDate("Regular AMC service");
        insurance1.setTypeOfPolicy("7100 Athens Place, WA 20521-7100");
        movieList.add(insurance1);

        Insurance insurance2 = new Insurance();
        insurance2.setPolicyNumber("Acme Color LaserJet 3800");
        insurance2.setStartDate("General Service");
        insurance2.setEndDate("Monthly service");
        insurance2.setTypeOfPolicy("8400 London Place, WA 20521-8400");
        movieList.add(insurance2);

        Insurance insurance3 = new Insurance();
        insurance3.setPolicyNumber("Acme LaserJet 7200 Series");
        insurance3.setStartDate("Repair");
        insurance3.setEndDate("Power issues");
        insurance3.setTypeOfPolicy("5520 Quebec Place, WA 20521-5520");
        movieList.add(insurance3);

        Insurance insurance4 = new Insurance();
        insurance4.setPolicyNumber("Acme LaserJet 400 M401");
        insurance4.setStartDate("General Service");
        insurance4.setEndDate("Regular servicing");
        insurance4.setTypeOfPolicy("6170 Peshwar Place, WA 20521-6170");
        movieList.add(insurance4);*/
    }

    private String getJsonString(){
        String jsonString="[\n" +
                "  {\n" +
                "\t\t\"printername\": \"Acme LaserJet 400 M401\",\n" +
                "\t\t\"repairtype\": \"Repair\",\n" +
                "\t\t\"errordescription\": \"Paper load error\",\n" +
                "\t\t\"address\": \"#87, 6th cross, Cambridge Layout, Bangalore\"\n" +
                "\t}, {\n" +
                "\t\t\"printername\": \"Acme Aficio CL3500N PS\",\n" +
                "\t\t\"repairtype\": \"Maintenance\",\n" +
                "\t\t\"errordescription\": \"Regular AMC service\",\n" +
                "\t\t\"address\": \"#201/1, 10th cross, Jayanagar, Bangalore\"\n" +
                "\t}, {\n" +
                "\t\t\"printername\": \"Acme Color LaserJet 3800\",\n" +
                "\t\t\"repairtype\": \"General Service\",\n" +
                "\t\t\"errordescription\": \"Monthly service\",\n" +
                "\t\t\"address\": \"#27, UB City, Lavelle Road, Bangalore\"\n" +
                "\t}, {\n" +
                "\t\t\"printername\": \"Acme LaserJet 7200 Series\",\n" +
                "\t\t\"repairtype\": \"Repair\",\n" +
                "\t\t\"errordescription\": \"Power issues\",\n" +
                "\t\t\"address\": \"#16/1, Indiranagar, Bangalore\"\n" +
                "\t}, {\n" +
                "\t\t\"printername\": \"Acme LaserJet 415 M401\",\n" +
                "\t\t\"repairtype\": \"General servicing\",\n" +
                "\t\t\"errordescription\": \"Regular Service\",\n" +
                "\t\t\"address\": \"#16/1, Indiranagar, Bangalore\"\n" +
                "\t}\n" +
                "]";
        return  jsonString;
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onRefresh() {

    }
}
