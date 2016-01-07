package com.moon.myreadapp.mvvm.viewmodels;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.View;

import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.PopupMenu;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.moon.appframework.action.RouterAction;
import com.moon.appframework.core.XApplication;
import com.moon.appframework.core.XDispatcher;
import com.moon.myreadapp.R;
import com.moon.myreadapp.common.adapter.ArticleRecAdapter;
import com.moon.myreadapp.common.components.pulltorefresh.PullToRefreshRecyclerView;
import com.moon.myreadapp.common.components.recyclerview.RecyclerItemClickListener;
import com.moon.myreadapp.common.components.rss.RssHelper;
import com.moon.myreadapp.common.components.toast.TastyToast;
import com.moon.myreadapp.common.event.UpdateFeedEvent;
import com.moon.myreadapp.constants.Constants;
import com.moon.myreadapp.mvvm.models.ModelHelper;
import com.moon.myreadapp.mvvm.models.dao.Article;
import com.moon.myreadapp.mvvm.models.dao.Feed;
import com.moon.myreadapp.ui.ArticleActivity;
import com.moon.myreadapp.ui.FeedActivity;
import com.moon.myreadapp.util.BuiltConfig;
import com.moon.myreadapp.util.Conver;
import com.moon.myreadapp.util.DBHelper;
import com.moon.myreadapp.util.PreferenceUtils;
import com.moon.myreadapp.util.VibratorHelper;
import com.moon.myreadapp.util.ViewUtils;
import com.rey.material.app.Dialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by moon on 15/10/23.
 */
public class FeedViewModel extends BaseViewModel {

    private FeedActivity mView;

    private RecyclerItemClickListener articleClickListener;
    private ArticleRecAdapter mAdapter;

    private long feedId;
    private Feed feed;
    private int currentPosition = -1;
    private Dialog mDialog;
    private boolean showAllArticles;

    public FeedViewModel(FeedActivity view, long feedId) {
        this.mView = view;
        this.feedId = feedId;
        this.feed = DBHelper.Query.getFeed(feedId);
        initViews();
        initEvents();
    }

    @Override
    public void initViews() {
        this.mView.setTitle(feed.getTitle());
        showAllArticles = PreferenceUtils.getInstance(mView).getBooleanParam(Constants.FEED_SHOW_ALL,true);
        mAdapter = new ArticleRecAdapter(getBaseData());
    }

    private List<Article> getBaseData() {
        return DBHelper.Query.getArticlesByID(feedId, showAllArticles?Article.Status.NORMAL_AND_FAVOR:Article.Status.NORMAL_AND_FAVOR_BUT_UNREAD);
    }


    @Override
    public void initEvents() {
        articleClickListener = new RecyclerItemClickListener(mView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                readArticle(mAdapter.getItem(position), position);
                updateFeed();
                Bundle bundle = new Bundle();
                bundle.putLong(Constants.ARTICLE_ID, mAdapter.getItem(position).getId());
                bundle.putInt(Constants.ARTICLE_POS, position);
                XDispatcher.from(mView).dispatch(new RouterAction(ArticleActivity.class, bundle, true));
            }

            @Override
            public void onItemLongClick(final View view, final int position) {
                //XLog.d("onItemLongClick execute!");
                final Article article = mAdapter.getmData().get(position);
                currentPosition = position;
                //震动
                VibratorHelper.shock(VibratorHelper.TIME.SHORT);
                View v = mView.getLayoutInflater().inflate(R.layout.menu_singer_article, null);

                mDialog = new Dialog(mView).
                        contentView(v).
                        cancelable(true).
                        layoutParams(-1, -2);
                mDialog.show();

                // 弹出对话框:收藏|已读|删除
//                Menu menu = ViewUtils.showPopupMenu(mView, view.findViewById(R.id.article_title), R.menu.menu_single_article, new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(android.view.MenuItem item) {
//                        int id = item.getItemId();
//                        switch (id) {
//                            case R.id.action_read:
//                                readArticle(article,position);
//                                updateFeed();
//                                break;
//                            case R.id.action_read_favor:
//                                //收藏
//                                if (article.getStatus() == Article.Status.NORMAL.status) {
//                                    article.setStatus(Article.Status.FAVOR.status);
//                                    DBHelper.UpDate.saveArticle(article);
//                                    Snackbar.make(view, BuiltConfig.getString(R.string.action_favor) + BuiltConfig.getString(R.string.success), Snackbar.LENGTH_SHORT).show();
//                                } else {
//                                    article.setStatus(Article.Status.NORMAL.status);
//                                    DBHelper.UpDate.saveArticle(article);
//                                    Snackbar.make(view, BuiltConfig.getString(R.string.action_favor_back) + BuiltConfig.getString(R.string.success), Snackbar.LENGTH_SHORT).show();
//                                }
//                                break;
//                            case R.id.action_read_delete:
//                                //删除
//                                mAdapter.remove(position);
//                                article.setStatus(Article.Status.DELETE.status);
//                                DBHelper.UpDate.saveArticle(article);
//
//                                Snackbar.make(view, BuiltConfig.getString(R.string.action_delete) + BuiltConfig.getString(R.string.success), Snackbar.LENGTH_SHORT).show();
//                                break;
//                        }
//                        return false;
//                    }
//                });

                //操作menu行为

                //已读
                v.findViewById(R.id.action_read).setVisibility(article.getUse_count() <= 0 ? View.VISIBLE : View.GONE);
                //menu.findItem(R.id.action_read).setVisible(article.getUse_count() <= 0);
                //收藏
                ((Button) (v.findViewById(R.id.action_read_favor))).setText(BuiltConfig.getString(article.getStatus() == Article.Status.FAVOR.status ? R.string.action_favor_back : R.string.action_favor));
                // menu.findItem(R.id.action_read_favor).setTitle(BuiltConfig.getString(article.getStatus() == Article.Status.FAVOR.status ? R.string.action_favor_back : R.string.action_favor));

            }

        });
    }

    public ArticleRecAdapter getmAdapter() {
        return mAdapter;
    }

    public RecyclerItemClickListener getArticleClickListener() {
        return articleClickListener;
    }

    @Override
    public void clear() {
        mView = null;
    }


    /**
     * 刷新
     *
     * @param feedList
     */
    public void refresh(final PullToRefreshRecyclerView feedList) {
        RssHelper.getMostRecentNews(feed.getUrl(), new RssHelper.IRssListener() {
            @Override
            public void onSuccess(final SyndFeed syndFeed) {
                feedList.post(new Runnable() {
                    @Override
                    public void run() {

                        //设置刷新时间
                        feedList.getHeaderLoadingLayout().setLastUpdatedLabel(Conver.ConverToString(new Date(), "HH:mm"));
                        //完成刷新
                        feedList.onPullDownRefreshComplete();


                        //获取的文章list
                        Feed feed = DBHelper.Util.feedConert(syndFeed, DBHelper.Query.getUserId());
                        ArrayList<Article> articles = DBHelper.Util.getArticles(syndFeed);
                        if (articles == null || articles.size() == 0) {
                            //没有获取到数据
                            return;
                        }

                        //result 为获取新更新的文章
                        ArrayList<Article> result = ModelHelper.getUpDateArticlesByFeedId(feedId, articles);


                        boolean haveNewDate = result != null && result.size() > 0;

                        //插入数据库
                        if (haveNewDate) {
                            DBHelper.Insert.articles(result);
                        }
                        articles = null;

                        //设置提示
                        showNotice(haveNewDate ? BuiltConfig.getString(R.string.notice_update, feed.getTitle(), result.size()) : BuiltConfig.getString(R.string.notice_update_none), TastyToast.STYLE_MESSAGE);

                        //重新设置数据
                        if (haveNewDate) {
                            mAdapter.setmData(getBaseData());
                        }
                        updateFeed();

                    }
                });

            }

            @Override
            public void onError(final String msg) {
                showNotice(BuiltConfig.getString(R.string.notice_update_none),TastyToast.STYLE_MESSAGE);
                feedList.onPullDownRefreshComplete();
//
            }
        });
    }

    /**
     * 弹出顶部提示
     *
     * @param txt
     */
    private TastyToast showNotice(String txt,TastyToast.Style style) {
        TastyToast toast = TastyToast.makeText(mView, txt, style).enableSwipeDismiss().setLayoutBelow(mView.findViewById(R.id.toolbar));
        toast.setOutAnimation(AnimationUtils.loadAnimation(mView, R.anim.toast_out));
        toast.show();
        return toast;
        /*
        final View view = LayoutInflater.from(mView).inflate(R.layout.common_notice_bar, null);

        feedList.getmAdapter().addHeader(view);

        View m =feedList.getmAdapter().getHeader(0);
        final TextView tv = (TextView) m.findViewById(R.id.info);
        tv.setText(txt + time);
        //设置放大的动画
        if (time % 2 == 0) {
            animate(tv).setDuration(1000).scaleXBy(.5f).scaleYBy(.5f).setInterpolator(new AnticipateOvershootInterpolator());
        } else {
            animate(tv).setDuration(1000).scaleXBy(-.5f).scaleYBy(-.5f).setInterpolator(new AnticipateOvershootInterpolator());
        }
        time++;
        feedList.postDelayed(new Runnable() {
            @Override
            public void run() {
                feedList.getmAdapter().removeHeader(view);

            }
        }, 2000);
        */
    }


    private void readArticle(Article article, int position) {
        article.setUse_count(article.getUse_count() + 1);
        DBHelper.UpDate.saveArticle(article);
        mAdapter.notifyItemChanged(position);
    }

    private void updateFeed() {
        UpdateFeedEvent event = new UpdateFeedEvent(feed,UpdateFeedEvent.TYPE.STATUS);
        event.setStatus(UpdateFeedEvent.NORMAL);
        XApplication.getInstance().bus.post(event);
    }

    public void btnOnClick(View v) {

        if (currentPosition >= 0 || currentPosition < mAdapter.getmData().size()) {
            int id = v.getId();
            final Article article = mAdapter.getmData().get(currentPosition);
            switch (id) {
                case R.id.action_read:
                    readArticle(article, currentPosition);
                    updateFeed();
                    break;
                case R.id.action_read_favor:
                    //收藏
                    if (article.getStatus() == Article.Status.NORMAL.status) {
                        article.setStatus(Article.Status.FAVOR.status);
                        DBHelper.UpDate.saveArticle(article);
                        showNotice(BuiltConfig.getString(R.string.action_favor) + BuiltConfig.getString(R.string.success),TastyToast.STYLE_ALERT).setDuration(1000);
                    } else {
                        article.setStatus(Article.Status.NORMAL.status);
                        DBHelper.UpDate.saveArticle(article);
                        showNotice(BuiltConfig.getString(R.string.action_favor_back) + BuiltConfig.getString(R.string.success),TastyToast.STYLE_ALERT).setDuration(1000);
                    }
                    break;
                case R.id.action_read_delete:
                    //删除
                    mAdapter.remove(currentPosition);
                    article.setStatus(Article.Status.DELETE.status);
                    DBHelper.UpDate.saveArticle(article);
                    showNotice(BuiltConfig.getString(R.string.action_delete) + BuiltConfig.getString(R.string.success),TastyToast.STYLE_ALERT).setDuration(1000);
                    break;
            }

        }
        if(mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
        }
    }

    public void updateSet(boolean showAllArticles) {
        this.showAllArticles = showAllArticles;

        mAdapter.setmData(getBaseData());
        updateFeed();
    }
}
