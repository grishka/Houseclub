package me.grishka.houseclub.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.ViewImageLoader;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.views.UsableRecyclerView;
import me.grishka.houseclub.R;
import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.methods.InviteToApp;
import me.grishka.houseclub.api.methods.SearchPeople;
import me.grishka.houseclub.api.model.FullUser;
import me.grishka.houseclub.api.model.InviteUser;
import me.grishka.houseclub.api.model.SearchUser;

import static android.Manifest.permission.READ_CONTACTS;

public class InviteListFragment extends BaseSearchFragment {

    private static final int REQUEST_READ_CONTACTS = 0;

    private InviteListRecyclerAdapter adapter;

    public InviteListFragment() {
        min_query = 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <VH extends RecyclerView.ViewHolder> RecyclerView.Adapter<VH> getRecyclerAdapter() {
        if (adapter == null) {
            adapter = new InviteListFragment.InviteListRecyclerAdapter();
        }
        return (RecyclerView.Adapter<VH>) adapter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        readContacts();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getContactList("");
            }
        }
    }

    private boolean askContactsPermission() {

        if (getContext().checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        return false;
    }

    private void readContacts() {
        if (!askContactsPermission()) {
            return;
        } else {
            getContactList("");
        }
    }

    private void getContactList(String query) {

        List<InviteUser> users = new ArrayList<>();
        int count = 0, limit = 10;

        ContentResolver cr = getContext().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {

                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));

                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                    if (cur.getInt(cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));

                            if(query.equals("") || (
                                    Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE).matcher(name).find() ||
                                    Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE).matcher(phoneNo).find())) {

                                InviteUser u = new InviteUser();
                                u.name = name;
                                u.phone = phoneNo;
                                users.add(u);

                                count++;
                                if (count > limit) {
                                    pCur.moveToLast();
                                    cur.moveToLast();
                                }

                            }
                        }
                        pCur.close();
                    }


            }
        }
        if(cur!=null){
            cur.close();
        }


        dataLoaded();
        if (adapter != null && users != null && !users.isEmpty()) {
            onUsersFound();
            adapter.updateUsers(users);
        } else {
            onUsersNotFound();
        }

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
        getContactList(query);
    }

    @Override
    public void onRefresh() {
    }

    private class InviteListRecyclerAdapter extends RecyclerView.Adapter<InviteListFragment.InviteListRecyclerAdapter.UserViewHolder> {

        private final List<InviteUser> userList = new ArrayList<>();

        @NonNull
        @Override
        public InviteListFragment.InviteListRecyclerAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_user, parent, false);
            return new InviteListFragment.InviteListRecyclerAdapter.UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InviteListFragment.InviteListRecyclerAdapter.UserViewHolder holder, int position) {
            InviteUser user = userList.get(position);
            holder.itemView.setOnClickListener(v -> {

			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle(R.string.invite_dialog_title);
			builder.setMessage(getString(R.string.invite_dialog_text, user.name, user.phone));

			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new InviteToApp(user.name, user.phone, "")
							.wrapProgress(getActivity())

							.setCallback(new Callback<BaseResponse>(){
								@Override
								public void onSuccess(BaseResponse result){
									Toast.makeText(getContext(), R.string.invite_ok, Toast.LENGTH_SHORT).show();
								}

								@Override
								public void onError(ErrorResponse error){
                                    Toast.makeText(getContext(), getString(R.string.invite_err, ""), Toast.LENGTH_SHORT).show();
                                    error.showToast(getContext());
								}
							})
							.exec();
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					Toast.makeText(getContext(), "cancel", Toast.LENGTH_SHORT).show();
				}
			});

			builder.show();


            });
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        public void updateUsers(List<InviteUser> users) {
            userList.clear();
            userList.addAll(users);
            notifyDataSetChanged();
        }

        private class UserViewHolder extends RecyclerView.ViewHolder {
            private final TextView userNameView, userBioView;

            UserViewHolder(View view) {
                super(view);

                userNameView = view.findViewById(R.id.tvUsername);
                userBioView = view.findViewById(R.id.tvUserBio);

            }

            void bind(InviteUser user) {
                userNameView.setText(user.name);
                userBioView.setText(user.phone);

            }
        }
    }





}