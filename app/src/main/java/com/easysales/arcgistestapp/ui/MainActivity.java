package com.easysales.arcgistestapp.ui;

import android.os.Bundle;

import com.easysales.arcgistestapp.R;
import com.easysales.arcgistestapp.ui.base.BaseActivity;

import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showSceneFragment();
    }

    public void showRouteFragment() {
        RouteFragment routeFragment = RouteFragment.newInstance();
        replaceFragment(routeFragment, RouteFragment.TAG);
    }

    public void showSceneFragment() {
        SceneFragment sceneFragment = SceneFragment.newInstance();
        replaceFragment(sceneFragment, SceneFragment.TAG);
    }
}
