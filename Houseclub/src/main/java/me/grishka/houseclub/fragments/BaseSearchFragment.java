package me.grishka.houseclub.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import me.grishka.appkit.fragments.LoaderFragment;
import me.grishka.houseclub.R;

abstract class BaseSearchFragment extends LoaderFragment {

    public static final String KEY_SEARCH_TYPE = "key_search_type";
    private static final long DELAY = 200;

    private RecyclerView recyclerView;
    private TextView errorTextView;
    private String errorMessage;

    protected String searchQuery;
    private SearchView searchView;
    private SearchView.OnQueryTextListener onQueryTextListener;
    private long timestamp = System.currentTimeMillis();

    protected abstract <VH extends RecyclerView.ViewHolder> RecyclerView.Adapter<VH> getRecyclerAdapter();

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void loadData() {
        dataLoading = true;
        doLoadData();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.toolbar).setVisibility(View.GONE);
        showContent();

        searchView = view.findViewById(R.id.searchView);

        int searchTypeOrdinal = getArguments().getInt(KEY_SEARCH_TYPE);
        SearchType searchType = SearchType.values()[searchTypeOrdinal];

        String hint;
        if (searchType == SearchType.PEOPLE) {
            hint = view.getContext().getString(R.string.search_people_hint);
        } else {
            hint = view.getContext().getString(R.string.search_clubs_hint);
        }

        if (searchType == SearchType.PEOPLE) {
            errorMessage = view.getContext().getString(R.string.search_error_no_users);
        } else {
            errorMessage = view.getContext().getString(R.string.search_error_no_clubs);
        }

        searchView.setQueryHint(hint);
        onQueryTextListener = new OnSearchQueryTextListener();


        recyclerView = view.findViewById(R.id.rvSearch);
        recyclerView.setAdapter(getRecyclerAdapter());

        errorTextView = view.findViewById(R.id.tvSearchError);

        view.findViewById(R.id.tvSearchCancel).setOnClickListener(v -> getActivity().onBackPressed());
    }

    @Override
    public void onResume() {
        super.onResume();

        searchView.setOnQueryTextListener(onQueryTextListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        searchView.setOnQueryTextListener(null);
    }

    protected void onUsersNotFound() {
        if (errorTextView != null && errorMessage != null && errorTextView.getVisibility() != View.VISIBLE) {
            recyclerView.setVisibility(View.GONE);
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setText(errorMessage);
        }
    }

    protected void onUsersFound() {
        if (recyclerView != null && recyclerView.getVisibility() != View.VISIBLE) {
            errorTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void onQueryChanged(String query) {
        long currentTimeStamp = System.currentTimeMillis();
        if (currentTimeStamp - timestamp < DELAY) {
            timestamp = currentTimeStamp;
            return;
        }

        if (query == null || query.length() <= 2) {
            timestamp = currentTimeStamp;
            return;
        }
        timestamp = currentTimeStamp;
        searchQuery = query;
        loadData();
    }

    private class OnSearchQueryTextListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            onQueryChanged(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            onQueryChanged(newText);
            return false;
        }
    }

    public enum SearchType {
        PEOPLE,
        CLUBS
    }
}
