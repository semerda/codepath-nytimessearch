package purpleblue.com.nytimessearch.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.barryzhang.temptyview.TEmptyView;
import com.barryzhang.temptyview.TViewUtil;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import purpleblue.com.nytimessearch.R;
import purpleblue.com.nytimessearch.adapters.ArticlesAdapter;
import purpleblue.com.nytimessearch.fragments.SearchSettingsDialogFragment;
import purpleblue.com.nytimessearch.listeners.EndlessRecyclerViewScrollListener;
import purpleblue.com.nytimessearch.listeners.RecyclerItemClickListener;
import purpleblue.com.nytimessearch.models.Article;
import purpleblue.com.nytimessearch.net.NYTimesRestClient;
import purpleblue.com.nytimessearch.net.NetworkClass;

public class SearchActivity extends AppCompatActivity {

    GridView gvResults;
    String query;

    private ArrayList<Article> articles;
    private ArticlesAdapter adapter;
    private RecyclerView rvArticles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.purpleblue_com);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Today's News");

        // Custom New York Times Font for Toolbar Title
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/newyorktimes.ttf");
        TextView tvTitle = (TextView) toolbar.getChildAt(0);
        tvTitle.setTextSize(32);
        tvTitle.setTypeface(custom_font);

        setupViews();
        //initEmptyView();

        // Check if network and internet is available
        if (!NetworkClass.isNetworkAvailable(this) && !NetworkClass.isInternetConnected()) {
            Toast.makeText(this, "Internet appears to be down. This app needs internet to function.", Toast.LENGTH_SHORT).show();
        } else {
            // Load default/latest articles
            searchArticlesUsingQuery("", 0, true);
        }
    }

    public void setupViews() {
        articles = new ArrayList<>();
        adapter = new ArticlesAdapter(this, articles);
        rvArticles = (RecyclerView) findViewById(R.id.rvArticles);
        // Attach the adapter to the recyclerview to populate items
        rvArticles.setAdapter(adapter);
        // Set layout manager to position the items
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        // Attach the layout manager to the recycler view
        //rvArticles.setHasFixedSize(true);
        rvArticles.setLayoutManager(gridLayoutManager);
        rvArticles.setItemAnimator(new SlideInUpAnimator());

        // Make sure we can click on each item to see more detail about the article
        rvArticles.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, float x, float y) {
                // Create an intent to display the article
                Intent i = new Intent(view.getContext(), ArticleActivity.class);
                // Get the article to display
                Article article = articles.get(position);
                // Pass in the article
                i.putExtra("article", article);
                // Launch the activity
                startActivity(i);
            }
        }));

        // Endless scrolling
        // Ref: https://github.com/codepath/android_guides/wiki/Endless-Scrolling-with-AdapterViews-and-RecyclerView
        rvArticles.addOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Toast.makeText(SearchActivity.this, "Loading page " + page, Toast.LENGTH_SHORT).show();

                searchArticlesUsingQuery(query, page, false);
            }
        });
    }

    public void initEmptyView() {
        // Ref: https://android-arsenal.com/details/1/3886
        TEmptyView.init(TViewUtil.EmptyViewBuilder.getInstance(this)
                .setShowText(true)
                .setEmptyText(R.string.no_data)
                .setShowButton(false)
                .setShowIcon(true)
                .setIconSrc(R.drawable.ic_block_black_48dp));
        TViewUtil.setEmptyView(gvResults);
    }

    private void showSettingsDialog() {
        FragmentManager fm = getSupportFragmentManager();
        SearchSettingsDialogFragment searchSettingsDialogFragment = SearchSettingsDialogFragment.newInstance(this.getString(R.string.action_search_settings));
        searchSettingsDialogFragment.show(fm, "fragment_search_settings");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchArticlesUsingQuery(query, 0, true);

                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showSettingsDialog();
            /*
            // Ref: https://github.com/afollestad/material-dialogs
            boolean wrapInScrollView = true;
            new MaterialDialog.Builder(this)
                    .customView(R.layout.content_search_settings, wrapInScrollView)
                    .positiveText(R.string.action_save)
                    .negativeText(R.string.action_cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dpBeginDate = (DatePicker) dialog.findViewById(R.id.dpBeginDate);
                            //Log.d(String.format("%s-%s-%s", dpBeginDate.getYear(), dpBeginDate.getYear(), dpBeginDate.getYear()));
                            sSortOrder = (Spinner) dialog.findViewById(R.id.sSortOrder);
                            cbArts = (CheckBox) dialog.findViewById(R.id.cbArts);
                            //Log.d("DEBUG", String.valueOf(cbArts.isChecked()));
                            cbFashionStyle = (CheckBox) dialog.findViewById(R.id.cbFashionStyle);
                            cbSports = (CheckBox) dialog.findViewById(R.id.cbSports);

                            Toast.makeText(SearchActivity.this, which + ": " + dialog, Toast.LENGTH_SHORT).show();

                            // Use SharedPreferences to store and retrieve data?
                            // http://guides.codepath.com/android/Storing-and-Accessing-SharedPreferences
                        }
                    })
                    .show();
                */
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void searchArticlesUsingQuery(String query, int page, Boolean isNewSearch) {
        this.query = query;
        if (isNewSearch) { adapter.clearData(); }
        if (!query.isEmpty()) { Toast.makeText(this, "Searching for " + query, Toast.LENGTH_LONG).show(); }

        NYTimesRestClient client = new NYTimesRestClient(this);
        client.searchArticles(query, page, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray articleJsonResults = null;
                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");

                    if (articleJsonResults.length() == 0) {
                        //TViewUtil.setEmptyView(gvResults);
                        Toast.makeText(SearchActivity.this, "Sorry, Nothing found", Toast.LENGTH_LONG).show();
                    } else {
                        Integer start_index = articles.size();
                        articles.addAll(Article.fromJSONArray(articleJsonResults));
                        adapter.notifyItemRangeInserted(start_index, articles.size());

                        Log.d("DEBUG", articles.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
