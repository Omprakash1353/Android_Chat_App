package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.utils.AndroidUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LoginOTP extends AppCompatActivity {

    String phoneNumber;
    Long timeoutSeconds = 60L;
    String verificationCode;
    PhoneAuthProvider.ForceResendingToken resendingToken;

    TextView provideNumber;
    EditText otpCode;
    Button verifyOtp;
    ProgressBar verifyOtpProgress;
    TextView resendOtp;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Loading OTP");
        setContentView(R.layout.activity_login_otp);

        otpCode = findViewById(R.id.otpCode);
        verifyOtp = findViewById(R.id.verifyOtp);
        verifyOtpProgress = findViewById(R.id.verifyOtpProgress);
        resendOtp = findViewById(R.id.resendOtp);

        phoneNumber = getIntent().getExtras().getString("phoneNumber");
        Toast.makeText(getApplicationContext(), phoneNumber, Toast.LENGTH_LONG).show();

        provideNumber = findViewById(R.id.provideNumber);
        provideNumber.setText("Enter the OTP sent to " + phoneNumber);

        System.out.println("About to start the otp sending");
        sendOtp(phoneNumber, false);

        verifyOtp.setOnClickListener((v) -> {
            System.out.println("Verifying OTP");
            String enteredOTP = otpCode.getText().toString();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOTP);
            signIn(credential);
            setInProgress(true);
        });

        resendOtp.setOnClickListener((v) -> {
            sendOtp(phoneNumber, true);
        });
    }

    void sendOtp(String phoneNumber, boolean isResend) {
        startResendTimer();
        setInProgress(true);
        System.out.println("Sending code");
        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(mAuth).setPhoneNumber(phoneNumber).setTimeout(timeoutSeconds, TimeUnit.SECONDS).setActivity(this).setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signIn(phoneAuthCredential);
                setInProgress(false);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                AndroidUtils.showToast(getApplicationContext(), "OTP verification failed");
                setInProgress(false);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                System.out.println("Code sent");
                verificationCode = s;
                resendingToken = forceResendingToken;
                AndroidUtils.showToast(getApplicationContext(), "OTP sent successfully");
                setInProgress(false);
            }
        });

        if (isResend) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    void setInProgress(boolean inProgress) {
        if (inProgress) {
            verifyOtpProgress.setVisibility(View.VISIBLE);
            verifyOtp.setVisibility(View.GONE);
        } else {
            verifyOtpProgress.setVisibility(View.GONE);
            verifyOtp.setVisibility(View.VISIBLE);
        }
    }

    void signIn(PhoneAuthCredential phoneAuthCredential) {
        setInProgress(true);
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                setInProgress(false);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(LoginOTP.this, LoginUsernameActivity.class);
                    intent.putExtra("phone", phoneNumber);
                    startActivity(intent);
                } else {
                    AndroidUtils.showToast(getApplicationContext(), "OTP veritification failed");
                }
            }
        });
    }

    void startResendTimer() {
        resendOtp.setEnabled(false);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeoutSeconds--;
                resendOtp.setText("Resend OTP in " + timeoutSeconds + "s");
                if (timeoutSeconds <= 0) {
                    resendOtp.setText("Resend OTP");
                    timeoutSeconds = 60L;
                    timer.cancel();
                    runOnUiThread(() -> {
                        resendOtp.setEnabled(true);
                    });
                }
            }
        }, 0, 1000);
    }
}