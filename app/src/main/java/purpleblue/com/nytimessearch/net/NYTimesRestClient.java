package purpleblue.com.nytimessearch.net;

import android.content.Context;
import android.content.SharedPreferences;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import purpleblue.com.nytimessearch.Constants;

/**
 * Created by ernest on 7/29/16.
 *
 * Resources
 * Net: http://loopj.com/android-async-http/
 * NYTimes: http://developer.nytimes.com/article_search_v2.json#/Console/GET/articlesearch.json
 */
public class NYTimesRestClient {
    private static final String BASE_URL = "https://api.nytimes.com/svc/search/v2/";
    private static AsyncHttpClient client = new AsyncHttpClient();
    Context context;

    public NYTimesRestClient(Context context) {
        this.client.setTimeout(20 * 1000);

        this.context = context;
    }

    public void searchArticles(String query, Integer page, JsonHttpResponseHandler handler) {
        // Ref: https://developer.nytimes.com/article_search_v2.json#/README

        String url = getAbsoluteUrl("articlesearch.json");

        RequestParams params = new RequestParams();
        params.put("api-key", Constants.APIKEY_NEWYORKTIMES);

        if (page > 0) { params.put("page", page); }
        if (!query.isEmpty()) { params.put("q", query); }
        //params.put("hl", true);

        SharedPreferences mSettings = context.getSharedPreferences(Constants.SHAREDPREF_SEARCHSETTINGS, 0);

        String paramBeginDate = mSettings.getString("begin_date", null);
        if (!paramBeginDate.isEmpty()) { params.put("begin_date", paramBeginDate.replace("-", "")); }

        String paramSort = mSettings.getString("sort", null);
        if (!paramSort.isEmpty()) { params.put("sort", paramSort); }

        String paramNewsDesk = mSettings.getString("fq", null);
        if (!paramNewsDesk.isEmpty()) { params.put("fq", paramNewsDesk); }

        client.get(url, params, handler);
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
