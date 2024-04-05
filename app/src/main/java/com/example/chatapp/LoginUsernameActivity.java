package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.chatapp.model.UserModel;
import com.example.chatapp.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginUsernameActivity extends AppCompatActivity {

    EditText username;
    Button next;
    ProgressBar nextProgress;
    String phoneNumber;
    UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_username);

        username = findViewById(R.id.username);
        next = findViewById(R.id.next);
        nextProgress = findViewById(R.id.nextProgress);

        phoneNumber = getIntent().getExtras().getString("phone");
        getUsername();

        next.setOnClickListener((v) -> {
            setUsername();
        });
    }

    void setUsername() {
        String user_name = username.getText().toString();
        if (user_name.isEmpty() || user_name.length() < 3) {
            username.setError("Username length should be of at least 3 char");
            return;
        }
        setInProgress(true);
        if (userModel != null) {
            userModel.setUsername(user_name);
        } else {
            userModel = new UserModel(phoneNumber, user_name, Timestamp.now(), FirebaseUtil.currentUserId());
        }
        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setInProgress(false);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(LoginUsernameActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    void getUsername() {
        setInProgress(true);
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                setInProgress(false);
                if (task.isSuccessful()) {
                    userModel = task.getResult().toObject(UserModel.class);
                    if (userModel != null) {
                        username.setText(userModel.getUsername());
                    }
                }
            }
        });
    }

    void setInProgress(boolean inProgress) {
        if (inProgress) {
            nextProgress.setVisibility(View.VISIBLE);
            next.setVisibility(View.GONE);
        } else {
            nextProgress.setVisibility(View.GONE);
            next.setVisibility(View.VISIBLE);
        }
    }
}