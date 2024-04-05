package com.example.chatapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.chatapp.model.UserModel;
import com.example.chatapp.utils.AndroidUtils;
import com.example.chatapp.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class LoginUsernameActivity extends AppCompatActivity {

    EditText username;
    Button next;
    ProgressBar nextProgress;
    String phoneNumber;
    UserModel userModel;
    ImageView img;
    ActivityResultLauncher<Intent> imagePickerLauncher;
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_username);

        username = findViewById(R.id.username);
        next = findViewById(R.id.next);
        nextProgress = findViewById(R.id.nextProgress);
        img = findViewById(R.id.user_pic);

        phoneNumber = getIntent().getExtras().getString("phone");
        getUsername();

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                    selectedImageUri = data.getData();
                    AndroidUtils.setProfilePic(getApplicationContext(), selectedImageUri, img);
                }
            }
        });

        img.setOnClickListener(v -> {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512).createIntent(new Function1<Intent, Unit>() {
                @Override
                public Unit invoke(Intent intent) {
                    imagePickerLauncher.launch(intent);
                    return null;
                }
            });
        });

        next.setOnClickListener((v) -> {
            setUsername();
            updateImg();
        });
    }

    void updateImg() {
        setInProgress(true);
        if (selectedImageUri != null) {
            FirebaseUtil.getCurrentProfilePicStorageRef().putFile(selectedImageUri).addOnCompleteListener(task -> {
                updateToFirestore();
            });

        }
    }

    void updateToFirestore() {
        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful()) {
                AndroidUtils.showToast(getApplicationContext(), "Updated successfully");
            } else {
                AndroidUtils.showToast(getApplicationContext(), "Failed to update");
            }
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