package com.moutamid.daiptv.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.fxn.stash.Stash;
import com.moutamid.daiptv.BaseActivity;
import com.moutamid.daiptv.adapters.MyListAdapter;
import com.moutamid.daiptv.databinding.ActivityMyListBinding;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;

public class MyListActivity extends BaseActivity {
    ActivityMyListBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.back.setOnClickListener(v -> onBackPressed());

        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        ArrayList<FavoriteModel> list  = Stash.getArrayList(userModel.id, FavoriteModel.class);

        MyListAdapter adapter = new MyListAdapter(this, list);
        binding.myList.setAdapter(adapter);
    }
}
