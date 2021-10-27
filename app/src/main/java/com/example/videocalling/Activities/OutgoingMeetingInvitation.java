package com.example.videocalling.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.videocalling.Models.User;
import com.example.videocalling.R;
import com.example.videocalling.databinding.ActivityOutgoingMeetingInvitationBinding;

public class OutgoingMeetingInvitation extends AppCompatActivity {

    ActivityOutgoingMeetingInvitationBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutgoingMeetingInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String meetingType = getIntent().getStringExtra("type");
        if (meetingType!=null){
            if (meetingType.equals("video")){
                binding.imageMeetingType.setImageResource(R.drawable.ic_video);
            }
        }

        User user = (User) getIntent().getSerializableExtra("user");
        if (user!=null){
            binding.textFirstChar.setText(user.firstName.substring(0,1));
            binding.textUsername.setText(String.format("%s %s",user.firstName, user.lastName));
            binding.textEmail.setText(user.email);

            binding.imageStopInvitation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }
}