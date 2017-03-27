package ru.jxt.usbtoreasyclient;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private EditText name;
    private EditText password;
    private Button button;
    private ProgressBar progressBar;
    private OnLoginCallback mOnLoginCallback;

    interface OnLoginCallback {
        void onLoginCallback();
    }

    public void setOnLoginCallback(OnLoginCallback onLoginCallback) {
        mOnLoginCallback = onLoginCallback;
    }

    public OnLoginCallback getOnLoginCallback() {
        return mOnLoginCallback;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_login, null);

        name = (EditText) view.findViewById(R.id.editText2);
        password = (EditText) view.findViewById(R.id.editText3);
        button = (Button) view.findViewById(R.id.button);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        resetTextFields();
        resetViewState();
    }

    private void resetViewState() {
        button.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void resetTextFields() {
        name.setText("");
        password.setText("");
    }

    @Override
    public void onClick(View v) {
        button.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        //прячем клавиатуру
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

        if(isNetworkConnected()) {
            String sName = name.getText().toString();
            String sPassword = password.getText().toString();

            if (!sName.isEmpty() && !sPassword.isEmpty())
                new LoginAsyncTask(sName, sPassword).execute();
            else {
                Toast.makeText(getActivity(), "Заполните все поля!", Toast.LENGTH_SHORT).show();
                resetViewState();
            }
        }
        else {
            Toast.makeText(getActivity(), "Отсутствует подключение к интернету!", Toast.LENGTH_SHORT).show();
            resetViewState();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    class LoginAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private Network mNetwork;
        private String sName;
        private String sPassword;

        LoginAsyncTask(String name, String password) {
            mNetwork = Network.getInstance();
            sName = name;
            sPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Document doc = mNetwork.getLoginDocument(sName, sPassword);
            return doc != null;
        }

        @Override
        protected void onPostExecute(Boolean loginOk) {
            if (loginOk) {
                Context context = getActivity().getApplicationContext();
                mNetwork.saveCookie(context);

                mOnLoginCallback.onLoginCallback();
                SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                if(mSharedPreferences.getBoolean("switch_preference_1", false)) {
                    //mSharedPreferences.edit().putBoolean("isSheduledServiceStarted", true).apply();
                    //new SheduledService().startAsync(getActivity(), 2 * 60 * 1000);
                    String minuts = mSharedPreferences.getString("list_preference_1", "30");
                    new SheduledService().startAsync(context, Integer.parseInt(minuts) * 60 * 1000, false);
                }
            }
            else {
                Toast.makeText(getContext(), "Неверные имя и/или пароль", Toast.LENGTH_SHORT).show();
                mNetwork.clearCookie();
                resetViewState();
            }
        }
    }
}
