package com.example.vmac.WatBot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.ibm.WatBot.R;
import com.worklight.common.WLAnalytics;
import com.worklight.wlclient.api.WLAuthorizationManager;
import com.worklight.wlclient.api.WLClient;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLLogoutResponseListener;
import com.worklight.wlclient.api.WLResourceRequest;
import com.worklight.wlclient.api.WLResponse;
import com.worklight.wlclient.api.WLResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

public class SocialAuthActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 007;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    private Button btnSignIn;
    private Button btnSignOut, btnRevokeAccess;
    private RelativeLayout llProfileLayout;
    private ImageView imgProfilePic;
    private TextView txtName, txtEmail;
    private RatingBar ratingBar = null;
    private SocialLoginChallengeHandler socialLoginChallengeHandler;
    //Flag to know from where we signInWithGoogle
    protected boolean isSignInFromChallenge = false;
    protected SocialAuthActivity.Vendor currentVendor = SocialAuthActivity.Vendor.GOOGLE;
    public static final String SOCIAL_LOGIN_SCOPE = "accessRestricted";

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.social_auth_activity);

        btnSignIn = (Button) findViewById(R.id.sign_in_google);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        btnRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);
        llProfileLayout = (RelativeLayout) findViewById(R.id.llProfile);
        imgProfilePic = (ImageView) findViewById(R.id.user_profile_photo);
        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        ratingBar = (RatingBar) findViewById(R.id.pop_ratingbar);
        ratingBar.setRating(5);

        btnSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnRevokeAccess.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Customizing G+ button
       // btnSignIn.setSize(SignInButton.SIZE_STANDARD);
      //  btnSignIn.setScopes(gso.getScopeArray());
     //   socialLoginChallengeHandler = new SocialLoginChallengeHandler("socialLogin", this);
        //Register the SocialLoginChallengeHandler
     //   WLClient.getInstance().registerChallengeHandler(socialLoginChallengeHandler);

        //Init WLClient
        WLClient.createInstance(this);
        WLAnalytics.init(this.getApplication());
        WLAnalytics.enable();
        WLAnalytics.addDeviceEventListener(WLAnalytics.DeviceEvent.LIFECYCLE);
        socialLoginChallengeHandler = new SocialLoginChallengeHandler("socialLogin", this);

        //Register the SocialLoginChallengeHandler
        WLClient.getInstance().registerChallengeHandler(socialLoginChallengeHandler);

    }


    protected void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    /**
     * Call adapter which protected with scope "socialLogin"
     * The adapter returns display name and user attributes in JSON format
     */
    private void callProtectedAdapter() {
        WLResourceRequest wlResourceRequest = new WLResourceRequest(URI.create("/adapters/HelloSocialUser/hello"), WLResourceRequest.GET,"accessRestricted");
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

                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                final String responseText = wlFailResponse.getResponseText();

            }
        });
    }




    private void signOut() {
        WLAuthorizationManager.getInstance().logout("socialLogin", new WLLogoutResponseListener() {
            @Override
            public void onSuccess() {
                Log.i("Log Out","Successful");
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                Log.i("Log Out  Failed",wlFailResponse.getErrorMsg());

            }
        });
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }

    private void proccedToJobs() {
        Intent intent = new Intent(this, HomeActivity.class);
        //Create the bundle
        Bundle bundle = new Bundle();
        bundle.putString("user_id", txtEmail.getText().toString());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            loginToSocialVendor(Vendor.GOOGLE, account.getIdToken());
            String email = account.getEmail();

            Log.e(TAG, "display name: " + account.getDisplayName());

            String personName = account.getDisplayName();
            if(account.getPhotoUrl()!=null){
                String personPhotoUrl = account.getPhotoUrl().toString();
                Log.e(TAG, "Name: " + personName + ", email: " + email
                        + ", Image: " + personPhotoUrl);
                Glide.with(getApplicationContext()).load(personPhotoUrl)
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgProfilePic);
            }
            txtName.setText(personName);
            txtEmail.setText(email);
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
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
            socialLoginChallengeHandler.submitChallengeAnswer(credentials);
     }

    private JSONObject createJSONCredentials(String vendor, String token) {
        JSONObject credentials = new JSONObject();
        try {
            credentials.put("token", token);
            credentials.put("vendor", vendor);
        } catch (JSONException e) {

        }
        return credentials;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.sign_in_google:
                callProtectedAdapter();
               // signIn();
                break;

            case R.id.btn_sign_out:
                signOut();
                break;

            case R.id.btn_revoke_access:
                proccedToJobs();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
           // showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                  //  hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
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

    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            btnSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnRevokeAccess.setVisibility(View.VISIBLE);
            llProfileLayout.setVisibility(View.VISIBLE);
        } else {
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnRevokeAccess.setVisibility(View.GONE);
            llProfileLayout.setVisibility(View.GONE);
        }
    }
}
