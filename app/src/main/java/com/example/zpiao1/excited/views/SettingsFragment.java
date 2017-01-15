package com.example.zpiao1.excited.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.adapters.SettingsItemAdapter;
import com.example.zpiao1.excited.data.SettingsItem;
import com.example.zpiao1.excited.data.SingleLineSettingsItem;
import com.example.zpiao1.excited.data.TwoLinesSettingsItem;
import com.example.zpiao1.excited.data.User;
import com.example.zpiao1.excited.user.UserManager;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment implements SettingsItemAdapter.OnSettingsItemClickListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private List<SettingsItem> mItems;
    private User mUser;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserManager.getUser(getContext(), new UserManager.UserHandler() {
            @Override
            public synchronized void handleUser(User user) {
                mUser = user;
            }

            @Override
            public void handleError(Throwable throwable) {
                Log.e(TAG, "getUser", throwable);
            }
        });
        mItems = new ArrayList<>();
        mItems.add(new SingleLineSettingsItem(R.drawable.ic_photo_camera,
                getString(R.string.pref_title_change_photo)));
        mItems.add(new TwoLinesSettingsItem(R.drawable.ic_pen,
                getString(R.string.pref_title_change_name),
                mUser != null ? mUser.getDisplayName() : null));
        mItems.add(new SingleLineSettingsItem(R.drawable.ic_key,
                getString(R.string.pref_title_change_password)));
        mItems.add(new SingleLineSettingsItem(R.drawable.ic_facebook,
                getString(R.string.pref_title_link_facebook)));
        mItems.add(new SingleLineSettingsItem(R.drawable.ic_google_plus,
                getString(R.string.pref_title_link_google)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        // Set the adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        SettingsItemAdapter adapter = new SettingsItemAdapter(mItems, this);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onSettingsItemClicked(int position) {
        Log.d(TAG, "onSettingsItemClicked");
    }
}
