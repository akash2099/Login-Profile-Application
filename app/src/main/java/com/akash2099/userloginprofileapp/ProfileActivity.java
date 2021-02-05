package com.akash2099.userloginprofileapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    TextView email;
    EditText username, first_name, last_name, profession, age;
    ImageView profile_imageView;

    private static final String PROFILE_VALIDATION_OK="OK";
    private static final String PROFILE_VALIDATION_NOT_OK="NOT_OK";

    ProgressBar update_progress_bar;

    // Firebase Realtime Database
    private static final String FIREBASE_ERROR = "FIREBASE_ERROR";
    private static final String REALTIME_DATABASE_ROOT = "LoginProfileUsers";
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    // Firebase Storage
    private StorageReference storageReference;

    // For Image Upload
    private static final String PROFILE_IMAGE_KEY = "profileImage";
    private static final int PICK_IMAGE_REQUEST = 111;
    private Uri mImageUri;
    private String imageUrl = "";
    private static final String PICASSO_INFO="PICASSO_INFO";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get reference of Firebase Realtime Database
        database = FirebaseDatabase.getInstance();
        // realtime database reference
        databaseReference = database.getReference().child(REALTIME_DATABASE_ROOT);
        // storage reference
        storageReference = FirebaseStorage.getInstance().getReference("LoginProfileImages");

        update_progress_bar= findViewById(R.id.update_progress_bar);
        update_progress_bar.setIndeterminate(true);
        update_progress_bar.setVisibility(View.INVISIBLE);

        email= findViewById(R.id.email_user);
        username= findViewById(R.id.username);
        profession= findViewById(R.id.profession);
        first_name= findViewById(R.id.first_name);
        last_name= findViewById(R.id.last_name);
        age= findViewById(R.id.age);
        profile_imageView= findViewById(R.id.profile_image);

        String url_test="https://firebasestorage.googleapis.com/v0/b/fir-practice-4cb42.appspot.com/o?name=Images%2F1607520752419.jpg&uploadType=resumable&upload_id=ABg5-Ux-GzZFDpKCBUnIBIkl5kb1pWn8l3ioNvlI6TbdHjrh3HUgGPo1nalIZjls1855NqrXlinAPuOV85N1sT21cgi6G9l5MQ&upload_protocol=resumable";
        String url_test2="https://th.bing.com/th/id/OIP.z9DgCgl3z9jhbiJDMuwsOAHaEK?pid=Api&rs=1";
        // Picasso.get().load(url_test).into(profile_imageView);

        profile_imageView.setOnClickListener(v -> {
            OpenImageChooser();
            /*
            Picasso.get().load(url_test).into(profile_imageView, new Callback() {
                @Override
                public void onSuccess() {
                    update_progress_bar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(ProfileActivity.this, "Error : "+ e, Toast.LENGTH_SHORT).show();
                    Log.e(PICASSO_INFO,"Error : "+e);
                }
            });

             */
        });

        // setting default profile info initially after login
        setDefaultProfileInfo();


        // RESET LAST SAVED PROFILE INFO
        ImageButton reset_button= findViewById(R.id.reset_profile_info);

        reset_button.setOnClickListener(v -> {
            setDefaultProfileInfo();
        });


        // UPDATE PROFILE
        Button update_button= findViewById(R.id.update_profile_info);

        update_button.setOnClickListener(v -> {
            updateProfileInfo();
        });


        // LOG OUT
        Button logout_button= findViewById(R.id.logout_button);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // initialize firebase auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        logout_button.setOnClickListener(v -> {
            signOutUser();
        });
    }


    // fetch from Firebase
    private void setDefaultProfileInfo(){
        update_progress_bar.setVisibility(View.VISIBLE);
        // Get all the above from firebase
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
//            String name = user.getDisplayName();
//            String email = user.getEmail();
//            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
//            boolean emailVerified = user.isEmailVerified();

            String uid = user.getUid();

            databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    RegisteredUser registeredUser = dataSnapshot.getValue(RegisteredUser.class);
                    String got_email = registeredUser.getEmail();
                    String got_username = registeredUser.getUser_name();
                    String got_first_name = registeredUser.getFirst_name();
                    String got_last_name = registeredUser.getLast_name();
                    int got_age = registeredUser.getAge();
                    String got_profession = registeredUser.getProfession();
                    String profile_image = registeredUser.getProfileImage();

                    // Setting other fields
                    email.setText(got_email);
                    username.setText(got_username);
                    first_name.setText(got_first_name);
                    last_name.setText(got_last_name);
                    profession.setText(got_profession);

                    if (got_age == 0) {
                        age.setText("");
                    } else {
                        age.setText(String.valueOf(got_age));
                    }
                    imageUrl=profile_image;

                    if (profile_image.trim().isEmpty()) {
                        profile_imageView.setImageResource(R.drawable.default_profile_image);
                        update_progress_bar.setVisibility(View.INVISIBLE);
                    } else {

                        // download and set the image into imageView
                         Picasso.get().load(profile_image).into(profile_imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                update_progress_bar.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(ProfileActivity.this, "Error : "+ e, Toast.LENGTH_SHORT).show();
                                Log.e(PICASSO_INFO,"Error : "+e);
                            }
                        });

//                        Glide.with(getApplicationContext()).load(profile_image).into(profile_imageView);

//                        Toast.makeText(ProfileActivity.this, "Failed to download profile pic! "+profile_image, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, "Error in updating database : "+databaseError, Toast.LENGTH_SHORT).show();
                    Log.e(FIREBASE_ERROR,"Error in updating database : "+databaseError);
                }

            });
        }

    }

    private void updateProfileInfo(){
        update_progress_bar.setVisibility(View.VISIBLE);

        String un=username.getText().toString();
        String fn=first_name.getText().toString();
        String ln=last_name.getText().toString();
        String pr=profession.getText().toString();

        String ag_st=age.getText().toString();
        int ag=0;
        if(!ag_st.trim().isEmpty())
            ag=Integer.parseInt(ag_st);

        ImageView pf_image=profile_imageView;
        
        String piv_code=profileInfoValidation(fn,ln,un,pr,ag,pf_image);

        if(piv_code.equals(PROFILE_VALIDATION_OK)){
            updateUserFirebase(fn,ln,un,pr,ag,pf_image);
        }
        else{
            update_progress_bar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Profile update failed!", Toast.LENGTH_SHORT).show();
        }

    }

    private String profileInfoValidation(String fn, String ln, String un, String pr, int ag, ImageView pf_image){

        // check fn, ln, pr,ag
        if(un.trim().isEmpty()){
            Toast.makeText(this, "Username not valid!", Toast.LENGTH_SHORT).show();
            return PROFILE_VALIDATION_NOT_OK;
        }

        // Uniqueness of username doesn't matter for now

        // pf_image is always valid for now
        return PROFILE_VALIDATION_OK;
    }

    // upload to Firebase
    private void updateUserFirebase(String fn, String ln, String un, String pr, int ag, ImageView pf_image){

        // Replace first_name, last_name, profession, user_name
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
//            String name = user.getDisplayName();
//            String email = user.getEmail();
//            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
//            boolean emailVerified = user.isEmailVerified();

            String uid = user.getUid();

            /*
            RegisteredUser registeredUser1=new RegisteredUser();
            registeredUser1.setEmail(email.getText().toString());
            registeredUser1.setUser_name(un);
            registeredUser1.setFirst_name(fn);
            registeredUser1.setLast_name(ln);
            registeredUser1.setAge(ag);
            registeredUser1.setProfession(pr);
            registeredUser1.setProfileImage(""); // empty for now
            databaseReference.child(uid).setValue(registeredUser1);
            */

            if (mImageUri != null) {
//            StorageReference fileReference = storageReference.child(uid).child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));
                StorageReference fileReference = storageReference.child(uid).child(PROFILE_IMAGE_KEY + "." + getFileExtension(mImageUri));
                // upload data to firebase storage
                fileReference.putFile(mImageUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                // show progress bar even after upload complete
//                            progressBar.setVisibility(View.INVISIBLE);

                                /*
                                String imageUrl = fileReference.getDownloadUrl().toString();
                                RegisteredUser registeredUser=new RegisteredUser();
                                registeredUser.setEmail(email.getText().toString());
                                registeredUser.setUser_name(un);
                                registeredUser.setFirst_name(fn);
                                registeredUser.setLast_name(ln);
                                registeredUser.setAge(ag);
                                registeredUser.setProfession(pr);
                                registeredUser.setProfileImage(imageUrl); // empty for now
                                databaseReference.child(uid).setValue(registeredUser);
                                update_progress_bar.setVisibility(View.INVISIBLE);
                                */

                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // upload data to realtime database
                                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        imageUrl = uri.toString();
                                        RegisteredUser registeredUser=new RegisteredUser();
                                        registeredUser.setEmail(email.getText().toString());
                                        registeredUser.setUser_name(un);
                                        registeredUser.setFirst_name(fn);
                                        registeredUser.setLast_name(ln);
                                        registeredUser.setAge(ag);
                                        registeredUser.setProfession(pr);
                                        registeredUser.setProfileImage(imageUrl); // empty for now
                                        databaseReference.child(uid).setValue(registeredUser);
                                        update_progress_bar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(ProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                                    }
                                });



//                            UploadImage uploadImage = new UploadImage(mFileUploadName.getText().toString().trim(), taskSnapshot.getUploadSessionUri().toString());
//                            String uploadId = databaseReference.push().getKey(); // this is an unique key
                                String upload_session_uri =taskSnapshot.getUploadSessionUri().toString();


                                /*
                                RegisteredUser registeredUser=new RegisteredUser();
                                registeredUser.setEmail(email.getText().toString());
                                registeredUser.setUser_name(un);
                                registeredUser.setFirst_name(fn);
                                registeredUser.setLast_name(ln);
                                registeredUser.setAge(ag);
                                registeredUser.setProfession(pr);
                                registeredUser.setProfileImage(upload_session_uri); // empty for now
                                databaseReference.child(uid).setValue(registeredUser);
                                */
                                Toast.makeText(ProfileActivity.this, "Profile Image uploaded successfully! "+upload_session_uri, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//                            long totalByteCount = snapshot.getTotalByteCount();
//                            long bytesTransferred = snapshot.getBytesTransferred();
//                            float left = ((float) bytesTransferred / (float) totalByteCount) * 100;
//                            progressBar.setProgress((int) left);
//
//                            System.out.println("hithis " + left + " " + totalByteCount + " " + bytesTransferred);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileActivity.this, "File failed to upload!", Toast.LENGTH_SHORT).show();
                                update_progress_bar.setVisibility(View.INVISIBLE);

                            }
                        });
            } else {
                update_progress_bar.setVisibility(View.INVISIBLE);
//                upload_session_uri="";
//                Toast.makeText(this, "No file selected!", Toast.LENGTH_SHORT).show();
            }

            RegisteredUser registeredUser=new RegisteredUser();
            registeredUser.setEmail(email.getText().toString());
            registeredUser.setUser_name(un);
            registeredUser.setFirst_name(fn);
            registeredUser.setLast_name(ln);
            registeredUser.setAge(ag);
            registeredUser.setProfession(pr);
            registeredUser.setProfileImage(imageUrl); // empty for now
            databaseReference.child(uid).setValue(registeredUser);
//            update_progress_bar.setVisibility(View.INVISIBLE);
            Toast.makeText(ProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();




            // Write database
            // Later make it map and use update child..........LEFT

            /*
            // USING MAP LATER, BELOW IS THE BETTER APPROACH THEN I NEED NOT TO HAVE ALL THE VALUES
            Map<String, Object> postValues = new HashMap<String,Object>();

            mDatabase.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                postValues.put(snapshot.getKey(),snapshot.getValue());
                            }
                            postValues.put("email", email);
                            postValues.put("firstName", firstName);
                            postValues.put("lastName", lastName);
                            mDatabase.child("users").child(userId).updateChildren(postValues);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    }
                );
            */


//            upload_session_uri="";
            // But need the image url to access it later
//            String upload_url=UploadFileToFirebase(uid);
//            Toast.makeText(this, upload_url, Toast.LENGTH_SHORT).show();
//            registeredUser.setProfileImage(upload_url); // empty for now


//        String unique_key = databaseReference.push().getKey(); // this is an unique key
//            databaseReference.child(uid).setValue(registeredUser);
//            Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_LONG).show();
        }
        else{
//            upload_session_uri="";
            Toast.makeText(this, "Profile Updated Failed!", Toast.LENGTH_LONG).show();
        }
    }

    private void OpenImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*"); // mime type
        intent.setAction(Intent.ACTION_GET_CONTENT); // setting actions system
        startActivityForResult(intent, PICK_IMAGE_REQUEST); // get result
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();

            // Load image into ImageView
            Picasso.get().load(mImageUri).into(profile_imageView);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

//    private String UploadFileToFirebase(String uid) {
//        if (mImageUri != null) {
////            StorageReference fileReference = storageReference.child(uid).child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));
//            StorageReference fileReference = storageReference.child(uid).child(PROFILE_IMAGE_KEY + "." + getFileExtension(mImageUri));
//            // upload data to firebase storage
//            fileReference.putFile(mImageUri)
//                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                            // show progress bar even after upload complete
////                            progressBar.setVisibility(View.INVISIBLE);
//                        }
//                    })
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            // upload data to realtime database
////                            UploadImage uploadImage = new UploadImage(mFileUploadName.getText().toString().trim(), taskSnapshot.getUploadSessionUri().toString());
////                            String uploadId = databaseReference.push().getKey(); // this is an unique key
//                            upload_session_uri =taskSnapshot.getUploadSessionUri().toString();
//                            Toast.makeText(ProfileActivity.this, "Profile Image uploaded successfully! "+upload_session_uri, Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
////                            long totalByteCount = snapshot.getTotalByteCount();
////                            long bytesTransferred = snapshot.getBytesTransferred();
////                            float left = ((float) bytesTransferred / (float) totalByteCount) * 100;
////                            progressBar.setProgress((int) left);
////
////                            System.out.println("hithis " + left + " " + totalByteCount + " " + bytesTransferred);
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(ProfileActivity.this, "File failed to upload!", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        } else {
//            upload_session_uri="";
//            Toast.makeText(this, "No file selected!", Toast.LENGTH_SHORT).show();
//        }
//        return upload_session_uri;
//    }

    private void signOutUser(){
        // Google
        signOutGoogle(); // sign_out
//        revokeAccessGoogle(); // sign_out and disconnect account

        // Facebook
//        signOutFacebook();

        // GitHub
//        signOutGitHub();

        // Twitter
//        signOutTwitter();

        // default
        // Firebase sign out
//        mAuth.signOut();
        finish();
    }

    // sign out but not disconnect account
    private void signOutGoogle() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ProfileActivity.this, "Sign Out Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // sign out and disconnect user
    private void revokeAccessGoogle() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ProfileActivity.this, "Access Revoked Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signOutFacebook(){
        // Firebase sign out
        mAuth.signOut();

        // Facebook log out
        LoginManager.getInstance().logOut(); // log out user from facebook
    }

    private void signOutGitHub(){
        // Firebase sign out
        mAuth.signOut();
        // revoke access only from gitHub profile
    }

    private void signOutTwitter(){
        // Firebase sign out
        mAuth.signOut();
        // revoke access idk
    }

}