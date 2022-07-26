package id.my.hubkontak;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SinglePageArticleActivity extends AppCompatActivity {

    private WebView webView;
    private RelativeLayout layoutError;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_page_article);

        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnReload = findViewById(R.id.btnReload);
        TextView txtTitle = findViewById(R.id.txtTitle);
        webView = (WebView) findViewById(R.id.webView);
        layoutError = (RelativeLayout) findViewById(R.id.layoutError);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        txtTitle.setText(getIntent().getStringExtra("title"));
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.reload();
            }
        });
        webView.loadUrl(getIntent().getStringExtra("url"));
        webView.setWebViewClient(new myWebClient());
        webView.setWebChromeClient(new myWebChrome());
        webView.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (webView.canGoBack()) {
                webView.goBack();
            }else {
                finish();
            }
        }
        return true;
    }

    private class myWebChrome extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressBar.setProgress(newProgress);
            if (newProgress == 100) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }
    private class myWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.startsWith("tel:") || url.startsWith("whatsapp:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            }
            return false;
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            view.setVisibility(View.GONE);
            layoutError.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            layoutError.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }
    }
}