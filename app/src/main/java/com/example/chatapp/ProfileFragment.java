package com.example.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chatapp.model.UserModel;
import com.example.chatapp.utils.AndroidUtils;
import com.example.chatapp.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProfileFragment extends Fragment {

    ImageView profilePic;
    EditText usernameInput, phoneInput;
    ProgressBar progressBar;
    Button updateProfile;
    TextView logoutBtn;

    UserModel currentUserModel;

    ActivityResultLauncher<Intent> imagePickerLauncher;
    Uri selectedImageUri;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                    selectedImageUri = data.getData();
                    AndroidUtils.setProfilePic(getContext(), selectedImageUri, profilePic);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePic = view.findViewById(R.id.user_profile_pic);
        usernameInput = view.findViewById(R.id.profile_username);
        phoneInput = view.findViewById(R.id.profile_phone);
        updateProfile = view.findViewById(R.id.update_profile);
        progressBar = view.findViewById(R.id.profile_Progress);
        logoutBtn = view.findViewById(R.id.logout);

        getUserData();

        updateProfile.setOnClickListener(v -> {
            updateBtnClick();
        });

        logoutBtn.setOnClickListener(v -> {
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUtil.logout();
                    Intent intent = new Intent(getContext(), SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
        });

        profilePic.setOnClickListener(v -> {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512).createIntent(new Function1<Intent, Unit>() {
                @Override
                public Unit invoke(Intent intent) {
                    imagePickerLauncher.launch(intent);
                    return null;
                }
            });
        });
        return view;
    }

    void updateBtnClick() {
        String newUsername = usernameInput.getText().toString();
        if (newUsername.isEmpty() || usernameInput.length() < 3) {
            usernameInput.setError("Username length should be at least of 3 characters");
            return;
        }
        currentUserModel.setUsername(newUsername);
        setInProgress(true);
        if (selectedImageUri != null) {
            FirebaseUtil.getCurrentProfilePicStorageRef().putFile(selectedImageUri).addOnCompleteListener(task -> {
                updateToFirestore();
            });

        } else {
            updateToFirestore();
        }
    }

    void updateToFirestore() {
        FirebaseUtil.currentUserDetails().set(currentUserModel).addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful()) {
                AndroidUtils.showToast(getContext(), "Updated successfully");
            } else {
                AndroidUtils.showToast(getContext(), "Failed to update");
            }
        });
    }

    void getUserData() {
        setInProgress(true);
        FirebaseUtil.getCurrentProfilePicStorageRef().getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri uri = task.getResult();
                AndroidUtils.setProfilePic(getContext(), uri, profilePic);
            }

        });
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            currentUserModel = task.getResult().toObject(UserModel.class);
            usernameInput.setText(currentUserModel.getUsername());
            phoneInput.setText(currentUserModel.getPhone());
        });
    }

    void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            updateProfile.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            updateProfile.setVisibility(View.VISIBLE);
        }
    }
}