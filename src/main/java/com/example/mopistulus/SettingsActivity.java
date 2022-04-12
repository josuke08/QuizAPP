package com.example.mopistulus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, PrefsStorage {

    private final int MAX_SIZE = 20;
    private final int MIN_SIZE = 5;

    private EditText etSize;
    private EditText etType;
    private Button bApply;

    private String currentUsername;
    private int length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        currentUsername = getCurrentUserName();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((TextView)findViewById(R.id.tbUsername)).setText(currentUsername);

        etType = (EditText) findViewById(R.id.quiztype);
        etSize = (EditText) findViewById(R.id.quizsize);
        bApply = (Button) findViewById(R.id.bApply);
        bApply.setOnClickListener(this);

        setInitSettings();
    }

    private void setInitSettings() {
        etType.setText(getQuizType());
        etSize.setText(getQuizSize());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bApply:
                if (verifyLength() && verifyType()) {
                    save();
                    startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                }
                break;
        }
    }

    private void save() {
        setQuizSize(etSize.getText().toString().trim());
        setQuizType(etType.getText().toString().trim());
    }

    private boolean verifyLength() {
        int length = Integer.valueOf(etSize.getText().toString().trim());

        if (etSize.getText().toString().trim().isEmpty()) {
            etSize.setError("Enter a value");
            etSize.requestFocus();
            return false;
        }

        if (length < MIN_SIZE || length > MAX_SIZE) {
            etSize.setError("it should be at least 5 and at most 20");
            etSize.requestFocus();
            return false;
        }

        return true;
    }

    private boolean verifyType() {
        String quizType = etType.getText().toString().trim();

        if (quizType.isEmpty()) {
            etType.setError("Enter a value");
            etType.requestFocus();
            return false;
        }

        if (!quizType.toLowerCase().equals("flags") && !quizType.toLowerCase().equals("countries")) {
            etType.setError("it should be either flags or countries");
            etType.requestFocus();
            return false;
        }

        return true;
    }

    //TOOLBAR///////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbaroptions, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.itSettings).setVisible(false);
        if(getCurrentUserName().equals(DEFAULT_USERNAME))
            menu.findItem(R.id.itLogout).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itLogout:
                logOut();
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                break;
        }
        return true;
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
    }

    //preferences///////////////////////////////////////////////////////////////////////////////////
    public String getQuizSize() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return prefs.getString(QUIZ_SIZE, DEFAULT_QUIZ_SIZE);
    }

    public void setQuizSize(String quizSize) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(QUIZ_SIZE, quizSize).apply();
    }

    public void setCurrentUserName(String currentUserName) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        currentUserName = currentUserName.substring(0, 1).toUpperCase() + currentUserName.substring(1);
        editor.putString(CURRENTUSERNAME, currentUserName).apply();
    }

    public String getCurrentUserName(){
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return prefs.getString(CURRENTUSERNAME, DEFAULT_USERNAME);
    }

    public String getQuizType(){
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return prefs.getString(QUIZ_TYPE, DEFAULT_QUIZ_TYPE);
    }

    public void setQuizType(String quizType) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(QUIZ_TYPE, quizType.toLowerCase()).apply();
    }
}