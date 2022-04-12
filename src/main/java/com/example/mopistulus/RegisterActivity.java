package com.example.mopistulus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, PrefsStorage {

    private FirebaseAuth mAuth;
    private TextView register, signin;
    private EditText etName, etEmail, etPwd, etCfPwd;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_register);

        setCurrentUserName(DEFAULT_USERNAME);
        setToolbar();

        register = (TextView) findViewById(R.id.bRegister);
        register.setOnClickListener(this);


        signin = (TextView) findViewById(R.id.tvSignin);
        signin.setOnClickListener(this);

        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPwd = (EditText) findViewById(R.id.etPassword);
        etCfPwd = (EditText) findViewById(R.id.cfPassword);

        pb = (ProgressBar) findViewById(R.id.progressBar);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvSignin:
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.bRegister:
                registerUser();
                break;
        }
    }


    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();
        String cfpwd = etCfPwd.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Full name is required");
            etName.requestFocus();
            return;
        }

        if(name.toLowerCase().equals(DEFAULT_USERNAME.toLowerCase())){
            etName.setError("A name can't be " + DEFAULT_USERNAME);
            etName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email is not valid");
            etEmail.requestFocus();
            return;
        }

        if (pwd.isEmpty()) {
            etPwd.setError("Password is required");
            etPwd.requestFocus();
            return;
        }

        if (pwd.length() < 5) {
            etPwd.setError("Password should at least have 5 characters");
            etPwd.requestFocus();
            return;
        }

        if (cfpwd.isEmpty()) {
            etCfPwd.setError("Confirmation password is required");
            etCfPwd.requestFocus();
            return;
        }

        if (!cfpwd.equals(pwd)) {
            etCfPwd.setError("It doesn't match the password");
            etCfPwd.requestFocus();
            return;
        }

        pb.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(name, email);
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        setCurrentUserName(name);
                                        startActivity(new Intent(RegisterActivity.this, QuestionActivity.class));
                                    } else {
                                        Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                    pb.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            pb.setVisibility(View.GONE);
                        }
                    }
                });
    }

    //TOOLBAR///////////////////////////////////////////////////////////////////////////////////////
    private void setToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbaroptions, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.itLogout).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itSettings:
                startActivity(new Intent(RegisterActivity.this, SettingsActivity.class));
                break;
        }
        return true;
    }

    //PREFERENCES///////////////////////////////////////////////////////////////////////////////////
    private void setCurrentUserName(String userName) {
        SharedPreferences sharedPreferences = getSharedPreferences(PrefsStorage.SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
        editor.putString(CURRENTUSERNAME, userName).apply();
    }
}