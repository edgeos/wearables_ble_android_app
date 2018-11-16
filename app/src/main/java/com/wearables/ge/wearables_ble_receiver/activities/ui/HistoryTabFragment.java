package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.wearables.ge.wearables_ble_receiver.R;


public class HistoryTabFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";

    public static final String TAB_NAME = "History";

    private ScaleAnimation expandAnimation = new ScaleAnimation(1, 1, 0, 1);
    private ScaleAnimation collapseAnimation = new ScaleAnimation(1, 1, 1, 0);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_history, container, false);
        Bundle args = getArguments();
        /*
        ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
        */

        LinearLayout expanableLayout1 = (LinearLayout) rootView.findViewById(R.id.collapsibleContainer1);
        Switch switchButton1 = (Switch) rootView.findViewById(R.id.expand1);
        switchButton1.setChecked(true);
        switchButton1.setOnClickListener( v -> {
            if (switchButton1.isChecked()) {
                Toast.makeText(this.getContext(), "expanding...", Toast.LENGTH_LONG).show();
                expandView(expanableLayout1, 500);

            } else {
                Toast.makeText(this.getContext(), "collapsing...", Toast.LENGTH_LONG).show();
                collapseView(expanableLayout1, 500);
            }
        });


        LinearLayout expanableLayout2 = (LinearLayout) rootView.findViewById(R.id.collapsibleContainer2);
        Switch switchButton2 = (Switch) rootView.findViewById(R.id.expand2);
        switchButton2.setChecked(true);
        switchButton2.setOnClickListener( v -> {
            if (switchButton2.isChecked()) {
                Toast.makeText(this.getContext(), "expanding...", Toast.LENGTH_LONG).show();
                expandView(expanableLayout2, 500);
            } else {
                Toast.makeText(this.getContext(), "collapsing...", Toast.LENGTH_LONG).show();
                collapseView(expanableLayout2, 500 );
            }
        });



        return rootView;
    }


    private void expandView(View view, long duration) {
        View parentContainer = view.getRootView();
        expandAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(params);
                parentContainer.requestLayout();
            }
            @Override
            public void onAnimationEnd(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        expandAnimation.setDuration(duration);

        //expanableLayout1.animate().scaleY(1);
        view.startAnimation(expandAnimation);
    }


    private void collapseView(View view, long duration) {
        View parentContainer = view.getRootView();
        collapseAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = 0;
                view.setLayoutParams(params);
                parentContainer.requestLayout();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        collapseAnimation.setDuration(500);
        //expanableLayout1.animate().scaleY(0);
        view.startAnimation(collapseAnimation);
    }
}
