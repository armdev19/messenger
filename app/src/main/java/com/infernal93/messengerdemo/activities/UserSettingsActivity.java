package com.infernal93.messengerdemo.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.infernal93.messengerdemo.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSettingsActivity extends AppCompatActivity {

    private Button updateUserSettingsBtn;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;


    private static final int GALLERY_PICK = 1;
    private StorageReference userImageProfileData;
    private ProgressDialog loadingBar;
    private Toolbar userSettingsToolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        updateUserSettingsBtn = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userImageProfileData = FirebaseStorage.getInstance().getReference().child("Profile Images");

        userSettingsToolBar = findViewById(R.id.user_settings_toolbar);
        userSettingsToolBar.inflateMenu(R.menu.options_menu);
        userSettingsToolBar.setTitle("User Info");
        userSettingsToolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_30dp);
        userSettingsToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        updateUserSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateUserSettings();
            }
        });

        retrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGalleryImage();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    //.setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final Uri resultUri = result.getUri();

                final StorageReference filePath = userImageProfileData.child(currentUserID + "jpg");


                // Upload image at Storage
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                databaseReference.child("Users").child(currentUserID).child("image")
                                        .setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    Toast.makeText(UserSettingsActivity.this, "Image save is Database"
                                                            , Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                } else {

                                                    String message = task.getException().toString();
                                                    Toast.makeText(UserSettingsActivity.this, "Error: " + message
                                                            , Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }
                        });
                    }
                });

            }

        }
    }

    private void updateUserSettings() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {

            Toast.makeText(UserSettingsActivity.this, "Please, write your User name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(setUserStatus)) {

            Toast.makeText(UserSettingsActivity.this, "Please, write your BIO", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(setUserName) && TextUtils.isEmpty(setUserStatus)) {

            Toast.makeText(UserSettingsActivity.this, "Please, write your User name and BIO", Toast.LENGTH_SHORT).show();
        } else {

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setUserStatus);
            databaseReference.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendUserToMainActivity();
                                Toast.makeText(UserSettingsActivity.this, "Username and BIO saves", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(UserSettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }
    }

    private void retrieveUserInfo() {
        databaseReference.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {

                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                        } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {

                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();


                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);

                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {


                    }
                });
    }


    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(UserSettingsActivity.this, MainActivity.class);
        // mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        // finish();
    }

    private void getGalleryImage() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_PICK);
    }
}
