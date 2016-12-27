package comvoroninlevan.httpsgithub.symplymusic;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {

    private ArrayList<Songs> songsArrayList;
    private SongsAdapter mAdapter;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private TextView title;
    private ImageButton playPause;
    private ImageButton play;
    private TextView timePass;
    private TextView timeLeft;
    private SeekBar seekBar;
    private ImageView shuffle;
    private ImageView repeat;
    ListView listOfSongs;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private int songPosition;
    private double currentTime;
    private Handler handler = new Handler();

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {

            releasePlayer();
            if(isShuffle){
                Random random = new Random();
                songPosition = random.nextInt(songsArrayList.size());
            }else if(isRepeat){
                songPosition += 0;
            }else {
                songPosition++;
            }

            if(songPosition > songsArrayList.size()-1){
                songPosition = 0;
            }
            playSong(songPosition);
        }
    };

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int changeFocus) {
            if(changeFocus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    changeFocus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){
                mediaPlayer.pause();
                //!!!!!!!!!!!!!!!!!!!!!
                //mediaPlayer.seekTo(0);
            }else if(changeFocus == AudioManager.AUDIOFOCUS_GAIN){
                mediaPlayer.start();
            }else if(changeFocus == AudioManager.AUDIOFOCUS_LOSS){
                releasePlayer();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        timePass = (TextView)findViewById(R.id.timePass);
        timeLeft = (TextView)findViewById(R.id.timeLeft);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setClickable(false);
        shuffle = (ImageView)findViewById(R.id.shuffle);
        shuffle.setImageResource(0);
        repeat = (ImageView)findViewById(R.id.repeat);
        repeat.setImageResource(0);
        play = (ImageButton)findViewById(R.id.play);
        play.setImageResource(R.drawable.playgold);

        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);

        listOfSongs = (ListView)findViewById(R.id.listOfSongs);
        songsArrayList = new ArrayList<>();

        makePermissionRequest();
        //getSongsFromDevice();

        mAdapter = new SongsAdapter(this, songsArrayList);
        listOfSongs.setAdapter(mAdapter);
        title = (TextView)findViewById(R.id.songTitle);


        listOfSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                play.setVisibility(View.GONE);
                playPause.setVisibility(View.VISIBLE);
                songPosition = position;
                playSong(songPosition);
            }
        });

        /*
        -------------------BUTTONS---------------------------------
         */

        playPause = (ImageButton)findViewById(R.id.play_pause);
        playPause.setVisibility(View.GONE);
        playPause.setImageResource(R.drawable.playgold);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPauseSong();
            }
        });

        ImageButton previousSong = (ImageButton)findViewById(R.id.previousSong);
        previousSong.setImageResource(R.drawable.skip_previous_gold);
        previousSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play.setVisibility(View.GONE);
                playPause.setVisibility(View.VISIBLE);
                previousSong();
            }
        });

        ImageButton nextSong = (ImageButton)findViewById(R.id.nextSong);
        nextSong.setImageResource(R.drawable.skip_next_gold);
        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play.setVisibility(View.GONE);
                playPause.setVisibility(View.VISIBLE);
                nextSong();
            }
        });
    }

    public void  makePermissionRequest(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }else{
            getSongsFromDevice();
        }
    }

    @NonNull
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSongsFromDevice();

                }else{
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    }
                }

            }

        }
    }

    /*
    -----------------------------RETRIEVING_DATA--------------------------------------
     */

    public void getSongsFromDevice(){

        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);

        if(songCursor != null && songCursor.moveToFirst()){

            int songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);


            do {
                long currentId = songCursor.getLong(songId);
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                String currentData = songCursor.getString(songData);

                songsArrayList.add(new Songs(currentId, currentTitle, currentArtist, currentData));
                songsArrayList.remove(new Songs(currentId, currentTitle, currentArtist, currentData));
            }while(songCursor.moveToNext());
            songCursor.close();
        }
    }

    private void releasePlayer(){

        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }

    /*
    --------------------------------MANAGING_PLAYER-----------------------------------
     */

    public void playFirstSong(View view){

        if(songsArrayList.size() != 0) {
            play.setVisibility(View.GONE);
            playPause.setVisibility(View.VISIBLE);
            playPause.setImageResource(R.drawable.pausegold);
            songPosition = 0;
            playSong(songPosition);
        }else{
            Toast.makeText(PlayerActivity.this, "No songs", Toast.LENGTH_SHORT).show();
        }
    }

    public void playSong(int position){
        releasePlayer();
        Songs currentSong = mAdapter.getItem(position);
        Uri songUri = Uri.parse(currentSong.getData());
        title.setText(currentSong.getSongTitle());
        int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            playPause.setImageResource(R.drawable.pausegold);
            mediaPlayer = MediaPlayer.create(PlayerActivity.this, songUri);
            mediaPlayer.start();

            notification(currentSong);

            double totalTime = mediaPlayer.getDuration();
            currentTime = mediaPlayer.getCurrentPosition();

            timePass.setText(String.format("%02d : %02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) currentTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) currentTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    currentTime))));

            timeLeft.setText(String.format("%02d : %02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) totalTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) totalTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    totalTime))));

            seekBar.setProgress((int)currentTime);
            seekBar.setMax((int)totalTime);
            handler.postDelayed(timeUpdater, 100);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean userInput) {
                    if(userInput) mediaPlayer.seekTo(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mediaPlayer.setOnCompletionListener(mCompletionListener);
        }
    }
    public void playPauseSong(){

        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPause.setImageResource(R.drawable.playgold);
        }else{
            mediaPlayer.start();
            playPause.setImageResource(R.drawable.pausegold);
        }
    }
    public void nextSong(){

        if(songsArrayList.size() != 0) {
            songPosition++;
            if (songPosition > songsArrayList.size() - 1) {
                songPosition = 0;
            }
            playSong(songPosition);
        }else{
            Toast.makeText(PlayerActivity.this, "No songs", Toast.LENGTH_SHORT).show();
        }
    }
    public void previousSong(){

        if(songsArrayList.size() != 0) {
            songPosition--;
            if (songPosition < 0) {
                songPosition = songsArrayList.size() - 1;
            }
            playSong(songPosition);
        }else{
            Toast.makeText(PlayerActivity.this, "No songs", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    -----------------------------MENU----------------------------------------
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_shuffle:
                shuffle();
                return true;
            case R.id.action_repeat:
                repeat();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    ------------------------SUFFLE_REPEAT---------------------------------------
     */

    public void shuffle(){

        if(!isShuffle){
            isShuffle = true;
            isRepeat = false;
            shuffle.setImageResource(R.drawable.shufflegold);
            repeat.setImageResource(0);
        }else{
            isShuffle = false;
            shuffle.setImageResource(0);
        }
    }
    public void repeat(){

        if(!isRepeat){
            isRepeat = true;
            isShuffle = false;
            repeat.setImageResource(R.drawable.repeatgold);
            shuffle.setImageResource(0);
        }else{
            isRepeat = false;
            repeat.setImageResource(0);
        }
    }

    private Runnable timeUpdater = new Runnable() {
        @Override
        public void run() {
            currentTime = mediaPlayer.getCurrentPosition();
            timePass.setText(String.format("%02d : %02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) currentTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) currentTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    currentTime))));

            seekBar.setProgress((int)currentTime);
            handler.postDelayed(this, 100);
        }
    };

    //-------------------------------------NOTIFICATION-----------------------------------

    private void notification(Songs currentSong){

        Intent notificationIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.playgold)
                .setTicker(currentSong.getSongTitle())
                .setOngoing(true)
                .setContentTitle("Now Playing")
                .setContentText(currentSong.getSongTitle());

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

}
