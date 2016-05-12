package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

public class StockGraphActivity extends Activity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    final int STOCK_DETAILS_LOADER = 0;

    LineChartView lineChartView;
    LineSet dataset;

    String stockSymbol;
    String TAG_MILESTONE = "MLST>";
    String TAG_DATA = "DATA>";
    String TAG_RED_FLAG = "RDFLG>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        lineChartView = (LineChartView) findViewById(R.id.linechart);

        // get the stock symbol from the intent extras
        stockSymbol = getIntent().getStringExtra(MyStocksActivity.TAG_STOCK_SYMBOL);

        // prepare the loader
        getLoaderManager().initLoader(STOCK_DETAILS_LOADER, null, this);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri baseUri = QuoteProvider.Quotes.CONTENT_URI;

        // the graph just has to be between the stock's value and time
        // so this will be the projection
        String[] projection = new String[]{
                QuoteColumns.BIDPRICE,
        };

        // get the loader for only that particular ticker symbol asked for
        String select = "(" + QuoteColumns.SYMBOL + "= ?)";
        return new CursorLoader(this, baseUri, projection, select, new String[] {stockSymbol}, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // initialize LineSet variable
        // this will contain the values used to plot the points

        dataset = new LineSet();

        int dayCounter = 1; // to keep track of the number of days (for labelling)
        int bidPriceColumnIndex = data.getColumnIndex(QuoteColumns.BIDPRICE);

        float bidPrice;
        // we need max and min to find out the range
        // if we don't break the graph the plot will be a straight line :-P
        float maxBidPrice = Float.MIN_VALUE;
        float minBidPrice = Float.MAX_VALUE;

        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            bidPrice = Float.parseFloat(data.getString(bidPriceColumnIndex));
            // update max and min
            maxBidPrice = bidPrice > maxBidPrice? bidPrice: maxBidPrice;
            minBidPrice = bidPrice < minBidPrice? bidPrice: minBidPrice;
            dataset.addPoint("Day " + dayCounter, bidPrice);
            dayCounter++;
        }

        lineChartView.addData(dataset);
        lineChartView.show();
    }

    public void onLoaderReset(Loader<Cursor> loader) {

    }


}
