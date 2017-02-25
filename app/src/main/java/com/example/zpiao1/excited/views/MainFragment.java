package com.example.zpiao1.excited.views;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.adapters.EventImagePagerAdapter;
import com.example.zpiao1.excited.adapters.IconAdapter;
import com.example.zpiao1.excited.data.CategoryIcon;
import com.example.zpiao1.excited.logic.OnTouchActionListener;
import com.example.zpiao1.excited.server.IEventRequest;
import com.example.zpiao1.excited.server.ServerUtils;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface to handle interaction events. Use
 * the {@link MainFragment#newInstance} factory method to create an instance of this fragment.
 */
public class MainFragment extends Fragment implements
    OnTouchActionListener,
    IconAdapter.OnCategoryCheckedChangeListener {

  private static final String TAG = MainFragment.class.getSimpleName();
  private static final float Y_CHANGE_THRESHOLD = 300f;

  private static final String[] CATEGORIES =
      {"movie", "art", "sports", "nightlife", "kids", "expo"};

  private View mRootView;
  private IconAdapter mIconAdapter;
  private EventImagePagerAdapter mPagerAdapter;
  private ViewPager mViewPager;

  private int mGarbageIconDefaultHeight;
  private int mStarIconDefaultHeight;

  private boolean[] mCategoryCheckedStates;

  private CompositeDisposable mDisposable;

  private OnFragmentInteractionListener mListener;

  public MainFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of this fragment using the provided
   * parameters.
   *
   * @return A new instance of fragment MainFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static MainFragment newInstance() {
    MainFragment fragment = new MainFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Logging App Activations
    mDisposable = new CompositeDisposable();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    mRootView = inflater.inflate(R.layout.fragment_main, container, false);

    // set up the toolbar
    Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);

    MainActivity mainActivity = (MainActivity) getActivity();
    mainActivity.setSupportActionBar(toolbar);

    DrawerLayout drawer = (DrawerLayout) mainActivity.findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        mainActivity, drawer, toolbar, R.string.navigation_drawer_open,
        R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    // Add the events to the view pager
    mViewPager = (ViewPager) mRootView.findViewById(R.id.event_view_pager);
    loadGridView();

    fetchEvents();

    mGarbageIconDefaultHeight = mRootView.findViewById(R.id.garbage_image).getLayoutParams().height;
    mStarIconDefaultHeight = mRootView.findViewById(R.id.star_image).getLayoutParams().height;

    return mRootView;
  }

  // TODO: Rename method, update argument and hook method into UI event
  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  private void loadGridView() {
    GridView gridView = (GridView) mRootView.findViewById(R.id.icon_container);
    ArrayList<CategoryIcon> categoryIcons = new ArrayList<>();
    categoryIcons.add(new CategoryIcon("Movie", R.drawable.ic_film_reel,
        R.drawable.ic_film_reel_grey));
    categoryIcons.add(new CategoryIcon("Art", R.drawable.ic_masks,
        R.drawable.ic_masks_grey));
    categoryIcons.add(new CategoryIcon("Sports", R.drawable.ic_archery,
        R.drawable.ic_archery_grey));
    categoryIcons.add(new CategoryIcon("Nightlife", R.drawable.ic_wine_glass,
        R.drawable.ic_wine_glass_grey));
    categoryIcons.add(new CategoryIcon("Kids", R.drawable.ic_boy,
        R.drawable.ic_boy_grey));
    categoryIcons.add(new CategoryIcon("Expo", R.drawable.ic_exhibition,
        R.drawable.ic_exhibition_grey));
    mIconAdapter = new IconAdapter(getContext(), categoryIcons, this);

    gridView.setAdapter(mIconAdapter);

    // Initialize all the categories to be checked
    mCategoryCheckedStates = new boolean[categoryIcons.size()];
    for (int i = 0; i < mCategoryCheckedStates.length; ++i) {
      mCategoryCheckedStates[i] = true;
    }
  }

  public void changeIconGradually(float startTouchY, float currentTouchY) {
    float dy = currentTouchY - startTouchY;
    // Swiping down, should change the garbage icon
    ImageView iconImage;
    int color;
    int defaultHeight;
    float scale = Math.abs(dy) / Y_CHANGE_THRESHOLD;
    if (scale > 1) {
      scale = 1;
    }
    if (dy > 10) {
      iconImage = (ImageView) mRootView.findViewById(R.id.garbage_image);
      color = ContextCompat.getColor(getContext(), R.color.lightRed);
      defaultHeight = mGarbageIconDefaultHeight;
      changeIconGraduallyHelper(iconImage, color, defaultHeight, scale);
    } else if (dy < -10) {
      iconImage = (ImageView) mRootView.findViewById(R.id.star_image);
      color = ContextCompat.getColor(getContext(), R.color.lightGreen);
      defaultHeight = mStarIconDefaultHeight;
      changeIconGraduallyHelper(iconImage, color, defaultHeight, scale);
    }
  }

  private void changeIconGraduallyHelper(ImageView iconImage, int color, int defaultHeight, float
      scale) {
    // Change the size of the ImageView
    ViewGroup.LayoutParams params = iconImage.getLayoutParams();
    params.height = (int) (defaultHeight * (1 + scale));
    iconImage.setLayoutParams(params);

    // Change the color of the ImageView
    int alpha = (int) (0xFF * 0.5f * scale);
    int red = Color.red(color);
    int green = Color.green(color);
    int blue = Color.blue(color);
    iconImage.setBackgroundColor(Color.argb(alpha, red, green, blue));
  }

  public void resetIcons() {
    ImageView garbageImage = (ImageView) mRootView.findViewById(R.id.garbage_image);
    ImageView startImage = (ImageView) mRootView.findViewById(R.id.star_image);
    resetIconsHelper(garbageImage, mGarbageIconDefaultHeight);
    resetIconsHelper(startImage, mStarIconDefaultHeight);
  }

  private void resetIconsHelper(ImageView iconImage, int defaultHeight) {
    ViewGroup.LayoutParams params = iconImage.getLayoutParams();
    params.height = defaultHeight;
    iconImage.setLayoutParams(params);

    iconImage.setBackgroundColor(
        ContextCompat.getColor(
            getContext(), android.R.color.transparent));
  }

  public void onImageRemoved(Uri uri) {
  }

  public void onImageStarred(Uri uri) {
  }

  public void onCategoryCheckedChanged(int position, boolean isChecked) {
    if (mCategoryCheckedStates == null) {
      throw new RuntimeException("mCategoryCheckedStates is null");
    }
    // If there is change in selection of categories
    if (mCategoryCheckedStates[position] != isChecked) {
      mCategoryCheckedStates[position] = isChecked;
      fetchEvents();
    }
  }

  private String buildEventSelection() {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < mCategoryCheckedStates.length; ++i) {
      if (mCategoryCheckedStates[i]) {
        selected.add(CATEGORIES[i]);
      }
    }
    return selected.isEmpty() ? "none" : TextUtils.join("|", selected);
  }

  private void fetchEvents() {
    IEventRequest request = ServerUtils.getRetrofit()
        .create(IEventRequest.class);
    ServerUtils.addToDisposable(mDisposable,
        request.getEvents(buildEventSelection()),
        events -> {
          mPagerAdapter = new EventImagePagerAdapter(getChildFragmentManager(),
              events, MainFragment.this);
          mViewPager.setAdapter(mPagerAdapter);
        },
        throwable -> Log.e(TAG, "fetchEvents", throwable));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mDisposable.clear();
  }

  @Override
  public void onActionUp() {
    resetIcons();
  }

  @Override
  public void onActionDown() {
    // Do nothing
  }

  @Override
  public void onActionMove(float startX, float currentX, float startY, float currentY) {
    changeIconGradually(startY, currentY);
  }

  /**
   * This interface must be implemented by activities that contain this fragment to allow an
   * interaction in this fragment to be communicated to the activity and potentially other fragments
   * contained in that activity. <p> See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html" >Communicating with
   * Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {

    // TODO: Update argument type and name
    void onFragmentInteraction(Uri uri);
  }
}
