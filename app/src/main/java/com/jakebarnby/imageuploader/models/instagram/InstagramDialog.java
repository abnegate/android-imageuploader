package com.jakebarnby.imageuploader.models.instagram;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.jakebarnby.imageuploader.R;
import com.jakebarnby.imageuploader.activities.MainActivity;

/**
 *
 */
@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
public class InstagramDialog extends Dialog {
    static final String TAG = "Instagram-Android";

    private ProgressDialog mProgressDialog;
    private WebView mWebView;

    private String mAuthUrl;
    private String mRedirectUri;

    private InstagramDialogListener mListener;
    private TextView mTitle;

    public InstagramDialog(Context context, String authUrl, String redirectUri, InstagramDialogListener listener) {
        super(context);

        mAuthUrl = authUrl;
        mListener = listener;
        mRedirectUri = redirectUri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Please wait...");
        setContentView(R.layout.dialog_instagram);

        mTitle = (TextView) findViewById(R.id.textview_web_title);
        mTitle.setVisibility(View.INVISIBLE);
        setUpWebView();
    }

    /**
     * Set up webview
     */
    private void setUpWebView() {
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new InstagramWebViewClient());
        mWebView.setInitialScale(1);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSavePassword(false);
        mWebView.getSettings().setSaveFormData(false);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setScrollbarFadingEnabled(false);

        mWebView.loadUrl(mAuthUrl);
    }

    /**
     * Clear the webview cache
     */
    public void clearCache() {
        mWebView.clearCache(true);
        mWebView.clearHistory();
        mWebView.clearFormData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mListener.onCancel();
    }

    /**
     * Web client for intercepting Instagram responses
     */
    private class InstagramWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(mRedirectUri)) {
                if (url.contains("code")) {
                    String temp[] = url.split("=");
                    mListener.onSuccess(temp[1]);
                } else if (url.contains("error")) {
                    String temp[] = url.split("=");
                    mListener.onError(temp[temp.length - 1]);
                }
                InstagramDialog.this.dismiss();
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(description);
            InstagramDialog.this.dismiss();
            Log.d(TAG, "Page error: " + description);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mProgressDialog.show();
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mTitle.setVisibility(View.VISIBLE);
            mProgressDialog.dismiss();
        }
    }

    public interface InstagramDialogListener {
        public abstract void onSuccess(String code);
        public abstract void onCancel();
        public abstract void onError(String error);
    }
}
