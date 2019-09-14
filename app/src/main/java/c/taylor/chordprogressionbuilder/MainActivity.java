package c.taylor.chordprogressionbuilder;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView KeyHeader;
    private Spinner KeySpinner;
    private CheckBox minorCheck;
    private Button continueButton;
    private Button confirmationButton;
    private TextView chordQualityTextView;
    private TextView triadNotesTextView;
    private Button[] chordButtons = new Button[8];
    private LinearLayout[] buttonLayouts = new LinearLayout[8];
    private String key = "";
    private boolean loaded;
    private SoundPool soundPool;
    private Map<String, Integer> Sounds = new HashMap<>();
    private static String[] sharpNotes = new String[] {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private static String[] flatNotes = new String[] {"C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"};
    private static String[][] NOTES = new String[2][24];
    private String[] notes = new String[14];
    private ArrayList<String> triad = new ArrayList<>();
    private boolean isMinor;
    ArrayAdapter<CharSequence> adapter;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();
        initializeNotes();
        adapter = ArrayAdapter.createFromResource(this, R.array.keys, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        KeySpinner.setAdapter(adapter);
        if(getIntent().getExtras() != null){
            Bundle data = getIntent().getExtras();
            key = data.getString("key");
            notes = data.getStringArray("notes");
            for (int i = 0; i < chordButtons.length; i++) {
                (chordButtons[i]).setText(getSimplifiedNote(notes[i]));
            }
        }
        KeySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                key = parent.getItemAtPosition(position).toString() + "4";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        //The confirmation button method when clicked calls this method
        confirmationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //If the key isn't blank, then the notes are created and the chord buttons are updated with the text of each
                //note in the key
                if(key != null) {
                    isMinor = minorCheck.isChecked();
                    setNotes();
                    for (int i = 0; i < chordButtons.length; i++) {
                        (chordButtons[i]).setText(getSimplifiedNote(notes[i]));
                    }
                    String headerText = "Chords in " + getSimplifiedNote(key);
                    if(isMinor){
                        headerText += " Minor";
                    }else{
                        headerText += " Major";
                    }
                    KeyHeader.setText(headerText + ':');
                }

            }
        });
        //sets the onclick for the confirmation button, opening the next activity
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextActivity();
            }
        });

        dl = (DrawerLayout)findViewById(R.id.activity_main);
        t = new ActionBarDrawerToggle(this, dl,R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();

        nv = (NavigationView)findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.saved_progressions:
                        openStoredProgressions();
                        break;
                    case R.id.progression_builder:
                        reloadPage();
                        break;
                    default:
                        return true;
                }
                return true;

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Toast.makeText(MainActivity.this, "Test", Toast.LENGTH_LONG).show();
        if(t.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
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

    //pre: make sure that there is some type of key that is generated
    public void openNextActivity(){
        if(chordButtons[0].getText().equals("")){
            Toast.makeText(this, "Pick a key before you begin building your progression!", Toast.LENGTH_LONG).show();
        }
        else {
            stopSounds();
            Intent intent = new Intent(this, chordprogressionbuilder2.class);
            intent.putExtra("key", key);
            intent.putExtra("notes", notes);
            intent.putExtra("isMinor", isMinor);
            soundPool.release();
            startActivity(intent);
        }
    }

    public void openStoredProgressions(){
        stopSounds();
        Intent intent = new Intent(this, storedprogressions.class);
        soundPool.release();
        startActivity(intent);
    }

    public void reloadPage(){
        stopSounds();
        Intent intent = new Intent(this, MainActivity.class);
        soundPool.release();
        startActivity(intent);
    }

    //This method finds the ID of the button that was pressed and passes it to the method that plays the chord
    public void chordClick(View v){
        for(int i = 0; i < 8; i++){
            if(chordButtons[i].getId() == v.getId()){
                if(loaded && (chordButtons[i].getText()).length() > 0) {
                    stopSounds();
                    playChord(chordButtons[i]);
                    openInfoBox(chordButtons[i]);
                    buttonLayouts[i].setBackground(getResources().getDrawable(R.drawable.selected_button));
                    buttonLayouts[i].setPadding(0,0,0,0);
                }
            }else{
                buttonLayouts[i].setBackground(getResources().getDrawable(R.drawable.chord_button));
                buttonLayouts[i].setPadding(0,0,0,0);
            }
        }
    }

    //This method stops all of the sounds going on, to prevent chords from bleeding over each other
    public void stopSounds(){
        for(int i  = 0 ; i < 34; i++){
            soundPool.stop(i);
        }
    }
    //This method plays the triad of the button that was touched
    public void playChord(Button rootButton) {
        String rootNote = ((String)rootButton.getText());
        makeTriad(getUnsimplifiedNote(rootNote));
        for(int i = 0; i < triad.size(); i++) {
            AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            float curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float volume = curVolume/maxVolume;
            String noteName = triad.get(i).substring(0,1).toUpperCase() + triad.get(i).substring(1);
            soundPool.play(Sounds.get(noteName), volume , volume , 1 , 0 , 1);
        }
    }

    //This method takes the rootNote of the button and creates a 3 note triad based on the key that the user selected
    public void makeTriad(String rootNote) {
        int startIndex = 0;
        triad = new ArrayList<>();
        for(int i = 0; i < notes.length; i++){
            if(notes[i].equals(rootNote))
                startIndex = i;
        }
        for(int a = 0; a < 3; a++){
            int noteIndex = startIndex + (a*2);
            if(noteIndex >= notes.length)
                noteIndex -= (notes.length);
            triad.add(notes[noteIndex]);
        }
    }

    //This is the master method for printing the info about the chord that was clicked onto the screen
    public void openInfoBox(Button chord){
        displayChordQuality(chord);
        displayNotes(chord);
    }

    //This method displays the notes of the chord selected
    public void displayNotes(Button chord) {
        makeTriad(getUnsimplifiedNote((String)chord.getText()));
        triadNotesTextView.setText("");
        for(int i = 0; i < triad.size()-1; i++){
            triadNotesTextView.append(getSimplifiedNote(triad.get(i)).toUpperCase() + "-");
        }
        triadNotesTextView.append(getSimplifiedNote(triad.get(triad.size()-1)).toUpperCase());
    }

    //This method takes the chord and based on its position in the key, displays its quality(Major, minor, diminished)
    public void displayChordQuality(Button chord) {
        String rootNote = (String)chord.getText();
        chordQualityTextView.setText(rootNote + " ");
        rootNote = getUnsimplifiedNote(rootNote);
        if(!isMinor){
            if(rootNote.equals(notes[1]) || rootNote.equals(notes[2]) || rootNote.equals(notes[5])){
                chordQualityTextView.append("Minor Chord");
            }
            else if(rootNote.equals(notes[6])){
                chordQualityTextView.append("Diminished Chord");
            }
            else{
                chordQualityTextView.append("Major Chord");
            }
        }
        else{
            if(rootNote.equals(notes[0]) || rootNote.equals(notes[3]) || rootNote.equals(notes[4])){
                chordQualityTextView.append("Minor Chord");
            }
            else if(rootNote.equals(notes[1])){
                chordQualityTextView.append("Diminished Chord");
            }
            else{
                chordQualityTextView.append("Major Chord");
            }
        }
    }

    //This method just connects the UI on the screen(Dropdown, checkbox, buttons) to variables in the file so their values can
    //be accessed
    public void initializeUI(){
        KeySpinner = (Spinner)findViewById(R.id.KeySpinner);
        loaded = false;
        minorCheck = (CheckBox)findViewById(R.id.MajorMinorCheckBox);
        confirmationButton = findViewById(R.id.ConfirmationButton);
        continueButton = (Button)findViewById(R.id.ContinueButton);
        chordQualityTextView = findViewById(R.id.ChordQualityTextView);
        triadNotesTextView = findViewById(R.id.ChordNoteTextView);
        chordButtons[0] = findViewById(R.id.ChordButton1);
        chordButtons[1] = findViewById(R.id.ChordButton2);
        chordButtons[2] = findViewById(R.id.ChordButton3);
        chordButtons[3] = findViewById(R.id.ChordButton4);
        chordButtons[4] = findViewById(R.id.ChordButton5);
        chordButtons[5] = findViewById(R.id.ChordButton6);
        chordButtons[6] = findViewById(R.id.ChordButton7);
        chordButtons[7] = findViewById(R.id.ChordButton8);
        buttonLayouts = new LinearLayout[]{
                findViewById(R.id.ChordKey1), findViewById(R.id.ChordKey2),
                findViewById(R.id.ChordKey3), findViewById(R.id.ChordKey4),
                findViewById(R.id.ChordKey5), findViewById(R.id.ChordKey6),
                findViewById(R.id.ChordKey7), findViewById(R.id.ChordKey8)
        };
        KeyHeader = findViewById(R.id.KeyHeader);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
            soundPool = new SoundPool.Builder().setMaxStreams(17).setAudioAttributes(audioAttributes).build();
        } else {
            soundPool = new SoundPool(17, AudioManager.STREAM_MUSIC, 0);
        }
        createSounds();
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                loaded = true;
            }
        });
    }

    //Initializes the 2d array of notes for easier creation of keys
    public void initializeNotes(){
        for(int c = 0; c < sharpNotes.length; c++){
            NOTES[0][c] = sharpNotes[c] + "4";
            NOTES[1][c] = flatNotes[c] + "4";
            NOTES[0][c + sharpNotes.length] = sharpNotes[c] + "5";
            NOTES[1][c + sharpNotes.length] = flatNotes[c] + "5";
        }
    }

    //This method creates all of the sound notes from local files
    public void createSounds() {
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

    //This method takes the key given and updates the eight buttons to display the correct chords in the key
    public void setNotes(){
        if((key.length() == 2 && key.charAt(0) != 'F') || (key.length() > 2 && key.charAt(1) == '#')) {
            //The key is using sharp notes
            makeKey(0);
        }
        else {
            //Otherwise the key is using flat notes
            makeKey(1);
        }
    }

    //Given whether its a sharp or flat scale, this creates an array of the 8 notes in the key given by the user
    public void makeKey(int rowNum){
        int index = 0;
        notes[0] = null;
        while(notes[0] == null){
            if(key.equals(NOTES[rowNum][index])) {
                notes[0] = NOTES[rowNum][index];
            }
            else{
                index++;
            }
        }
        if(!isMinor) {
            for(int a = 1; a < 8; a++) {
                if(a == 3 || a == 7)
                    index++;
                else
                    index = index + 2;
                notes[a] = NOTES[rowNum][index];
            }
            //set proper chords to lowercase
            notes[1] = notes[1].toLowerCase();
            notes[2] = notes[2].toLowerCase();
            notes[5] = notes[5].toLowerCase();
            notes[6] = notes[6].toLowerCase();
        }
        else {
            for(int a = 1; a < 8; a++) {
                if(a == 2 || a == 5)
                    index++;
                else
                    index = index + 2;
                notes[a] = NOTES[rowNum][index];
            }
            //set proper chords to lowercase
            notes[0] = notes[0].toLowerCase();
            notes[3] = notes[3].toLowerCase();
            notes[4] = notes[4].toLowerCase();
            notes[1] = notes[1].toLowerCase();
            notes[7] = notes[7].toLowerCase();
        }

        for(int x = 8; x < notes.length; x++){
            notes[x] = notes[x - 7].substring(0,notes[x-7].length()-1) + "5";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }

}
