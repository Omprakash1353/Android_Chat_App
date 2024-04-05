package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.ChatActivity;
import com.example.chatapp.R;
import com.example.chatapp.model.ChatroomModel;
import com.example.chatapp.model.UserModel;
import com.example.chatapp.utils.AndroidUtils;
import com.example.chatapp.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int i, @NonNull ChatroomModel model) {
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds()).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                boolean lastMessageSendByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

                UserModel otherUserModel = task.getResult().toObject(UserModel.class);

                FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl().addOnCompleteListener(t -> {
                    if(t.isSuccessful()) {
                        Uri uri = t.getResult();
                        AndroidUtils.setProfilePic(context, uri, holder.profilePic);
                    }

                });
                holder.usernameText.setText(otherUserModel.getUsername());
                if(lastMessageSendByMe)
                    holder.lastMessageText.setText("You: " + model.getLastMessage());
                else
                    holder.lastMessageText.setText(model.getLastMessage());
                holder.lastMessageTime.setText(FirebaseUtil.timestamptoString(model.getLastMessageTimestamp()));

                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ChatActivity.class);
                    AndroidUtils.passUserModelAsIntent(intent, otherUserModel);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                });
            }
        });
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, viewGroup, false);
        return new ChatroomModelViewHolder(view);
    }

    class ChatroomModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText;
        TextView lastMessageText, lastMessageTime;
        ImageView profilePic;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_pic_img_view);
        }
    }

}
