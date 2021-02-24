package me.grishka.houseclub.fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.views.UsableRecyclerView;
import me.grishka.houseclub.R;
import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.methods.GetFollowers;
import me.grishka.houseclub.api.methods.GetSuggestedInvites;
import me.grishka.houseclub.api.methods.InviteToApp;
import me.grishka.houseclub.api.model.Contact;
import me.grishka.houseclub.api.model.FullUser;

import static android.Manifest.permission.READ_CONTACTS;

public class InviteListFragment extends SearchListFragment {

	private List<Contact> contacts = null;
	private final Map<String,String> a_contacts = new HashMap<>();
	private List<Contact> r_contacts = null;

	private static final int REQUEST_READ_CONTACTS = 0;

	private static final int limit = 50;

	public InviteListFragment() {
		min_query_lenght = 0;
	}

	private InviteListAdapter adapter;



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
				readContacts();
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
			contacts = getContactList();
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					reqestData();
				}
			});
		}
	}


	private void searchContacts(String query) {

		if(query == null)
			query = "";

		List<FullUser> users = new ArrayList<>();

		users.clear();

		int i =0;
		for (Contact contact : r_contacts) {

			contact.name = a_contacts.get(contact.phone_number);

			Pattern pattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
			if (query.equals("") || (
					pattern.matcher(contact.name + contact.phone_number).find())) {

				FullUser user = new FullUser();
				user.name = contact.name;
				user.dsplayname = contact.phone_number;
				String in_app = contact.in_app ? getString(R.string.yes) : getString(R.string.no);
				String is_invited = contact.is_invited ? getString(R.string.yes) : getString(R.string.no);
				user.bio = contact.phone_number + getString(R.string.contact_separator) +
					getString(R.string.contact_in_app, in_app) + getString(R.string.contact_separator) +
					getString(R.string.contact_is_invited, is_invited) + getString(R.string.contact_separator) +
					getString(R.string.contact_num_friends, contact.num_friends);
				users.add(user);

				i++;
				if(i > limit) break;
			}

		}

		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				data.clear();
				onDataLoaded(users, false);
			}
		});



	}

	private List<Contact> getContactList() {

		List<Contact> m_contacts = new ArrayList<>();

		ContentResolver cr = getContext().getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
				null, null, null, null);

		if ((cur != null ? cur.getCount() : 0) > 0) {
			while (cur.moveToNext()) {

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

						m_contacts.add(new Contact(name, phoneNo));
						a_contacts.put(phoneNo, name);

					}
					pCur.close();
				}


			}
		}
		if (cur != null) {
			cur.close();
		}

		return m_contacts;

	}


	void reqestData() {

		currentRequest=new GetSuggestedInvites(contacts)
				.setCallback(new SimpleCallback<GetSuggestedInvites.Response>(this){
					@Override
					public void onSuccess(GetSuggestedInvites.Response result){
						currentRequest=null;
						r_contacts = result.suggested_invites;

						Toast.makeText(getContext(), getString(R.string.contact_invites, result.num_invites), Toast.LENGTH_SHORT).show();

						searchContacts(searchQuery);
					}
				})
				.exec();

	}


	@Override
	protected void doLoadData(int offset, int count) {

		showProgress();

		if(r_contacts == null) {
			Runnable r = () -> {
				if (contacts == null) {
					readContacts();
				}
			};
			new Thread(r).start();
		} else {
			searchContacts(searchQuery);
		}



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
			builder.setMessage(getString(R.string.invite_dialog_text, item.name, item.dsplayname));

			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new InviteToApp(item.name, item.dsplayname, "")
							.wrapProgress(getActivity())

							.setCallback(new Callback<BaseResponse>(){
								@Override
								public void onSuccess(BaseResponse result){
									Toast.makeText(getContext(), R.string.invite_ok, Toast.LENGTH_SHORT).show();
								}

								@Override
								public void onError(ErrorResponse error){
									Toast.makeText(getContext(), R.string.invite_err, Toast.LENGTH_SHORT).show();
									error.showToast(getContext());
								}
							})
							.exec();
				}
			});
			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

			builder.show();
		}
	}



} 