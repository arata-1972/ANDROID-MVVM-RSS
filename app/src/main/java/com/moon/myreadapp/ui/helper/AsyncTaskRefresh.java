package com.moon.myreadapp.ui.helper;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.moon.appframework.common.log.XLog;
import com.moon.appframework.common.util.SafeAsyncTask;
import com.moon.appframework.core.XApplication;
import com.moon.myreadapp.common.components.rss.FeedNetwork;
import com.moon.myreadapp.common.components.rss.RssHelper;
import com.moon.myreadapp.common.event.UpdateFeedEvent;
import com.moon.myreadapp.mvvm.models.ModelHelper;
import com.moon.myreadapp.mvvm.models.dao.Article;
import com.moon.myreadapp.mvvm.models.dao.Feed;
import com.moon.myreadapp.util.DBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moon on 16/06/13.
 */
public class AsyncTaskRefresh extends SafeAsyncTask<ArrayList<Feed>, UpdateFeedEvent, String> {

    private static String TAG = AsyncTaskRefresh.class.getSimpleName();


    private StatusListener listener;

    public AsyncTaskRefresh(StatusListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(ArrayList<Feed>... params) {
        ArrayList<Feed> feeds = params[0];
        if(feeds == null || feeds.size() == 0){
            return "";
        }
        for (int i = 0; i < feeds.size(); i++) {
            if (isCancelled()){
                return "";
            }
            try {
                //原来的feed feeds.get(i);
                XLog.d(TAG + "feed :" + feeds.get(i).getTitle() + " 开始更新");
                //通知正在更新这个feed
                UpdateFeedEvent event =  new UpdateFeedEvent(feeds.get(i), UpdateFeedEvent.TYPE.STATUS);
                event.setStatus(UpdateFeedEvent.ON_UPDATE);
                event.setNotice("开始更新....");
                publishProgress(event);

                Feed feed = FeedNetwork.getInstance().load(feeds.get(i).getUrl());
                //转换出文章list
                List<Article> articles = feed.getArticles();
                //过滤,获取新数据;
                ArrayList<Article> result = ModelHelper.getUpDateArticlesByFeedId(feeds.get(i).getId(),articles);
                XLog.d(TAG + "feed :" + feeds.get(i).getTitle() + "id : " + feeds.get(i).getId()+ " 更新完毕,共获得更新的文章:" + result.size());
                //插入数据
                DBHelper.Insert.articles(result);
                //DBHelper.Insert.feed(feed);
                //通知更新结束
                UpdateFeedEvent event1 =  new UpdateFeedEvent(feeds.get(i), UpdateFeedEvent.TYPE.STATUS);
                event1.setStatus(UpdateFeedEvent.NORMAL);
                event1.setNotice("更新完毕....");
                publishProgress(event1);
                if(isCancelled()){
                    throw new Exception("update cancel!");
                }
            } catch (Exception e) {
                XLog.d(TAG + "feed :" + e);
                //更新失败...
                UpdateFeedEvent event1 =  new UpdateFeedEvent(feeds.get(i), UpdateFeedEvent.TYPE.STATUS);
                event1.setStatus(UpdateFeedEvent.FAIL);
                event1.setNotice("更新失败....");
                publishProgress(event1);
            }

        }
        return "";
    }

    @Override
    protected void onSafePostExecute(String s) {
        super.onSafePostExecute(s);
        listener.onSuccess();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        listener.onCancel();
    }

    @Override
    protected void onProgressUpdate(UpdateFeedEvent... values) {
        UpdateFeedEvent progress = values[0];
        XApplication.getInstance().bus.post(progress);
    }

    public interface StatusListener {
        void onSuccess();

        void onCancel();
    }


}
