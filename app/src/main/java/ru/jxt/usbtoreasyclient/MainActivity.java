package ru.jxt.usbtoreasyclient;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "usbtorEasyClient";
    public static final String APP_PACKAGE = "com.jxt.usbtoreasyclient";

    public static final String usbtorMainPage = "http://usbtor.ru";
    public static final String usbtorLoginPage = "http://usbtor.ru/login.php";
    public static final String usbtorInboxPage = "http://usbtor.ru/privmsg.php?folder=inbox";
    public static final String usbtorNewPostsPage = "http://usbtor.ru/search.php?new=1";

    public static final String START_UPDATESERVICE = APP_PACKAGE + ".action.START_UPDATESERVICE";

    public static final int repeatingRequestCode = 305305;

    private LoginFragment mLoginFragment;
    private ForumFragment mForumFragment;
    private NavigationView mNavigationView;
    private SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //задаем права
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //загружаем cookies, если есть
        Network mNetwork = Network.getInstance();
        mNetwork.loadCookie(this);

        if(mNetwork.cookieExists()) {
            if(isNetworkConnected()) {
                if(mNetwork.getValidCookieDocument(MainActivity.usbtorMainPage) != null)
                    replaceToForumFragmentAndSetting(true, null, R.id.main_page);
                else
                    replaceToLoginFragmentAndSetting(true);
            }
            else {
                replaceToNoConnectedFragmentAndSetting(true);
            }
        }
        else {
            replaceToLoginFragmentAndSetting(true);
        }

//        if(isNetworkConnected()) {
//
//        }
//        else {
//
//        }
//
//        //проверяем если у нас cookies и достоверны ли они еще
//        if(mNetwork.cookieExists() && mNetwork.getValidCookieDocument(MainActivity.usbtorMainPage) != null)
//            replaceToForumFragmentAndSetting(true, null, R.id.main_page);
//        else {
//            replaceToLoginFragmentAndSetting(true);
//            //setIsLoginPreference(false);
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //прячем клавиатуру
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(drawerView.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer != null)
            drawer.closeDrawer(GravityCompat.START);

        Fragment currentFragment = getFragmentManager().findFragmentByTag("current_fragment");
        switch(item.getItemId()) {
            case R.id.main_page:
                replaceToForumAndGoUrl(currentFragment, usbtorMainPage, R.id.main_page);
                break;
            case R.id.private_messages:
                replaceToForumAndGoUrl(currentFragment, usbtorInboxPage, R.id.private_messages);
                break;
            case R.id.new_forum_posts:
                replaceToForumAndGoUrl(currentFragment, usbtorNewPostsPage, R.id.new_forum_posts);
                break;
            case R.id.settings:
                if(!(currentFragment instanceof SettingsFragment)) {
                    Fragment f = (mSettingsFragment == null) ? (mSettingsFragment = new SettingsFragment()) : mSettingsFragment;
                    replaceCurrentFragment(f);
                }
                break;
            case R.id.login:
                if(!(currentFragment instanceof LoginFragment))
                    replaceToLoginFragmentAndSetting(false);
                break;
            case R.id.exit:
                if(checkInternet()) {
                    replaceToLoginFragmentAndSetting(true);
                    new LogoutAsyncTask().execute();
                }
                return false;
        }
        return true;
    }

    void replaceToForumAndGoUrl(Fragment currentFragment, String url, int pageId) {
        if(checkInternet()) {
            if (!(currentFragment instanceof ForumFragment))
                replaceToForumFragmentAndSetting(true, url, pageId);

            if (!mForumFragment.getUrl().equalsIgnoreCase(url))
                mForumFragment.loadUrl(url);
        }
    }

    boolean checkInternet() {
        boolean internet = isNetworkConnected();
        if(!internet)
            Toast.makeText(this, "Отсутствует подключение к интернету!", Toast.LENGTH_SHORT).show();
        return internet;
    }

    void replaceCurrentFragment(Fragment f) {
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                .replace(R.id.content, f, "current_fragment")
                .commit();
    }

    class LogoutAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Network.getInstance().logout(MainActivity.this);
            new SheduledService().stopAsync(getApplicationContext());
            //setIsLoginPreference(false);
            return null;
        }
    }

    void replaceToNoConnectedFragmentAndSetting(boolean setting) {
        NoConnectedFragment f = new NoConnectedFragment();
        f.setOnConnectedCallback(new NoConnectedFragment.OnConnectedCallback() {
            @Override
            public void onConnectedCallback() {
                replaceToForumFragmentAndSetting(true, null, R.id.main_page);
            }
        });
        replaceCurrentFragment(f);
        if(setting) {
            NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
            Menu mMenu = nv.getMenu();
            mMenu.setGroupVisible(R.id.forum_group, false);
            mMenu.setGroupVisible(R.id.settings_group, false);
            mMenu.setGroupVisible(R.id.login_group, false);
        }
    }

    void replaceToLoginFragmentAndSetting(boolean setting) {
        Fragment f = (mLoginFragment == null) ? (mLoginFragment = new LoginFragment()) : mLoginFragment;
        replaceCurrentFragment(f);

        if(setting) {
            NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
            Menu mMenu = nv.getMenu();
            mMenu.setGroupVisible(R.id.forum_group, false);
            mMenu.setGroupVisible(R.id.settings_group, false);
            mMenu.setGroupVisible(R.id.login_group, true);
            nv.setCheckedItem(R.id.login);
        }

        if(mLoginFragment.getOnLoginCallback() == null) {
            mLoginFragment.setOnLoginCallback(new LoginFragment.OnLoginCallback() {
                @Override
                public void onLoginCallback() {
                    replaceToForumFragmentAndSetting(true, null, R.id.main_page);
                    //setIsLoginPreference(true);
                }
            });
        }
    }

    void replaceToForumFragmentAndSetting(boolean setting, String url, int id) {
        ForumFragment f = (mForumFragment == null) ? (mForumFragment = new ForumFragment()) : mForumFragment;
        f.setUrl(url);
        replaceCurrentFragment(f);

        if(setting) {
            NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
            Menu mMenu = nv.getMenu();
            mMenu.setGroupVisible(R.id.forum_group, true);
            mMenu.setGroupVisible(R.id.settings_group, true);
            mMenu.setGroupVisible(R.id.login_group, false);
            nv.setCheckedItem(id);
        }
    }

//    void setIsLoginPreference(boolean value) {
//        SharedPreferences mSharedPreferences =
//                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        mSharedPreferences.edit().putBoolean("login", value).apply();
//    }

}
