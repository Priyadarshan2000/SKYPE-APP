package com.example.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegistrationActivity extends AppCompatActivity {
    private CountryCodePicker ccp;
    private EditText phoneText;
    private EditText codeText;
    private Button continueAndNextBtn;
    private String checker = "", phoneNumber = "";
    private RelativeLayout relativeLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);


        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        continueAndNextBtn = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);

        continueAndNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (continueAndNextBtn.getText().equals("Submit") || checker.equals("code sent")) {
                    String verificationcode = codeText.getText().toString();
                    if (verificationcode.equals("")) {
                        Toast.makeText(RegistrationActivity.this, "Please Write verification code first", Toast.LENGTH_SHORT).show();
                    } else {
                        loadingBar.setTitle("code Number Verification");
                        loadingBar.setTitle("Please wait,while we are Verifying your code.");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationcode);
                        signInWithPhoneAuthCredential(credential);
                    }

                } else {
                    phoneNumber = ccp.getFullNumberWithPlus();
                    if (!phoneNumber.equals("")) {
                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setTitle("Please wait,while we are Verifying your phone number");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 1, TimeUnit.MINUTES, RegistrationActivity.this, mCallbacks);


                    } else {
                        Toast.makeText(RegistrationActivity.this, "Please Write vailid phone number", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(RegistrationActivity.this, "Invalid phone Number.............", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                relativeLayout.setVisibility(View.VISIBLE);

                continueAndNextBtn.setText("Continue");
                codeText.setVisibility(View.GONE);

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mVerificationId = s;
                mResendToken = forceResendingToken;
                relativeLayout.setVisibility(View.GONE);
                checker = "code sent";
                continueAndNextBtn.setText("Submit");
                codeText.setVisibility(View.VISIBLE);

                loadingBar.dismiss();

                Toast.makeText(RegistrationActivity.this, "code has been sent,please check..", Toast.LENGTH_SHORT).show();

            }
        };
    }

    @Override
    protected void onStart()
     {
        super.onStart();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(RegistrationActivity.this, "Congratulations,you are logged in succesfully", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();

                        } else {
                            loadingBar.dismiss();
                            String e = task.getException().toString();
                            Toast.makeText(RegistrationActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(RegistrationActivity.this, ContactsActivity.class);
        startActivity(intent);
        finish();

    }
}
