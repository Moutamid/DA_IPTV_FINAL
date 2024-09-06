package com.moutamid.daiptv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.moutamid.daiptv.BaseActivity;
import com.moutamid.daiptv.MainActivity;
import com.moutamid.daiptv.databinding.ActivityLoginBinding;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoginActivity extends BaseActivity {
    ActivityLoginBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        boolean addProfile;

        if (getIntent() != null) {
            addProfile = getIntent().getBooleanExtra("addProfile", false);
        } else {
            addProfile = false;
        }

        if (addProfile) {
            ArrayList<UserModel> userList = Stash.getArrayList(Constants.USER_LIST, UserModel.class);
            UserModel model = userList.get(0);
            binding.url.getEditText().setText(model.url);
            binding.url.setEnabled(false);
        }

        binding.signin.setOnClickListener(v -> {
            if (valid()) {
                String url = binding.url.getEditText().getText().toString();
                UserModel userModel = new UserModel(
                        UUID.randomUUID().toString(),
                        binding.name.getEditText().getText().toString().trim(),
                        binding.username.getEditText().getText().toString().trim(),
                        binding.password.getEditText().getText().toString().trim(),
                        url
                );
                Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show();
                AtomicBoolean isDuplicate = new AtomicBoolean(false);
                Constants.databaseReference().child("USERS").get()
                        .addOnSuccessListener(dataSnapshot -> {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    UserModel model = snapshot.getValue(UserModel.class);
                                    if (model.username.equals(userModel.username) && model.password.equals(userModel.password) && model.url.equals(userModel.url)) {
                                        isDuplicate.set(true);
                                        break;
                                    }
                                }
                            }
                            if (!isDuplicate.get()) {
                                Constants.databaseReference().child("USERS").child(UUID.randomUUID().toString())
                                        .setValue(userModel).addOnSuccessListener(unused -> {
                                            ArrayList<UserModel> userList = Stash.getArrayList(Constants.USER_LIST, UserModel.class);
                                            userList.add(userModel);
                                            Stash.put(Constants.USER, userModel);
                                            Stash.put(Constants.USER_LIST, userList);

//                                            startActivity(new Intent(this, LoadingScreenActivity.class));
//                                            finish();

                                            if (addProfile) {
                                                startActivity(new Intent(this, MainActivity.class));
                                                finish();
                                            } else {
                                                startActivity(new Intent(this, LoadingScreenActivity.class));
                                                finish();
                                            }
                                        }).addOnFailureListener(e -> {
                                            e.printStackTrace();
                                            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                ArrayList<UserModel> userList = Stash.getArrayList(Constants.USER_LIST, UserModel.class);
                                userList.add(userModel);
                                Stash.put(Constants.USER, userModel);
                                Stash.put(Constants.USER_LIST, userList);

//                                startActivity(new Intent(this, LoadingScreenActivity.class));
//                                finish();
                                if (addProfile) {
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                } else {
                                    startActivity(new Intent(this, LoadingScreenActivity.class));
                                    finish();
                                }
                            }
                        }).addOnFailureListener(e -> {
                            e.printStackTrace();
                            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private boolean valid() {
        if (binding.name.getEditText().getText().toString().isEmpty()) {
            binding.name.getEditText().setError("Name is empty");
            binding.name.getEditText().requestFocus();
            return false;
        }
        if (binding.username.getEditText().getText().toString().isEmpty()) {
            binding.username.getEditText().setError("Username is empty");
            binding.username.getEditText().requestFocus();
            return false;
        }
        if (binding.password.getEditText().getText().toString().isEmpty()) {
            binding.password.getEditText().setError("Password is empty");
            binding.password.getEditText().requestFocus();
            return false;
        }
        if (binding.url.getEditText().getText().toString().isEmpty()) {
            binding.url.getEditText().setError("URL is empty");
            binding.url.getEditText().requestFocus();
            return false;
        }
        if (!Patterns.WEB_URL.matcher(binding.url.getEditText().getText().toString()).matches()) {
            binding.url.getEditText().setError("URL is invalid");
            binding.url.getEditText().requestFocus();
            return false;
        }
        return true;
    }

}
