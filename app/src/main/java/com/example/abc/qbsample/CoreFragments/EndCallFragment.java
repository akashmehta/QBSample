package com.example.abc.qbsample.CoreFragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.abc.qbsample.R;

/**
 * Created by abc on 5/5/2016.
 */
public class EndCallFragment extends Fragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_end_call,null);
        return rootView;
    }
}
