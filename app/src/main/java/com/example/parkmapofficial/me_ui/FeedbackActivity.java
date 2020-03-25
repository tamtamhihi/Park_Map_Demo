package com.example.parkmapofficial.me_ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.parkmapofficial.R;
import com.example.parkmapofficial.sharedpref.CustomSharedPreferences;

import java.util.Objects;

public class FeedbackActivity extends AppCompatActivity {

    private CustomSharedPreferences appMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appMode = new CustomSharedPreferences(this);
        if (appMode.getNightModeState())
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_feedback);

        final EditText uSubject = findViewById(R.id.subject);
        final EditText uFeedback = findViewById(R.id.feedback);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.feedback_title);
        Button submitButton = findViewById(R.id.submit_feedback);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uFeedback.getText().toString().trim().isEmpty()) {
                    displayToast(getString(R.string.feedback_ask));
                    return;
                }
                Intent sendEmail = new Intent(Intent.ACTION_SEND);
                sendEmail.setType("message/rfc822");
                sendEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{"tam.ngocbang.nguyen@gmail.com"});
                sendEmail.putExtra(Intent.EXTRA_SUBJECT, uSubject.getText().toString());
                sendEmail.putExtra(Intent.EXTRA_TEXT, uFeedback.getText().toString());

                try {
                    startActivity(Intent.createChooser(sendEmail, getString(R.string.email_via)));
                }
                catch (ActivityNotFoundException e) {
                    displayToast(getString(R.string.no_email_clients));
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
            Log.e("FeedbackActivity", "Up button pressed");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
