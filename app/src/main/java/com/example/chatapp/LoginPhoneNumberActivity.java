package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.hbb20.CountryCodePicker;

public class LoginPhoneNumberActivity extends AppCompatActivity {

    CountryCodePicker countryCode;
    EditText phoneNumber;
    Button sendCode;
    ProgressBar sendCodeProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone_number);

        countryCode = findViewById(R.id.countryCode);
        phoneNumber = findViewById(R.id.phoneNumber);
        sendCode = findViewById(R.id.sendCode);
        sendCodeProgress = findViewById(R.id.sendCodeProgress);

        sendCodeProgress.setVisibility(View.GONE);

        countryCode.registerCarrierNumberEditText(phoneNumber);
        sendCode.setOnClickListener((v) -> {
            if (!countryCode.isValidFullNumber()) {
                phoneNumber.setError("Phone number is not valid");
                return;
            }
            Intent intent = new Intent(LoginPhoneNumberActivity.this, LoginOTP.class);
            intent.putExtra("phoneNumber", countryCode.getFullNumberWithPlus());
            startActivity(intent);
        });
    }
}