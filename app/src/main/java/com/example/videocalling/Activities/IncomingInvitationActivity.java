package com.example.videocalling.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.videocalling.R;
import com.example.videocalling.databinding.ActivityIncomingInvitationBinding;

public class IncomingInvitationActivity extends AppCompatActivity {

    ActivityIncomingInvitationBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomingInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}