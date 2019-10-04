package com.infernal93.messengerdemo.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.infernal93.messengerdemo.activities.MainActivity;
import com.infernal93.messengerdemo.R;

public class RegisterActivity extends AppCompatActivity {

    private Button createAccountButton, phoneAccountButton;
    private EditText registerEmail, registerPassword;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        createAccountButton = findViewById(R.id.registration_button);
        phoneAccountButton = findViewById(R.id.phone_registration_button);
        registerEmail = findViewById(R.id.register_email);
        registerPassword = findViewById(R.id.register_password);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(this);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {

        String email = registerEmail.getText().toString();
        String password = registerPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(RegisterActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            progressDialog.setTitle("Creating New Account");
                            progressDialog.setMessage("Please wait, creating new account");
                            progressDialog.setCanceledOnTouchOutside(true);
                            progressDialog.show();

                            if (task.isSuccessful()) {

                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                String currentUserID = firebaseAuth.getCurrentUser().getUid();
                                databaseReference.child("Users").child(currentUserID).setValue("");

                                databaseReference.child("Users").child(currentUserID).child("deviceToken")
                                        .setValue(deviceToken);


                                sendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account created!", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
        }
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
