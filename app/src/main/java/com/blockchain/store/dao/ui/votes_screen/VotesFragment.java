package com.blockchain.store.dao.ui.votes_screen;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blockchain.store.dao.adapters.ProposalsAdapter;
import com.blockchain.store.dao.database.model.Proposal;
import com.blockchain.store.dao.interfaces.Callbacks;
import com.blockchain.store.dao.ui.votes_screen.main_votes_screen.MainVotesFragment;
import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.interfaces.NavigationCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VotesFragment extends Fragment implements Callbacks.ProposalCallback {

    private ProposalsAdapter adapter;
    private NavigationCallback navigationCallback;

    @BindView(R.id.proposals_recyclerView)
    RecyclerView proposalsRecyclerView;

    public static VotesFragment newInstance(List<Proposal> proposals) {
        Bundle args = new Bundle();
        args.putParcelableArrayList("Proposals", (ArrayList<? extends Parcelable>) proposals);
        VotesFragment fragment = new VotesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        navigationCallback = (NavigationCallback) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_votes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (getArguments() != null) {
            ArrayList<Proposal> proposals = getArguments().getParcelableArrayList("Proposals");
            getArguments().clear();
            showProposals(proposals);
        }
    }

    public void showProposals(ArrayList<Proposal> proposals) {
        if (adapter == null) {
            proposalsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new ProposalsAdapter(proposals, this);
            proposalsRecyclerView.setAdapter(adapter);
            proposalsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (getParentFragment() != null) ((MainVotesFragment)getParentFragment()).onScroll(dy);
                }
            });
        } else {
            adapter.updateItems(proposals);
        }
    }

    @Override
    public void onItemClicked(Proposal proposal) {
        navigationCallback.onProposalClicked(proposal);
    }
}
