package moose.com.ac.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import moose.com.ac.App;
import moose.com.ac.ArticleViewActivity;
import moose.com.ac.R;
import moose.com.ac.common.Config;
import moose.com.ac.data.ArticleCollects;
import moose.com.ac.data.DbHelper;
import moose.com.ac.retrofit.article.Article;
import moose.com.ac.util.AppUtils;

/**
 * Created by Farble on 2015/8/15 16.
 */
public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListVH> implements ArticleListVH.ArticleItemClickListener {
    private static final String TAG = "ArticleListAdapter";
    private String TAB_NAME = ArticleCollects.ArticleEntry.TABLE_NAME;
    private List<Article> lists = new ArrayList<>();
    private Activity mActivity;
    private ArticleListVH.ArticleItemClickListener listener;
    private DbHelper dbHelper;
    private int channnel = Config.COMPLEX;//add channel support

    public ArticleListAdapter(List<Article> lists) {
        this.lists = lists;
    }

    public ArticleListAdapter(List<Article> lists, Activity mActivity) {
        this(lists);
        this.mActivity = mActivity;
        dbHelper = new DbHelper(mActivity);
        setListener(this);
    }

    public ArticleListAdapter(List<Article> lists, Activity mActivity, int channnel) {
        this(lists,mActivity);
        dbHelper = new DbHelper(mActivity);
        this.channnel = channnel;
        setListener(this);
    }

    @Override
    public ArticleListVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        ArticleListVH vh = new ArticleListVH(v, listener);
        vh.num = (TextView) v.findViewById(R.id.rank);
        vh.title = (TextView) v.findViewById(R.id.title);
        vh.user = (TextView) v.findViewById(R.id.source);
        vh.time = (TextView) v.findViewById(R.id.posted);
        vh.comment = (TextView) v.findViewById(R.id.text);
        vh.mark = (ImageView) v.findViewById(R.id.bookmarked);
        return vh;
    }

    @Override
    public void onBindViewHolder(ArticleListVH holder, int position) {
        final Article article = lists.get(position);
        holder.num.setText(String.valueOf(position));
        holder.title.setText(article.getTitle());
        holder.user.setText(String.valueOf(article.getViews()) + " views  " + "by " + article.getUser().getUsername());
        holder.time.setText(AppUtils.formatDateByLongTime(String.valueOf(article.getReleaseDate()), mActivity.getString(R.string.format_date)).substring(5));
        holder.comment.setText(article.getComments().toString());
        holder.mark.setVisibility(dbHelper.isExits(TAB_NAME, String.valueOf(article.getContentId())) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public ArticleListVH.ArticleItemClickListener getListener() {
        return listener;
    }

    public void setListener(ArticleListVH.ArticleItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(View view, int position) {
        Article article = lists.get(position);
        article.setSavedate(String.valueOf(System.currentTimeMillis()));
        if (!App.isVistor()) {
            App.getDbHelper().insertArticle(article,ArticleCollects.ArticleHistoryEntry.TABLE_NAME,article.getChannelId());
        }
        Intent intent = new Intent(mActivity, ArticleViewActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putSerializable(Config.ARTICLE, article);
        intent.putExtras(mBundle);
        new Handler().postDelayed(() -> mActivity.startActivity(intent), 50);//make sure db insert done before intent
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Article article = lists.get(position);
        if (dbHelper.isExits(TAB_NAME, String.valueOf(article.getContentId()))) {//exits
            article.setIsfav(Config.NO_ST);//set not fav
            dbHelper.deleteArticle(TAB_NAME, String.valueOf(article.getContentId()));//remove from db
        } else {
            article.setIsfav(Config.STORE);//set not fav
            article.setSavedate(String.valueOf(System.currentTimeMillis()));//set save date
            dbHelper.insertArticle(article, TAB_NAME,channnel);//remove from db
        }
        notifyDataSetChanged();
    }

    public int getChannnel() {
        return channnel;
    }

    public void setChannnel(int channnel) {
        this.channnel = channnel;
    }
}
