/**
 *    © Copyright 2016 IBM Corp.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.example.vmac.WatBot;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.ibm.WatBot.R;
import com.worklight.common.Logger;
import com.worklight.wlclient.api.WLAuthorizationManager;
import com.worklight.wlclient.api.WLClient;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLLoginResponseListener;
import com.worklight.wlclient.api.WLResourceRequest;
import com.worklight.wlclient.api.WLResponse;
import com.worklight.wlclient.api.WLResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URL;

/**
 * SocialMainActivity - activity which demonstrate the following:
 * 1. Logged in into 'socialLogin' (with Google or with Facebook) security check.
 * 2. Invoke protected resource adapter "/hello" which protected with 'socialLogin' security check.
 *
 * @author Ishai Borovoy
 * @since 14/03/2016
 */
public class SocialMainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    public static final String FACEBOOK_PERMISSIONS = "public_profile,email";

    private static final String SOCIAL_LOGIN_TAG = SocialMainActivity.class.getPackage().getName();
    private static final int GOOGLE_GET_TOKEN_INTENT = 9002;
    public static final String SOCIAL_LOGIN_SCOPE = "accessRestricted";

    //Flag to know from where we signInWithGoogle
    protected boolean isSignInFromChallenge = false;
    protected Vendor currentVendor = Vendor.GOOGLE;

    //Google SignIn
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions googleSignInOptions;
    private SocialLoginChallengeHandler socialLoginChallengeHandler;
    private TextView statusView;
    private ImageView userPictureView;
    //Logger
    private Logger wlLogger;

    //Vendor enum
    protected enum Vendor {
        GOOGLE("google"),
        Facebook("facebook");
        private final String value;

        /**
         * @param value the vendor string value
         */
        Vendor(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_GET_TOKEN_INTENT) {
            // Google
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        wlLogger.debug("onConnectionFailed:" + connectionResult);
    }

    /**
     * Click listener
     *
     * @param v the clicked view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_google:
                this.isSignInFromChallenge = false;
                currentVendor = Vendor.GOOGLE;
                signInWithGoogle();
                break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Init Activity GUI
        setContentView(R.layout.social_auth);
        findViewById(R.id.sign_in_google).setOnClickListener(this);

        getSupportActionBar().hide();
        statusView = (TextView) findViewById(R.id.statusTextView);


      //  socialLoginChallengeHandler = new SocialLoginChallengeHandler("socialLogin", this);

        //Register the SocialLoginChallengeHandler
        WLClient.getInstance().registerChallengeHandler(socialLoginChallengeHandler);

        //Init social platforms SDKs
        initGoogleSDK();


        wlLogger = Logger.getInstance(SOCIAL_LOGIN_TAG);
        wlLogger.debug(getClass().getName() + " init");
        Logger.send();
    }

    /**
     * Sign in to Google.  On success call to login into socialLogin scope
     */
    protected void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_GET_TOKEN_INTENT);
    }


    /**
     * Login to MFP server with the token which returned from the vendor (Google/Facebook)
     *
     * @param vendor - vendor user has logged in with
     * @param token  -  the returned token from the vendor (Google/Facebook)
     */
    private void loginToMFPWithSocialVendor(final String vendor, String token) {
        JSONObject credentials = createJSONCredentials(vendor, token);
        if (credentials == null) return;

        WLAuthorizationManager.getInstance().login("socialLogin", credentials, new WLLoginResponseListener() {
            @Override
            public void onSuccess() {
                final String msg = String.format("Logged in successfully with %s", vendor);
                updateStatus(msg);
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                String msg = String.format("Logged in failed with %s", vendor);
                updateStatus(msg);
            }
        });
    }


    /**
     * Init the Google SDK
     */
    private void initGoogleSDK() {
        if (googleSignInOptions == null) {

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
//
//            googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                    .requestIdToken(SocialMainActivity.this.getResources().getString(R.string.google_server_client_id)).requestEmail()
//                    .build();
//
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .enableAutoManage(this /*Activity*/, this /* OnConnectionFailedListener */)
//                    .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
//                    .build();
        }
    }

    /**
     * Handle Google Sign In
     *
     * @param result result that return from onActivityResult
     */
    private void handleGoogleSignInResult(GoogleSignInResult result) {
        wlLogger.debug("handleSignInResult (Google):" + result.isSuccess());
       //  Toast.makeText(SocialMainActivity.this,result.getStatus().getStatusMessage(),Toast.LENGTH_LONG).show();
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount account = result.getSignInAccount();
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
           // loginToSocialVendor(Vendor.GOOGLE, account.getIdToken());
        } else {
            wlLogger.error("Google SignIn failed" + result.getStatus());
        }
    }

    /**
     * Create JSON credentials to send as challenge answer.
     * This is the format that the security check on server is exacted.
     *
     * @param vendor the social vendor
     * @param token  the returned token from the social vendor
     * @return JSONObject containing the credentials
     */
    private JSONObject createJSONCredentials(String vendor, String token) {
        JSONObject credentials = new JSONObject();
        try {
            credentials.put("token", token);
            credentials.put("vendor", vendor);
        } catch (JSONException e) {
            wlLogger.debug(e.getMessage());
        }
        return credentials;
    }

    /**
     * Call adapter which protected with scope "socialLogin"
     * The adapter returns display name and user attributes in JSON format
     */
    private void callProtectedAdapter() {
        WLResourceRequest wlResourceRequest = new WLResourceRequest(URI.create("/adapters/HelloSocialUser/hello"), WLResourceRequest.GET, SOCIAL_LOGIN_SCOPE);
        wlResourceRequest.send(new WLResponseListener() {
            @Override
            public void onSuccess(WLResponse wlResponse) {
                final JSONObject responseJSON = wlResponse.getResponseJSON();
                try {
                    String userDisplayName = responseJSON.getString("displayName");

                    String status = "Hello " + userDisplayName;
                    if (!responseJSON.isNull("email")) {
                        status += "\n\n" + responseJSON.getString("email");
                    }
                    updateStatus(status);

                    String vendor = (String)responseJSON.get("socialLoginVendor");
                   // String userPictureURL = getPictureURLFromResponse(responseJSON, vendor);
                 //   updateProfilePicture(userPictureURL);
                } catch (Exception e) {
                    wlLogger.error("Parsing JSON failed", e);
                }
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                final String responseText = wlFailResponse.getResponseText();
                updateStatus(responseText);
            }
        });
    }




    /**
     * Update the status text view
     *
     * @param status the status
     */
    private void updateStatus(final String status) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        statusView.setText(status);
                        wlLogger.debug(status);
                        Logger.send();
                    }
                }
        );
    }

    /**
     * Update user profile picture if exist on user attributes
     * @param picture the user profile picture
     * @throws Exception
     */
    private void updateProfilePicture(final String picture) throws Exception {
        URL userPictureURL = new URL(picture);
        final Drawable userPicture = Drawable.createFromStream(userPictureURL.openStream(), "src");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userPictureView.setImageDrawable(userPicture);
            }
        });
    }

    /**
     * Reset the user profile picture
     */
    private void resetUserProfilePic() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userPictureView.setImageResource(0);
            }
        });
    }

    /**
     * Reset the user profile picture
     */
    private void resetStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusView.setText("");
            }
        });
    }

    /**
     * Login by call to preemptive login in case user initiated the login.
     * Or by sending challenge answer, if the request came with a challenge.
     *
     * @param vendor - the social vendor
     * @param token  - the token
     */
    private void loginToSocialVendor(Vendor vendor, String token) {
        JSONObject credentials = createJSONCredentials(vendor.value, token);
        if (isSignInFromChallenge) {
            socialLoginChallengeHandler.submitChallengeAnswer(credentials);
        } else {
            SocialMainActivity.this.loginToMFPWithSocialVendor(vendor.value, token);
        }
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
      //     signOut();
        super.onDestroy();
    }
}
