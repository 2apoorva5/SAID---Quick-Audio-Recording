package com.developerdepository.said;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class AudioListFragment extends Fragment implements View.OnClickListener {

    private ConstraintLayout audioPlayerSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private ImageButton audioListClose;
    private ImageView audioListEmptyImg;
    private TextView audioListEmptyText;
    private RecyclerView audioList;

    private ArrayList<File> audioFiles;

    private ListAudioAdapter listAudioAdapter;

    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private File fileToPlay = null;

    //Audio Player
    private ImageButton audioPlayBtn, audioNextBtn, audioPreviousBtn;
    private TextView audioPlayerTitle, audioFileName;
    private SeekBar audioPlayerSeekBar;
    private Handler seekBarHandler;
    private Runnable updateSeekBar;

    public AudioListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        audioListClose = view.findViewById(R.id.audio_list_close);
        audioListEmptyImg = view.findViewById(R.id.audio_list_empty_img);
        audioListEmptyText = view.findViewById(R.id.audio_list_empty_text);
        audioList = view.findViewById(R.id.audio_list);
        audioPlayerSheet = view.findViewById(R.id.audio_player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(audioPlayerSheet);
        audioPlayBtn = view.findViewById(R.id.audio_player_play_btn);
        audioNextBtn = view.findViewById(R.id.audio_player_next_btn);
        audioPreviousBtn = view.findViewById(R.id.audio_player_previous_btn);
        audioPlayerTitle = view.findViewById(R.id.audio_player_title);
        audioFileName = view.findViewById(R.id.audio_player_file_name);
        audioPlayerSeekBar = view.findViewById(R.id.audio_player_seek_bar);

        String path = getActivity().getExternalFilesDir("/audio/").getAbsolutePath();
        File directory = new File(path);
        File[] listFiles = directory.listFiles();
        audioFiles = new ArrayList<>(Arrays.asList(listFiles));

        listAudioAdapter = new ListAudioAdapter(audioFiles);

        audioList.setHasFixedSize(true);
        audioList.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList.setAdapter(listAudioAdapter);

        if (listAudioAdapter.getItemCount() > 0) {
            listAudioAdapter.notifyDataSetChanged();
            audioListEmptyImg.setVisibility(View.GONE);
            audioListEmptyText.setVisibility(View.GONE);
        } else if (listAudioAdapter.getItemCount() == 0) {
            listAudioAdapter.notifyDataSetChanged();
            audioListEmptyImg.setVisibility(View.VISIBLE);
            audioListEmptyText.setVisibility(View.VISIBLE);
        }

        audioListClose.setOnClickListener(this);
        audioPlayBtn.setOnClickListener(this);
        audioPreviousBtn.setOnClickListener(this);
        audioNextBtn.setOnClickListener(this);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        audioPlayerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(0);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fileToPlay != null) {
                    int progress = seekBar.getProgress();
                    mediaPlayer.seekTo(progress);
                    resumeAudio();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio_list_close:
                getActivity().onBackPressed();
                break;
            case R.id.audio_player_play_btn:
                if (isPlaying) {
                    pauseAudio();
                } else {
                    if (fileToPlay != null) {
                        resumeAudio();
                    }
                }
                break;
            case R.id.audio_player_next_btn:
                if (mediaPlayer.isPlaying()) {
                    if (mediaPlayer.getCurrentPosition() + 2000 <= mediaPlayer.getDuration()) {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 2000);
                    } else {
                        mediaPlayer.seekTo(mediaPlayer.getDuration());
                    }
                }
                break;
            case R.id.audio_player_previous_btn:
                if (mediaPlayer.isPlaying()) {
                    if (mediaPlayer.getCurrentPosition() - 2000 >= 0) {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 2000);
                    } else {
                        mediaPlayer.seekTo(0);
                    }
                }
                break;
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void pauseAudio() {
        audioPlayerTitle.setText(getActivity().getResources().getText(R.string.audio_player_title_paused));
        mediaPlayer.pause();
        audioPlayBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.audio_btn_play, null));
        isPlaying = false;
        seekBarHandler.removeCallbacks(updateSeekBar);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void resumeAudio() {
        audioPlayerTitle.setText(getActivity().getResources().getText(R.string.audio_player_title_playing));
        mediaPlayer.start();
        audioPlayBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.audio_btn_pause, null));
        isPlaying = true;
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekBar, 0);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void stopAudio() {
        audioPlayBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.audio_btn_play, null));
        isPlaying = false;
        mediaPlayer.stop();
        seekBarHandler.removeCallbacks(updateSeekBar);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void playAudio(File fileToPlay) {
        mediaPlayer = new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        audioPlayBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.audio_btn_pause, null));
        audioFileName.setText(fileToPlay.getName());
        audioPlayerTitle.setText(getActivity().getResources().getText(R.string.audio_player_title_playing));
        isPlaying = true;
        mediaPlayer.setOnCompletionListener(mp -> {
            audioPlayBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.audio_btn_play, null));
            isPlaying = false;
            audioPlayerTitle.setText(getActivity().getResources().getText(R.string.audio_player_title_finished));
        });

        audioPlayerSeekBar.setMax(mediaPlayer.getDuration());

        seekBarHandler = new Handler();
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekBar, 0);
    }

    private void updateRunnable() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                audioPlayerSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                seekBarHandler.postDelayed(this, 0);
            }
        };
    }

    public class ListAudioAdapter extends RecyclerView.Adapter<ListAudioAdapter.AudioViewHolder> {

        private ArrayList<File> fileArrayList;
        private TimeAgo timeAgo;

        public ListAudioAdapter(ArrayList<File> fileArrayList) {
            this.fileArrayList = fileArrayList;
        }

        @NonNull
        @Override
        public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_audio_list_item, parent, false);
            timeAgo = new TimeAgo();
            return new AudioViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
            holder.listItemTitle.setText(fileArrayList.get(position).getName());
            holder.listItemDate.setText(timeAgo.getTimeAgo(fileArrayList.get(position).lastModified()));
        }

        @Override
        public int getItemCount() {
            return fileArrayList.size();
        }

        public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private ImageView listItemPlayBtn;
            private ImageButton listItemDelete;
            private TextView listItemTitle, listItemDate;

            public AudioViewHolder(@NonNull View itemView) {
                super(itemView);

                listItemPlayBtn = itemView.findViewById(R.id.list_item_play_btn);
                listItemDelete = itemView.findViewById(R.id.list_item_delete);
                listItemTitle = itemView.findViewById(R.id.list_item_title);
                listItemDate = itemView.findViewById(R.id.list_item_date);

                listItemPlayBtn.setOnClickListener(this);
                listItemDelete.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.list_item_play_btn:
                        fileToPlay = fileArrayList.get(getAdapterPosition());
                        if (isPlaying) {
                            stopAudio();
                            playAudio(fileToPlay);
                        } else {
                            playAudio(fileToPlay);
                        }
                        break;
                    case R.id.list_item_delete:
                        if (fileArrayList.get(getAdapterPosition()) != fileToPlay) {
                            MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                                    .setTitle(String.valueOf(getActivity().getResources().getText(R.string.delete_file_dialog_title)))
                                    .setMessage(String.valueOf(getActivity().getResources().getText(R.string.delete_file_dialog_message)))
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", R.drawable.material_dialog_okay, (dialogInterface, which) -> {
                                        dialogInterface.dismiss();
                                        fileArrayList.get(getAdapterPosition()).delete();
                                        fileArrayList.remove(getAdapterPosition());
                                        notifyDataSetChanged();
                                        notifyItemRemoved(getAdapterPosition());
                                        notifyItemRangeChanged(getAdapterPosition(), fileArrayList.size());
                                        Toast.makeText(getContext(), String.valueOf(getActivity().getResources().getText(R.string.delete_file_toast)), Toast.LENGTH_SHORT).show();
                                    })
                                    .setNegativeButton("Cancel", R.drawable.material_dialog_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                                    .build();
                            materialDialog.show();
                        } else {
                            Toast.makeText(getContext(), String.valueOf(getActivity().getResources().getText(R.string.cannot_delete_file_toast)), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isPlaying) {
            pauseAudio();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isPlaying) {
            pauseAudio();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isPlaying) {
            pauseAudio();
        }
    }
}
