package c.taylor.chordprogressionbuilder;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.TextAppearanceSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class chordprogressionbuilder2 extends AppCompatActivity {

    private Button[][] chordButtons = new Button[4][4];
    private Spinner[] customProgression = new Spinner[4];
    private chordQueue[] commonProgQueues = new chordQueue[4];
    private chordQueue customQueue;
    private LinearLayout[][] buttonLayouts = new LinearLayout[5][4];
    private ImageButton[] playButtons = new ImageButton[4];
    private ImageButton[] downloadButtons = new ImageButton[4];
    private String key;
    private ArrayList<String> alteringChord = new ArrayList<>();
    private String alteringRoot = "";
    private int[] alteringButtonPos = new int[2];
    private int progressionIndex = 0;
    private boolean playCustom = false;
    private SoundPool soundPool;
    private Button returnButton;
    private EditText BPMText;
    private Map<String, Integer> Sounds = new HashMap<>();
    private String[] notes = new String[14];
    private String[] spinnerNotes = new String[7];
    private ArrayList<String> currentChord = new ArrayList<>();
    private boolean isMinor;
    private ArrayList<String> tempTriad;
    private Handler myHandler;
    private Runnable r;
    private Dialog popupDialog;
    private int index = 0;
    private ArrayList<Integer> soundIDsPlaying = new ArrayList<>();
    private int delay;
    private boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chordprogressionbuilder2);
        popupDialog = new Dialog(this);
        returnButton = findViewById(R.id.ReturnButton);
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
        for(int i = 0; i < spinnerNotes.length; i++){
            spinnerNotes[i] = getSimplifiedNote(notes[i]);
        }
        customQueue = new chordQueue(4);
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
        for(int i = 0; i < customProgression.length; i++){
            customProgression[i].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int queuePos = 0;
                    for(int i = 0; i < customProgression.length; i++){
                        if(parent.getId() == customProgression[i].getId()){
                            queuePos = i;
                        }
                    }
                    makeChord(getUnsimplifiedNote(parent.getItemAtPosition(position).toString()), 3);
                    customQueue.addChord(currentChord, queuePos);
                    ((TextView) parent.getChildAt(0)).setTextSize(24);
                    ((TextView) parent.getChildAt(0)).setGravity(Gravity.CENTER);
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }

            });
            customProgression[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    chordHold(view);
                    return true;
                }
            });
        }
        for(int r = 0; r < chordButtons.length; r++){
            for(int c = 0; c < chordButtons[0].length; c++){
                chordButtons[r][c].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        chordHold(view);
                        return true;
                    }
                });
            }
        }
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
                makeChord(notes[progressionIndices[i][j]], 3);
                commonProgQueues[i].addChord(currentChord,j);
                chordButtons[i][j].setText(getSimplifiedNote(commonProgQueues[i].getChord(j).get(0)));
            }
        }
    }

    public void playProgression(View v){
        for(int i = 0; i < 4; i++){
            if(playButtons[i].getId() == v.getId()){
                progressionIndex = i;
                playCustom = false;
                playButton();
            }
        }
    }

    public void playCustomProgression(View v){
        playCustom = true;
        progressionIndex = 4;
        playButton();
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
        for(int i = 0; i < customProgression.length; i++){
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, spinnerNotes);
            customProgression[i].setAdapter(spinnerArrayAdapter);

        }
        buttonLayouts = new LinearLayout[][]{
                {findViewById(R.id.Chord1Key1), findViewById(R.id.Chord1Key2), findViewById(R.id.Chord1Key3), findViewById(R.id.Chord1Key4)},
                {findViewById(R.id.Chord2Key1), findViewById(R.id.Chord2Key2), findViewById(R.id.Chord2Key3), findViewById(R.id.Chord2Key4)},
                {findViewById(R.id.Chord3Key1), findViewById(R.id.Chord3Key2), findViewById(R.id.Chord3Key3), findViewById(R.id.Chord3Key4)},
                {findViewById(R.id.Chord4Key1), findViewById(R.id.Chord4Key2), findViewById(R.id.Chord4Key3), findViewById(R.id.Chord4Key4)},
                {findViewById(R.id.Chord5Key1), findViewById(R.id.Chord5Key2), findViewById(R.id.Chord5Key3), findViewById(R.id.Chord5Key4)},
        };
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


    //Possible Feature: on click of common chord progressions, play the clicked chord
    public void chordClick(View v){
        //Toast.makeText(this, "Button press", Toast.LENGTH_SHORT).show();
    }

    //On holding of a chord, opens the popup window to alter the chords attributes if so desired
    public void chordHold(View v){
        Integer tgtProgression = null;
        Integer tgtChord = null;
        for(int i = 0; i < customProgression.length; i++){
            if(v.getId() == customProgression[i].getId()){
                tgtProgression = -1;
                tgtChord = i;
                alteringRoot = customProgression[i].getSelectedItem().toString();
            }
        }
        if(tgtProgression == null){
            for(int r = 0; r < chordButtons.length; r++){
                for(int c = 0; c < chordButtons[r].length; c++ ){
                    if(v.getId() == chordButtons[r][c].getId()){
                        tgtProgression = r;
                        tgtChord = c;
                        alteringRoot = chordButtons[r][c].getText().toString();
                    }
                }
            }
        }
        if(tgtProgression == -1){
            alteringChord = customQueue.getChord(tgtChord);
        }else{
            alteringChord = commonProgQueues[tgtProgression].getChord(tgtChord);
        }
        alteringButtonPos = new int[]{tgtProgression, tgtChord};
        popupDialog.setContentView(R.layout.chord_popout_window);
        popupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupDialog.show();
    }

    public void radioInversionChange(View v){
        String buttonText = ((RadioButton)v).getText().toString();
        if(alteringChord.size() == 3) {
            makeChord(alteringRoot, 3);
            alteringChord = currentChord;
        }else{
            makeChord(alteringRoot, 4);
            alteringChord = currentChord;
        }
        String temp;
        switch (buttonText){
            default:
                //do nothing, already set to root triad position
                break;
            case "1st Inversion":
                temp = alteringChord.get(1);
                alteringChord.set(1, alteringChord.get(0));
                alteringChord.set(0, temp);
                break;
            case "2nd Inversion":
                temp = alteringChord.get(2);
                alteringChord.set(2, alteringChord.get(1));
                alteringChord.set(1, alteringChord.get(0));
                alteringChord.set(0, temp);
                break;
            case "3rd Inversion":
                if(alteringChord.size() == 3) {
                    Toast.makeText(this, "uhoh, it isn't a 7th chord!", Toast.LENGTH_SHORT).show();
                }else{
                    temp = alteringChord.get(3);
                    alteringChord.set(3, alteringChord.get(2));
                    alteringChord.set(2, alteringChord.get(1));
                    alteringChord.set(1, alteringChord.get(0));
                    alteringChord.set(0, temp);
                }
                break;
        }
        if(alteringButtonPos[0] == -1){
            customQueue.addChord(alteringChord, alteringButtonPos[1]);
        }else{
            commonProgQueues[alteringButtonPos[0]].addChord(alteringChord, alteringButtonPos[1]);
        }

    }

    public void radioTypeChange(View v){
        String buttonText = ((RadioButton)v).getText().toString();
        switch (buttonText){
            default:
                makeChord(alteringRoot, 3);
                alteringChord = currentChord;
                break;
            case "7th Chord":
                makeChord(alteringRoot, 4);
                alteringChord = currentChord;
                break;
        }
        if(alteringButtonPos[0] == -1){
            customQueue.addChord(alteringChord, alteringButtonPos[1]);
        }else{
            commonProgQueues[alteringButtonPos[0]].addChord(alteringChord, alteringButtonPos[1]);
        }
    }

    //This method takes the rootNote of the button and creates a 3 note triad based on the key that the user created in the first
    //activity
    public void makeChord(String rootNote, int chordSize) {
        int startIndex = 0;
        currentChord = new ArrayList<>();
        for(int i = 0; i < notes.length/2; i++){
            if(notes[i].equals(rootNote))
                startIndex = i;
        }
        for(int a = 0; a < chordSize; a++){
            int noteIndex = startIndex + (a*2);
            if(noteIndex >= notes.length)
                noteIndex -= (notes.length - 1);
            currentChord.add(notes[noteIndex]);
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
        int length = customQueue.getSize() - 1;
        if(!playCustom){
            length = commonProgQueues[progressionIndex].getSize() - 1;
        }
        //checks to make sure all of the sounds are loaded before trying to play the progression
        if(loaded) {
            playChord();
            //Calls each chord that is currently in the queue, sepereated by a time of delay, based on the BPM entered
            for (int i = 0; i < length; i++) {
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
//        if(index == 0){
//            for(int i = 0; i < 4; i++) {
//                buttonLayouts[progressionIndex][i].setBackground(getResources().getDrawable(R.drawable.chord_button));
//            }
//        }
//        else {
//            buttonLayouts[progressionIndex][index - 1].setBackground(getResources().getDrawable(R.drawable.chord_button));
//        }
//        //The current playing button is given an outline to show what chord is playing
//        buttonLayouts[progressionIndex][index].setBackground(getResources().getDrawable(R.drawable.selected_button));

        if(!playCustom){
            tempTriad = commonProgQueues[progressionIndex].getChord(index);
        }else{
            tempTriad = customQueue.getChord(index);
        }
        soundIDsPlaying = new ArrayList<>();
        //Go through the triad created for the chord and plays each note together
        for (int a = 0; a < tempTriad.size(); a++) {
            soundIDsPlaying.add(soundPool.play(Sounds.get(tempTriad.get(a).toUpperCase()), 1, 1, 1, 0, 1));
        }
        index++;
        if(index > 4) {
            index = 0;
            playCustom = false;
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
