package com.example.videocalling.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videocalling.Adapters.UsersAdapter;
import com.example.videocalling.Listeners.UserListener;
import com.example.videocalling.Models.User;
import com.example.videocalling.Utilities.Constants;
import com.example.videocalling.Utilities.PreferenceManager;
import com.example.videocalling.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements UserListener {

    ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<User> users;
    private UsersAdapter usersAdapter;

    private final int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());




        binding.textTitle.setText(String.format("%s %s",
                preferenceManager.getString(Constants.KEY_FIRST_NAME),
                preferenceManager.getString(Constants.KEY_LAST_NAME)));

        binding.textSignOut.setOnClickListener(v -> signOut());



        FirebaseInstallations.getInstance().getToken(false).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<InstallationTokenResult> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    sendFCMTokenToDatabase(task.getResult().getToken());
                }
            }
        });



        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(users, this);
        binding.usersRecyclerView.setAdapter(usersAdapter);


        binding.swipeRefreshLayout.setOnRefreshListener(this::getUsers);

        getUsers();
        //checkForBatteryOptimizations();
    }

    private void getUsers() {
        binding.swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    binding.swipeRefreshLayout.setRefreshing(false);
                    String myUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        users.clear();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (myUserId.equals(documentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.firstName = documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                            user.lastName = documentSnapshot.getString(Constants.KEY_LAST_NAME);
                            user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            usersAdapter.notifyDataSetChanged();
                        } else {
                            binding.textErrorMessage.setText(String.format("%s", "No users available"));
                            binding.textErrorMessage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        binding.textErrorMessage.setText(String.format("%s", "No users available"));
                        binding.textErrorMessage.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void sendFCMTokenToDatabase(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID));

        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this, "Unable to send token: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void signOut() {
        Toast.makeText(this, "Signing Out...", Toast.LENGTH_SHORT).show();

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID));

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(aVoid -> {
                    preferenceManager.clearPreferences();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this, "Unable to sign out: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void initiateVideoMeeting(User user) {
        final boolean isValidToken = user.token != null && !user.token.trim().isEmpty();

        if (!isValidToken) {
            Toast.makeText(this, user.firstName + " " + user.lastName + " is not available for meeting", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), OutgoingMeetingInvitation.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "video");
            startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(User user) {
        final boolean isValidToken = user.token != null && !user.token.trim().isEmpty();

        if (!isValidToken) {
            Toast.makeText(this, user.firstName + " " + user.lastName + " is not available for meeting", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), OutgoingMeetingInvitation.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "audio");
            startActivity(intent);
        }
    }

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
        if (isMultipleUsersSelected) {
            binding.imageConference.setVisibility(View.VISIBLE);
            binding.imageConference.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), OutgoingMeetingInvitation.class);
                intent.putExtra("selectedUsers", new Gson().toJson(usersAdapter.getSelectedUsers()));
                intent.putExtra("type", "video");
                intent.putExtra("isMultiple", true);
                startActivity(intent);

                System.out.println(usersAdapter.getSelectedUsers().toString());
            });
        } else {
            binding.imageConference.setVisibility(View.GONE);
        }
    }

//    private void checkForBatteryOptimizations() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
//                new AlertDialog.Builder(MainActivity.this)
//                        .setTitle("Waring")
//                        .setMessage("Battery optimization is enabled. It can interrupt running background services")
//                        .setPositiveButton("Disable", (dialog, which) -> {
//                            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//                            startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATIONS);
//                        })
//                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                        .show();
//            }
//        }
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS) {
//            checkForBatteryOptimizations();
//        }
//    }
}