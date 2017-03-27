package ru.jxt.usbtoreasyclient;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class NoConnectedFragment extends Fragment implements View.OnClickListener {

    private Button button;
    private ProgressBar progressBar;
    private OnConnectedCallback mOnConnectedCallback;

    interface OnConnectedCallback {
        void onConnectedCallback();
    }

    public OnConnectedCallback getOnConnectedCallback() {
        return mOnConnectedCallback;
    }

    public void setOnConnectedCallback(OnConnectedCallback mOnConnectedCallback) {
        this.mOnConnectedCallback = mOnConnectedCallback;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_no_connected, null);

        button = (Button) view.findViewById(R.id.button);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        button.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (isNetworkConnected()) {
            mOnConnectedCallback.onConnectedCallback();
        }
        button.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
