package c.taylor.chordprogressionbuilder;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class chordprogressionbuilder2 extends AppCompatActivity {

    private Button[] chordButtons = new Button[8];
    private Button[] progressionButtons = new Button[8];
    private String key;
    private SoundPool soundPool;
    private Button returnButton;
    private EditText BPMText;
    private Map<String, Integer> Sounds = new HashMap<>();
    private String[] notes = new String[14];
    private String[] triad = new String[3];
    private boolean chordSelected;
    private chordQueue queue = new chordQueue(8, 3);
    private String[] tempTriad;
    private Handler myHandler;
    private Runnable r;
    private int index = 0;
    private int[] soundIDsPlaying = new int[3];
    private boolean looped;
    private boolean pause;
    private int delay;
    private ImageButton loopButton;
    private ImageButton pauseButton;
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
        r = new Runnable() {
            @Override
            public void run() {
                stopSounds();
                playChord();
            }
        };

        initializeUI();
        //This sets all of the buttons text to the simplified version of each note to each note in the key
        for (int i = 0; i < chordButtons.length; i++) {
            (chordButtons[i]).setText(getSimplifiedNote(notes[i]));
        }
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

    //This is called when the return button is called, and opens the first screen where you pick a key
    public void changeActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        soundPool.release();
        startActivity(intent);
    }

    //This just initializes all the UI and sets them to variables to manipulate them later
    public void initializeUI(){
        //Connects all the chord buttons
        chordButtons[0] = findViewById(R.id.ChordButton1);
        chordButtons[1] = findViewById(R.id.ChordButton2);
        chordButtons[2] = findViewById(R.id.ChordButton3);
        chordButtons[3] = findViewById(R.id.ChordButton4);
        chordButtons[4] = findViewById(R.id.ChordButton5);
        chordButtons[5] = findViewById(R.id.ChordButton6);
        chordButtons[6] = findViewById(R.id.ChordButton7);
        chordButtons[7] = findViewById(R.id.ChordButton8);
        //Connects the 8 buttons used to play the progression
        progressionButtons[0] = findViewById(R.id.ProgressionButton1);
        progressionButtons[1] = findViewById(R.id.ProgressionButton2);
        progressionButtons[2] = findViewById(R.id.ProgressionButton3);
        progressionButtons[3] = findViewById(R.id.ProgressionButton4);
        progressionButtons[4] = findViewById(R.id.ProgressionButton5);
        progressionButtons[5] = findViewById(R.id.ProgressionButton6);
        progressionButtons[6] = findViewById(R.id.ProgressionButton7);
        progressionButtons[7] = findViewById(R.id.ProgressionButton8);
        BPMText = findViewById(R.id.BPMText);
        loopButton = findViewById(R.id.LoopButton);
        pauseButton = findViewById(R.id.imageButton2);
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

    //Called when a chord button is clicked, it calls addChord, and updates the button to have an outline, showing it was
    //selected
    public void chordClick(View v){
        for(int i = 0; i < 8; i++){
            if(chordButtons[i].getId() == v.getId()) {
                addChord(chordButtons[i]);
                for (int a = 0; a < 8; a++) {
                    if(chordButtons[a].equals(chordButtons[i]))
                        chordButtons[a].setBackground(getResources().getDrawable(R.drawable.selected_button));
                    else
                        chordButtons[a].setBackground(getResources().getDrawable(R.drawable.background));
                }
            }
        }
    }

    //Called when the loop button is pressed, updates the outline around it to show if active or not
    public void changeLoop(View v){
        looped = !looped;
        if(looped)
            loopButton.setBackground(getResources().getDrawable(R.drawable.selected_button));
        else
            loopButton.setBackground(getResources().getDrawable(R.drawable.background));
    }

    //When the pause button is pressed, changes background to show it was selected, then goes away after 1/2 a second
    public void pauseProgression(View v){
        pause = true;
        pauseButton.setBackground(getResources().getDrawable(R.drawable.selected_button));
        looped = false;
        loopButton.setBackground(getResources().getDrawable(R.drawable.background));
        Handler wait = new Handler();
        Runnable delay = new Runnable() {
            @Override
            public void run() {
                pauseButton.setBackground(getResources().getDrawable(R.drawable.background));
            }
        };
        wait.postDelayed(delay, 500);
    }

    //If one of the buttons in the workspace is clicked, check to see if a chord is selected. If it is
    //then you add the selected chord to the progression button that was clicked. If a chord wasnt selected, then
    // remove that chord and reset the text to blank
    public void progressionClick(View v){
        for(int i = 0; i < 8; i++){
            if(progressionButtons[i].getId() == v.getId()){
                if(!chordSelected) {
                    queue.removeChord(i);
                    progressionButtons[i].setText("");
                }
                else{
                    queue.addChord(triad, i);
                    progressionButtons[i].setText(getSimplifiedNote(triad[0]));
                    chordSelected = false;
                    //Once added, all the chord buttons go back to no outline since nothing is selected
                    for(int a = 0; a < 8; a++){
                        chordButtons[a].setBackground(getResources().getDrawable(R.drawable.background));
                    }
                }
            }
        }
    }

    //This method creates a triad based on the chord that was selected, and sets chordSelected equal to true, allowing it
    //to be added to the progression workflow
    public void addChord(Button rootButton) {
        String rootNote = (String)rootButton.getText();
        rootNote = getUnsimplifiedNote(rootNote);
        makeTriad(rootNote);
        chordSelected = true;
    }

    //This method takes the rootNote of the button and creates a 3 note triad based on the key that the user created in the first
    //activity
    public void makeTriad(String rootNote) {
        int startIndex = 0;
        for(int i = 0; i < notes.length/2; i++){
            if(notes[i].equals(rootNote))
                startIndex = i;
        }
        for(int a = 0; a < triad.length; a++){
            int noteIndex = startIndex + (a*2);
            if(noteIndex >= notes.length)
                noteIndex -= (notes.length - 1);
            triad[a] = notes[noteIndex];
        }
    }

    //Accesor method to get the current BPM
    public int getBPM(){
        if(BPMText.getText().toString().equals(""))
            return 60;
        else
            return Integer.parseInt(BPMText.getText().toString());
    }

    //When the playButton is pressed, this method is called, and takes the current BPM and creates the delay between each chord
    public void playButton(View v){
        index = 0;
        pause = false;
        stopSounds();
        int BPM = getBPM();
        delay = (int)((60.0/BPM) *(1000));
        //Toast.makeText(this, "Currently playing " + queue.toString(), Toast.LENGTH_SHORT).show();
        //checks to make sure all of the sounds are loaded before trying to play the progression
        if(loaded) {
            playChord();
            //Calls each chord that is currently in the queue, sepereated by a time of delay, based on the BPM entered
            for (int i = 0; i < queue.getSize(); i++) {
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
        if(index == 0){
            for(int i = 0; i < progressionButtons.length; i++) {
                progressionButtons[i].setBackground(getResources().getDrawable(R.drawable.progression_background));
            }
        }
        else {
            progressionButtons[index - 1].setBackground(getResources().getDrawable(R.drawable.progression_background));
        }
        //The current playing button is given an outline to show what chord is playing
        progressionButtons[index].setBackground(getResources().getDrawable(R.drawable.currently_playing_button));
        tempTriad = queue.getChord(index);
        if(tempTriad[0].equals(" ")){
            //There is a rest
        }
        else{
            //If the progression isn't paused, it goes through the triad created for the chord and plays each note together
            if(!pause) {
                for (int a = 0; a < tempTriad.length; a++) {
                    soundIDsPlaying[a] = soundPool.play(Sounds.get(tempTriad[a]), 1, 1, 1, 0, 1);
                }
            }
        }
        index++;
        //If the index is at the max number of chords, either stops or resets depending on the loop button
        if(index == (queue.getSize() + 1)) {
            index = 0;
            //If the loop button is pressed, then the playButton method is called again, which plays the whole progression again
            if(looped){
                Handler repeat = new Handler();
                Runnable r2 = new Runnable() {
                    @Override
                    public void run() {
                        View view = findViewById(R.id.imageButton);
                        playButton(view);
                    }
                };
                repeat.postDelayed(r2, delay);
            }
        }
    }

    //This method stops all of the sounds going on, to prevent chords from bleeding over each other
    public void stopSounds(){
        for(int i  = 0 ; i < soundIDsPlaying.length; i++){
            soundPool.stop(soundIDsPlaying[i]);
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
