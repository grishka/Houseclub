package me.grishka.houseclub.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import me.grishka.appkit.fragments.ToolbarFragment;

public abstract class BaseToolbarFragment extends ToolbarFragment {

    @Override
    public boolean wantsLightStatusBar() {
        return true;
    }

    @Override
    public boolean wantsLightNavigationBar() {
        return true;
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
}
