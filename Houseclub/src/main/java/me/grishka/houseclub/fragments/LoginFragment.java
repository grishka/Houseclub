package me.grishka.houseclub.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.houseclub.R;
import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseSession;
import me.grishka.houseclub.api.methods.CompletePhoneNumberAuth;
import me.grishka.houseclub.api.methods.ResendPhoneNumberAuth;
import me.grishka.houseclub.api.methods.StartPhoneNumberAuth;

public class LoginFragment extends BaseToolbarFragment {

    private EditText phoneInput, codeInput;
    private Button resendBtn, nextBtn;
    private CountryCodePicker countryCodePicker;
    private LinearLayout resendCodeLayout;
    private boolean sentCode = false;
    private PhoneNumberUtil phoneNumberUtil;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//		setTitle(R.string.login);
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);

        phoneNumberUtil = PhoneNumberUtil.createInstance(getActivity());
        phoneInput = view.findViewById(R.id.phone_input);
        codeInput = view.findViewById(R.id.code_input);
        resendBtn = view.findViewById(R.id.resend_code);
        nextBtn = view.findViewById(R.id.next);
        countryCodePicker = view.findViewById(R.id.country_code_picker);
        resendCodeLayout = view.findViewById(R.id.resend_code_layout);

        codeInput.setVisibility(View.GONE);
        resendCodeLayout.setVisibility(View.GONE);

        countryCodePicker.registerPhoneNumberTextView(phoneInput);
        nextBtn.setOnClickListener(this::onNextClick);
        resendBtn.setOnClickListener(this::onResendClick);
        phoneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phoneInput.getText().toString(), countryCodePicker.getSelectedCountryNameCode());
                    String country = phoneNumberUtil.getRegionCodeForNumber(number);
                    if (country != null)
                        countryCodePicker.setCountryForNameCode(country);
                } catch (NumberParseException igonre) {
                }
            }
        });

        return view;
    }

    private String getCleanPhoneNumber() {
        return countryCodePicker.getNumber();
    }

    private void onNextClick(View v) {
        if (sentCode) {
            new CompletePhoneNumberAuth(getCleanPhoneNumber(), codeInput.getText().toString())
                    .wrapProgress(getActivity())
                    .setCallback(new SimpleCallback<CompletePhoneNumberAuth.Response>(this) {
                        @Override
                        public void onSuccess(CompletePhoneNumberAuth.Response result) {
                            ClubhouseSession.userToken = result.authToken;
                            ClubhouseSession.userID = result.userProfile.userId + "";
                            ClubhouseSession.isWaitlisted = result.isWaitlisted;
                            ClubhouseSession.write();
                            if (result.isWaitlisted) {
                                Nav.goClearingStack(getActivity(), WaitlistedFragment.class, null);
                            } else if (result.userProfile.username == null) {
                                Nav.goClearingStack(getActivity(), RegisterFragment.class, null);
                            } else {
                                Nav.goClearingStack(getActivity(), HomeFragment.class, null);
                            }
                        }
                    })
                    .exec();
        } else {
            new StartPhoneNumberAuth(getCleanPhoneNumber())
                    .wrapProgress(getActivity())
                    .setCallback(new SimpleCallback<BaseResponse>(this) {
                        @Override
                        public void onSuccess(BaseResponse result) {
                            sentCode = true;
                            phoneInput.setEnabled(false);
                            countryCodePicker.setClickable(false);
                            codeInput.setVisibility(View.VISIBLE);
                            resendCodeLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(ErrorResponse error) {
                            Toast.makeText(LoginFragment.this.getContext(), "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .exec();
        }
    }

    private void onResendClick(View v) {
        new ResendPhoneNumberAuth(getCleanPhoneNumber())
                .wrapProgress(getActivity())
                .exec();
    }
}
