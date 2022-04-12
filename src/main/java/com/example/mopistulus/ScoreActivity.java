package com.example.mopistulus;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class ScoreActivity extends AppCompatActivity implements View.OnClickListener, PrefsStorage {

    private TextView txtScore;
    private Button retry, share;
    private app.futured.donut.DonutProgressView donutView;

    private String currentUsername;
    private int score;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        currentUsername = getCurrentUserName();

        setToolbar();

        retry = (Button) findViewById(R.id.bRetry);
        retry.setOnClickListener(this);
        share = (Button) findViewById(R.id.bShare);
        share.setOnClickListener(this);

        txtScore = (TextView) findViewById(R.id.txtScore);

        importScore();
        startDonutAnimation();
        startCountAnimation();

    }

    private void importScore() {
        Bundle bundle = getIntent().getExtras();
        this.score = bundle.getInt("score");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startDonutAnimation() {
        donutView = (app.futured.donut.DonutProgressView) findViewById(R.id.donut_view);
        donutView.setAnimationDurationMs(1000);
        donutView.setCap(100);
        donutView.addAmount("", score, getColor(R.color.purple_500));
    }


    private void startCountAnimation() {
        ValueAnimator animator = ValueAnimator.ofInt(0, score); //0 is min number, score is max number
        animator.setDuration(1000); //Duration is in milliseconds
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                txtScore.setText(animation.getAnimatedValue().toString() + "%");
            }
        });
        animator.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bRetry:
                startActivity(new Intent(ScoreActivity.this, QuestionActivity.class));
                break;
            case R.id.bShare:
                break;
        }
    }

    //TOOLBAR///////////////////////////////////////////////////////////////////////////////////////
    private void setToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((TextView)findViewById(R.id.tbUsername)).setText(getCurrentUserName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbaroptions, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(getCurrentUserName().equals(DEFAULT_USERNAME))
            menu.findItem(R.id.itLogout).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itSettings:
                startActivity(new Intent(ScoreActivity.this, SettingsActivity.class));
                break;
            case R.id.itLogout:
                logOut();
                startActivity(new Intent(ScoreActivity.this, MainActivity.class));
                break;
        }
        return true;
    }

    private void logOut(){
        FirebaseAuth.getInstance().signOut();
    }

    //PREFERENCES///////////////////////////////////////////////////////////////////////////////////
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
}