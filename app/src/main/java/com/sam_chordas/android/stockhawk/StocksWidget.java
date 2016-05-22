package com.sam_chordas.android.stockhawk;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

// import com.sam_chordas.stockhawk.R;

/**
 * Implementation of App Widget functionality.
 */
public class StocksWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stocks_widget_layout);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setRemoteAdapter(R.id.stocksListView, new Intent(context, StocksWidgetService.class));

        // set the onClickPendingIntent
        Intent stocksIntent = new Intent(context, MyStocksActivity.class);

        //TODO: Try implementing individual behaviours

        PendingIntent stocksPendingIntent = PendingIntent.getActivity(context, 0, stocksIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // launch activity when the title 'STOCK HAWK' is clicked
        views.setOnClickPendingIntent(R.id.appwidget_text, stocksPendingIntent);
        // launch activity when any of the list view items are clicked
        views.setPendingIntentTemplate(R.id.stocksListView, stocksPendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

