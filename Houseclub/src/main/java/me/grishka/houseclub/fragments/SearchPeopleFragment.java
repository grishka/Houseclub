package me.grishka.houseclub.fragments;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.imageloader.ViewImageLoader;
import me.grishka.houseclub.R;
import me.grishka.houseclub.api.methods.SearchPeople;
import me.grishka.houseclub.api.model.SearchUser;

public class SearchPeopleFragment extends BaseSearchFragment {

    private SearchPeopleRecyclerAdapter adapter;

    @SuppressWarnings("unchecked")
    @Override
    protected <VH extends RecyclerView.ViewHolder> RecyclerView.Adapter<VH> getRecyclerAdapter() {
        if (adapter == null) {
            adapter = new SearchPeopleRecyclerAdapter();
        }
        return (RecyclerView.Adapter<VH>) adapter;
    }

    @Override
    protected void doLoadData() {
        String query = searchQuery;
        if (query == null) {
            return;
        }

        if (currentRequest != null) {
            currentRequest.cancel();
        }

        currentRequest = new SearchPeople(query)
                .setCallback(new SimpleCallback<SearchPeople.Resp>(this) {
                    @Override
                    public void onSuccess(SearchPeople.Resp result) {
                        dataLoaded();
                        if (adapter != null && result.users != null && !result.users.isEmpty()) {
                            onUsersFound();
                            adapter.updateUsers(result.users);
                        } else {
                            onUsersNotFound();
                        }
                    }
                })
                .exec();
    }

    @Override
    public void onRefresh() {
    }

    private class SearchPeopleRecyclerAdapter extends RecyclerView.Adapter<SearchPeopleRecyclerAdapter.UserViewHolder> {

        private final List<SearchUser> userList = new ArrayList<>();

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            SearchUser user = userList.get(position);
            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("id", user.userId);
                Nav.go(getActivity(), ProfileFragment.class, args);
            });
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        public void updateUsers(List<SearchUser> users) {
            userList.clear();
            userList.addAll(users);
            notifyDataSetChanged();
        }

        private class UserViewHolder extends RecyclerView.ViewHolder {
            private final TextView userNameView, userBioView;
            private final ImageView userPhotoView;

            UserViewHolder(View view) {
                super(view);

                userNameView = view.findViewById(R.id.tvUsername);
                userBioView = view.findViewById(R.id.tvUserBio);
                userPhotoView = view.findViewById(R.id.ivUserPhoto);
            }

            void bind(SearchUser user) {
                userNameView.setText(user.name);
                userBioView.setText(user.bio);

                ColorDrawable placeholder = new ColorDrawable(0xFF808080);

                ViewImageLoader.load(userPhotoView, placeholder, user.photoUrl);
            }
        }
    }
}