package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.w3c.dom.Text;

public class StockGraphActivity extends Activity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    final int STOCK_DETAILS_LOADER = 0;

    LineChartView lineChartView;
    LineSet dataset;

    TextView bidPriceTextView;

    String stockSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        lineChartView = (LineChartView) findViewById(R.id.linechart);
        bidPriceTextView = (TextView) findViewById(R.id.bidPriceTextView);

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
            maxBidPrice = Math.max(bidPrice, maxBidPrice);
            minBidPrice = Math.min(bidPrice, minBidPrice);
            dataset.addPoint(Integer.toString(dayCounter), bidPrice);
            dayCounter++;
        }

        int minBorder = ((int) (Math.floor(minBidPrice) / 10)) * 10;
        int maxBorder = (int) (Math.ceil(maxBidPrice / 10)) * 10;

        // add some formatting to the graph
        // courtesy https://github.com/diogobernardino/WilliamChart/blob/master/sample/src/com/db/williamchartdemo/linechart/LineCardOne.java
        dataset.setColor(Color.parseColor("#758cbb"))
                .setFill(Color.parseColor("#2d374c"))
                .setDotsColor(Color.parseColor("#758cbb"))
                .setThickness(4)
                .setDashed(new float[]{10f,10f});

        bidPriceTextView.setText(getString(R.string.stock_title, stockSymbol));

        lineChartView.addData(dataset);
        // Chart
        lineChartView.setBorderSpacing(Tools.fromDpToPx(15))
                .setAxisBorderValues(minBorder - 5, maxBorder + 5, 5)
                .setLabelsColor(Color.parseColor("#6a84c3"))
                .setXAxis(true)
                .setYAxis(true);

        lineChartView.show();
    }

    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
