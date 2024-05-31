package com.moutamid.daiptv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;

import androidx.annotation.Nullable;

import com.fxn.stash.Stash;
import com.moutamid.daiptv.BaseActivity;
import com.moutamid.daiptv.MainActivity;
import com.moutamid.daiptv.databinding.ActivityEditProfileBinding;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.Constants;

public class EditProfileActivity extends BaseActivity {
    ActivityEditProfileBinding binding;
    UserModel userModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);

        binding.username.getEditText().setText(userModel.username);
        binding.password.getEditText().setText(userModel.password);
        binding.url.getEditText().setText(userModel.url);

        binding.back.setOnClickListener(v -> onBackPressed());

        binding.signin.setOnClickListener(v -> {
            if (valid()) {
                String url = binding.url.getEditText().getText().toString() + ":8080/";
                UserModel userModel = new UserModel(
                        this.userModel.id,
                        binding.username.getEditText().getText().toString(),
                        binding.password.getEditText().getText().toString(),
                        url
                );
                Stash.put(Constants.USER, userModel);
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
