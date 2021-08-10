package com.ozgurerdogan.chatexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ozgurerdogan.chatexample.adapter.ChatAdapter;
import com.ozgurerdogan.chatexample.databinding.ActivityGroupChatBinding;
import com.ozgurerdogan.chatexample.models.MessageModel;

import java.util.ArrayList;
import java.util.Date;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseUser user;
    ArrayList<MessageModel> messageModelArrayList;
    String sendUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GroupChatActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        auth=FirebaseAuth.getInstance();
        user=FirebaseAuth.getInstance().getCurrentUser();
        database=FirebaseDatabase.getInstance();

        messageModelArrayList=new ArrayList<>();
        sendUid=user.getUid();

        binding.userName.setText("Friends Group Chat");

        binding.recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        final ChatAdapter chatAdapter=new ChatAdapter(messageModelArrayList,this);
        binding.recyclerViewChat.setAdapter(chatAdapter);


        //----set data--------------------------
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String message=binding.etMessage.getText().toString();
                final MessageModel model =new MessageModel();
                Long timestamp=new Date().getTime();
                //model.setMessageId(timestamp.toString());
                model.setSendUid(sendUid);
                model.setMessage(message);
                model.setTimestamp(timestamp);

                binding.etMessage.setText("");

                database.getReference()
                        .child("Group Chat")
                        .child(String.valueOf(timestamp))
                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            }
        });

        //----------get data--------------------------

        database.getReference()
                .child("Group Chat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageModelArrayList.clear();
                for (DataSnapshot data:snapshot.getChildren()){
                    MessageModel model=data.getValue(MessageModel.class);
                    messageModelArrayList.add(model);

                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
}