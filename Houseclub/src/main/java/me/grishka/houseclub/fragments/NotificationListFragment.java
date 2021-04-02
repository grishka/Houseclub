package me.grishka.houseclub.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.views.UsableRecyclerView;
import me.grishka.houseclub.R;
import me.grishka.houseclub.api.methods.GetNotifications;
import me.grishka.houseclub.api.model.Notification;

public class NotificationListFragment extends BaseRecyclerFragment<Notification> {
    private NotificationListAdapter adapter;

    public NotificationListFragment() {
        super(50);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        loadData();
        setTitle(R.string.notifications_title);
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        if (adapter == null) {
            adapter = new NotificationListAdapter();
        }
        return adapter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getToolbar().setElevation(0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getToolbar().setElevation(0);
    }

    @Override
    protected void doLoadData(int offset, int count) {
        currentRequest = new GetNotifications(getArguments().getInt("id"), 50, offset / 50 + 1)
                .setCallback(new SimpleCallback<GetNotifications.Response>(this) {
                    @Override
                    public void onSuccess(GetNotifications.Response result) {
                        currentRequest = null;
                        onDataLoaded(result.notifications, data.size() + preloadedData.size() + result.notifications.size() < result.count);
                    }
                })
                .exec();
    }

    private class NotificationListAdapter extends RecyclerView.Adapter<NotificationViewHolder> implements ImageLoaderRecyclerAdapter {

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new NotificationViewHolder();
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            holder.bind(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public int getImageCountForItem(int position) {
            return data.get(position).userProfile.photoUrl != null ? 1 : 0;
        }

        @Override
        public String getImageURL(int position, int image) {
            return data.get(position).userProfile.photoUrl;
        }
    }

    private class NotificationViewHolder extends BindableViewHolder<Notification> implements ImageLoaderViewHolder, UsableRecyclerView.Clickable {

        public TextView name, message, time;
        public Button followBtn;
        public ImageView photo;
        private Drawable placeholder = new ColorDrawable(getResources().getColor(R.color.grey));

        public NotificationViewHolder() {
            super(getActivity(), R.layout.notification_list_row);

            name = findViewById(R.id.name);
            message = findViewById(R.id.message);
            time = findViewById(R.id.time);
            photo = findViewById(R.id.photo);
        }

        @Override
        public void onBind(Notification item) {
            itemView.setAlpha(item.inUnread ? 1F : 0.5F);
            if (item.userProfile != null) {
                name.setText(item.userProfile.name);
            } else {
                name.setText("someone");
            }

            message.setText(item.message);
            time.setText(DateUtils.getRelativeTimeSpanString(item.timeCreated.getTime()));

            if (item.userProfile != null && item.userProfile.photoUrl != null)
                imgLoader.bindViewHolder(adapter, this, getAdapterPosition());
            else
                photo.setImageDrawable(placeholder);
        }

        @Override
        public void setImage(int index, Bitmap bitmap) {
            photo.setImageBitmap(bitmap);
        }

        @Override
        public void clearImage(int index) {
            photo.setImageDrawable(placeholder);
        }

        @Override
        public void onClick() {
            Bundle args = new Bundle();
            args.putInt("id", item.userProfile.userId);
            Nav.go(getActivity(), ProfileFragment.class, args);
        }
    }
}
