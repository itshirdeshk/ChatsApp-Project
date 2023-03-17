package com.hirdesh.chatsappproject.Activites;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hirdesh.chatsappproject.Models.User;
import com.hirdesh.chatsappproject.databinding.ActivitySetupProfileBinding;

import java.util.Objects;

public class SetupProfileActivity extends AppCompatActivity {


    ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;
    ActivityResultLauncher<String> mTakePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Profile...");
        dialog.setCancelable(false);

        mTakePhoto = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> binding.imageView.setImageURI(result));

        binding.imageView.setOnClickListener(view -> mTakePhoto.launch("image/*"));

        binding.continueBtn.setOnClickListener(view -> {
            String name = binding.nameBox.getText().toString();

            if (name.isEmpty()) {
                binding.nameBox.setError("Please type a Name...");
                return;
            }

            dialog.show();

            if (selectedImage != null) {
                StorageReference reference = storage.getReference().child("Profiles").child(Objects.requireNonNull(auth.getUid()));
                reference.putFile(selectedImage).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            String uid = auth.getUid();
                            String phoneNumber = Objects.requireNonNull(auth.getCurrentUser()).getPhoneNumber();
                            String name1 = binding.nameBox.getText().toString();

                            User user = new User(uid, name1, phoneNumber, imageUrl);

                            database.getReference()
                                    .child("Users")
                                    .child(uid)
                                    .setValue(user)
                                    .addOnSuccessListener(unused -> {
                                        dialog.dismiss();
                                        Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                        });
                    }
                });
            }
            else {
                String uid = auth.getUid();
                String phoneNumber = Objects.requireNonNull(auth.getCurrentUser()).getPhoneNumber();

                User user = new User(uid, name, phoneNumber, "No Image");

                assert uid != null;
                database.getReference()
                        .child("Users")
                        .child(uid)
                        .setValue(user)
                        .addOnSuccessListener(unused -> {
                            dialog.dismiss();
                            Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            if(data.getData() != null) {
                binding.imageView.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }
}