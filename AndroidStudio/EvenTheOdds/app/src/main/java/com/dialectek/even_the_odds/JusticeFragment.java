package com.dialectek.even_the_odds;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class JusticeFragment extends Fragment {

    private WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.justice_fragment, container, false);
        mWebView = (WebView) view.findViewById(R.id.webview);
        mWebView.loadUrl("https://google.com");

        // Enable Javascript.
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Force links and redirects to open in the WebView instead of in a browser.
        mWebView.setWebViewClient(new WebViewClient());

        return view;
    }
}
