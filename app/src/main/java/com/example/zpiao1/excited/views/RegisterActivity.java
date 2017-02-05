package com.example.zpiao1.excited.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.logic.AccountUtils;
import com.example.zpiao1.excited.server.HttpError;
import com.example.zpiao1.excited.server.HttpErrorUtils;
import com.example.zpiao1.excited.server.IUserRequest;
import com.example.zpiao1.excited.server.ServerUtils;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import java.util.HashMap;

import io.reactivex.disposables.CompositeDisposable;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private TextInputEditText mEmailText;
    private TextInputEditText mPasswordText;
    private TextInputEditText mConfirmPasswordText;
    private TextInputEditText mNameText;
    private TextInputLayout mEmailInput;
    private TextInputLayout mPasswordInput;
    private TextInputLayout mConfirmPasswordInput;
    private TextInputLayout mNameInput;
    private Button mRegisterBtn;
    private ProgressBar mProgressBar;

    private CompositeDisposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDisposable = new CompositeDisposable();
        mEmailText = (TextInputEditText) findViewById(R.id.register_email);
        mPasswordText = (TextInputEditText) findViewById(R.id.register_password);
        mConfirmPasswordText = (TextInputEditText) findViewById(R.id.register_confirm_password);
        mNameText = (TextInputEditText) findViewById(R.id.register_name);
        mRegisterBtn = (Button) findViewById(R.id.register_btn);
        mEmailInput = (TextInputLayout) findViewById(R.id.email_input);
        mPasswordInput = (TextInputLayout) findViewById(R.id.password_input);
        mConfirmPasswordInput = (TextInputLayout) findViewById(R.id.confirm_password_input);
        mNameInput = (TextInputLayout) findViewById(R.id.name_input);
        mProgressBar = (ProgressBar) findViewById(R.id.register_progress_bar);

        configureUI();
    }

    private void configureUI() {
        mEmailText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {    // when it loses focus
                AccountUtils.checkEmail(mEmailText, mEmailInput);
            }
        });

        mEmailText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                AccountUtils.checkEmail(mEmailText, mEmailInput);
                return mEmailInput.getError() == null;
            }
            return false;
        });

        mPasswordText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                AccountUtils.checkPassword(mPasswordText, mPasswordInput);
            }
        });

        mPasswordText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                AccountUtils.checkPassword(mPasswordText, mPasswordInput);
                return mPasswordInput.getError() == null;
            }
            return false;
        });

        mConfirmPasswordText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                AccountUtils.checkConfirmPasswordAgainstPassword(mConfirmPasswordText,
                        mConfirmPasswordInput,
                        mPasswordText);
            }
        });

        mConfirmPasswordText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                AccountUtils.checkConfirmPasswordAgainstPassword(mConfirmPasswordText,
                        mConfirmPasswordInput,
                        mPasswordText);
                return mConfirmPasswordInput.getError() == null;
            }
            return false;
        });

        mRegisterBtn.setOnClickListener(view -> {
            // Check the input fields
            AccountUtils.checkEmail(mEmailText, mEmailInput);
            AccountUtils.checkPassword(mPasswordText, mPasswordInput);
            AccountUtils.checkConfirmPasswordAgainstPassword(mConfirmPasswordText,
                    mConfirmPasswordInput,
                    mPasswordText);
            if (mEmailInput.getError() != null
                    || mPasswordInput.getError() != null
                    || mConfirmPasswordInput.getError() != null) {
                Toast.makeText(RegisterActivity.this, "Please fill in the field correctly",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // Show the progress bar
            showRegisteringUi();
            register(mEmailText.getText().toString(),
                    mPasswordText.getText().toString(),
                    mNameText.getText().toString());
        });
    }

    private void register(String email, String password, String name) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("name", name);
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        ServerUtils.addToDisposable(mDisposable,
                request.register(body),
                registerResponse -> {
                    hideRegisteringUi();
                    if (registerResponse.success) {
                        new AlertDialog.Builder(RegisterActivity.this)
                                .setTitle(R.string.prompt_verify_email)
                                .setMessage(R.string.verify_email_message)
                                .setPositiveButton("Verify", (dialog, which) -> {
                                    Uri url = Uri.parse(registerResponse.url);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, url);
                                    if (intent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(RegisterActivity.this,
                                                "Cannot open your browser. Please check your email account.",
                                                Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                    dialog.dismiss();
                                    finish();
                                })
                                .setNegativeButton("Later", (dialog, which) -> {
                                    dialog.dismiss();
                                    finish();
                                })
                                .show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error in registering your account",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                throwable -> {
                    Log.e(TAG, "register", throwable);
                    if (throwable instanceof HttpException) {
                        HttpException exception = (HttpException) throwable;
                        HttpError error = HttpErrorUtils.convert(exception);
                        Log.d(TAG, "error: " + error.method);
                        Log.d(TAG, "error: " + error.err.message);
                        Log.d(TAG, "error: " + error.err.name);
                        if (error.err.name.equals("UserExistsError"))
                            Toast.makeText(RegisterActivity.this, "The email address is used already",
                                    Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(RegisterActivity.this, "Error in registering your account",
                                    Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error in registering your account",
                                Toast.LENGTH_SHORT).show();
                    }
                    hideRegisteringUi();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    private void showRegisteringUi() {
        mEmailText.setEnabled(false);
        mPasswordText.setEnabled(false);
        mConfirmPasswordText.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        mRegisterBtn.setVisibility(View.GONE);
    }

    private void hideRegisteringUi() {
        mEmailText.setEnabled(true);
        mPasswordText.setEnabled(true);
        mConfirmPasswordText.setEnabled(true);
        mProgressBar.setVisibility(View.GONE);
        mRegisterBtn.setVisibility(View.VISIBLE);
    }
}
