package com.blockchain.store.playmarket.ui.login_screen.password_prompt_screen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.interfaces.LoginPromptCallback;
import com.blockchain.store.playmarket.ui.fingerprint_screen.FingerprintConfiguringActivity;
import com.blockchain.store.playmarket.ui.login_screen.LoginPromptActivity;
import com.blockchain.store.playmarket.ui.login_screen.LoginViewModel;
import com.blockchain.store.playmarket.ui.main_list_screen.MainMenuActivity;
import com.blockchain.store.playmarket.utilities.ToastUtil;
import com.google.android.material.textfield.TextInputLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PasswordPromptFragment extends Fragment implements PasswordPromptContract.View {

    private LoginPromptCallback loginPromptCallback;
    private PasswordPromptPresenter presenter;
    private LoginViewModel loginViewModel;
    private String jsonData;
    private String accountAddress;

    @BindView(R.id.password_editText) EditText passwordEditText;
    @BindView(R.id.available_fingerprint_imageView) ImageView availableFingerImageView;
    @BindView(R.id.not_available_fingerprint_imageView) ImageView notAvailableFingerImageView;
    @BindView(R.id.info_textView) TextView infoTextView;
    @BindView(R.id.fingerprint_layout) LinearLayout fingerPrintLayout;
    @BindView(R.id.configure_fingerprint_button) Button configureFingerprintButton;
    @BindView(R.id.password_textInputLayout)
    TextInputLayout passwordTextInputLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password_prompt, container, false);
        ButterKnife.bind(this, view);

        presenter = new PasswordPromptPresenter();
        presenter.init(this, getContext());

        fingerprintAvailable();

        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        loginViewModel.jsonData.observe(getActivity(), s -> jsonData = s);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fingerprintAvailable();
    }

    @OnClick(R.id.cancel_login_button)
    void cancelLoginButtonClicked() {
        clearScreenData();
    }

    @OnClick(R.id.continue_login_button)
    void continueLoginButtonClicked() {
        String accountPassword = passwordEditText.getText().toString();

        if (jsonData == null) createNewAccount(accountPassword);
        else importExistingAccount(jsonData, accountPassword);
    }

    @OnClick(R.id.configure_fingerprint_button)
    void configureButtonClicked() {
        startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LoginPromptCallback) {
            loginPromptCallback = (LoginPromptCallback) context;
        }
    }

    private void openMainActivity() {
        startActivity(new Intent(getContext(), MainMenuActivity.class));
        getActivity().finish();
    }

    private void openWelcomeActivity(String address) {
        ((LoginPromptActivity) getActivity()).openNextActivity(address);
    }

    private void openFingerprintActivity(String accountPassword) {
        Intent intent = new Intent(getContext(), FingerprintConfiguringActivity.class);
        intent.putExtra(FingerprintConfiguringActivity.PASSWORD, accountPassword);
        startActivityForResult(intent, 1);
    }

    private void createNewAccount(String accountPassword) {
        if (presenter.checkPasswordForNewAccount(accountPassword)) {
            accountAddress = presenter.createNewAccount(accountPassword);
            if (presenter.checkSensorState(getContext()).equals(PasswordPromptContract.sensorState.READY))
                openFingerprintActivity(accountPassword);
            else openWelcomeActivity(accountAddress);
        }
    }

    private void importExistingAccount(String accountData, String accountPassword) {
        if (presenter.importAccount(accountData, accountPassword)) {
            if (presenter.checkSensorState(getContext()).equals(PasswordPromptContract.sensorState.READY))
                openFingerprintActivity(accountPassword);
            else {
                openMainActivity();
                ToastUtil.showToast(R.string.import_successful);
            }
        } else showPasswordError(getResources().getString(R.string.wrong_password));
    }


    @Override
    public void showPasswordError(String errorText) {
        passwordTextInputLayout.setPasswordVisibilityToggleTintList(AppCompatResources.getColorStateList(getContext(), R.color.red_error_color));
        passwordTextInputLayout.setError(errorText);
    }

    private void clearScreenData() {
        passwordTextInputLayout.setPasswordVisibilityToggleTintList(AppCompatResources.getColorStateList(getContext(), R.color.colorAccent));
        passwordEditText.setText("");
        passwordTextInputLayout.setError("");
        loginViewModel.jsonData.setValue(null);
        loginPromptCallback.openWelcomeFragment();
    }

    private void fingerprintAvailable() {

        if (presenter.checkSensorState(getContext()).equals(PasswordPromptContract.sensorState.NOT_SUPPORTED)) {
            configureFingerprintButton.setVisibility(View.GONE);
            fingerPrintLayout.setVisibility(View.GONE);
        } else if (presenter.checkSensorState(getContext()).equals(PasswordPromptContract.sensorState.NO_FINGERPRINTS)) {
            fingerPrintLayout.setBackgroundColor(getResources().getColor(R.color.light_gray_background_color));
            infoTextView.setText(getResources().getString(R.string.is_not_configured));
            infoTextView.setTextColor(getResources().getColor(R.color.exchange_gray_text_color));
            configureFingerprintButton.setVisibility(View.VISIBLE);
            availableFingerImageView.setVisibility(View.GONE);
            notAvailableFingerImageView.setVisibility(View.VISIBLE);

        } else if (presenter.checkSensorState(getContext()).equals(PasswordPromptContract.sensorState.READY)) {
            fingerPrintLayout.setBackgroundColor(getResources().getColor(R.color.light_green_background_color));
            infoTextView.setTextColor(getResources().getColor(R.color.colorAccent));
            infoTextView.setText(getResources().getString(R.string.is_available));
            configureFingerprintButton.setVisibility(View.GONE);
            availableFingerImageView.setVisibility(View.VISIBLE);
            notAvailableFingerImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        String resultMessage = data.getStringExtra(FingerprintConfiguringActivity.RESULT);
        ToastUtil.showToast(resultMessage);
        if (jsonData == null)
            ((LoginPromptActivity) getActivity()).openNextActivity(accountAddress);
        else ((LoginPromptActivity) getActivity()).openMainActivity();
    }
}
