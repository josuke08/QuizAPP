package com.example.mopistulus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText$InspectionCompanion;
import androidx.appcompat.widget.Toolbar;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PrefsStorage {

    private EditText etEmail, etPwd;
    private TextView register;
    private Button signin;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference ref;

    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setCurrentUserName(DEFAULT_USERNAME);
        setToolbar();

        mAuth = FirebaseAuth.getInstance();

        etEmail = (EditText) findViewById(R.id.etLogin);
        etPwd = (EditText) findViewById(R.id.etPassword);

        register = (TextView) findViewById(R.id.tvRegister);
        register.setOnClickListener(this);
        signin = (Button) findViewById(R.id.bLogin);
        signin.setOnClickListener(this);
    }

    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.tvRegister:
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                break;
            case R.id.bLogin:
                String email = etEmail.getText().toString().trim().toLowerCase();
                if (!email.equals(DEFAULT_USERNAME.toLowerCase()))
                    loginUser();
                else
                    startActivity(new Intent(MainActivity.this, QuestionActivity.class));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    private void getCurrentUserName() {
        ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("I ve been here", "");
                User userObj = snapshot.getValue(User.class);
                if (userObj != null) {
                    currentUserName = userObj.name;
                    setCurrentUserName(currentUserName);
                    Log.d("Guess what", "The user " + currentUserName + " logged");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Capitaine sthg happened", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();

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

        mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("NOP", task.getException().getMessage());
                }
                else
                    Log.e("YEP", "I logged");
            }
        });

        user = mAuth.getCurrentUser();
        if(user == null){
            Log.d("IDK WHY", "But I am null");
            return;
        }
        getCurrentUserName();
        startActivity(new Intent(MainActivity.this, QuestionActivity.class));

    }

    //TOOLBAR///////////////////////////////////////////////////////////////////////////////////////

    private void setToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
        }
        return true;
    }

    //PREFERENCES///////////////////////////////////////////////////////////////////////////////////
    public String getPrefsCurrentUserName(){
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return prefs.getString(CURRENTUSERNAME, DEFAULT_USERNAME);
    }

    public void setCurrentUserName(String currentUserName) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        currentUserName = currentUserName.substring(0, 1).toUpperCase() + currentUserName.substring(1);
        editor.putString(CURRENTUSERNAME, currentUserName).apply();
    }

    public void setQuizSize(String quizSize) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(QUIZ_SIZE, quizSize).apply();
    }

}