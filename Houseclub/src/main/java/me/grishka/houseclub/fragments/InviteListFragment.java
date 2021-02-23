package me.grishka.houseclub.fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.views.UsableRecyclerView;
import me.grishka.houseclub.R;
import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.methods.InviteToApp;
import me.grishka.houseclub.api.methods.SearchPeople;
import me.grishka.houseclub.api.model.FullUser;

import static android.Manifest.permission.READ_CONTACTS;

public class InviteListFragment extends SearchListFragment {

    private InviteListAdapter adapter;

    private static final int REQUEST_READ_CONTACTS = 0;

    public InviteListFragment() {
        min_query_lenght = 0;
    }

    @Override
    protected RecyclerView.Adapter getAdapter(){
        if(adapter==null){
            adapter=new InviteListAdapter();
        }
        return adapter;
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

    private void readContacts(String query) {
        if (!askContactsPermission()) {
            return;
        } else {
            getContactList(query);
        }
    }

    private void getContactList(String query) {

        List<FullUser> users = new ArrayList<>();
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

                        if (query.equals("") || (
                                Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE).matcher(name).find() ||
                                        Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE).matcher(phoneNo).find())) {

                            FullUser u = new FullUser();
                            u.name = name;
                            u.bio = phoneNo;
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
        if (cur != null) {
            cur.close();
        }


        data.clear();
        onDataLoaded(users, false);

    }


    @Override
    protected void doLoadData(int offset, int count) {

    if(searchQuery != null)
        readContacts(searchQuery);
    else
        readContacts("");



    }





    private class InviteListAdapter extends RecyclerView.Adapter<IviteViewHolder> implements ImageLoaderRecyclerAdapter {

        @NonNull
        @Override
        public IviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            return new IviteViewHolder();
        }

        @Override
        public void onBindViewHolder(@NonNull IviteViewHolder holder, int position){
            holder.bind(data.get(position));
        }

        @Override
        public int getItemCount(){
            return data.size();
        }

        @Override
        public int getImageCountForItem(int position){
            return data.get(position).photoUrl!=null ? 1 : 0;
        }

        @Override
        public String getImageURL(int position, int image){
            return data.get(position).photoUrl;
        }
    }

    private class IviteViewHolder extends BindableViewHolder<FullUser> implements ImageLoaderViewHolder, UsableRecyclerView.Clickable{

        public TextView name, bio;
        public Button followBtn;
        public ImageView photo;
        private Drawable placeholder=new ColorDrawable(0xFF808080);

        public IviteViewHolder(){
            super(getActivity(), R.layout.user_list_row);

            name=findViewById(R.id.name);
            bio=findViewById(R.id.bio);
            followBtn=findViewById(R.id.follow_btn);
            photo=findViewById(R.id.photo);
        }

        @Override
        public void onBind(FullUser item){
            name.setText(item.name);
            bio.setText(item.bio);
            followBtn.setVisibility(View.GONE);
            photo.setVisibility(View.GONE);
        }

        @Override
        public void setImage(int index, Bitmap bitmap){
            photo.setImageBitmap(bitmap);
        }

        @Override
        public void clearImage(int index){
            photo.setImageDrawable(placeholder);
        }

        @Override
        public void onClick(){

			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle(R.string.invite_dialog_title);
			builder.setMessage(getString(R.string.invite_dialog_text, item.name, item.bio));

			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new InviteToApp(item.name, item.bio, "")
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
				}
			});

			builder.show();
        }
    }



} 