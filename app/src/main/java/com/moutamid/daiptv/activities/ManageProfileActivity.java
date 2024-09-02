package com.moutamid.daiptv.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.fxn.stash.Stash;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.moutamid.daiptv.BaseActivity;
import com.moutamid.daiptv.MainActivity;
import com.moutamid.daiptv.R;
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
        String name = model.name == null ? model.username : model.name;
        binding.username1.setText(name);

        if (userList.size() >= 2) {
            binding.layout2.setVisibility(View.VISIBLE);
            UserModel model1 = userList.get(1);
            String name1 = model1.name == null ? model1.username : model1.name;
            binding.username2.setText(name1);
        }

        if (userList.size() >= 3) {
            binding.layout3.setVisibility(View.VISIBLE);
            UserModel model1 = userList.get(2);
            String name1 = model1.name == null ? model1.username : model1.name;
            binding.username3.setText(name1);
        }

        if (userList.size() >= 4) {
            binding.layout4.setVisibility(View.VISIBLE);
            binding.add.setVisibility(View.GONE);
            UserModel model1 = userList.get(3);
            String name1 = model1.name == null ? model1.username : model1.name;
            binding.username4.setText(name1);
        }

        binding.add.setOnClickListener(v -> {
            Constants.checkFeature(ManageProfileActivity.this, Features.ADD_PROFILE);
            startActivity(new Intent(ManageProfileActivity.this, LoginActivity.class).putExtra("addProfile", true));
            finish();
        });

//        binding.profile1.setOnLongClickListener(v -> {
//            updateName(0);
//            return false;
//        });
//        binding.profile2.setOnLongClickListener(v -> {
//            updateName(1);
//            return false;
//        });
//        binding.profile3.setOnLongClickListener(v -> {
//            updateName(2);
//            return false;
//        });
//        binding.profile4.setOnLongClickListener(v -> {
//            updateName(3);
//            return false;
//        });

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

    private void updateName(int pos) {
        ArrayList<UserModel> userList = Stash.getArrayList(Constants.USER_LIST, UserModel.class);

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.update_username);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        TextInputLayout name = dialog.findViewById(R.id.name);
        MaterialCardView signin = dialog.findViewById(R.id.signin);

        signin.setOnClickListener(v -> {
            String username = name.getEditText().getText().toString();
            if (username.isEmpty()) {
                Toast.makeText(this, "Le nom est vide", Toast.LENGTH_SHORT).show();
            } else {
                userList.get(pos).name = username;
                switch (pos) {
                    case 0:
                        binding.username1.setText(username);
                        break;
                    case 1:
                        binding.username2.setText(username);
                        break;
                    case 2:
                        binding.username3.setText(username);
                        break;
                    case 3:
                        binding.username4.setText(username);
                        break;
                    default:
                        break;
                }
                Stash.put(Constants.USER_LIST, userList);
                dialog.dismiss();
            }
        });
    }
}
