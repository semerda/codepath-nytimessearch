package purpleblue.com.nytimessearch.adapters;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import purpleblue.com.nytimessearch.BR;
import purpleblue.com.nytimessearch.R;
import purpleblue.com.nytimessearch.TimeClass;
import purpleblue.com.nytimessearch.models.Article;

/**
 * Created by ernest on 7/27/16.
 *
 * Ref: http://guides.codepath.com/android/Using-the-RecyclerView
 */

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.BindingHolder> {

    // Store a member variable for the contacts
    private List<Article> mArticles;
    // Store the context for easy access
    private Context mContext;

    // Pass in the contact array into the constructor
    public ArticlesAdapter(Context context, List<Article> articles) {
        mArticles = articles;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding binding;

        public BindingHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article_result, parent, false);
        BindingHolder holder = new BindingHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        final Article article = mArticles.get(position);
        holder.getBinding().setVariable(BR.article, article);
        holder.getBinding().executePendingBindings();
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mArticles.size();
    }

    @BindingAdapter({"bind:imageUrl"})
    public static void loadImage(ImageView view, String url) {
        // Android doesn't have a way to load images so we use Glide instead of Picasso
        // because it's better with memory: https://inthecheesefactory.com/blog/get-to-know-glide-recommended-by-google/en
        // Ref: https://futurestud.io/blog/glide-image-resizing-scaling
        Glide.with(view.getContext())
                .load(url)
                .override(300, 300) // resizes the image to these dimensions (in pixel)
                .centerCrop() // this cropping technique scales the image so that it fills the requested bounds and then crops the extra.
                .placeholder(R.drawable.ic_autorenew_black_48dp)
                .error(R.drawable.nyt_when_empty)
                .into(view);
        view.setAlpha((float) 0.8);
    }

    @BindingAdapter({"bind:pubDate"})
    public static void loadTimeAgo(TextView view, String publishedDate) {
        String timeAgoString = "Not Available";
        if (publishedDate != null) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                timeAgoString = TimeClass.getTimeAgo((Date)formatter.parse(publishedDate), view.getContext());
            } catch(ParseException e) {
                e.printStackTrace();
            }
        }

        view.setText(timeAgoString);
    }

    public void clearData() {
        int sizeOfList = mArticles.size();
        // clear list
        mArticles.clear();
        // let the adapter know about the changes and reload view.
        //this.notifyDataSetChanged();
        this.notifyItemRangeRemoved(0, sizeOfList);
    }
}
