package com.example.coralmap.ui;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.coralmap.R;
import com.example.coralmap.models.Utilisateur;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import static android.text.TextUtils.isEmpty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;


public class Registration extends AppCompatActivity implements
        View.OnClickListener
{
    private static final String TAG = "Registration";

    //widgets
    private EditText mEmail, mUsername, mPassword, mConfirmPassword;
    private ProgressBar mProgressBar;

    //vars
    private FirebaseFirestore mDb;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mEmail = (EditText) findViewById(R.id.input_email);
        mUsername = (EditText) findViewById(R.id.input_username);
        mPassword = (EditText) findViewById(R.id.input_password);
        mConfirmPassword = (EditText) findViewById(R.id.input_confirm_password);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        findViewById(R.id.btn_register).setOnClickListener(this);

        mDb = FirebaseFirestore.getInstance();

        hideSoftKeyboard();
    }

    /**
     * Register a new email and password to Firebase Authentication
     * @param email
     * @param password
     */
    public void registerNewEmail(final String email, String password) {
        showDialog();

        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: AuthState: " + FirebaseAuth.getInstance().getCurrentUser().getUid());

                                Utilisateur user = new Utilisateur();
                                user.setEmail(email);
                                user.setUsername(mUsername.getText().toString());
                                user.setUser_id(FirebaseAuth.getInstance().getUid());

                                FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                        .setPersistenceEnabled(true)
                                        .build();
                                mDb.setFirestoreSettings(settings);

                                DocumentReference newUserRef = mDb
                                        .collection(getString(R.string.collection_users))
                                        .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

                                newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        hideDialog();

                                        if (task.isSuccessful()) {
                                            redirectLoginScreen();
                                        } else {
                                            View parentLayout = findViewById(android.R.id.content);
                                            Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            } else {
                                View parentLayout = findViewById(android.R.id.content);
                                Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                                hideDialog();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Registration.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            hideDialog();
        }
    }

    /**
     * Redirects the user to the login screen
     */
    private void redirectLoginScreen(){
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");

        Intent intent = new Intent(Registration.this, Login.class);
        startActivity(intent);
        finish();
    }


    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_register) {
            Log.d(TAG, "onClick: attempting to register.");

            //check for null valued EditText fields
            if (!isEmpty(mEmail.getText().toString())
                    && !isEmpty(mPassword.getText().toString())
                    && !isEmpty(mConfirmPassword.getText().toString())) {

                //check if passwords match
                if (mPassword.getText().toString().equals(mConfirmPassword.getText().toString())) {

                    //Initiate registration task
                    registerNewEmail(mEmail.getText().toString(), mPassword.getText().toString());
                } else {
                    Toast.makeText(Registration.this, "Passwords do not Match", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(Registration.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
            }
        }
    }
}