package me.grishka.houseclub.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;

import me.grishka.appkit.api.SimpleCallback;
import me.grishka.houseclub.R;
import me.grishka.houseclub.api.methods.SearchPeople;

public class SearchListFragment extends UserListFragment {

	private SearchView searchView;
	private SearchView.OnQueryTextListener onQueryTextListener;

	protected static int min_query_lenght = 2;
	protected String searchQuery;
	private static final long DELAY = 200;
	private long timestamp = System.currentTimeMillis();

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		setTitle(R.string.search_people_hint);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		View search_panel = view.inflate(getContext(), R.layout.search_panel, null);

		searchView = search_panel.findViewById(R.id.searchView);
		searchView.setQueryHint(getString(R.string.search_people_hint));
		onQueryTextListener = new OnSearchQueryTextListener();

		getToolbar().addView(search_panel);
	}

	protected void onQueryChanged(String query) {
		long currentTimeStamp = System.currentTimeMillis();
		if (currentTimeStamp - timestamp < DELAY) {
			timestamp = currentTimeStamp;
			return;
		}

		if (query == null && min_query_lenght > 0 || query.length() <= min_query_lenght) {
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

	@Override
	protected void doLoadData(int offset, int count) {
		if (currentRequest != null) {
			currentRequest.cancel();
		}

		currentRequest = new SearchPeople(searchQuery)
				.setCallback(new SimpleCallback<SearchPeople.Resp>(this) {
					@Override
					public void onSuccess(SearchPeople.Resp result) {
						currentRequest=null;
						data.clear();
						onDataLoaded(result.users, false);
					}
				})
				.exec();
	}


}