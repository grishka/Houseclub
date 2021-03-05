package me.grishka.houseclub.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.houseclub.R;
import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.methods.UpdateName;
import me.grishka.houseclub.api.methods.UpdateUsername;

public class RegisterFragment extends BaseToolbarFragment {

    private EditText firstNameInput, lastNameInput, usernameInput;
    private Button nextBtn;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setTitle(R.string.register);
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register, container, false);

        firstNameInput = view.findViewById(R.id.first_name_input);
        lastNameInput = view.findViewById(R.id.last_name_input);
        usernameInput = view.findViewById(R.id.username_input);
        nextBtn = view.findViewById(R.id.next);

        nextBtn.setOnClickListener(this::onNextClick);

        return view;
    }

    private void onNextClick(View v) {
        String first = firstNameInput.getText().toString();
        String last = lastNameInput.getText().toString();
        String username = usernameInput.getText().toString();

        if (first.length() < 2 || last.length() < 2 || username.length() < 2) {
            Toast.makeText(getActivity(), R.string.all_fields_are_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.length() > 16) {
            Toast.makeText(getActivity(), R.string.username_limit, Toast.LENGTH_SHORT).show();
            return;
        }

        new UpdateName(first + " " + last)
                .wrapProgress(getActivity())
                .setCallback(new Callback<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse result) {
                        new UpdateUsername(username)
                                .wrapProgress(getActivity())
                                .setCallback(new Callback<BaseResponse>() {
                                    @Override
                                    public void onSuccess(BaseResponse result) {
                                        Toast.makeText(getActivity(), R.string.welcome_to_clubhouse, Toast.LENGTH_SHORT).show();
                                        Nav.goClearingStack(getActivity(), HomeFragment.class, null);
                                    }

                                    @Override
                                    public void onError(ErrorResponse error) {
                                        error.showToast(getActivity());
                                    }
                                })
                                .exec();
                    }

                    @Override
                    public void onError(ErrorResponse error) {
                        error.showToast(getActivity());
                    }
                })
                .exec();
    }
}
