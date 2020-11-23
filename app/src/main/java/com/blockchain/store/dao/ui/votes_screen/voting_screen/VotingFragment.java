package com.blockchain.store.dao.ui.votes_screen.voting_screen;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;

import com.blockchain.store.dao.data.TokenBalance;
import com.blockchain.store.dao.database.model.Proposal;
import com.blockchain.store.dao.database.model.Rules;
import com.blockchain.store.dao.ui.DaoConstants;
import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.interfaces.NavigationCallback;
import com.blockchain.store.playmarket.utilities.dialogs.DialogManager;
import com.blockchain.store.playmarket.utilities.ToastUtil;
import com.google.android.gms.common.util.ArrayUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VotingFragment extends Fragment implements VotingContract.View {

    private VotingPresenter presenter = new VotingPresenter();
    private NavigationCallback callback;

    private static String PROPOSAL_TAG = "Proposal";
    private TokenBalance tokenBalance;
    private Proposal proposal;

    @BindView(R.id.description_textView) TextView descriptionTextView;
    @BindView(R.id.totalVoted_progressBar) ProgressBar totalVotedProgressBar;
    @BindView(R.id.totalVoted_textView) TextView totalVotedTextView;
    @BindView(R.id.totalVotes_textView) TextView totalVotesTextView;
    @BindView(R.id.votes_textView) TextView votesTextView;
    @BindView(R.id.quorum_progressBar) ProgressBar quorumProgressBar;
    @BindView(R.id.quorumPercent_textView) TextView quorumPercentTextView;
    @BindView(R.id.minQuorum_textView) TextView minQuorumTextView;
    @BindView(R.id.quorum_textView) TextView quorumTextView;
    @BindView(R.id.majority_progressBar) ProgressBar majorityProgressBar;
    @BindView(R.id.majorityPercent_textView) TextView majorityPercentTextView;
    @BindView(R.id.minMajority_textView) TextView minMajorityTextView;
    @BindView(R.id.majority_textView) TextView majorityTextView;
    @BindView(R.id.ongoingGroup)
    Group ongoingGroup;
    @BindView(R.id.finishGroup) Group finishGroup;
    @BindView(R.id.voteForSupport_button) Button voteForSupportButton;
    @BindView(R.id.voteAgainst_button) Button voteAgainstButton;
    @BindView(R.id.execute_button) Button executeButton;
    @BindView(R.id.isAccepted_textView) TextView isAcceptedTextView;
    @BindView(R.id.isExecuted_textView) TextView isExecutedTextView;
    @BindView(R.id.localBalance_textView) TextView localBalanceTextView;
    @BindView(R.id.repositoryBalance_textView) TextView repositoryBalanceTextView;
    @BindView(R.id.progressBar) ProgressBar progressbar;
    @BindView(R.id.notLockedBalance_textView) TextView notLockedBalanceTextView;

    public static Fragment newInstance(Proposal proposal) {
        Fragment fragment = new VotingFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(PROPOSAL_TAG, proposal);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (NavigationCallback) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        presenter.init(this);
        if (getArguments() != null) {
            proposal = getArguments().getParcelable(PROPOSAL_TAG);
            if (proposal != null) {
                bindData(proposal);
                Proposal.ProposalType proposalType = proposal.getProposalType();
                switch (proposalType) {
                    case Ongoing:
                        showOngoingProposal();
                        break;
                    case NotExecutedAccepted:
                        showNotExecutedAcceptedProposal();
                        break;
                    case NotExecutedNotAccepted:
                        showNotExecutedNotAcceptedProposal();
                        break;
                    case Executed:
                        showExecutedProposal(proposal);
                        break;
                    case Unexecutable:
                        showFailedProposal();
                        break;
                }
            }
        }
    }

    @OnClick(R.id.close_image_button)
    void onCloseClicked() {
        if (getActivity() != null) getActivity().onBackPressed();
    }

    @OnClick(R.id.voteForSupport_button)
    void onSupportClicked() {
        if (tokenBalance.getRepositoryBalance().equals("0.0")){
            ToastUtil.showToast("You have no tokens in repository");
        } else if (tokenBalance.getNotLockedBalance().equals("0.0")) {
            new DialogManager().showVotingDialog(true, tokenBalance.getRepositoryBalance(), getContext(), (isUnlock) -> {
                if (isUnlock) {
                    presenter.votingForProposal(proposal.proposalID, true, "");
                    if (getActivity() != null) getActivity().onBackPressed();
                } else Toast.makeText(getContext(), "Wrong password", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @OnClick(R.id.voteAgainst_button)
    void onAgainstClicked() {
        new DialogManager().showVotingDialog(false, tokenBalance.getRepositoryBalance(), getContext(), (isUnlock) -> {
            if (isUnlock) {
                presenter.votingForProposal(proposal.proposalID, false, "");
                if (getActivity() != null) getActivity().onBackPressed();
            } else Toast.makeText(getContext(), "Wrong password", Toast.LENGTH_SHORT).show();

        });
    }

    @OnClick(R.id.execute_button)
    void onExecuteClicked() {
        new DialogManager().showConfirmDialog(getContext(), (isUnlock) -> {
            if (isUnlock) {
                presenter.executeProposal(proposal);
                if (getActivity() != null) getActivity().onBackPressed();
            } else Toast.makeText(getContext(), "Wrong password", Toast.LENGTH_SHORT).show();
        });
    }

    @OnClick(R.id.details_button)
    void onDetailsClicked() {
        callback.onProposalDetailsClicked(proposal);
    }


    private void bindData(Proposal proposal) {

        Rules rules = presenter.getRules();

        descriptionTextView.setText(proposal.description);

        String totalPercentage = presenter.obtainPercentage(String.valueOf(proposal.numberOfVotes), DaoConstants.TOTAL_DAO_TOKEN);
        totalVotesTextView.setText(String.valueOf(getTokenWithDecimals(DaoConstants.TOTAL_DAO_TOKEN)));
        totalVotedProgressBar.setProgress(Integer.valueOf(totalPercentage));
        totalVotedTextView.setText(totalPercentage + " %");
        votesTextView.setText(String.valueOf(presenter.getTokenDecimals(proposal.numberOfVotes)));

        String quorumPercentage = presenter.obtainPercentage(String.valueOf(proposal.numberOfVotes), String.valueOf(rules.minimumQuorum));
        quorumProgressBar.setProgress(Integer.valueOf(quorumPercentage));
        quorumPercentTextView.setText(quorumPercentage + " %");
        minQuorumTextView.setText(String.valueOf(presenter.getTokenDecimals(rules.minimumQuorum)));
        quorumTextView.setText(String.valueOf(presenter.getTokenDecimals(proposal.numberOfVotes)));

        String majorityPercentage = presenter.obtainPercentage(String.valueOf(proposal.votesSupport), String.valueOf(rules.requisiteMajority));
        majorityProgressBar.setProgress(Integer.valueOf(majorityPercentage));
        majorityPercentTextView.setText(majorityPercentage + " %");
        minMajorityTextView.setText(String.valueOf(presenter.getTokenDecimals(rules.requisiteMajority)));
        majorityTextView.setText(String.valueOf(presenter.getTokenDecimals(proposal.votesSupport)));
    }

    private void showOngoingProposal() {
        showComponents(progressbar);
        presenter.getDaoTokenBalance();
    }

    private void showFailedProposal() {
        isAcceptedTextView.setText(getResources().getString(R.string.not_accepted));
        isExecutedTextView.setText(getResources().getString(R.string.not_executed));
        isAcceptedTextView.setTextColor(getResources().getColor(R.color.colorRed));
        isExecutedTextView.setTextColor(getResources().getColor(R.color.colorRed));
        showComponents(finishGroup);
    }

    private void showNotExecutedNotAcceptedProposal() {
        isAcceptedTextView.setText(getResources().getString(R.string.not_accepted));
        isExecutedTextView.setText(getResources().getString(R.string.not_executed));
        isAcceptedTextView.setTextColor(getResources().getColor(R.color.colorRed));
        isExecutedTextView.setTextColor(getResources().getColor(R.color.colorRed));
        showComponents(finishGroup, executeButton);
    }

    private void showNotExecutedAcceptedProposal() {
        isAcceptedTextView.setText(getResources().getString(R.string.accepted));
        isExecutedTextView.setText(getResources().getString(R.string.not_executed));
        isAcceptedTextView.setTextColor(getResources().getColor(R.color.colorGreen));
        isExecutedTextView.setTextColor(getResources().getColor(R.color.colorRed));
        showComponents(finishGroup, executeButton);
    }

    private void showExecutedProposal(Proposal proposal) {
        isExecutedTextView.setText(getResources().getString(R.string.executed));
        isExecutedTextView.setTextColor(getResources().getColor(R.color.colorGreen));
        if (proposal.proposalPassed) {
            isAcceptedTextView.setText(getResources().getString(R.string.accepted));
            isAcceptedTextView.setTextColor(getResources().getColor(R.color.colorGreen));
        } else {
            isAcceptedTextView.setText(getResources().getString(R.string.not_accepted));
            isAcceptedTextView.setTextColor(getResources().getColor(R.color.colorRed));
        }
        showComponents(finishGroup);
    }

    @Override
    public void setDaoTokenBalance(TokenBalance tokenBalance) {
        this.tokenBalance = tokenBalance;
        localBalanceTextView.setText(this.tokenBalance.getLocalBalance());
        repositoryBalanceTextView.setText(this.tokenBalance.getRepositoryBalance());
        notLockedBalanceTextView.setText(this.tokenBalance.getNotLockedBalance());
        showComponents(ongoingGroup, voteForSupportButton, voteAgainstButton);
    }

    private void showComponents(View... views) {
        if (ArrayUtils.contains(views, ongoingGroup)) ongoingGroup.setVisibility(View.VISIBLE);
        else ongoingGroup.setVisibility(View.GONE);

        if (ArrayUtils.contains(views, finishGroup)) finishGroup.setVisibility(View.VISIBLE);
        else finishGroup.setVisibility(View.GONE);

        if (ArrayUtils.contains(views, voteForSupportButton)) voteForSupportButton.setVisibility(View.VISIBLE);
        else voteForSupportButton.setVisibility(View.GONE);

        if (ArrayUtils.contains(views, voteAgainstButton)) voteAgainstButton.setVisibility(View.VISIBLE);
        else voteAgainstButton.setVisibility(View.GONE);

        if (ArrayUtils.contains(views, executeButton)) executeButton.setVisibility(View.VISIBLE);
        else executeButton.setVisibility(View.GONE);

        if (ArrayUtils.contains(views, progressbar)) progressbar.setVisibility(View.VISIBLE);
        else progressbar.setVisibility(View.GONE);
    }

    private double getTokenWithDecimals(String balance) {
        if (Long.valueOf(balance) == 0) {
            return 0;
        } else {
            return (Double.valueOf(balance) / Math.pow(10, 4));
        }
    }

}
