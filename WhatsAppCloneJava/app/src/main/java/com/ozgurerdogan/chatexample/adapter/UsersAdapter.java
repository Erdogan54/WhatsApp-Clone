package com.ozgurerdogan.chatexample.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.ozgurerdogan.chatexample.ChatDetailActivity;
import com.ozgurerdogan.chatexample.R;
import com.ozgurerdogan.chatexample.databinding.SampleShowUserBinding;
import com.ozgurerdogan.chatexample.models.MessageModel;
import com.ozgurerdogan.chatexample.models.Users;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersHolder> {

    ArrayList<Users> usersArrayList;
    Context context;
    Users users;


    int SENDER_VIEW_TYPE=1;
    int RECEIVE_VIEW_TYPE=2;


    public UsersAdapter(ArrayList<Users> usersArrayList, Context context) {
        this.usersArrayList = usersArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public UsersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            SampleShowUserBinding binding=SampleShowUserBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
            return new UsersHolder(binding);

    }


    @Override
    public int getItemViewType(int position) {

        users=usersArrayList.get(position);
        if (users.getUserId().equals(FirebaseAuth.getInstance().getUid())){
            return SENDER_VIEW_TYPE;
        }else{
            return RECEIVE_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull UsersHolder holder, int position) {
        Users users=usersArrayList.get(position);
        holder.binding.userNameList.setText(users.getUserName());
        Picasso.get().load(users.getProfilepic()).placeholder(R.drawable.ic_profile).into(holder.binding.image);

        final String myUid=FirebaseAuth.getInstance().getUid();
        final String userUid= users.getUserId();

        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(myUid+userUid)
                .limitToLast(1)
                .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                        MessageModel messageModel=dataSnapshot1.getValue(MessageModel.class);
                        String messageSendUid=messageModel.getSendUid();
                        if (messageSendUid.equals(myUid)){
                            holder.binding.lastMessage.setText("Ben: "+messageModel.getMessage());
                        }else{
                            holder.binding.lastMessage.setText(messageModel.getMessage());
                        }

                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e.getMessage());
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(context, ChatDetailActivity.class);
                intent.putExtra("userId",users.getUserId());
                intent.putExtra("profilePic",users.getProfilepic());
                intent.putExtra("userName",users.getUserName());
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public class UsersHolder extends RecyclerView.ViewHolder {

        SampleShowUserBinding binding;

        public UsersHolder(@NonNull SampleShowUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }



}
