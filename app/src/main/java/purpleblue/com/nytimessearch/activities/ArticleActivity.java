package purpleblue.com.nytimessearch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import purpleblue.com.nytimessearch.R;
import purpleblue.com.nytimessearch.models.Article;
import purpleblue.com.nytimessearch.net.NetworkClass;

public class ArticleActivity extends AppCompatActivity {

    Article article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        article = (Article) getIntent().getParcelableExtra("article");
        //Log.d("DEBUG", String.valueOf(article));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set article title into Activity title
        getSupportActionBar().setTitle(article.getHeadline());

        setupViews(article);

        if (!NetworkClass.isNetworkAvailable(this) && !NetworkClass.isInternetConnected()) {
            Toast.makeText(this, "Internet appears to be down. This app needs internet to function.", Toast.LENGTH_SHORT).show();
        }
    }

    public void setupViews(Article article) {
        WebView webView = (WebView) findViewById(R.id.wvArticle);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl(article.getWebUrl());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_share, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch reference to the share action provider
        ShareActionProvider miShareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // Attach Share for a WebView URL
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // pass in the URL currently being used by the WebView
        shareIntent.putExtra(Intent.EXTRA_TEXT, article.getWebUrl());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.getHeadline());

        miShareAction.setShareIntent(shareIntent);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                // ProjectsActivity is my 'home' activity
                super. onBackPressed();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

}
