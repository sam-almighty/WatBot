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
import com.worklight.common.WLAnalytics;
import com.worklight.ibmmobilefirstplatformfoundationliveupdate.LiveUpdateManager;
import com.worklight.ibmmobilefirstplatformfoundationliveupdate.api.ConfigurationListener;
import com.worklight.jsonstore.api.JSONStoreAddOptions;
import com.worklight.jsonstore.api.JSONStoreCollection;
import com.worklight.jsonstore.api.JSONStoreInitOptions;
import com.worklight.jsonstore.api.WLJSONStore;
import com.worklight.jsonstore.database.SearchFieldType;
import com.worklight.jsonstore.exceptions.JSONStoreAddException;
import com.worklight.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLResourceRequest;
import com.worklight.wlclient.api.WLResponse;
import com.worklight.wlclient.api.WLResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
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
    private SwipeRefreshLayout swipeRefreshLayout;

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
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        listView.setAdapter(adapter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        FloatingActionButton callfab = (FloatingActionButton) findViewById(R.id.fabcall);
       // callfab.setVisibility(View.GONE);

        LiveUpdateManager.getInstance().obtainConfiguration("com.acme.chat.testusers",false, new ConfigurationListener() {

            @Override
            public void onSuccess(com.worklight.ibmmobilefirstplatformfoundationliveupdate.api.Configuration configuration) {
               if(configuration.isFeatureEnabled("com.acme.chat")){
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           fab.setVisibility(View.VISIBLE);
                       }
                   });

               }else{
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           fab.setVisibility(View.GONE);
                       }
                   });

               }

            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fab.setVisibility(View.GONE);
                    }
                });

            }
        });

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
                JSONObject json = new JSONObject();
                try {
                    json.put("Helpline usage", "Phone");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                WLAnalytics.log("HelplineCalled", json);
                Log.i("WatBotAnalytics","Helpline Called ");
                startActivity(intent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject json = new JSONObject();
                try {
                    json.put("Helpline usage", "Watson Chat");
                   // json.put("Watson Chat Clicked", "1");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                WLAnalytics.log("WatBotMessage", json);
                Log.i("WatBotAnalytics","Chat clicked ");
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);

                fetchJobs();
            }
        });

    }


    private   WLResponseListener responseListener = new WLResponseListener() {
        @Override
        public void onFailure(final WLFailResponse response) {
            // handle failure
            Log.e("JobsError", response.getErrorMsg());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                    hideProgressDialog();
                }
            });

        }

        @Override
        public void onSuccess(WLResponse response) {
            try {
               // JSONObject responseJson = new JSONObject(response.getResponseText());
               // String joblist= responseJson.getString("joblist");
                final JSONArray loadedDocuments = new JSONArray(response.getResponseText());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            populateListItems(loadedDocuments);
                            adapter.notifyDataSetInvalidated();

                            hideProgressDialog();
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }finally {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                    }
                });


            } catch(Exception e) {
                e.printStackTrace();
                // error decoding JSON data
            }
        }
    };


    private void fetchJobs(){
        swipeRefreshLayout.setRefreshing(true);
        try {
           // showProgressDialog();
            WLResourceRequest request = new WLResourceRequest(new URI("/adapters/CloudantJava/"), WLResourceRequest.GET);
          //  WLResourceRequest request = new WLResourceRequest(new URI("/adapters/JSONStoreAdapter/getPeople"), WLResourceRequest.GET);
            request.send(responseListener);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            // handle error
        }
    }


    private void populateListItems(JSONArray loadedDocuments) throws JSONException, JSONStoreAddException, JSONStoreDatabaseClosedException {
        movieList.clear();
        JSONStoreAddOptions options = new JSONStoreAddOptions();
        options.setMarkDirty(true);

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
        mProgressDialog = new ProgressDialog(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog == null) {

                    mProgressDialog.setMessage(getString(R.string.loading));
                    mProgressDialog.setIndeterminate(true);
                }

                mProgressDialog.show();
            }
        });
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onRefresh() {
        fetchJobs();
    }
}
