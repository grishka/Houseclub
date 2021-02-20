package me.grishka.houseclub.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.ToolbarFragment;
import me.grishka.houseclub.R;
import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseSession;
import me.grishka.houseclub.api.methods.CheckForUpdate;
import me.grishka.houseclub.api.methods.CompletePhoneNumberAuth;
import me.grishka.houseclub.api.methods.ResendPhoneNumberAuth;
import me.grishka.houseclub.api.methods.StartPhoneNumberAuth;

public class LoginFragment extends BaseToolbarFragment{

	private EditText phoneInput, codeInput;
	private Button resendBtn, nextBtn;
	private boolean sentCode=false;

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		setTitle(R.string.login);
	}

	@Override
	public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view=inflater.inflate(R.layout.login, container, false);

		phoneInput=view.findViewById(R.id.phone_input);
		codeInput=view.findViewById(R.id.code_input);
		resendBtn=view.findViewById(R.id.resend_code);
		nextBtn=view.findViewById(R.id.next);

		codeInput.setVisibility(View.GONE);
		resendBtn.setVisibility(View.GONE);

		nextBtn.setOnClickListener(this::onNextClick);
		resendBtn.setOnClickListener(this::onResendClick);

		return view;
	}

	private String getCleanPhoneNumber(){
		String number=phoneInput.getText().toString().replaceAll("[^\\d]", "");
		return '+'+number;
	}

	private void onNextClick(View v){
		if(sentCode){
			new CompletePhoneNumberAuth(getCleanPhoneNumber(), codeInput.getText().toString())
					.wrapProgress(getActivity())
					.setCallback(new SimpleCallback<CompletePhoneNumberAuth.Response>(this){
						@Override
						public void onSuccess(CompletePhoneNumberAuth.Response result){
							ClubhouseSession.userToken=result.authToken;
							ClubhouseSession.userID=result.userProfile.userId+"";
							ClubhouseSession.isWaitlisted=result.isWaitlisted;
							ClubhouseSession.write();
							if(result.isWaitlisted){
								Nav.goClearingStack(getActivity(), WaitlistedFragment.class, null);
							}else if(result.userProfile.username==null){
								Nav.goClearingStack(getActivity(), RegisterFragment.class, null);
							}else{
								Nav.goClearingStack(getActivity(), HomeFragment.class, null);
							}
						}
					})
					.exec();
		}else{
			new StartPhoneNumberAuth(getCleanPhoneNumber())
					.wrapProgress(getActivity())
					.setCallback(new SimpleCallback<BaseResponse>(this){
						@Override
						public void onSuccess(BaseResponse result){
							sentCode=true;
							phoneInput.setEnabled(false);
							codeInput.setVisibility(View.VISIBLE);
							resendBtn.setVisibility(View.VISIBLE);
						}
					})
					.exec();
		}
	}

	private void onResendClick(View v){
		new ResendPhoneNumberAuth(getCleanPhoneNumber())
				.wrapProgress(getActivity())
				.exec();
	}
}
