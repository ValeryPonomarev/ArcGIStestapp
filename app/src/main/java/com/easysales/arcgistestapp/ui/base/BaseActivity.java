package com.easysales.arcgistestapp.ui.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.easysales.arcgistestapp.R;

/**
 * Created by lordp on 22.06.2017.
 */

public class BaseActivity extends AppCompatActivity {

    public void replaceFragment(BaseFragment fragment, String tag){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment, tag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(tag)
                .commit();
    }
}
