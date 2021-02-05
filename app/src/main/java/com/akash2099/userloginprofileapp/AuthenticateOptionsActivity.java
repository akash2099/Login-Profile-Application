package com.akash2099.userloginprofileapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AuthenticateOptionsActivity extends AppCompatActivity {
    // ALL
    private FirebaseAuth mAuth;


    // Google LogIn
//    private static final String TAG = "GoogleActivity";
    Button google_login_button;
    private static final int GOOGLE_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;


    // Facebook LogIn
    CallbackManager mCallbackManager;
    LoginButton facebook_login_button;
    private static final String FACEBOOK_EMAIL = "email";
    private static final String FACEBOOK_PUBLIC_PROFILE = "public_profile";
    private static final String FACEBOOK_TAG="FB_TAG";


    private static final String OAUTH_TAG = "OAUTH_TAG"; // for GitHub and Twitter

    // GitHub LogIn
    Button github_login_button;

    // Twitter LogIn
    Button twitter_login_button;


    // Firebase Realtime Database
    private static final String REALTIME_DATABASE_ROOT = "LoginProfileUsers";
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate_options);


        // For Google
        google_login_button=(Button)findViewById(R.id.gmail_login_button);

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        google_login_button.setOnClickListener(v -> {
            google_login();
        });



        // For Facebook
        mCallbackManager = CallbackManager.Factory.create();
        facebook_login_button=(LoginButton) findViewById(R.id.facebook_login_button);
        facebook_login_button.setReadPermissions(FACEBOOK_EMAIL, FACEBOOK_PUBLIC_PROFILE);
        // facebook_login_button.setReadPermissions(Arrays.asList(EMAIL));
        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        facebook_login_button.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Step 1: After this login to facebook will be done
                Toast.makeText(AuthenticateOptionsActivity.this, "Facebook Login Initiated!", Toast.LENGTH_SHORT).show();

                // Step 2: The below function will log user into firebase
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(AuthenticateOptionsActivity.this, "Facebook Login Cancelled!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(FACEBOOK_TAG, "Facebook Login Error : "+exception);
                Toast.makeText(AuthenticateOptionsActivity.this, "Facebook Login Error!", Toast.LENGTH_SHORT).show();
            }
        });



        // For GitHub
        OAuthProvider.Builder gitHubProvider = OAuthProvider.newBuilder("github.com");

        github_login_button=(Button)findViewById(R.id.github_login_button);

        github_login_button.setOnClickListener(v -> {
            github_login(gitHubProvider);
        });



        // For Twitter
        OAuthProvider.Builder twitterProvider = OAuthProvider.newBuilder("twitter.com");

        twitter_login_button=(Button)findViewById(R.id.twitter_login_button);

        twitter_login_button.setOnClickListener(v -> {
            twitter_login(twitterProvider);
        });



        // Save User Data to Realtime Database Firebase
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child(REALTIME_DATABASE_ROOT);

        // setting child event listener
//        setChildEventListenerForUser();
    }

    public void setChildEventListenerForUser() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String name="Human";
                notificationOn(name); // turn on the notification for registered user
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Setting up the basic notification for android Oreo and up
    private void notificationOn(String name) {
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID="com.akash2099.userloginprofileapp.notification";

        String message=" Thank you for registration!";

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel notificationChannel=new NotificationChannel(NOTIFICATION_CHANNEL_ID,"Notification", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("ITech Prophecy");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableLights(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(androidx.media.R.drawable.notification_icon_background)
                .setContentTitle("Welcome")
                .setContentText(name+message)
                .setContentInfo("Info");

        notificationManager.notify(new Random().nextInt(),notificationBuilder.build());
//        notificationManager.notify(1,notificationBuilder.build());
    }


    // FOR ALL
    public void go_to_profile_activity(){

        // create user in firebase................
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
//            String name = user.getDisplayName();
            String email = user.getEmail();
//            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
//            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();

            databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.getValue() == null) {
                        notificationOn("Human");
                        writeToRealtimeDatabase(uid,email); // IF CHILD DOESN'T EXIST NO USER IN THE REAL TIME DATABASE
                    }
                    else{
                        // user linking via different SignIn methods [ left later ] ..................

                        Intent intent=new Intent(AuthenticateOptionsActivity.this,ProfileActivity.class);
                        startActivity(intent);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }


    }

    private void writeToRealtimeDatabase(String unique_key, String email_id_received){
        // Write database
        RegisteredUser registeredUser=new RegisteredUser();
        registeredUser.setEmail(email_id_received);
        registeredUser.setUser_name(email_id_received);
        registeredUser.setFirst_name("");
        registeredUser.setLast_name("");
        registeredUser.setAge(0);
        registeredUser.setProfession("");
        registeredUser.setProfileImage(""); // empty for now

//        String unique_key = databaseReference.push().getKey(); // this is an unique key
        databaseReference.child(unique_key).setValue(registeredUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent=new Intent(AuthenticateOptionsActivity.this,ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            go_to_profile_activity();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Toast.makeText(this, "Firebase Authenticating with google: "+account.getId(), Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
//                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google SignIn Failed!", Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }



    // For Google
    public void google_login(){
        googleSignIn();
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(String idToken) {

        // ...SHOW the progress bar
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(AuthenticateOptionsActivity.this, "Authentication Successful using Google!", Toast.LENGTH_SHORT).show();
                            go_to_profile_activity();
                            //                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
//                            updateUI(null);
                            Toast.makeText(AuthenticateOptionsActivity.this, "Authentication Failed using Google!", Toast.LENGTH_SHORT).show();
                        }

                        // ...HIDE the progress bar
                    }
                });
    }



    // For Facebook
    private void handleFacebookAccessToken(AccessToken token) {
        // ...SHOW the progress bar
        Toast.makeText(this, "Handle facebook access token : "+token, Toast.LENGTH_SHORT).show();
        Log.d(FACEBOOK_TAG, "Handle facebook access token : "+token.getToken());

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(AuthenticateOptionsActivity.this, "Facebook SignIn Success!", Toast.LENGTH_SHORT).show();
                            go_to_profile_activity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(AuthenticateOptionsActivity.this, "Authentication failed : "+task.getException(), Toast.LENGTH_SHORT).show();
                            Log.e(FACEBOOK_TAG, "Authentication failed : "+task.getException());
                        }

                        // ...HIDE the progress bar
                    }
                });
    }



    // For GitHub
    public void github_login(OAuthProvider.Builder gitHubProvider){

        // Request read access to a user's email addresses.
        // This must be preconfigured in the app's API permissions.
//        List<String> scopes =
//                new ArrayList<String>() {
//                    {
//                        add("user:email");
//                    }
//                };
//        gitHubProvider.setScopes(scopes);

        // for registering new user only, if already exist email id etc then error
//        oAuthSignIn(gitHubProvider,"GitHub");

        // for linking to existing user
//        oAuthLinkExistingUser(gitHubProvider,"GitHub");

        // for refreshing login credentials for sensitive information
        oAuthRefreshLoginSensitive(gitHubProvider,"GitHub");
    }


    // For Twitter
    public void twitter_login(OAuthProvider.Builder twitterProvider){

        // Target specific email with login hint.
//        provider.addCustomParameter("lang", "fr");

        //............. set up twitter in developer console...............//
        Toast.makeText(this, "Twitter login is not enabled!", Toast.LENGTH_SHORT).show();

        // for registering new user only, if already exist email id etc then error
//        oAuthSignIn(twitterProvider,"Twitter");

        // for linking to existing user
//        oAuthLinkExistingUser(twitterProvider,"Twitter");

        // for refreshing login credentials for sensitive information
//        oAuthRefreshLoginSensitive(twitterProvider,"Twitter");
    }


    private void oAuthSignIn(OAuthProvider.Builder provider, String provider_name) {
        Task<AuthResult> pendingResultTask = mAuth.getPendingAuthResult();
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    // User is signed in.
                                    // IdP data available in
                                    // authResult.getAdditionalUserInfo().getProfile().
                                    // The OAuth access token can also be retrieved:
                                    // authResult.getCredential().getAccessToken().
                                    Toast.makeText(AuthenticateOptionsActivity.this, provider_name+" LogIn Successful!", Toast.LENGTH_SHORT).show();
//                                    handleGitHubAccessToken(authResult.getCredential().getAccessToken());
                                    go_to_profile_activity();
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure.
                                    Toast.makeText(AuthenticateOptionsActivity.this, provider_name+" Login Failed!", Toast.LENGTH_SHORT).show();
                                    Log.e(OAUTH_TAG,provider_name+" Failed : "+e);
                                }
                            });
        } else {
            // There's no pending result so you need to start the sign-in flow.
            // See below.
            mAuth.startActivityForSignInWithProvider(/* activity= */ this, provider.build())
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    // User is signed in.
                                    // IdP data available in
                                    // authResult.getAdditionalUserInfo().getProfile().
                                    // The OAuth access token can also be retrieved:
                                    // authResult.getCredential().getAccessToken().
                                    Toast.makeText(AuthenticateOptionsActivity.this, provider_name+" LogIn Successful!", Toast.LENGTH_SHORT).show();
                                    go_to_profile_activity();
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure.
                                    Toast.makeText(AuthenticateOptionsActivity.this, provider_name+" Login Failed!", Toast.LENGTH_SHORT).show();
                                    Log.e(OAUTH_TAG,provider_name+" Failed : "+e);
                                }
                            });
        }
    }

    private void oAuthLinkExistingUser(OAuthProvider.Builder provider, String provider_name){
        // The user is already signed-in.
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        firebaseUser
                .startActivityForLinkWithProvider(/* activity= */ this, provider.build())
                .addOnSuccessListener(
                        new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                // GitHub credential is linked to the current user.
                                // IdP data available in
                                // authResult.getAdditionalUserInfo().getProfile().
                                // The OAuth access token can also be retrieved:
                                // authResult.getCredential().getAccessToken().
                                Toast.makeText(AuthenticateOptionsActivity.this, provider_name+" Linking Successful!", Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure.
                                Toast.makeText(AuthenticateOptionsActivity.this, provider_name+" Linking Failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void oAuthRefreshLoginSensitive(OAuthProvider.Builder provider, String provider_name){
        // The user is already signed-in.
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        firebaseUser
                .startActivityForReauthenticateWithProvider(/* activity= */ this, provider.build())
                .addOnSuccessListener(
                        new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                // User is re-authenticated with fresh tokens and
                                // should be able to perform sensitive operations
                                // like account deletion and email or password
                                // update.
                                Toast.makeText(AuthenticateOptionsActivity.this, provider_name+" Re-LogIn Successful!", Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure.
                                Toast.makeText(AuthenticateOptionsActivity.this, provider_name+" Re-LogIn Failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

}