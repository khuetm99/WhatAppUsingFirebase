package com.example.admin.whatappusingfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendverificationcodeButton, verifiButton;
    private EditText InputPhoneNumber, InputVerificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        sendverificationcodeButton = findViewById(R.id.send_ver_code_button);
        verifiButton = findViewById(R.id.verify_button);
        InputPhoneNumber = findViewById(R.id.phone_number_input);
        InputVerificationCode = findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(this);

        sendverificationcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = InputPhoneNumber.getText().toString().trim();

                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(PhoneLoginActivity.this, "Phone number is required...", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Phone Verification ");
                    loadingBar.setMessage("please wait, while we are  authenticating your phone...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            PhoneLoginActivity.this,
                            callbacks
                    );

                }
            }
        });

        verifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendverificationcodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = InputVerificationCode.getText().toString().trim();
                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please write verification code first...", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Verification Code ");
                    loadingBar.setMessage("please wait, while we are  verifying verification code...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number, please enter correct phone number with" +
                        "your country code....", Toast.LENGTH_SHORT).show();
                sendverificationcodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                verifiButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                //save verification ID and resending token so we can use them later
                mVerificationId = s;
                mResendToken = forceResendingToken;
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code as been sent", Toast.LENGTH_SHORT).show();

                sendverificationcodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                verifiButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loadingBar.dismiss();
                    Toast.makeText(PhoneLoginActivity.this, "Congratulations, you're logged in successfully...", Toast.LENGTH_SHORT).show();
                    SendUserToMainActivity();
                } else {
                    String message = task.getException().toString();
                    Toast.makeText(PhoneLoginActivity.this, "Error  :" + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

}
