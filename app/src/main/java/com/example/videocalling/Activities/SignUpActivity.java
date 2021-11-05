package com.example.videocalling.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.videocalling.Utilities.Constants;
import com.example.videocalling.Utilities.PreferenceManager;
import com.example.videocalling.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.textSignIn.setOnClickListener(v -> onBackPressed());


        binding.buttonSignUp.setOnClickListener(v -> {
            if (getInputsContentText(binding.inputFirstName).isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Enter first name", Toast.LENGTH_SHORT).show();
            } else if (getInputsContentText(binding.inputLastName).isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Enter last name", Toast.LENGTH_SHORT).show();
            } else if (getInputsContentText(binding.inputEmail).isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(getInputsContentText(binding.inputEmail)).matches()) {
                Toast.makeText(SignUpActivity.this, "Enter valid email", Toast.LENGTH_SHORT).show();
            } else if (getInputsContentText(binding.inputPassword).isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
            } else if (getInputsContentText(binding.inputConfirmPassword).isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Confirm your password", Toast.LENGTH_SHORT).show();
            } else if (!getInputsContentText(binding.inputPassword).equals(getInputsContentText(binding.inputConfirmPassword))) {
                Toast.makeText(SignUpActivity.this, "Password & confirm password must be same", Toast.LENGTH_SHORT).show();
            } else {
                signUp();
            }
        });
    }

    private void signUp() {
        binding.buttonSignUp.setVisibility(View.INVISIBLE);
        binding.signUpProgressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME, getInputsContentText(binding.inputFirstName));
        user.put(Constants.KEY_LAST_NAME, getInputsContentText(binding.inputLastName));
        user.put(Constants.KEY_EMAIL, getInputsContentText(binding.inputEmail));
        user.put(Constants.KEY_PASSWORD, getInputsContentText(binding.inputPassword));

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_FIRST_NAME, getInputsContentText(binding.inputFirstName));
                    preferenceManager.putString(Constants.KEY_LAST_NAME, getInputsContentText(binding.inputLastName));
                    preferenceManager.putString(Constants.KEY_EMAIL, getInputsContentText(binding.inputEmail));
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    binding.signUpProgressBar.setVisibility(View.INVISIBLE);
                    binding.buttonSignUp.setVisibility(View.VISIBLE);
                    Toast.makeText(SignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getInputsContentText(EditText editText) {
        return editText.getText().toString().trim();
    }
}