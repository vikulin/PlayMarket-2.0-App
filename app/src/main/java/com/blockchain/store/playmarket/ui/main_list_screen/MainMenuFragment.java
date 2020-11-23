package com.blockchain.store.playmarket.ui.main_list_screen;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.adapters.AppListAdapter;
import com.blockchain.store.playmarket.data.entities.AppDispatcherType;
import com.blockchain.store.playmarket.data.entities.Category;
import com.blockchain.store.playmarket.interfaces.AppListCallbacks;
import com.blockchain.store.playmarket.interfaces.AppListHolderCallback;
import com.blockchain.store.playmarket.utilities.EndlessRecyclerOnScrollListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainMenuFragment extends Fragment implements MainFragmentContract.View, EndlessRecyclerOnScrollListener.EndlessCallback, AppListHolderCallback {
    private static final String TAG = "MainMenuFragment";
    private static final String CATEGORY_EXTRA_ARGS = "category_extra_args";
    private static final String LIST_STATE_KEY = "list_state_key";

    @BindView(R.id.recycler_view_main)
    RecyclerView recyclerView;
    @BindView(R.id.progress_bar_main) ProgressBar progressBar;

    private AppListCallbacks mainCallback;
    private AppListAdapter adapter;
    private Category category;
    private MainFragmentContract.Presenter presenter;
    private boolean isFragmentVisible = false;

    public MainMenuFragment() {
        // Required empty public constructor
    }

    public static MainMenuFragment newInstance(Category category) {
        Bundle args = new Bundle();
        args.putParcelable(CATEGORY_EXTRA_ARGS, category);
        MainMenuFragment fragment = new MainMenuFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.category = getArguments().getParcelable(CATEGORY_EXTRA_ARGS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        ButterKnife.bind(this, view);
        setRetainInstance(true);
        attachPresenter();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void attachPresenter() {
        presenter = new MainFragmentPresenter();
        presenter.init(this);
        presenter.setProvider(category);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AppListCallbacks) {
            mainCallback = (AppListCallbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AppListCallbacks");
        }
    }

    @Override
    public void firstItemsReady(AppDispatcherType appDispatcherType) {
        adapter.addNewItems(appDispatcherType);
    }

    @Override
    public void onDispatchersReady(ArrayList<AppDispatcherType> appDispatcherTypes) {
        setRecyclerView(appDispatcherTypes);
    }

    private void setRecyclerView(ArrayList<AppDispatcherType> appDispatcherTypes) {
        recyclerView.setNestedScrollingEnabled(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new AppListAdapter(category.subCategories, appDispatcherTypes, this, mainCallback, this);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter.onDestroy();
        mainCallback = null;
    }

    @Override
    public void onLoadMore(int currentPage, AppDispatcherType dispatcherType) {
        Log.d(TAG, "onLoadMore() called with: currentPage = [" + currentPage + "], dispatcherType = [" + dispatcherType + "]");
        presenter.loadNewData(dispatcherType);

    }

    /* check for fragment visibility*/
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: ");
        Parcelable parcelable = recyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, parcelable);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, "onViewStateRestored: ");
        if (savedInstanceState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(LIST_STATE_KEY));
        }
    }

    @Override
    public void onViewHolderCreated(AppDispatcherType dispatcherType) {
        presenter.requestNewItems(dispatcherType);
    }

    @Override
    public void onItemRepeatRequestClicked(AppDispatcherType appDispatcherType) {
        presenter.loadNewData(appDispatcherType);
    }
}
