package com.blockchain.store.playmarket.ui.library_screen.my_apps_screen;


import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.adapters.SearchListAdapter;
import com.blockchain.store.playmarket.data.entities.App;
import com.blockchain.store.playmarket.interfaces.AppListCallbacks;
import com.blockchain.store.playmarket.ui.app_detail_screen.AppDetailActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyAppsFragment extends Fragment implements AppListCallbacks {
    private static final String TAG = "MyAppsFragment";
    private static final String NEW_INSTANCE_ARGS = "new_instance_args";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view) TextView emptyView;

    private SearchListAdapter adapter;
    private ArrayList<App> appsList = new ArrayList<>();
    private boolean isShowOnlyUpdatedApps = false;

    public MyAppsFragment() {
        // Required empty public constructor
    }


    public static MyAppsFragment newInstance(boolean isShowOnlyUpdatedApps) {
        Bundle args = new Bundle();
        args.putBoolean(NEW_INSTANCE_ARGS, isShowOnlyUpdatedApps);
        MyAppsFragment fragment = new MyAppsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isShowOnlyUpdatedApps = getArguments().getBoolean(NEW_INSTANCE_ARGS, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_apps, container, false);
        ButterKnife.bind(this, view);
        loadSavedApps();
        return view;
    }

    private void loadSavedApps() {
        if (isShowOnlyUpdatedApps) {
            populateRecyclerView(new ArrayList<>());
        } else {
            populateRecyclerView(appsList);
        }

    }

    private void populateRecyclerView(ArrayList<App> apps) {
        if (apps.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            adapter = new SearchListAdapter(apps, this);
            recyclerView.setAdapter(adapter);
        }

    }

    @Override
    public void onAppClicked(App app) {
        AppDetailActivity.start(getActivity(), app);
    }
}
