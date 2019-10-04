package com.infernal93.messengerdemo.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.infernal93.messengerdemo.R;
import com.infernal93.messengerdemo.activities.MainActivity;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerCodeBtn, verifyBtn;
    private EditText inputPhoneNumb, inputVerifyCode;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        sendVerCodeBtn = findViewById(R.id.send_ver_code_button);
        verifyBtn = findViewById(R.id.verify_button);
        inputPhoneNumb = findViewById(R.id.phone_number_input);
        inputVerifyCode = findViewById(R.id.verification_code_input);

        firebaseAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);


        sendVerCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String phoneNumber = inputPhoneNumb.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(PhoneLoginActivity.this, "Write phone number", Toast.LENGTH_SHORT).show();

                } else {

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,                               // Phone number to verify
                            60,                                    // Timeout duration
                            TimeUnit.SECONDS,                       // Unit of timeout
                            PhoneLoginActivity.this,        // Activity (for callback building)
                            callbacks);                           // OnVerificationStateChangedCallbacks
                }
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendVerCodeBtn.setVisibility(View.INVISIBLE);
                inputPhoneNumb.setVisibility(View.INVISIBLE);

                String verificationCode = inputVerifyCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please write verification code",
                            Toast.LENGTH_SHORT).show();
                } else {

                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please wait");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Invalid number. Please enter correct phone number",
                        Toast.LENGTH_SHORT).show();

                sendVerCodeBtn.setVisibility(View.VISIBLE);
                inputPhoneNumb.setVisibility(View.VISIBLE);
                verifyBtn.setVisibility(View.INVISIBLE);
                inputVerifyCode.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been send, please check and verify",
                        Toast.LENGTH_SHORT).show();

                sendVerCodeBtn.setVisibility(View.INVISIBLE);
                inputPhoneNumb.setVisibility(View.INVISIBLE);
                verifyBtn.setVisibility(View.VISIBLE);
                inputVerifyCode.setVisibility(View.VISIBLE);
            }
        };

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Logined successful", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error : " + message,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
