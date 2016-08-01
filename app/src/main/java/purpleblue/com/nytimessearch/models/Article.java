package purpleblue.com.nytimessearch.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ernest on 7/27/16.
 */
public class Article extends BaseObservable implements Parcelable {

    @Bindable
    public String getWebUrl() {
        return webUrl;
    }

    @Bindable
    public String getHeadline() {
        return headline;
    }

    @Bindable
    public String getThumbNail() {
        return thumbNail;
    }

    @Bindable
    public String getPublishedDate() { return publishedDate; }

    private String webUrl;
    private String headline;
    private String thumbNail;
    private String publishedDate;

    public Article(JSONObject jsonObject) {
        try {
            this.webUrl = jsonObject.getString("web_url");
            this.headline = jsonObject.getJSONObject("headline").getString("main");

            JSONArray multimedia = jsonObject.getJSONArray("multimedia");
            //Log.d("DEBUG", multimedia.toString());
            if (multimedia.length() > 0) {
                for (int x=0; x<multimedia.length(); x++) {
                    JSONObject multimediaJson = multimedia.getJSONObject(x);
                    if (multimediaJson.getString("subtype").equals("xlarge") && multimediaJson.getString("type").equals("image")) {
                        this.thumbNail = "http://www.nytimes.com/" + multimediaJson.getString("url");
                    }
                }
            } else {
                this.thumbNail = "";
            }

            this.publishedDate = jsonObject.getString("pub_date");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Article> fromJSONArray(JSONArray array) {
        ArrayList<Article> results = new ArrayList<>();

        for (int x = 0; x < array.length(); x++) {
            try {
                results.add(new Article(array.getJSONObject(x)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.webUrl);
        dest.writeString(this.headline);
        dest.writeString(this.thumbNail);
        dest.writeString(this.publishedDate);
    }

    protected Article(Parcel in) {
        this.webUrl = in.readString();
        this.headline = in.readString();
        this.thumbNail = in.readString();
        this.publishedDate = in.readString();
    }

    public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {
        @Override
        public Article createFromParcel(Parcel source) {
            return new Article(source);
        }

        @Override
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };
}
