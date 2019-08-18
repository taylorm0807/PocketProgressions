package c.taylor.chordprogressionbuilder;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class chordprogressionbuilder2 extends AppCompatActivity {

    private Button[][] chordButtons = new Button[4][4];
    private Button[] customProgression = new Button[4];
    private chordQueue[] commonProgQueues = new chordQueue[4];
    private ImageButton[] playButtons = new ImageButton[4];
    private ImageButton[] downloadButtons = new ImageButton[4];
    private String key;
    private int progressionIndex = 0;
    private SoundPool soundPool;
    private Button returnButton;
    private EditText BPMText;
    private Map<String, Integer> Sounds = new HashMap<>();
    private String[] notes = new String[14];
    private ArrayList<String> triad = new ArrayList<>();
    private boolean isMinor;
    private ArrayList<String> tempTriad;
    private Handler myHandler;
    private Runnable r;
    private int index = 0;
    private ArrayList<Integer> soundIDsPlaying = new ArrayList<>();
    private int delay;
    private boolean loaded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chordprogressionbuilder2);
        returnButton = (Button)findViewById(R.id.ReturnButton);
        Bundle extraData = getIntent().getExtras();
        myHandler = new Handler();
        key = extraData.getString("key");
        notes = extraData.getStringArray("notes");
        isMinor = extraData.getBoolean("isMinor");
        r = new Runnable() {
            @Override
            public void run() {
                stopSounds();
                playChord();
            }
        };

        initializeUI();
        for(int i = 0; i < commonProgQueues.length; i++){
            commonProgQueues[i] = new chordQueue(4);
        }
        //This sets all of the buttons text to the simplified version of each note to each note in the key
        //TODO: On load, propogate the 4 common progressions with the notes
        loadCommonProgressions();
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                loaded = true;
            }
        });
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeActivity();
            }
        });
    }

    //Fills in the 4 common progressions for the given key
    public void loadCommonProgressions(){
        int[][] progressionIndices;
        if(!isMinor){
            progressionIndices = new int[][]{
                {0,5,3,4},
                {0,3,4,0},
                {0,3,5,4},
                {0,5,1,4}
            };
        }else{
            progressionIndices = new int[][]{
                    {0,5,2,6},
                    {0,3,4,0},
                    {0,6,5,6},
                    {0,5,3,4}
            };
        }
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++) {
                makeTriad(notes[progressionIndices[i][j]]);
                commonProgQueues[i].addChord(triad,j);
                chordButtons[i][j].setText(getSimplifiedNote(commonProgQueues[i].getChord(j).get(0)));
            }
        }
    }

    public void playProgression(View v){
        for(int i = 0; i < 4; i++){
            if(playButtons[i].getId() == v.getId()){
                progressionIndex = i;
                playButton();
            }
        }
    }

    public void playCustomProgression(View v){

    }
    //This is called when the return button is called, and opens the first screen where you pick a key
    public void changeActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        soundPool.release();
        startActivity(intent);
    }

    //This just initializes all the UI and sets them to variables to manipulate them later
    public void initializeUI(){
        //Connects all the chord buttons
        chordButtons[0][0] = findViewById(R.id.Chord1Button1);
        chordButtons[0][1] = findViewById(R.id.Chord1Button2);
        chordButtons[0][2] = findViewById(R.id.Chord1Button3);
        chordButtons[0][3] = findViewById(R.id.Chord1Button4);
        chordButtons[1][0] = findViewById(R.id.Chord2Button1);
        chordButtons[1][1] = findViewById(R.id.Chord2Button2);
        chordButtons[1][2] = findViewById(R.id.Chord2Button3);
        chordButtons[1][3] = findViewById(R.id.Chord2Button4);
        chordButtons[2][0] = findViewById(R.id.Chord3Button1);
        chordButtons[2][1] = findViewById(R.id.Chord3Button2);
        chordButtons[2][2] = findViewById(R.id.Chord3Button3);
        chordButtons[2][3] = findViewById(R.id.Chord3Button4);
        chordButtons[3][0] = findViewById(R.id.Chord4Button1);
        chordButtons[3][1] = findViewById(R.id.Chord4Button2);
        chordButtons[3][2] = findViewById(R.id.Chord4Button3);
        chordButtons[3][3] = findViewById(R.id.Chord4Button4);
        customProgression[0] = findViewById(R.id.CustomChordButton1);
        customProgression[1] = findViewById(R.id.CustomChordButton2);
        customProgression[2] = findViewById(R.id.CustomChordButton3);
        customProgression[3] = findViewById(R.id.CustomChordButton4);
        BPMText = findViewById(R.id.BPMText);
        //Initialize the play and download buttons
        playButtons = new ImageButton[]{findViewById(R.id.Chord1Play), findViewById(R.id.Chord2Play), findViewById(R.id.Chord3Play), findViewById(R.id.Chord4Play)};
        downloadButtons = new ImageButton[]{findViewById(R.id.Chord1Save), findViewById(R.id.Chord2Save), findViewById(R.id.Chord3Save), findViewById(R.id.Chord4Save)};
        //Initializes the sound pool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
            soundPool = new SoundPool.Builder().setMaxStreams(6).setAudioAttributes(audioAttributes).build();
        } else {
            soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        }
        createSounds();
    }

    //This fills the sound pool with all of the local files to play all of the notes needed
    public void createSounds(){
        Sounds.put("A4", soundPool.load(this, R.raw.piano_a4, 1));
        Sounds.put("A#4", soundPool.load(this, R.raw.piano_as4, 1));
        Sounds.put("Ab4", soundPool.load(this, R.raw.piano_ab4, 1));
        Sounds.put("B4", soundPool.load(this, R.raw.piano_b4, 1));
        Sounds.put("Bb4", soundPool.load(this, R.raw.piano_bb4, 1));
        Sounds.put("C4", soundPool.load(this, R.raw.piano_c4, 1));
        Sounds.put("C#4", soundPool.load(this, R.raw.piano_cs4, 1));
        Sounds.put("D4", soundPool.load(this, R.raw.piano_d4, 1));
        Sounds.put("Db4", soundPool.load(this, R.raw.piano_db4, 1));
        Sounds.put("D#4", soundPool.load(this, R.raw.piano_ds4, 1));
        Sounds.put("E4", soundPool.load(this, R.raw.piano_e4, 1));
        Sounds.put("Eb4", soundPool.load(this, R.raw.piano_eb4, 1));
        Sounds.put("F4", soundPool.load(this, R.raw.piano_f4, 1));
        Sounds.put("F#4", soundPool.load(this, R.raw.piano_fs4, 1));
        Sounds.put("G4", soundPool.load(this, R.raw.piano_g4, 1));
        Sounds.put("Gb4", soundPool.load(this, R.raw.piano_gb4, 1));
        Sounds.put("G#4", soundPool.load(this, R.raw.piano_gs4, 1));

        Sounds.put("A5", soundPool.load(this, R.raw.piano_a5, 1));
        Sounds.put("A#5", soundPool.load(this, R.raw.piano_as5, 1));
        Sounds.put("Ab5", soundPool.load(this, R.raw.piano_ab5, 1));
        Sounds.put("B5", soundPool.load(this, R.raw.piano_b5, 1));
        Sounds.put("Bb5", soundPool.load(this, R.raw.piano_bb5, 1));
        Sounds.put("C5", soundPool.load(this, R.raw.piano_c5, 1));
        Sounds.put("C#5", soundPool.load(this, R.raw.piano_cs5, 1));
        Sounds.put("D5", soundPool.load(this, R.raw.piano_d5, 1));
        Sounds.put("Db5", soundPool.load(this, R.raw.piano_db5, 1));
        Sounds.put("D#5", soundPool.load(this, R.raw.piano_ds5, 1));
        Sounds.put("E5", soundPool.load(this, R.raw.piano_e5, 1));
        Sounds.put("Eb5", soundPool.load(this, R.raw.piano_eb5, 1));
        Sounds.put("F5", soundPool.load(this, R.raw.piano_f5, 1));
        Sounds.put("F#5", soundPool.load(this, R.raw.piano_fs5, 1));
        Sounds.put("G5", soundPool.load(this, R.raw.piano_g5, 1));
        Sounds.put("Gb5", soundPool.load(this, R.raw.piano_gb5, 1));
        Sounds.put("G#5", soundPool.load(this, R.raw.piano_gs5, 1));
    }


    public void chordClick(View v){
    }

    //This method takes the rootNote of the button and creates a 3 note triad based on the key that the user created in the first
    //activity
    public void makeTriad(String rootNote) {
        int startIndex = 0;
        triad = new ArrayList<>();
        for(int i = 0; i < notes.length/2; i++){
            if(notes[i].equals(rootNote))
                startIndex = i;
        }
        for(int a = 0; a < 3; a++){
            int noteIndex = startIndex + (a*2);
            if(noteIndex >= notes.length)
                noteIndex -= (notes.length - 1);
            triad.add(notes[noteIndex]);
        }
    }

    //Accessor method to get the current BPM
    public int getBPM(){
        if(BPMText.getText().toString().equals(""))
            return 60;
        else
            return Integer.parseInt(BPMText.getText().toString());
    }

    //When the playButton is pressed, this method is called, and takes the current BPM and creates the delay between each chord
    public void playButton(){
        index = 0;
        stopSounds();
        int BPM = getBPM();
        delay = (int)((60.0/BPM) *(1000));
        //checks to make sure all of the sounds are loaded before trying to play the progression
        if(loaded) {
            playChord();
            //Calls each chord that is currently in the queue, sepereated by a time of delay, based on the BPM entered
            for (int i = 0; i < commonProgQueues[progressionIndex].getSize() - 1; i++) {
                myHandler.postDelayed(r, delay * (i + 1));
            }
        }
        else{
            Toast.makeText(this, "The sounds need to finish loading in, try again in a few seconds.", Toast.LENGTH_LONG).show();
        }
    }

    //This is the method that plays the progression of chords that the user has added to the workspace
    public void playChord() {
        //This logic deals with the background of the buttons and making sure that the button is only selected if
        //it is currently playing, so the previous button is set back to normal
        //TODO: Change This Stuff to fit new app layout
//        if(index == 0){
//            for(int i = 0; i < progressionButtons.length; i++) {
//                progressionButtons[i].setBackground(getResources().getDrawable(R.drawable.progression_background));
//            }
//        }
//        else {
//            progressionButtons[index - 1].setBackground(getResources().getDrawable(R.drawable.progression_background));
//        }
//        //The current playing button is given an outline to show what chord is playing
//        progressionButtons[index].setBackground(getResources().getDrawable(R.drawable.currently_playing_button));


        tempTriad = commonProgQueues[progressionIndex].getChord(index);
        if(tempTriad.get(0).equals(" ")){
            //There is a rest
        }
        else{
            soundIDsPlaying = new ArrayList<>();
            //If the progression isn't paused, it goes through the triad created for the chord and plays each note together
            for (int a = 0; a < tempTriad.size(); a++) {
                soundIDsPlaying.add(soundPool.play(Sounds.get(tempTriad.get(a)), 1, 1, 1, 0, 1));
            }
        }
        index++;
        //If the index is at the max number of chords, either stops or resets depending on the loop button
        if(index == (commonProgQueues[0].getSize() + 1)) {
            index = 0;
        }
    }

    //This method stops all of the sounds going on, to prevent chords from bleeding over each other
    public void stopSounds(){
        for(int i  = 0 ; i < soundIDsPlaying.size(); i++){
            soundPool.stop(soundIDsPlaying.get(i));
        }
    }

    //This helper method takes a note like C4 and returns C, to make it more user-friendly
    public String getSimplifiedNote(String noteWithPos) {
        return noteWithPos.substring(0, noteWithPos.length() - 1);
    }

    //This helper method takes a user-friendly note like C and unsimplifies it to C4 or C5 depending on the key to allow
    //for the correct note to be played and used in other methods
    public String getUnsimplifiedNote(String noteWithoutPos){
        for(int i = 0; i < notes.length; i++){
            if(getSimplifiedNote(notes[i]).equals(noteWithoutPos))
                return notes[i];
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }
}
