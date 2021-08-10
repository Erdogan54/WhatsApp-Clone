package com.ozgurerdogan.chatexample;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ozgurerdogan.chatexample.databinding.ActivitySettingsBinding;
import com.ozgurerdogan.chatexample.models.Users;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;

    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Uri selectedImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        auth=FirebaseAuth.getInstance();

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(SettingsActivity.this,MainActivity.class);
                startActivity(i);
            }
        });

        registerLauncher();

        getDataFirebase();

        binding.settingSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userName=binding.userNameTxt.getText().toString();
                String about=binding.userAboutTxt.getText().toString();

                HashMap<String,Object> obj=new HashMap<>();
                obj.put("userName",userName);
                obj.put("about",about);

                database.getReference().child("Users").child(auth.getUid()).updateChildren(obj).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this,"Saved Succesfully",Toast.LENGTH_LONG).show();
                        }
                    }
                });


            }
        });


        binding.imageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                        Snackbar.make(getWindow().getDecorView().getRootView(),"Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give permission", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //izin isteme
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                            }
                        }).show();
                    }else {
                        //izin isteme
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }else{
                    Intent intentToGallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }
            }
        });



    }


    private void registerLauncher(){
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode()==RESULT_OK){
                    Intent intentFromResult=result.getData();
                    if (intentFromResult!=null){
                        selectedImageUri=intentFromResult.getData();
                        binding.imageProfile.setImageURI(selectedImageUri);
                        final StorageReference reference=storage.getReference().child("profilepic")
                                .child(FirebaseAuth.getInstance().getUid());

                        reference.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()){
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                                                    .child("profilepic").setValue(uri.toString());


                                            Toast.makeText(SettingsActivity.this, "Profile picture updated. ", Toast.LENGTH_SHORT).show();

                                        }
                                    });


                                }else{
                                    Toast.makeText(SettingsActivity.this, task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                }
            }
        });

        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                    Intent intentToGallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }else{
                    Toast.makeText(SettingsActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();

                }
            }
        });

    }

    public void getDataFirebase(){
        String uid=auth.getUid();
        database.getReference().child("Users")
                .child(uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    Users users=dataSnapshot.getValue(Users.class);

                    binding.userNameTxt.setText(users.getUserName());
                    binding.userAboutTxt.setText(users.getAbout());
                    Picasso.get().load(users.getProfilepic()).placeholder(R.drawable.ic_profile).into(binding.imageProfile);
                }
            }
        });

    }




}