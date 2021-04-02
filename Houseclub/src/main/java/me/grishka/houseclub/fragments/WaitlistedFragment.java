package me.grishka.houseclub.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.grishka.appkit.Nav;
import me.grishka.houseclub.R;
import me.grishka.houseclub.api.ClubhouseSession;

public class WaitlistedFragment extends BaseToolbarFragment {

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setTitle(R.string.waitlist);
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.waitlist, container, false);
        view.findViewById(R.id.log_out).setOnClickListener(this::logOut);
        return view;
    }

    private void logOut(View v) {
        ClubhouseSession.userID = ClubhouseSession.userToken = null;
        ClubhouseSession.write();
        Nav.goClearingStack(getActivity(), LoginFragment.class, null);
    }
}
