package com.jakebarnby.imageuploader.models;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.jakebarnby.imageuploader.R;

/**
 * Created by Jake on 1/19/2017.
 */

public class SourceLoginDialog extends Dialog {
    static final String TAG = "Login_Dialog";

    protected ProgressDialog mProgressDialog;
    protected WebView mWebView;

    protected String mAuthUrl;
    private String mRedirectUri;

    private SourceLoginDialogListener mListener;

    public SourceLoginDialog(Context context, String authUrl, String redirectUri, SourceLoginDialog.SourceLoginDialogListener listener) {
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
        setContentView(R.layout.dialog_sourcelogin);
        setUpWebView();
    }

    /**
     * Set up webview
     */
    protected void setUpWebView() {
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new SourceLoginDialog.SourceWebViewClient());
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
     * Web client for intercepting OAuth token request responses
     */
    private class SourceWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(mRedirectUri)) {
                if (url.contains("code=")) {
                    String temp[] = url.split("code=");
                    mListener.onSuccess(temp[1]);
                } else if (url.contains("token")) {
                    String temp[] = url.split("=");
                    String longToken = temp[1];
                    String temp2[] = longToken.split("&");
                    mListener.onSuccess(temp2[0]);
                }else if (url.contains("error")) {
                    String temp[] = url.split("=");
                    mListener.onError(temp[temp.length - 1]);
                }
                SourceLoginDialog.this.dismiss();
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(description);
            SourceLoginDialog.this.dismiss();
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
            mProgressDialog.dismiss();
        }
    }

    public interface SourceLoginDialogListener {
        public abstract void onSuccess(String code);
        public abstract void onCancel();
        public abstract void onError(String error);
    }
}
