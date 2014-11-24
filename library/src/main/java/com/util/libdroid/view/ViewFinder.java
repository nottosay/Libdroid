package com.util.libdroid.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.view.View;

/**
 * Created by wally.yan on 2014/10/21.
 */
public class ViewFinder {

    private View view;
    private Activity activity;
    private Fragment fragment;

    public ViewFinder(View view) {
        this.view = view;
    }

    public ViewFinder(Activity activity) {
        this.activity = activity;
    }

    public ViewFinder(Fragment fragment) {
        this.fragment = fragment;
    }

    public View findViewById(int id) {
        if (view != null) return view.findViewById(id);
        if (activity != null) return activity.findViewById(id);
        return fragment.getView().findViewById(id);
    }

    public View findViewById(int id, int parentId) {
        View pView = null;
        if (parentId > 0) {
            pView = this.findViewById(parentId);
        }

        View view = null;
        if (pView != null) {
            view = pView.findViewById(id);
        } else {
            view = this.findViewById(id);
        }
        return view;
    }


    public Context getContext() {
        if (view != null) return view.getContext();
        if (activity != null) return activity;
        if (fragment != null) return fragment.getActivity();
        return null;
    }
}
