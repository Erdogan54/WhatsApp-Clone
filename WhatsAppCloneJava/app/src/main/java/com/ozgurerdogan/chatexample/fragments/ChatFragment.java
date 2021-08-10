package com.ozgurerdogan.chatexample.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.ozgurerdogan.chatexample.R;
import com.ozgurerdogan.chatexample.adapter.UsersAdapter;
import com.ozgurerdogan.chatexample.databinding.FragmentChatBinding;
import com.ozgurerdogan.chatexample.models.Users;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;


public class ChatFragment extends Fragment {

    public ChatFragment() {
        //constructor
    }

    FragmentChatBinding binding;
    ArrayList<Users> usersArrayList=new ArrayList<>();
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseUser user;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentChatBinding.inflate(inflater,container,false);

        auth=FirebaseAuth.getInstance();
        user=FirebaseAuth.getInstance().getCurrentUser();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        binding.recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        UsersAdapter adapter=new UsersAdapter(usersArrayList,getContext());
        binding.recyclerViewChat.setAdapter(adapter);

        database.getReference().child("Users")
                .orderByChild("mail")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();
                String authGetUid= auth.getUid();

                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Users users=dataSnapshot.getValue(Users.class);
                    users.setUserId(dataSnapshot.getKey());

                    if (!users.getUserId().equals(authGetUid)){
                        usersArrayList.add(users);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        getDataFirebase();

        return binding.getRoot();
        //return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    public void getDataFirebase(){
        String phoneNumber=user.getPhoneNumber();
        String displayName=user.getDisplayName();
        String userId=user.getUid();
        String mail=user.getEmail();

        database.getReference().child("Users").child(userId)
                .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Users users=dataSnapshot.getValue(Users.class);

                if (TextUtils.isEmpty(users.getProfilepic()) ){

                    storage.getReference().child("profilepic").child(userId).getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    HashMap<String, Object> obj =new HashMap<>();
                                    obj.put("profilepic",uri.toString());
                                    database.getReference().child("Users").child(userId).updateChildren(obj);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Uri uri =user.getPhotoUrl();

                            if (uri !=null){ //farklı hesaplardan bir resmi varsa
                                HashMap<String, Object> obj =new HashMap<>();
                                obj.put("profilepic",uri.toString());
                                database.getReference().child("Users").child(userId).updateChildren(obj);

                            }
                        }
                    });

                }

                if (TextUtils.isEmpty(users.getPhoneNumber()) && !TextUtils.isEmpty(phoneNumber)){
                    HashMap<String,Object> obj =new HashMap<>();
                    obj.put("phoneNumber",phoneNumber);
                    database.getReference().child("Users").child(userId).updateChildren(obj);
                }

                if (TextUtils.isEmpty(users.getUserName()) && !TextUtils.isEmpty(displayName)){
                    HashMap<String,Object> obj =new HashMap<>();
                    obj.put("userName",displayName);
                    database.getReference().child("Users").child(userId).updateChildren(obj);
                }

                if (TextUtils.isEmpty(users.getUserId()) && !TextUtils.isEmpty(userId)){
                    HashMap<String,Object> obj =new HashMap<>();
                    obj.put("userId",userId);
                    database.getReference().child("Users").child(userId).updateChildren(obj);
                }
                if (TextUtils.isEmpty(users.getMail()) && !TextUtils.isEmpty(mail)){
                    HashMap<String,Object> obj1=new HashMap<>();
                    obj1.put("mail",mail);
                    database.getReference().child("Users").child(userId).updateChildren(obj1);
                }

            }
        });

    }
}