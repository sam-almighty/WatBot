package com.example.vmac.WatBot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

 public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
           
//            //------------- starter code added by the MFP plugin
//            WLClient.createInstance(this);
//            WLClient mfpClient = WLClient.getInstance();
//            mfpClient.connect(new WLResponseListener() {
//            @Override
//            public void onSuccess(WLResponse wlResponse) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(SplashActivity.this, "Successfully Connected to MFP", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//            }
//
//            @Override
//            public void onFailure(final WLFailResponse wlFailResponse) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(SplashActivity.this, "Failed to connect to MFP: "+wlFailResponse.getErrorMsg() , Toast.LENGTH_LONG).show();
//                    }
//                });
//
//                Log.e("Error",wlFailResponse.getErrorMsg());
//            }
//        });
        //-------------- end MFP code

        Intent intent = new Intent(this, SocialAuthActivity.class);
        startActivity(intent);
        finish();
    }
}
