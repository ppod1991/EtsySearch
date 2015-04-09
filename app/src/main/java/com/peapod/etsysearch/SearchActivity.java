package com.peapod.etsysearch;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * The main activity. Present search bar and Etsy search results
 */
public class SearchActivity extends ActionBarActivity implements AbsListView.OnScrollListener {

    private static final String TAG = "SearchActivity";
    private static final String TAG_ADAPTER_FRAGMENT = "adapter_fragment";
    private RestService restService;
    private final String ETSY_ENDPOINT = "https://api.etsy.com/v2";
    private final String API_KEY = "liwecjs0c3ssk6let4p1wqt9";

    private final int NUM_RESULTS = 25;
    private int requestedOffset = 0;
    private int loadedOffset = 0;
    private boolean requestedMore = false;

    private NewSearchResults newSearchResults = new NewSearchResults();
    private AdditionalSearchResults additionalSearchResults = new AdditionalSearchResults();

    private StaggeredGridView gridView;
    private SmoothProgressBar progressBar;
    private SmoothProgressBar scrollProgressBar;
    private AdapterFragment mAdapterFragment;
    public ListingAdapter adapter;

    private String query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Set tool-bar as actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Create retrofit network adapter
        RestAdapter restAdapter = new RestAdapter.Builder()
                                    .setEndpoint(ETSY_ENDPOINT)
                                    .setLogLevel(RestAdapter.LogLevel.FULL)
                                    .build();

        restService = restAdapter.create(RestService.class);

        //Retrieve components
        progressBar = (SmoothProgressBar) findViewById(R.id.load_progress_bar);
        scrollProgressBar = (SmoothProgressBar) findViewById(R.id.scroll_progress_bar);
        gridView = (StaggeredGridView) findViewById(R.id.grid_view);

        progressBar.setVisibility(View.GONE);
        scrollProgressBar.setVisibility(View.GONE);

        //Check for existing fragment and set gridview adapter accordingly
        FragmentManager fm = getFragmentManager();
        mAdapterFragment = (AdapterFragment) fm.findFragmentByTag(TAG_ADAPTER_FRAGMENT);

        if (mAdapterFragment == null) {
            mAdapterFragment = new AdapterFragment();
            fm.beginTransaction().add(mAdapterFragment, TAG_ADAPTER_FRAGMENT).commit();
        }

        adapter = new ListingAdapter(this,R.id.text_line);
        adapter.addAll(mAdapterFragment.listings);
        loadedOffset = adapter.getCount();

        //If there are previous results, then do not load new results
        if (adapter.getCount() == 0) {
            handleIntent(getIntent());
        }

        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(this);
    }

    //Because this activity is 'Single Top', override the existing intent with upon new search queries
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    //Initiate search query and show progress bar
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            requestedOffset = NUM_RESULTS;
            performQuery(query,NUM_RESULTS,0,newSearchResults);
        } else {
            requestedOffset = NUM_RESULTS;
            performQuery("etsy",NUM_RESULTS,0,newSearchResults);
        }
        progressBar.setVisibility(View.VISIBLE);
        gridView.setVisibility(View.GONE);
    }

    //Execute the given query
    public void performQuery(String query, int limit, int offset, Callback<Listings> cb) {
        Map<String,String> options = new HashMap<String, String>();
        options.put("api_key",API_KEY);
        options.put("limit",limit + "");
        options.put("offset",offset + "");
        options.put("keywords",query);
        options.put("includes", "MainImage");

        restService.getListings(options, cb);
    }


    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
        //Do nothing
    }

    //Modelled after Etsy's StaggeredGridView example
    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
        Log.d(TAG, "onScroll firstVisibleItem:" + firstVisibleItem +
                " visibleItemCount:" + visibleItemCount +
                " totalItemCount:" + totalItemCount);
        if (!requestedMore) {
            int lastInScreen = firstVisibleItem + visibleItemCount;
            if (lastInScreen >= totalItemCount) {
                Log.d(TAG, "onScroll lastInScreen - so load more");
                requestedMore = true;
                performQuery(query,NUM_RESULTS,loadedOffset,additionalSearchResults);
                requestedOffset += NUM_RESULTS;
                scrollProgressBar.setVisibility(View.VISIBLE);
            }
        }
    }

    //Collects and responds to the results of a New Search Query
    public class NewSearchResults implements Callback<Listings> {

        public void success(Listings results, Response response) {
            adapter.clear();
            adapter.addAll(results.results);
            Log.d(TAG, results.results.toString());
            loadedOffset = requestedOffset;

            progressBar.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }

        public void failure(RetrofitError error) {
            Toast.makeText(SearchActivity.this, "Eek, there was an error loading the results. Try Again!", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }

    }

    //Collects and responds to the results of a 'More Listings' query due to scrolling
    public class AdditionalSearchResults implements Callback<Listings> {

        public void success(Listings results, Response response) {
            adapter.notifyDataSetInvalidated();
            adapter.addAll(results.results);
            requestedMore = false;
            loadedOffset = requestedOffset;

            scrollProgressBar.setVisibility(View.GONE);
        }

        public void failure(RetrofitError error) {
            Toast.makeText(SearchActivity.this, "Eek, there was an error loading the results. Try Again!", Toast.LENGTH_LONG).show();
            scrollProgressBar.setVisibility(View.GONE);
        }
    }

    //Stores the state of the adapter upon rotation to maintain state during configuration changes
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        mAdapterFragment.listings = new ArrayList<Listing>();
        for (int i = 0; i < adapter.getCount(); ++i) {
            mAdapterFragment.listings.add(adapter.getItem(i));
        }
    }

    //UI-less fragment to hold the state of the loaded listings for device rotation
    public static class AdapterFragment extends Fragment {

        ArrayList<Listing> listings = new ArrayList<Listing>();
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
