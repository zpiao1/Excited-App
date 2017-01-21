package com.example.zpiao1.excited.views;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.adapters.SettingsItemAdapter;
import com.example.zpiao1.excited.data.SettingsItem;
import com.example.zpiao1.excited.data.SingleLineSettingsItem;
import com.example.zpiao1.excited.data.TwoLinesSettingsItem;
import com.example.zpiao1.excited.data.User;
import com.example.zpiao1.excited.logic.AccountUtils;
import com.example.zpiao1.excited.logic.ImageManager;
import com.example.zpiao1.excited.logic.Observer;
import com.example.zpiao1.excited.logic.UserManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends Fragment
        implements Observer<User>,
        UserManager.UserActivityHandler {

    public static final int SUBSCRIBE_ID = 1;
    public static final int REQUEST_READ = 1;
    public static final int REQUEST_TAKE_PHOTO = 2;
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private List<SettingsItem> mItems;
    private User mUser = null;
    private SettingsItemAdapter mAdapter;
    private Uri mPhotoUri;

    private SingleLineSettingsItem mChangePhotoItem,
            mChangePasswordItem,
            mFacebookItem,
            mGoogleItem;

    private TwoLinesSettingsItem mChangeNameItem;

    private View.OnClickListener mLinkFacebookListener, mUnlinkFacebookListener,
            mLinkGoogleListener, mUnlinkGoogleListener;

    private GoogleApiClient mGoogleApiClient;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance(GoogleApiClient client) {
        SettingsFragment fragment = new SettingsFragment();
        fragment.mGoogleApiClient = client;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) mainActivity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                mainActivity,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        buildSettingItems();

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        // Set the adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        mAdapter = new SettingsItemAdapter(mItems, getContext());
        recyclerView.setAdapter(mAdapter);

        UserManager.subscribeForUser(SUBSCRIBE_ID, this);
        return rootView;
    }

    private void showChangeNameDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_change_name, null);
        final TextInputEditText changeNameText = (TextInputEditText)
                contentView.findViewById(R.id.change_name_text);
        changeNameText.setText(mUser != null ? mUser.getDisplayName() : null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.pref_title_change_name))
                .setIcon(R.drawable.ic_pen)
                .setView(contentView)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        String name = changeNameText.getText().toString();
                        if (TextUtils.isEmpty(name) ||
                                name.equals(mUser.getDisplayName())) {
                            Toast.makeText(getContext(),
                                    "Display Name is Not Changed.",
                                    Toast.LENGTH_SHORT)
                                    .show();
                            dialogInterface.dismiss();
                        } else {
                            UserManager.changeUserName(name, getContext());
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private void showChangePasswordDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_change_password, null);
        final TextInputEditText originalPasswordText = (TextInputEditText)
                contentView.findViewById(R.id.original_password_text);
        final TextInputLayout originalPasswordLayout = (TextInputLayout)
                contentView.findViewById(R.id.original_password_layout);
        final TextInputEditText newPasswordText = (TextInputEditText)
                contentView.findViewById(R.id.new_password_text);
        final TextInputLayout newPasswordLayout = (TextInputLayout)
                contentView.findViewById(R.id.new_password_layout);
        final TextInputEditText newConfirmPasswordText = (TextInputEditText)
                contentView.findViewById(R.id.new_confirm_password_text);
        final TextInputLayout newConfirmPasswordLayout = (TextInputLayout)
                contentView.findViewById(R.id.new_confirm_password_layout);
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.pref_title_change_password))
                .setIcon(R.drawable.ic_key)
                .setView(contentView)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AccountUtils.checkOriginalPassword(originalPasswordText,
                                originalPasswordLayout);
                        AccountUtils.checkNewPasswordAgainstOriginalPassword(
                                newPasswordText,
                                newPasswordLayout,
                                originalPasswordText);
                        AccountUtils.checkConfirmPasswordAgainstPassword(
                                newConfirmPasswordText,
                                newConfirmPasswordLayout,
                                newPasswordText);
                        if (originalPasswordLayout.getError() == null &&
                                newPasswordLayout.getError() == null &&
                                newConfirmPasswordLayout.getError() == null) {
                            UserManager.changeUserPassword(originalPasswordText.getText()
                                            .toString(),
                                    newPasswordText.getText().toString(),
                                    getContext());
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showChangePhotoDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.pref_title_change_photo)
                .setIcon(R.drawable.ic_photo_camera)
                .setItems(R.array.photo_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "The " + which + " item is selected.");
                        if (which == 0) {
                            // Take Photo
                            takePictures();
                        } else if (which == 1) {
                            // Your Photos
                            selectPhotos();
                        }
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showUnlinkFacebookDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Disconnect from Facebook?")
                .setIcon(R.drawable.ic_facebook)
                .setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserManager.unlinkFacebookAccount(getContext(),
                                SettingsFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showLinkFacebookDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Connect to Facebook?")
                .setIcon(R.drawable.ic_facebook)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LoginManager manager = LoginManager.getInstance();
                        manager.registerCallback(UserManager.getCallbackManager(),
                                new FacebookCallback<LoginResult>() {
                                    @Override
                                    public void onSuccess(LoginResult result) {
                                        Log.d(TAG, "Facebook Login Success");
                                        UserManager.linkFacebookAccount(result,
                                                getContext(),
                                                SettingsFragment.this);
                                    }

                                    @Override
                                    public void onCancel() {
                                        Log.d(TAG, "Facebook Login Cancel");
                                    }

                                    @Override
                                    public void onError(FacebookException error) {
                                        Log.e(TAG, "Facebook Login Error", error);
                                    }
                                });
                        manager.logInWithReadPermissions(SettingsFragment.this,
                                Arrays.asList("email", "public_profile"));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showLinkGoogleDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Connect to Google?")
                .setIcon(R.drawable.ic_google_plus)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserManager.googleSignIn(mGoogleApiClient, SettingsFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showUnlinkGoogleDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Disconnect from Google?")
                .setIcon(R.drawable.ic_google_plus)
                .setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserManager.unlinkGoogleAccount(getContext(),
                                mGoogleApiClient,
                                SettingsFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public synchronized void onNext(User user) {
        Log.d(TAG, "onNext: user " + (user != null));
        if (user == null) {
            throw new NullPointerException("User is null!");
        }
        mUser = user;
        if (mItems == null) {
            throw new IllegalStateException("Items is null!");
        }
        if (user.status == User.STATUS_INIT ||
                user.status == User.STATUS_PASSWORD_CHANGED) {
            Log.d(TAG, "User status not useful");
        } else if (user.status == User.STATUS_LOGGED_OUT) {
            Log.d(TAG, "User is logged out");
        } else if (user.status == User.STATUS_LOGGED_IN) {
            Log.d(TAG, "mItems empty: " + mItems.isEmpty());

            if (mUser.facebookProfile != null) {
                mFacebookItem.setText(getString(R.string.pref_title_unlink_facebook));
                mFacebookItem.setOnClickListener(mUnlinkFacebookListener);
            } else {
                mFacebookItem.setText(getString(R.string.pref_title_link_facebook));
                mFacebookItem.setOnClickListener(mLinkFacebookListener);
            }

            if (mUser.googleProfile != null) {
                mGoogleItem.setText(getString(R.string.pref_title_unlink_google));
                mGoogleItem.setOnClickListener(mUnlinkGoogleListener);
            } else {
                mGoogleItem.setText(getString(R.string.pref_title_link_google));
                mGoogleItem.setOnClickListener(mLinkGoogleListener);
            }

            if (mItems.isEmpty()) {
                mItems.add(mChangePhotoItem);
                mItems.add(mChangeNameItem);
                mItems.add(mFacebookItem);
                mItems.add(mGoogleItem);
            }

            // If the user has registered using email, he must have a displayName
            if (mItems.contains(mChangePasswordItem)) {
                mItems.remove(mChangePasswordItem);
            }
            if (mUser.hasLocalProfile) {
                if (mChangePasswordItem == null) {
                    mChangePasswordItem = new SingleLineSettingsItem(R.drawable.ic_key,
                            getString(R.string.pref_title_change_password),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showChangePasswordDialog();
                                }
                            });
                }
                mItems.add(2, mChangePasswordItem);
            }

            mChangeNameItem.setText2(user.getDisplayName());
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "getUser", e);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UserManager.unsubscribeForUser(SUBSCRIBE_ID);
    }

    private void buildSettingItems() {
        mItems = new ArrayList<>();
        mChangePhotoItem = new SingleLineSettingsItem(R.drawable.ic_photo_camera,
                getString(R.string.pref_title_change_photo),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showChangePhotoDialog();
                    }
                });
        mChangeNameItem = new TwoLinesSettingsItem(R.drawable.ic_pen,
                getString(R.string.pref_title_change_name),
                mUser != null ? mUser.getDisplayName() : null,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showChangeNameDialog();
                    }
                });

        mFacebookItem = new SingleLineSettingsItem(R.drawable.ic_facebook,
                getString(R.string.pref_title_link_facebook),
                mLinkFacebookListener);

        mGoogleItem = new SingleLineSettingsItem(R.drawable.ic_google_plus,
                getString(R.string.pref_title_link_google),
                mLinkGoogleListener);

        mLinkFacebookListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLinkFacebookDialog();
            }
        };
        mUnlinkFacebookListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUnlinkFacebookDialog();
            }
        };
        mLinkGoogleListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLinkGoogleDialog();
            }
        };

        mUnlinkGoogleListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUnlinkGoogleDialog();
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_READ && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Log.d(TAG, "REQUEST_READ uri: " + data.getData());
                cropImage(data.getData());
            }
            return;
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                Uri resultUri = result.getUri();
                Log.d(TAG, "resultUri: " + resultUri);
                File file = new File(resultUri.getPath());
                UserManager.uploadImage(getContext(), file);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e(TAG, "error", result.getError());
            }
            return;
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (mPhotoUri != null) {
                cropImage(mPhotoUri);
            }
            return;
        }

        UserManager.handleActivityResult(this, requestCode, resultCode, data, this);
    }

    @Override
    public void onSuccess(int code) {
        Log.d(TAG, MESSAGES[code]);
        switch (code) {
            case LINK_FACEBOOK_SUCCESSFUL:
                Toast.makeText(getContext(), "You are connected to Facebook!",
                        Toast.LENGTH_SHORT)
                        .show();
                break;
            case LINK_GOOGLE_SUCCESSFUL:
                Toast.makeText(getContext(), "You are connected to Google!",
                        Toast.LENGTH_SHORT)
                        .show();
                break;
            case UNLINK_FACEBOOK_SUCCESSFUL:
                Toast.makeText(getContext(), "You are disconnected from Facebook",
                        Toast.LENGTH_SHORT)
                        .show();
                break;
            case UNLINK_GOOGLE_SUCCESSFUL:
                Toast.makeText(getContext(), "You are disconnected from Google",
                        Toast.LENGTH_SHORT)
                        .show();
                break;
        }
    }

    @Override
    public void onFail(Throwable throwable, int code) {
        Log.e(TAG, "handleUserActivity: " + MESSAGES[code], throwable);
    }

    private void selectPhotos() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_READ);
    }

    private void takePictures() {
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicIntent.resolveActivity(getContext().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = ImageManager.createImageFile(getContext());
            } catch (IOException e) {
                Log.e(TAG, "takePictures", e);
            }
            if (photoFile != null) {
                mPhotoUri = FileProvider.getUriForFile(getContext(),
                        "com.example.zpiao1.excited.fileprovider",
                        photoFile);
                Log.d(TAG, "mPhotoUri: " + mPhotoUri);
                takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                startActivityForResult(takePicIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void cropImage(Uri uri) {
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setRequestedSize(250, 250)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(getContext(), this);
    }
}