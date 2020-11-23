package com.blockchain.store.playmarket.ui.transaction_history_screen;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.adapters.TransactionHistoryAdapter;
import com.blockchain.store.playmarket.data.entities.TransactionModel;
import com.blockchain.store.playmarket.utilities.Constants;
import com.blockchain.store.playmarket.utilities.TransactionPrefsUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionHistoryFragment extends Fragment {

    private static final String STATUS_KEY = "status_key";
    private static final String SHOW_ALL_KEY = "show_all_transcation";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view) TextView emptyView;

    private Constants.TransactionStatus showTransactionType;
    private TransactionHistoryAdapter adapter;
    private TransactionHistoryViewModel viewModel;

    public static TransactionHistoryFragment instance(Constants.TransactionStatus statusToShow) {
        Bundle args = new Bundle();
        args.putSerializable(STATUS_KEY, statusToShow);
        TransactionHistoryFragment fragment = new TransactionHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public TransactionHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_history, container, false);
        ButterKnife.bind(this, view);
        showTransactionType = (Constants.TransactionStatus) getArguments().getSerializable(STATUS_KEY);
        viewModel = ViewModelProviders.of(getActivity()).get(TransactionHistoryViewModel.class);
        viewModel.transactionModels.observe(getActivity(), this::populateRecyclerView);
        return view;
    }

    private void populateRecyclerView(ArrayList<TransactionModel> transactionModels) {
        transactionModels = TransactionPrefsUtil.getTransactionByStatus(transactionModels, showTransactionType);
        if (!transactionModels.isEmpty()) {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            if (adapter != null) {
                adapter.reloadItems(transactionModels);
            } else {
                adapter = new TransactionHistoryAdapter(transactionModels);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setAdapter(adapter);
            }


        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }


    }

}
