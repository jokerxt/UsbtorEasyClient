package ru.jxt.usbtoreasyclient;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Map;

public class ForumFragment extends Fragment {

    private Activity activity;
    private WebView mWebView;
    private String url;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_forum, null);

        if(url == null || url.isEmpty())
            url = MainActivity.usbtorMainPage;

        activity = getActivity();

        mWebView = (WebView) view.findViewById(R.id.webview);
        mWebView.setWebChromeClient(new ProgressWebChromeClient());
        mWebView.setWebViewClient(new WebViewClient());
        WebSettings mWebSettings = mWebView.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setUserAgentString("Android");
        mWebSettings.setBuiltInZoomControls(true);
        mWebSettings.setDisplayZoomControls(false);

        Network mNetwork = Network.getInstance();
        Map<String, String> cookie = mNetwork.getCookie();
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(MainActivity.usbtorMainPage, String.format("bb_data=%s", cookie.get("bb_data")));

        mWebView.loadUrl(url);

        return view;
    }

    public void setUrl(String s) {
        url = s;
    }

    public String getUrl() {
        return url;
    }

    public void loadUrl(String s) {
        setUrl(s);
        mWebView.loadUrl(s);
    }

    class ProgressWebChromeClient extends WebChromeClient {
        public void onProgressChanged(WebView view, int progress) {
            activity.setTitle("Loading...");
            activity.setProgress(progress * 100);
            if(progress == 100)
                activity.setTitle(R.string.app_name);
        }
    }

}
