package com.addon.tsasaa.photogallerysearch;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    private final static String TAG = "SEARCH_LOG";
    private RecyclerView mPhotoRecycleView;
    private List<GalleryItem> mItems; // = new ArrayList<>();
    public final static String DETAIL_VIEW_ID = "DETAIL_VIEW";

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // When the activity is recreated it will be searched for again using its id or tag
        setHasOptionsMenu(true); // To receive menu callbacks
        mItems = new ArrayList<>();
        //new FetchItemsTask().execute();

        /////////// Start our intent service from here //////////////
        //Intent intent = PollService.newIntent(getActivity());
        //getActivity().startService(intent); // same as startActivity(intent); -> startService(intent)
        //PollService.setServiceAlarm(getActivity(), true);
        //new FetchItemsTask(null).execute();
        updateItems();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecycleView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_viewID);
        mPhotoRecycleView.setLayoutManager(new GridLayoutManager(getActivity(), 3)); // 3 line of GridLayout

        setupAdapter();

        return v; // super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setupAdapter() {
        if (isAdded()) {        // Confirms that fragment has been attached to an activity, so that getActivity won't be null
            mPhotoRecycleView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            //return new URLtoString().fetchItems();
            //String query = "anime comic con"; // Just for testing

            if (mQuery == null || mQuery.equals("")) {
                return new URLtoString().fetchRecentPhotos();
            } else {
                return new URLtoString().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private GalleryItem mGalleryItem;
        private ImageView mItemImageView;

        public PhotoHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            mItemImageView = (ImageView) v.findViewById(R.id.fragment_photo_gallery_image_viewID);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), DetailView.class);
            intent.putExtra(DETAIL_VIEW_ID, mGalleryItem.getUrl());
            //startActivityForResult(intent, 0);
            startActivity(intent);
            //Toast.makeText(getActivity(), mGalleryItem.getUrl(), Toast.LENGTH_SHORT).show();
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
//            Picasso.with(getActivity())
//                    .load(mGalleryItem.getUrl())
//                    .placeholder(R.drawable.nice)
//                    .into(mItemImageView);
            Glide.with(getActivity())
                    .load(mGalleryItem.getUrl())//.diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.nice)
                    .into(mItemImageView);
            /*
                Bitmap img = Picasso.with(this).load("http://").get();
                int w = img.getWidth();
                int h = img.getHeight();
             */
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItemList;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItemList = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItemList.get(position);
            photoHolder.bindGalleryItem(galleryItem);
        }

        @Override
        public int getItemCount() {
            return mGalleryItemList.size();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_searchID);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextChange(String s) {
                //Log.d(TAG, "QueryTextSubmit: " + s);
                QueryPreferences.setStoredQuery(getActivity(), s);
                updateItems();

                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String s) {
                //Log.d(TAG, "QueryTextChange: " + s);
                QueryPreferences.setStoredQuery(getActivity(), s);
                updateItems();
                // Hide
                // Check if no view has focus:
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                return true;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        /////////////////// TOGGLE SERVICE //////////////////
        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_pollingID);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_pollingString);
        } else {
            toggleItem.setTitle(R.string.start_pollingString);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clearID:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems(); // new execute
                return true;
            case R.id.menu_item_toggle_pollingID: // setOn or Off
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu(); // telling update its toolbar options menu !!!!
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
    }
}

