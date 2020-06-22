package com.developerdepository.said;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


/**
 * A simple {@link Fragment} subclass.
 */
public class SplashFragment extends Fragment {

    private ImageView appLogo;
    private TextView appSlogan, poweredBy1, poweredBy2;

    private NavController navController;

    private Animation topAnimation, bottomAnimation;

    public SplashFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        initViews(view);
        initAnimation();
    }

    private void initViews(View view) {
        appLogo = view.findViewById(R.id.app_logo);
        appSlogan = view.findViewById(R.id.app_slogan);
        poweredBy1 = view.findViewById(R.id.app_powered_by1);
        poweredBy2 = view.findViewById(R.id.app_powered_by2);
    }

    private void initAnimation() {
        topAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.start_top_animation);
        bottomAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.start_bottom_animation);
    }

    @Override
    public void onStart() {
        super.onStart();
        int SPLASH_TIMER = 4000;

        appLogo.setAnimation(topAnimation);
        appSlogan.setAnimation(bottomAnimation);
        poweredBy1.setAnimation(bottomAnimation);
        poweredBy2.setAnimation(bottomAnimation);

        new Handler().postDelayed(() -> {
            //Navigate to RecordFragment
            navController.navigate(R.id.action_splashFragment_to_recordFragment);
        }, SPLASH_TIMER);
    }
}
