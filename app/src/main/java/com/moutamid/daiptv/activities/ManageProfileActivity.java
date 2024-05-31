package com.moutamid.daiptv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.fxn.stash.Stash;
import com.moutamid.daiptv.BaseActivity;
import com.moutamid.daiptv.MainActivity;
import com.moutamid.daiptv.databinding.ActivityManageProfileBinding;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.Constants;
import com.moutamid.daiptv.utilis.Features;

import java.util.ArrayList;

public class ManageProfileActivity extends BaseActivity {
    ActivityManageProfileBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayList<UserModel> userList = Stash.getArrayList(Constants.USER_LIST, UserModel.class);
        UserModel model = userList.get(0);
        binding.username1.setText(model.username);

        if (userList.size() >= 2){
            binding.layout2.setVisibility(View.VISIBLE);
            UserModel model1 = userList.get(1);
            binding.username2.setText(model1.username);
        }

        if (userList.size() >= 3){
            binding.layout3.setVisibility(View.VISIBLE);
            UserModel model1 = userList.get(2);
            binding.username3.setText(model1.username);
        }

        if (userList.size() >= 4){
            binding.layout4.setVisibility(View.VISIBLE);
            binding.add.setVisibility(View.GONE);
            UserModel model1 = userList.get(3);
            binding.username3.setText(model1.username);
        }

        binding.add.setOnClickListener(v -> {
            Constants.checkFeature(ManageProfileActivity.this, Features.ADD_PROFILE);
            startActivity(new Intent(ManageProfileActivity.this, LoginActivity.class).putExtra("addProfile", true));
            finish();
        });

        binding.profile1.setOnClickListener(v -> {
            Stash.put(Constants.USER, userList.get(0));
            startActivity(new Intent(ManageProfileActivity.this, MainActivity.class));
            finish();
        });

        binding.profile2.setOnClickListener(v -> {
            Stash.put(Constants.USER, userList.get(1));
            startActivity(new Intent(ManageProfileActivity.this, MainActivity.class));
            finish();
        });

        binding.profile3.setOnClickListener(v -> {
            Stash.put(Constants.USER, userList.get(2));
            startActivity(new Intent(ManageProfileActivity.this, MainActivity.class));
            finish();
        });

        binding.profile4.setOnClickListener(v -> {
            Stash.put(Constants.USER, userList.get(3));
            startActivity(new Intent(ManageProfileActivity.this, MainActivity.class));
            finish();
        });
    }
}
