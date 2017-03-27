package ru.jxt.usbtoreasyclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public final class Network {

    private static Network _instance = null;

    private Network() {}

    public static synchronized Network getInstance() {
        if (_instance == null)
            _instance = new Network();
        return _instance;
    }

    //---------------------------------------------------------

    private Map<String, String> cookie;

    public void login(String name, String password) {
        try {
            if(cookie == null) {
                //получаем Response для cookies
                Connection.Response mainPageResponse = Jsoup.connect(MainActivity.usbtorMainPage)
                        .method(Connection.Method.GET)
                        .timeout(10000)
                        .execute();
                cookie = mainPageResponse.cookies();
            }

            //получаем Response для cookies пользователя
            Connection.Response loginForm = Jsoup.connect(MainActivity.usbtorLoginPage)
                    .method(Connection.Method.POST)
                    .cookies(cookie)
                    .timeout(10000)
                    .data("redirect", "index.php")
                    .data("login_username", name)
                    .data("login_password", password)
                    .data("autologin", "on")
                    .data("login", "Вход")
                    .execute();

            cookie = loginForm.cookies();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearCookie() {
        cookie = null;
    }

    public void saveCookie(@NonNull Context context) {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(cookie != null) {
            JSONObject jsonObject = new JSONObject(cookie);
            mSharedPreferences.edit().putString("cookie_json", jsonObject.toString()).apply();
        }
        else {
            mSharedPreferences.edit().remove("cookie_json").apply();
        }
    }

    public void loadCookie(@NonNull Context context) {
        if(cookie == null) {
            try {
                SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                String cookie_json = mSharedPreferences.getString("cookie_json", null);
                if (cookie_json != null) {
                    JSONObject jsonObject = new JSONObject(cookie_json);
                    cookie = new HashMap<>();
                    Iterator<String> keysItr = jsonObject.keys();
                    while (keysItr.hasNext()) {
                        String key = keysItr.next();
                        String value = (String) jsonObject.get(key);
                        cookie.put(key, value);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectForLogout() {
        try {
            Jsoup.connect(MainActivity.usbtorLoginPage)
                    .method(Connection.Method.GET)
                    .timeout(10000)
                    .cookies(cookie)
                    .data("logout", "1")
                    .execute();

            clearCookie();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logout(Context context) {
        loadCookie(context);
        connectForLogout();
        clearCookie();
        saveCookie(context);
    }

    public Document getDocument(String url) {
        Document doc = null;
        try {
            if(cookieExists()) {
                doc = Jsoup.connect(url)
                        .cookies(cookie)
                        .timeout(10000)
                        .get();
            }
        } catch (HttpStatusException hse) {
            String error = "Ошибка " + hse.getStatusCode();
            doc = Jsoup.parse(getHtmlOuter(error));
        } catch (SocketTimeoutException ste) {
            String error = "Время ожидания ответа от сервера вышло!\r\nПовторите попытку позже.";
            doc = Jsoup.parse(getHtmlOuter(error));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    String getHtmlOuter(String text) {
        return "<html><body><p class=\"jsoup error\">" + text + "</p></body></html>";
    }

    public boolean cookieExists() {
        return cookie != null;
    }

    public Map<String, String> getCookie() {
        return cookie;
    }

    public Document getValidCookieDocument(String url) {
        Document doc = getDocument(url);
        if (doc != null) {
            Elements els = doc.select("div.logged_in").select("li");
            if (!els.toString().contains("login.php?logout=1"))
                return null;
        }
        return doc;
    }

    public Document getLoginDocument(String sName, String sPassword) {
        login(sName, sPassword);
        return getValidCookieDocument(MainActivity.usbtorLoginPage);
    }

}
