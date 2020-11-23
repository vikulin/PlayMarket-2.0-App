package com.blockchain.store.playmarket.ui.pex_screen;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.dapps.Web3View;
import com.blockchain.store.playmarket.data.entities.DappTransaction;
import com.blockchain.store.playmarket.interfaces.BackPressedCallback;
import com.blockchain.store.playmarket.ui.main_list_screen.MainMenuActivity;
import com.blockchain.store.playmarket.utilities.AccountManager;
import com.blockchain.store.playmarket.utilities.Constants;
import com.blockchain.store.playmarket.utilities.ToastUtil;
import com.blockchain.store.playmarket.utilities.crypto.CryptoUtils;
import com.blockchain.store.playmarket.utilities.dialogs.DappTxDialog;
import com.blockchain.store.playmarket.utilities.dialogs.SignMessageDialog;
import com.blockchain.store.playmarket.utilities.drawable.HamburgerDrawable;
import com.google.gson.Gson;

import org.ethereum.geth.Account;
import org.ethereum.geth.Transaction;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wendu.dsbridge.CompletionHandler;

import static com.blockchain.store.playmarket.api.RestApi.BASE_URL_INFURA;


public class DappsFragment extends Fragment implements BackPressedCallback, DappTxDialog.TxDialogCallback {
    private static final String TAG = "DappsFragment";
    private static final String IS_OPEN_FOR_DEX_KEY = "is_open_for_dex";

    @BindView(R.id.web_view)
    Web3View webView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.browser_top_layout)
    LinearLayout topLayout;
    @BindView(R.id.webview_url_field)
    EditText urlField;
    @BindView(R.id.webview_home_field)
    ImageView homeField;
    @BindView(R.id.hamburger_icon)
    ImageView hamburgerIcon;
    @BindView(R.id.https_indicator)
    ImageView httpsIndicator;
    @BindView(R.id.horizontal_progress_bar)
    ProgressBar horizontalProgressBar;

    private boolean isOpenForDex = false;
    private Web3j web3j = Web3jFactory.build(new HttpService(BASE_URL_INFURA));
    private boolean isUserSawThisPage = false;
    private CompletionHandler lastKnownHandler;

    public static DappsFragment newInstance() {
        Bundle args = new Bundle();
        DappsFragment fragment = new DappsFragment();
        args.putBoolean(IS_OPEN_FOR_DEX_KEY, true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dapps, container, false);
        ButterKnife.bind(this, view);
        if (getArguments() != null) {
            isOpenForDex = getArguments().getBoolean(IS_OPEN_FOR_DEX_KEY, false);
            topLayout.setVisibility(View.GONE);
        }
        initEdittext();
        setWebView();
        return view;
    }

    @OnClick(R.id.hamburger_icon)
    void onHamburgerIcon() {
        ((MainMenuActivity) getActivity()).openDrawer();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !isUserSawThisPage) {
            this.isUserSawThisPage = true;

        }
    }

    private void initEdittext() {

        urlField.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String url = urlField.getText().toString();
                if (!urlField.getText().toString().startsWith("http")) {
                    url = "https://" + url;
                }
                if (!url.trim().isEmpty()) {
                    webView.loadUrl(url);
                }
                hideKeyboardFrom(v);
                return true;
            }
            return false;
        });
    }

    public void hideKeyboardFrom(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void setWebView() {
        hamburgerIcon.setImageDrawable(new HamburgerDrawable(getActivity()));
        webView.addJavascriptObject(new JsApi(getActivity()), "");
        webView.setCallback(new Web3View.Web3ViewCallback() {
            @Override
            public void onPageStarted(String page) {
                horizontalProgressBar.setVisibility(View.VISIBLE);
                httpsIndicator.setVisibility(View.GONE);
                urlField.setText(page);
            }

            @Override
            public void onPageFinished(String page) {
                horizontalProgressBar.setVisibility(View.GONE);
                if (page.startsWith("http://")) {
                    page = page.replaceFirst("http://", "");
                }
                if (page.startsWith("https://")) {
                    page = page.replaceFirst("https://", "");
                    httpsIndicator.setVisibility(View.VISIBLE);
                }
                urlField.setText(page);
            }


        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                horizontalProgressBar.setProgress(newProgress);
            }
        });

        loadDefaultUrl();

    }

    private void loadDefaultUrl() {
        if (isOpenForDex) {
            webView.loadUrl(Constants.PAX_URL);

        } else {
            webView.loadUrl(Constants.DAPPS_URL);
        }
    }

    @OnClick(R.id.webview_home_field)
    void onHomeClicked() {
        webView.clearHistory();
        loadDefaultUrl();
    }

    @Override
    public boolean isUserCanHandleBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        }

        return false;
    }


    @Override
    public void onAccountUnlocked(DappTransaction dappTransaction, boolean isNeedToSendTx) {
        try {
            Transaction dapTx = dappTransaction.createTx();

            if (isNeedToSendTx) {
                if (this.lastKnownHandler != null) {
                    lastKnownHandler.complete(dapTx.getHash().getHex());
                }
                sendRawTransaction(dapTx);
            } else {
                if (this.lastKnownHandler != null) {
                    lastKnownHandler.complete(dapTx.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToast(e.getMessage());
        }
    }

    public class JsApi {
        private Context context;

        public JsApi(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void getAccounts(Object abc, CompletionHandler handler) {
            Log.d(TAG, "getAccounts() called with: handler = [" + AccountManager.getAccount().getAddress().getHex() + "]" + abc);
            handler.complete(AccountManager.getAccount().getAddress().getHex());
        }

        @JavascriptInterface
        public void signMessage(Object serverResult, CompletionHandler handler) {
           sign(serverResult,handler);
        }


        @JavascriptInterface
        public void signTx(Object tx, CompletionHandler handler) {
            Log.d(TAG, "signTx() called with: tx = [" + tx + "], handler = [" + handler + "]");
            createTx(tx, handler);
        }

        @JavascriptInterface
        public void sign(Object tx, CompletionHandler handler) {
            Log.d(TAG, "sign() called with: tx = [" + tx + "], handler = [" + handler + "]");
            try {
                JSONObject jsonObject = new JSONObject(tx.toString());
                String data = jsonObject.getString("data");
                data = new String(Hex.decode(data.replaceFirst("0x", "")));
                String finalData = data;
                new SignMessageDialog(getActivity(), data, () -> {
                    try {
                        Account account = AccountManager.getAccount();
                        String msg = "\u0019Ethereum Signed Message:\n" + finalData.length() + finalData;
                        byte[] secondSignMsgBytes = AccountManager.getKeyManager().signString(account, msg);
                        String secondSignMsg = Hex.toHexString(secondSignMsgBytes);
                        handler.complete("0x" + secondSignMsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void createTx(Object tx, CompletionHandler handler) {
            lastKnownHandler = handler;
            DappTransaction dappTransaction = new Gson().fromJson(tx.toString(), DappTransaction.class);
            showFragmentDialog(dappTransaction, false);
        }

        @JavascriptInterface
        public void sendTransaction(Object tx, CompletionHandler handler) {
            lastKnownHandler = handler;
            DappTransaction dappTransaction = new Gson().fromJson(tx.toString(), DappTransaction.class);
            showFragmentDialog(dappTransaction, true);
        }

        private void showFragmentDialog(DappTransaction dappTransaction, boolean isNeedToSendTx) {
            DappTxDialog.newInstance(dappTransaction, isNeedToSendTx).show(getChildFragmentManager(), "fragment-tag");
            DappTxDialog fragmentDialog = (DappTxDialog) getChildFragmentManager().findFragmentByTag("fragment-tag");
            fragmentDialog.setCallback(DappsFragment.this);
        }
    }

    private void sendRawTransaction(Transaction tx) {
        web3j.ethSendRawTransaction("0x" + CryptoUtils.getRawTransaction(tx)).observable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onTxSend, this::onTxFailed);
    }

    private void onTxSend(EthSendTransaction ethSendTransaction) {
    }

    private void onTxFailed(Throwable throwable) {
    }

}
