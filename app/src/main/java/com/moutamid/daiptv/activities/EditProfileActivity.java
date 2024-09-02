package com.moutamid.daiptv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.Nullable;

import com.fxn.stash.Stash;
import com.moutamid.daiptv.BaseActivity;
import com.moutamid.daiptv.MainActivity;
import com.moutamid.daiptv.databinding.ActivityEditProfileBinding;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.Objects;

public class EditProfileActivity extends BaseActivity {
    ActivityEditProfileBinding binding;
    UserModel userModel;
    private static final String TAG = "EditProfileActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);

        String name = userModel.name == null ? "" : userModel.name;
        binding.name.getEditText().setText(name);
        binding.username.getEditText().setText(userModel.username);
        binding.password.getEditText().setText(userModel.password);
        binding.url.getEditText().setText(userModel.url.replace(":8080/", ""));

        binding.back.setOnClickListener(v -> onBackPressed());

        binding.signin.setOnClickListener(v -> {
            if (valid()) {
                String url = binding.url.getEditText().getText().toString() + ":8080/";
                UserModel userModel = new UserModel(
                        this.userModel.id,
                        binding.name.getEditText().getText().toString(),
                        binding.username.getEditText().getText().toString(),
                        binding.password.getEditText().getText().toString(),
                        url
                );
                ArrayList<UserModel> userList = Stash.getArrayList(Constants.USER_LIST, UserModel.class);
                int index = userList.stream()
                        .filter(model -> Objects.equals(model.id, userModel.id))
                        .findFirst()
                        .map(userList::indexOf)
                        .orElse(-1);
                Log.d(TAG, "onCreate: index  " + index);
                if (index != -1) {
                    userList.get(index).name = userModel.name;
                }
                Stash.put(Constants.USER, userModel);
                Stash.put(Constants.USER_LIST, userList);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    private boolean valid() {
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
