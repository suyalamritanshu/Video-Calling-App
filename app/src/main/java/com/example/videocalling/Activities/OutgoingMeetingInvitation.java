package com.example.videocalling.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.videocalling.Models.User;
import com.example.videocalling.Network.ApiClient;
import com.example.videocalling.Network.ApiService;
import com.example.videocalling.R;
import com.example.videocalling.Utilities.Constants;
import com.example.videocalling.Utilities.PreferenceManager;
import com.example.videocalling.databinding.ActivityOutgoingMeetingInvitationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingMeetingInvitation extends AppCompatActivity {

    ActivityOutgoingMeetingInvitationBinding binding;
    private PreferenceManager preferenceManager;
    private String inviterToken = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutgoingMeetingInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseInstallations.getInstance().getToken(false).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<InstallationTokenResult> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    inviterToken = task.getResult().getToken();
                }
            }
        });

        String meetingType = getIntent().getStringExtra("type");
        if (meetingType != null) {
            if (meetingType.equals("video")) {
                binding.imageMeetingType.setImageResource(R.drawable.ic_video);
            }
        }

        User user = (User) getIntent().getSerializableExtra("user");
        if (user != null) {
            binding.textFirstChar.setText(user.firstName.substring(0, 1));
            binding.textUsername.setText(String.format("%s %s", user.firstName, user.lastName));
            binding.textEmail.setText(user.email);

            binding.imageStopInvitation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });


        }
        if (meetingType!=null && user!=null){
            initiateMeeting(meetingType, user.token);
        }

    }

    private void initiateMeeting(String meetingType, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME, preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME, preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);

            body.put(Constants.REMOTE_MSG_TYPE, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);

        } catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                        Toast.makeText(OutgoingMeetingInvitation.this, "Invitation sent successfully", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(OutgoingMeetingInvitation.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(OutgoingMeetingInvitation.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}