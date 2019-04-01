package naihebaver.policeassistantapp.Activities;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.Objects;

import naihebaver.policeassistantapp.Helpers.SharedPreferencesHelper;
import naihebaver.policeassistantapp.R;

public class NewsActivity extends AppCompatActivity {

    SharedPreferencesHelper mSharedPrefsHelper;
    String mCodeState;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        setupToolbar("Новини");

        mSharedPrefsHelper = new SharedPreferencesHelper(this);

        switch (mSharedPrefsHelper.getUserState()){
            case "Zakarpats'ka oblast": mCodeState = "zk";
            break;
            case "L'vivs'ka oblast": mCodeState = "lv";
            break;
            case "Ivano-Frankivs'ka oblast": mCodeState = "if";
            break;
            case "Volyns'ka oblast": mCodeState = "vl";
            break;
            case "Ternopil's'ka oblast": mCodeState = "tp";
            break;
            case "Chernivets'ka oblast": mCodeState = "cv";
            break;
            case "Rivnens'ka oblast": mCodeState = "rv";
            break;
            case "Khmel'nytsʹka oblast": mCodeState = "hm";
            break;
            case "Zhytomyrsʹka oblast": mCodeState = "zt";
            break;
            case "Vinnytsʹka oblast": mCodeState = "vn";
            break;
            case "Kyyivsʹka oblast": mCodeState = "kv";
            break;
            //etc...

        }

        WebView view = findViewById(R.id.webView);
        view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
        view.getSettings().setJavaScriptEnabled(true);
        view.loadUrl("https://"+mCodeState+".npu.gov.ua/news");

    }

    public void setupToolbar(String tittle){
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        mTitle.setText(tittle);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if( id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
