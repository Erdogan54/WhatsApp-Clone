package com.ozgurerdogan.chatexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;

import android.os.Bundle;

import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.ozgurerdogan.chatexample.adapter.ChatAdapter;
import com.ozgurerdogan.chatexample.databinding.ActivityChatDetailBinding;
import com.ozgurerdogan.chatexample.models.MessageModel;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;


public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    ArrayList<MessageModel> messageModels;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        final String sendUid=auth.getUid();

        String recUid= getIntent().getStringExtra("userId");
        String userName=getIntent().getStringExtra("userName");
        String profilePic=getIntent().getStringExtra("profilePic");

        binding.userName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.ic_profile).into(binding.imageProfile);

        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChatDetailActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        messageModels=new ArrayList<>();

        binding.recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        final ChatAdapter chatAdapter=new ChatAdapter(messageModels,this);
        binding.recyclerViewChat.setAdapter(chatAdapter);

        final String senderRoom=sendUid+recUid;
        final String receiverRoom=recUid+sendUid;

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=binding.etMessage.getText().toString();

                final MessageModel messageModel=new MessageModel();
                long timestamp=new Date().getTime();
                messageModel.setTimestamp(timestamp);
                messageModel.setMessage(message);
                messageModel.setSendUid(sendUid);
                messageModel.setRecUid(recUid);
                binding.etMessage.setText("");

                //--------------set data----------------------------------
                database.getReference().child("chats")
                        .child(senderRoom)
                        .child(String.valueOf(timestamp))
                        .setValue(messageModel)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child(String.valueOf(timestamp))
                                        .setValue(messageModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                            }
                        });
            }
        });

        //------------get data----------------------------------------
        database.getReference()
                .child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for (DataSnapshot snapshot1:snapshot.getChildren()){
                            MessageModel model= snapshot1.getValue(MessageModel.class);
                            messageModels.add(model);
                        }
                        chatAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

}