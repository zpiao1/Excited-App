package com.example.zpiao1.excited.logic;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;

public class AccountUtils {

  public static void checkEmail(TextInputEditText emailText, TextInputLayout emailLayout) {
    String email = emailText.getText().toString();
    if (TextUtils.isEmpty(email)) {
      emailLayout.setError("Email is required");
      return;
    }
    int at = email.indexOf('@');
    int dot = email.lastIndexOf('.');
    if (at == -1 || dot == -1 || at > dot) {
      emailLayout.setError("Invalid email address");
    } else {
      emailLayout.setError(null);
    }
  }

  public static void checkPassword(TextInputEditText passwordText, TextInputLayout passwordLayout) {
    String password = passwordText.getText().toString();
    if (TextUtils.isEmpty(password)) {
      passwordLayout.setError("Password is required");
      return;
    }
    boolean lower = false, upper = false, digit = false, special = false;
    if (password.length() < 8 || password.length() > 16) {
      passwordLayout.setError("Password must be 8 to 16 characters long");
      return;
    }
    for (int i = 0; i < password.length(); ++i) {
      char ch = password.charAt(i);
      if (Character.isLowerCase(ch)) {
        lower = true;
      }
      if (Character.isUpperCase(ch)) {
        upper = true;
      }
      if (Character.isDigit(ch)) {
        digit = true;
      }
      if (!Character.isLetterOrDigit(ch)) {
        special = true;
      }
    }
    if (!lower) {
      passwordLayout.setError("Password must contain a lowercase letter");
    } else if (!upper) {
      passwordLayout.setError("Password must contain an uppercase letter");
    } else if (!digit) {
      passwordLayout.setError("Password must contain a digit");
    } else if (!special) {
      passwordLayout.setError("Password must contain a special character");
    } else {
      passwordLayout.setError(null);
    }
  }

  public static void checkConfirmPasswordAgainstPassword(TextInputEditText confirmPasswordText,
      TextInputLayout confirmPasswordLayout,
      TextInputEditText passwordText) {
    String confirmPassword = confirmPasswordText.getText().toString();
    String password = passwordText.getText().toString();

    if (TextUtils.isEmpty(confirmPassword)) {
      confirmPasswordLayout.setError("Confirm password is required");
      return;
    }
    if (!password.equals(confirmPassword)) {
      confirmPasswordLayout.setError("Confirm password does not match your password");
    } else {
      confirmPasswordLayout.setError(null);
    }
  }

  public static void checkOriginalPassword(TextInputEditText originalPasswordText,
      TextInputLayout originalPasswordLayout) {
    String originalPassword = originalPasswordText.getText().toString();
    if (TextUtils.isEmpty(originalPassword)) {
      originalPasswordLayout.setError("Original password is required");
    } else {
      originalPasswordLayout.setError(null);
    }
  }

  public static void checkNewPasswordAgainstOriginalPassword(TextInputEditText newPasswordText,
      TextInputLayout newPasswordLayout,
      TextInputEditText originalPasswordText) {
    String originalPassword = originalPasswordText.getText().toString();
    String newPassword = newPasswordText.getText().toString();
    if (TextUtils.isEmpty(originalPassword)) {
      newPasswordLayout.setError("New password is required");
      return;
    }
    if (newPassword.equals(originalPassword)) {
      newPasswordLayout.setError("New Password must be different from original");
    } else {
      checkPassword(newPasswordText, newPasswordLayout);
    }
  }
}
