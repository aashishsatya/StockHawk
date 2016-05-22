package com.sam_chordas.android.stockhawk;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by Aashish Satyajith on 12-May-16.
 * Most of the code courtesy of https://github.com/udacity/Advanced_Android_Development/compare/7.03_Choose_Your_Size...7.04_Integrating_the_Detail_Widget
 */
public class StocksWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                // since the widget will be just a miniature MyStocksActivity, we can use the same query given in it
                data = getContentResolver().query(
                        QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{
                                QuoteColumns._ID,
                                QuoteColumns.SYMBOL,
                                QuoteColumns.BIDPRICE,
                                QuoteColumns.PERCENT_CHANGE,
                                QuoteColumns.CHANGE,
                                QuoteColumns.ISUP},
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null
                );
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.list_item_quote);

                // get the data for that particular view
                String stockSymbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
                String bidPriceStr = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
                String percentChangeStr = data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
                int is_up = data.getInt(data.getColumnIndex(QuoteColumns.ISUP));

                // bind the data to the view
                views.setTextViewText(R.id.stock_symbol, stockSymbol);
                views.setTextViewText(R.id.bid_price, bidPriceStr);
                views.setTextViewText(R.id.change, percentChangeStr);
                if(is_up == 1) {
                    // use the green pill
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                }
                else {
                    // use the red pill
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                }

                // display either the percentage change or proper dollar value corresponding to user's choice
                if (Utils.showPercent) {
                    views.setTextViewText(R.id.change, data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
                }
                else {
                    views.setTextViewText(R.id.change, data.getString(data.getColumnIndex(QuoteColumns.CHANGE)));
                }

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(MyStocksActivity.TAG_STOCK_SYMBOL, stockSymbol);
                views.setOnClickFillInIntent(R.id.list_item_quote_linear_layout, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) {
                    return data.getLong(data.getColumnIndexOrThrow(QuoteColumns._ID));
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
