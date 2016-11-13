package com.inpen.shuffle.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.inpen.shuffle.R;
import com.inpen.shuffle.mainscreen.MainActivity;
import com.inpen.shuffle.model.AudioItem;
import com.inpen.shuffle.model.QueueRepository;
import com.inpen.shuffle.playerscreen.PlayerActivity;
import com.inpen.shuffle.utils.CustomTypes;
import com.inpen.shuffle.utils.LogHelper;

/**
 * Created by Abhishek on 11/12/2016.
 */

public class PlayerWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = LogHelper.makeLogTag(PlayerWidgetProvider.class);

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {
            final int widgetId = appWidgetIds[i];


            LogHelper.d(LOG_TAG, "Widget Updating!");
            //TODO implement views

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.player_widget);


            final QueueRepository queueRepository = QueueRepository.getInstance();
            if (queueRepository.getState().equals(CustomTypes.RepositoryState.INITIALIZED)) {
                LogHelper.d(LOG_TAG, "QueueRepo directly initialized, showing Media detail!");


                // Register an onClickListener
                Intent clickIntent = new Intent(context, PlayerActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context, 0, clickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widgetParent, pendingIntent);

                // show music data
                AudioItem item = queueRepository.getCurrentMusic();
                remoteViews.setTextViewText(R.id.widgetSongTitle, item.getmTitle());
                remoteViews.setTextViewText(R.id.widgetArtistName, item.getmArtist());
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            } else {

                // Register an onClickListener
                Intent clickIntent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context, 0, clickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widgetParent, pendingIntent);

                LogHelper.d(LOG_TAG, "MusicNot Playing, showing launch shuffle!");

                //Show launch player view
                remoteViews.setTextViewText(R.id.widgetSongTitle, context.getString(R.string.widget_inactive_string));
                remoteViews.setTextViewText(R.id.widgetArtistName, "");
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }
//            else {
//
//                LogHelper.d(LOG_TAG, "QueueRepo not directly initialized, showing Media detail asynchronously!");
//
//
//                queueRepository.loadQueue(context, new QueueRepository.CachedQueueLoadedCallback() {
//                    @Override
//                    public void onCachedQueueLoaded() {
//                        //show music data
//                        AudioItem item  = queueRepository.getCurrentMusic();
//                        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
//                                R.layout.player_widget);
//                        remoteViews.setTextViewText(R.id.widgetSongTitle, item.getmTitle());
//                        remoteViews.setTextViewText(R.id.widgetArtistName, item.getmArtist());
//                        appWidgetManager.updateAppWidget(widgetId, remoteViews);
//                    }
//                });
//            }

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName thisWidget = new ComponentName(context.getApplicationContext(), PlayerWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        if (appWidgetIds != null && appWidgetIds.length > 0) {
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }
}
