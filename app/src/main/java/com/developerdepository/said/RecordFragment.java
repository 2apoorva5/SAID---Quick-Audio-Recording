package com.developerdepository.said;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.skydoves.powermenu.CircularEffect;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hotchemi.android.rate.AppRate;
import maes.tech.intentanim.CustomIntent;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements View.OnClickListener {

    private NavController navController;

    private TextView recordTitle;
    private LottieAnimationView recordBarsAnimation;
    private ImageButton recordListBtn, recordBtn, recordCancelBtn, optionsMenu;
    private Chronometer recordTimer;

    private boolean isRecording = false;
    private int PERMISSION_CODE = 25;

    private MediaRecorder mediaRecorder;
    private String recordPath;
    private String recordedFile;

    private PowerMenu powerMenu;

    private AppUpdateManager mAppUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;
    private int RC_APP_UPDATE = 123;

    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        optionsMenu = view.findViewById(R.id.options_menu);
        recordTitle = view.findViewById(R.id.record_title);
        recordBarsAnimation = view.findViewById(R.id.record_bars_animation);
        recordListBtn = view.findViewById(R.id.record_list_btn);
        recordBtn = view.findViewById(R.id.record_btn);
        recordCancelBtn = view.findViewById(R.id.record_cancel_btn);
        recordTimer = view.findViewById(R.id.record_timer);

        AppRate.with(getActivity())
                .setInstallDays(1)
                .setLaunchTimes(3)
                .setRemindInterval(1)
                .setShowLaterButton(true)
                .setShowNeverButton(false)
                .monitor();

        AppRate.showRateDialogIfMeetsConditions(getActivity());

        recordListBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
        recordCancelBtn.setOnClickListener(this);

        optionsMenu.setOnClickListener(v -> {
            powerMenu = new PowerMenu.Builder(getContext())
                    .addItem(new PowerMenuItem("Privacy Policy", false))
                    .addItem(new PowerMenuItem("Rate SAID", false))
                    .addItem(new PowerMenuItem("Share SAID", false))
                    .addItem(new PowerMenuItem("App Info", false))
                    .setCircularEffect(CircularEffect.BODY)
                    .setMenuRadius(8f)
                    .setMenuShadow(8f)
                    .setTextColor(getResources().getColor(R.color.textMenuColor))
                    .setTextSize(16)
                    .setTextGravity(Gravity.CENTER)
                    .setMenuColor(getResources().getColor(R.color.viewColor))
                    .setOnMenuItemClickListener(onMenuItemClickListener)
                    .setTextTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                    .build();

            powerMenu.showAsAnchorLeftBottom(optionsMenu);
        });
    }

    private OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {
            powerMenu.setSelectedPosition(position);
            if (powerMenu.getSelectedPosition() == 0) {
                powerMenu.dismiss();
                String privacyPolicyUrl = "https://developerdepository.wixsite.com/said-policies";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl));
                startActivity(browserIntent);
            } else if(powerMenu.getSelectedPosition() == 1) {
                powerMenu.dismiss();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + getActivity().getPackageName())));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
                }
            } else if(powerMenu.getSelectedPosition() == 2) {
                powerMenu.dismiss();
                Intent shareIntent =   new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT,"SAID - Quick Audio Recording");
                String app_url = " https://play.google.com/store/apps/details?id=" + getActivity().getPackageName();
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, app_url);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            } else if(powerMenu.getSelectedPosition() == 3) {
                powerMenu.dismiss();
                startActivity(new Intent(getActivity(), AppInfoActivity.class));
                CustomIntent.customType(getActivity(), "bottom-to-up");
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_list_btn:
                if (isRecording) {
                    MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                            .setTitle(String.valueOf(getActivity().getResources().getText(R.string.record_list_btn_dialog_title)))
                            .setMessage(String.valueOf(getActivity().getResources().getText(R.string.record_list_btn_dialog_message)))
                            .setCancelable(false)
                            .setPositiveButton("Yes", R.drawable.material_dialog_okay, (dialogInterface, which) -> {
                                dialogInterface.dismiss();
                                stopRecording();
                                navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                            })
                            .setNegativeButton("Cancel", R.drawable.material_dialog_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                            .build();
                    materialDialog.show();
                } else {
                    navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                }
                break;
            case R.id.record_btn:
                if (isRecording) {
                    //Stop Recording
                    stopRecording();
                } else {
                    //Start Recording
                    if (checkPermissions()) {
                        startRecording();
                    }
                }
                break;
            case R.id.record_cancel_btn:
                @SuppressLint("UseCompatLoadingForDrawables") MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                        .setTitle(String.valueOf(getActivity().getResources().getText(R.string.record_cancel_btn_dialog_title)))
                        .setMessage(String.valueOf(getActivity().getResources().getText(R.string.record_cancel_btn_dialog_message)))
                        .setCancelable(false)
                        .setPositiveButton("Yes", R.drawable.material_dialog_okay, (dialogInterface, which) -> {
                            dialogInterface.dismiss();
                            recordBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.record_btn_stopped, null));
                            isRecording = false;
                            recordTitle.setText(getActivity().getResources().getText(R.string.record_title_default));
                            recordBarsAnimation.setVisibility(View.INVISIBLE);
                            recordCancelBtn.setVisibility(View.GONE);
                            recordTimer.stop();
                            recordTimer.setBase(SystemClock.elapsedRealtime());

                            mediaRecorder.stop();
                            mediaRecorder.release();
                            mediaRecorder = null;
                            File cancelFile = new File(recordPath + "/" + recordedFile);
                            cancelFile.delete();
                            Toast.makeText(getContext(), String.valueOf(getActivity().getResources().getText(R.string.recording_cancelled_toast)), Toast.LENGTH_LONG).show();
                        })
                        .setNegativeButton("Cancel", R.drawable.material_dialog_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                        .build();
                materialDialog.show();
                break;
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void stopRecording() {
        recordBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.record_btn_stopped, null));
        isRecording = false;
        recordTitle.setText(getActivity().getResources().getText(R.string.record_title_default));
        recordBarsAnimation.setVisibility(View.INVISIBLE);
        recordCancelBtn.setVisibility(View.GONE);
        recordTimer.stop();
        recordTimer.setBase(SystemClock.elapsedRealtime());

        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        Toast.makeText(getContext(), String.valueOf(getActivity().getResources().getText(R.string.recording_saved_toast)), Toast.LENGTH_LONG).show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void startRecording() {
        recordPath = getActivity().getExternalFilesDir("/audio/").getAbsolutePath();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA);
        Date now = new Date();

        recordedFile = "SAID_" + formatter.format(now) + ".3gp";

        recordBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.record_btn_recording, null));
        isRecording = true;
        recordCancelBtn.setVisibility(View.VISIBLE);
        recordTimer.setBase(SystemClock.elapsedRealtime());
        recordTimer.start();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        recordTitle.setText(String.format("Recording File....\n%s", recordedFile));
        recordBarsAnimation.setVisibility(View.VISIBLE);

        mediaRecorder.setOutputFile(recordPath + "/" + recordedFile);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAppUpdateManager = AppUpdateManagerFactory.create(getContext());

        installStateUpdatedListener = new InstallStateUpdatedListener() {
            @Override
            public void onStateUpdate(InstallState state) {
                if (state.installStatus() == InstallStatus.DOWNLOADED){
                    popupSnackbarForCompleteUpdate();
                } else if (state.installStatus() == InstallStatus.INSTALLED){
                    if (mAppUpdateManager != null){
                        mAppUpdateManager.unregisterListener(installStateUpdatedListener);
                    }

                } else {
                    Log.i(TAG, "InstallStateUpdatedListener: state: " + state.installStatus());
                }
            }
        };

        mAppUpdateManager.registerListener(installStateUpdatedListener);

        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                try {
                    mAppUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, getActivity(), RC_APP_UPDATE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate();
            } else {
                Log.e(TAG, "checkForAppUpdateAvailability: something else");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "onActivityResult: app download failed");
            }
        }
    }

    private void popupSnackbarForCompleteUpdate() {

        Snackbar snackbar =
                Snackbar.make(
                        getActivity().findViewById(R.id.coordinatorLayout_main),
                        "New update is ready!",
                        Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction("Install", view -> {
            if (mAppUpdateManager != null){
                mAppUpdateManager.completeUpdate();
            }
        });

        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRecording) {
            stopRecording();
        }

        if (mAppUpdateManager != null) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isRecording) {
            stopRecording();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            stopRecording();
        }
    }
}
