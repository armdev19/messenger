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
import android.widget.TextView;
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


public class LoginActivity extends AppCompatActivity {

    private Button loginButton, registerButton, phoneLoginButton;
    private EditText loginEmail, loginPassword;
    private TextView forgetPasswordLink;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        phoneLoginButton = findViewById(R.id.phone_login_button);
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        forgetPasswordLink = findViewById(R.id.forget_password_link);
        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allowUserToLogin();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToPhoneLoginActivity();
            }
        });
    }

    private void allowUserToLogin() {

        String email = loginEmail.getText().toString();
        String password = loginPassword.getText().toString();

        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {

            Toast.makeText(LoginActivity.this, "Please enter your email and password", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(email)) {

            Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
        } else {


            progressDialog.setTitle("Sign In");
            progressDialog.setMessage("Please wait, sign in");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                String currentUserId = firebaseAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                usersRef.child(currentUserId).child("deviceToken")
                                        .setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    sendUserToMainActivity();
                                                    Toast.makeText(LoginActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });




                            } else {

                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void sendUserToPhoneLoginActivity() {
        Intent phoneLoginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
        startActivity(phoneLoginIntent);
    }
}
