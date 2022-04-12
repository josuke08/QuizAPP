package com.example.mopistulus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class QuestionActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, PrefsStorage {

    private ImageView img;
    private RadioButton ans[];
    private Button next;
    private RadioGroup rg1, rg2;
    private TextView pageIndex;

    private static int counter;
    public static int score;
    private static Question[] allImages;
    private static Question[] allQuest;

    private String[] res;
    private int goodRes;
    private static boolean preventCascade = true;
    private int quizSize;
    private  String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        quizSize = getQuizSize();
        currentUsername = getCurrentUserName();

        setToolbar();

        next = (Button) findViewById(R.id.bNext);
        next.setOnClickListener(this);
        pageIndex = (TextView) findViewById(R.id.pageIndex);
        pageIndex.setText(counter + 1 + "/" + quizSize);

        if (allImages == null) {
            getAllQuestions();
        }
        else{

            setImage();
            setAnswers();
        }
    }

    private Question[] getAllQuestions(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("questions");
        FirebaseAuth.getInstance();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                allQuest = new Question[(int)snapshot.getChildrenCount()];
                int i = 0;
                for(DataSnapshot ss: snapshot.getChildren()){
                     allQuest[i++] = ss.getValue(Question.class);
                }

                setQuiz(quizSize);
                setImage();
                setAnswers();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return allQuest;
    }


    private void setQuiz(int length) {

        Log.d("I HAVE ", allQuest.length + " files in here");
        allImages = new Question[length];
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0, rand; i < length; i++) {
            do {
                rand = random.nextInt(allQuest.length);
            } while (containsAns(allImages, i, allQuest[rand].name));
            allImages[i] = allQuest[rand];
            Log.e("competitor", allImages[i].toString());
        }
    }

    private void setImage() {
        img = (ImageView) findViewById(R.id.imageTest);
        InputStream is = null;
        Log.e("I am at", counter + "");
        try {
            Log.e("hoho", ((getQuizType() == "countries")? allImages[counter].country: allImages[counter].flag).toLowerCase().substring(1));
            is = getAssets().open(((getQuizType() == "countries")? allImages[counter].country: allImages[counter].flag).toLowerCase().substring(1));
        } catch (IOException e) {
            Log.e("I am at", counter + "");
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        img.setImageBitmap(bitmap);

    }

    private boolean containsAns(Question[] ans, int n, String name) {
        for (int i = 0; i < n; i++) {
            if (name.equals(ans[i].name)) return true;
        }
        return false;
    }

    private boolean containsAns(String[] ans, int n, String name) {
        for (int i = 0; i < n; i++) {
            if (name.equals(ans[i])) return true;
        }
        return false;
    }

    private void setAnswers() {
        res = new String[4];
        Log.d("la taille", allQuest.length + "");

        rg1 = (RadioGroup) findViewById(R.id.res);
        rg1.setOnCheckedChangeListener((RadioGroup.OnCheckedChangeListener) this);
        rg2 = (RadioGroup) findViewById(R.id.ress);
        rg2.setOnCheckedChangeListener((RadioGroup.OnCheckedChangeListener) this);
        ans = new RadioButton[4];
        ans[0] = (RadioButton) findViewById(R.id.res1);
        ans[1] = (RadioButton) findViewById(R.id.res2);
        ans[2] = (RadioButton) findViewById(R.id.res3);
        ans[3] = (RadioButton) findViewById(R.id.res4);


        res[0] = allImages[counter].name;

        Random random = new Random(System.currentTimeMillis());
        for (int i = 1; i < 4; i++) {
            int rand;
            //while((rand = random.nextInt(allImages.length)) == counter);
            do {
                rand = random.nextInt(allQuest.length);
                res[i] = allQuest[rand].name;
            } while (containsAns(res, i, res[i]));

            //res[i] = allImages[rand].substring(0, allQuest[rand].lastIndexOf('.'));
        }


        goodRes = (int) random.nextInt(4);

        for (int i = 0, j = 1; i < 4; i++, j++) {
            if (goodRes == i) {
                ans[i].setText(res[0]);
                j--;
            } else {
                ans[i].setText(res[j]);
            }
        }
    }


    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.bNext:
                loadQuestion();
                break;
        }
    }

    public static void endQuiz() {
        allImages = null;
        allQuest = null;
        score = 0;
        counter = 0;
    }

    private void loadQuestion() {

        if (rg1.getCheckedRadioButtonId() == -1 && rg2.getCheckedRadioButtonId() == -1) {
            Log.e("Hey yo", "There is somehing weird here we have rg1 = " + rg1.getCheckedRadioButtonId() + " and " + rg2.getCheckedRadioButtonId());
            ((RadioButton) rg1.getChildAt(1)).setError("Choose an answer");
            rg1.requestFocus();
            return;
        }

        if ((goodRes < 2 && ((RadioButton) rg1.getChildAt(goodRes)).isChecked())
                || (goodRes >= 2 && ((RadioButton) rg2.getChildAt(goodRes - 2)).isChecked()))
            score++;
        counter++;

        Log.d("Hey", "Every thing is Okey here");
        Intent intent;
        if (counter < quizSize && counter < allQuest.length) {
            Log.e("Hey dude", "I ll go in with index " + counter + " because the limit is " + quizSize);
            intent = new Intent(QuestionActivity.this, QuestionActivity.class);
        } else {
            intent = new Intent(QuestionActivity.this, ScoreActivity.class);
            intent.putExtra("score", (int) ((double) score * 100 / quizSize));
            endQuiz();
        }


        startActivity(intent);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

        switch (radioGroup.getId()) {
            case R.id.res:

                if (rg2.getCheckedRadioButtonId() != -1 && preventCascade) {
                    preventCascade = false;
                    rg2.clearCheck();
                } else//to prevent false preventCascade from changing its value at the end of the listener
                    return;
                break;
            case R.id.ress:

                if (rg1.getCheckedRadioButtonId() != -1 && preventCascade) {
                    preventCascade = false;
                    rg1.clearCheck();
                } else//to prevent false preventCascade from changing its value at the end of the listener
                    return;
                break;
        }

        preventCascade = true;
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
        Log.e("HDHDH", getCurrentUserName());
        if(getCurrentUserName().equals(DEFAULT_USERNAME))
            menu.findItem(R.id.itLogout).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itSettings:
                startActivity(new Intent(QuestionActivity.this, SettingsActivity.class));
                break;
            case R.id.itLogout:
                logOut();
                startActivity(new Intent(QuestionActivity.this, MainActivity.class));
                break;
        }
        return true;
    }

    private void logOut(){
        FirebaseAuth.getInstance().signOut();
    }

    //PREFERENCES///////////////////////////////////////////////////////////////////////////////////
    public int getQuizSize() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return Integer.valueOf(prefs.getString(QUIZ_SIZE, DEFAULT_QUIZ_SIZE));
    }

    public String getCurrentUserName(){
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return prefs.getString(CURRENTUSERNAME, DEFAULT_USERNAME);
    }

    public void setCurrentUserName(String currentUserName) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        currentUserName = currentUserName.substring(0, 1).toUpperCase() + currentUserName.substring(1);
        editor.putString(CURRENTUSERNAME, currentUserName).apply();
    }

    public String getQuizType(){
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return prefs.getString(QUIZ_TYPE, DEFAULT_QUIZ_TYPE);
    }
}