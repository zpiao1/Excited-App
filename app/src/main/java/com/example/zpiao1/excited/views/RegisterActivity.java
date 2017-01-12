package com.example.zpiao1.excited.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.server.HttpError;
import com.example.zpiao1.excited.server.HttpErrorUtils;
import com.example.zpiao1.excited.server.IUserRequest;
import com.example.zpiao1.excited.server.RegisterResponse;
import com.example.zpiao1.excited.server.ServerUtils;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import java.util.HashMap;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

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
        mEmailText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {    // when it loses focus
                    String email = mEmailText.getText().toString();
                    checkEmail(email);
                }
            }
        });

        mEmailText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    String email = mEmailText.getText().toString();
                    checkEmail(email);
                    return mEmailInput.getError() == null;
                }
                return false;
            }
        });
        mPasswordText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    String password = mPasswordText.getText().toString();
                    checkPassword(password);
                }
            }
        });

        mPasswordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    String password = mPasswordText.getText().toString();
                    checkPassword(password);
                    return mPasswordInput.getError() == null;
                }
                return false;
            }
        });
        mConfirmPasswordText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    String password = mPasswordText.getText().toString();
                    String confirmPassword = mConfirmPasswordText.getText().toString();
                    checkConfirmPasswordAgainstPassword(confirmPassword, password);
                }
            }
        });
        mConfirmPasswordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    String password = mPasswordText.getText().toString();
                    String confirmPassword = mConfirmPasswordText.getText().toString();
                    checkConfirmPasswordAgainstPassword(confirmPassword, password);
                    return mConfirmPasswordInput.getError() == null;
                }
                return false;
            }
        });
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check the input fields
                String email = mEmailText.getText().toString();
                String password = mPasswordText.getText().toString();
                String confirmPassword = mConfirmPasswordText.getText().toString();
                checkEmail(email);
                checkPassword(password);
                checkConfirmPasswordAgainstPassword(confirmPassword, password);
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
            }
        });
    }

    private void register(final String email, String password, String name) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("name", name);
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        ServerUtils.addToDisposable(mDisposable,
                request.register(body),
                new Consumer<RegisterResponse>() {
                    @Override
                    public void accept(final RegisterResponse registerResponse) throws Exception {
                        hideRegisteringUi();
                        if (registerResponse.success) {
                            new AlertDialog.Builder(RegisterActivity.this)
                                    .setTitle(R.string.prompt_verify_email)
                                    .setMessage(R.string.verify_email_message)
                                    .setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
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
                                            dialogInterface.dismiss();
                                            finish();
                                        }
                                    })
                                    .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            finish();
                                        }
                                    })
                                    .show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error in registering your account",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
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
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    private void checkEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            mEmailInput.setError("Email is required");
            return;
        }
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        if (at == -1 || dot == -1 || at > dot)
            mEmailInput.setError("Invalid email address");
        else
            mEmailInput.setError(null);
    }

    private void checkPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            mPasswordInput.setError("Password is required");
            return;
        }
        boolean lower = false, upper = false, digit = false, special = false;
        if (password.length() < 8 || password.length() > 16) {
            mPasswordInput.setError("Password must be 8 to 16 characters long");
            return;
        }
        for (int i = 0; i < password.length(); ++i) {
            char ch = password.charAt(i);
            if (Character.isLowerCase(ch))
                lower = true;
            if (Character.isUpperCase(ch))
                upper = true;
            if (Character.isDigit(ch))
                digit = true;
            if (!Character.isLetterOrDigit(ch))
                special = true;
        }
        if (!lower)
            mPasswordInput.setError("Password must contain a lowercase letter");
        else if (!upper)
            mPasswordInput.setError("Password must contain an uppercase letter");
        else if (!digit)
            mPasswordInput.setError("Password must contain a digit");
        else if (!special)
            mPasswordInput.setError("Password must contain a special character");
        else
            mPasswordInput.setError(null);
    }

    private void checkConfirmPasswordAgainstPassword(String confirmPassword, String password) {
        if (TextUtils.isEmpty(confirmPassword)) {
            mConfirmPasswordInput.setError("Confirm password is required");
            return;
        }
        if (!password.equals(confirmPassword))
            mConfirmPasswordInput.setError("Confirm password does not match your password");
        else
            mConfirmPasswordInput.setError(null);
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
