package c.taylor.chordprogressionbuilder;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class storedprogressions extends AppCompatActivity {

    private static String FILENAME = "STORED_PROGRESSION";
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;
    private int numProgressions;
    private LinearLayout main;
    private ArrayList<chordQueue> storedProgressions;
    private Button[][] chordButtons;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storedprogressions);
        getSavedProgressions();
        main = findViewById(R.id.main_layout);
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
                    case R.id.progression_builder:
                        openProgressionBuilder();
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

        Toast.makeText(storedprogressions.this, "Test", Toast.LENGTH_LONG).show();
        if(t.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    public void openProgressionBuilder(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void getSavedProgressions(){
        try {
            FileInputStream file = openFileInput(FILENAME);
            int count = 0;
            try {
                while(file.available() > 1) {
                    count++;
                    ArrayList<ArrayList<String>> curProgression = new ArrayList<>();
                    //loop through 4 chords in progression
                    for (int i = 0; i < 4; i++) {
                        int index = file.read();
                        ArrayList<String> curTriad = new ArrayList<>();
                        //loop through # of notes in chord
                        for (int j = 0; j < index; j++) {
                            String curChord = "";
                            int c = file.read();
                            while (c != ' ') {
                                curChord += (char) c;
                                c = file.read();
                            }
                            curTriad.add(curChord);
                        }
                        curProgression.add(curTriad);
                        displayProgression(curProgression, count);
                    }
                }
            }
            catch(IOException e){
                Toast.makeText(this, "Cant read from file", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "No stored progressions found", Toast.LENGTH_LONG).show();
        }
    }

    public void displayProgression(ArrayList<ArrayList<String>> progression, int progNumber){
        LinearLayout progGroup = createLayout();
        String idStem = "Chord" + progNumber;
        for(int i = 0; i < 4; i++){
            createKey(idStem, i);
        }
    }

    public LinearLayout createLayout(){
        LinearLayout progressionGroup = new LinearLayout(this);
        main.addView(progressionGroup);
        progressionGroup.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, .15f));
        progressionGroup.setOrientation(LinearLayout.HORIZONTAL);
        progressionGroup.setWeightSum(1f);
        progressionGroup.setGravity(Gravity.CENTER);
        return progressionGroup;
    }

    public void createKey(String idStem, int chordNum){
        
    }
    /*
    <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="0dp"
            android:layout_weight=".15"
            android:layout_marginTop="12dp"
            android:orientation="horizontal"
            android:gravity="center"
            android:weightSum="1">
            <LinearLayout
                android:id="@+id/Chord1Key1"
                android:layout_width="0dp"
                android:layout_weight=".15"
                android:layout_height="match_parent"
                android:background="@drawable/chord_button"
                android:orientation="horizontal"
                android:padding = "0dp"
                android:weightSum="1">
                <Button
                    android:id="@+id/Chord1Button1"
                    android:layout_width="0dp"
                    android:layout_weight=".9"
                    android:layout_height="match_parent"
                    android:background="@android:color/transparent"
                    android:onClick="chordClick"
                    android:textSize="33sp"
                    android:textAllCaps="false"
                    style="?android:attr/borderlessButtonStyle"
                    />
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_weight=".1"
                    android:layout_height="match_parent"
                    android:orientation="vertical"
                    android:weightSum="1"
                    >
                    <Button
                        android:layout_width="match_parent"
                        android:layout_height="0dp"
                        android:layout_weight=".6"
                        android:background="@drawable/chord_button_spacer"
                        style="?android:attr/borderlessButtonStyle"/>
                </LinearLayout>
            </LinearLayout>
            <LinearLayout
                android:id="@+id/Chord1Key2"
                android:layout_width="0dp"
                android:layout_weight=".15"
                android:layout_height="match_parent"
                android:background="@drawable/chord_button"
                android:orientation="horizontal"
                android:padding="0dp"
                android:weightSum="1">
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_weight=".1"
                    android:layout_height="match_parent"
                    android:orientation="vertical"
                    android:weightSum="1"
                    >
                    <ImageButton
                        android:layout_width="match_parent"
                        android:layout_height="0dp"
                        android:layout_weight=".6"
                        style="?android:attr/borderlessButtonStyle"
                        android:background="@drawable/chord_button_spacer"/>
                </LinearLayout>
                <Button
                    android:id="@+id/Chord1Button2"
                    android:layout_width="0dp"
                    android:layout_weight=".8"
                    android:layout_height="match_parent"
                    android:background="@android:color/transparent"
                    android:onClick="chordClick"
                    android:textSize="33sp"
                    android:textAllCaps="false"
                    style="?android:attr/borderlessButtonStyle"
                    />
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_weight=".1"
                    android:layout_height="match_parent"
                    android:orientation="vertical"
                    android:weightSum="1"
                    >
                    <ImageButton
                        android:layout_width="match_parent"
                        android:layout_height="0dp"
                        android:layout_weight=".6"
                        style="?android:attr/borderlessButtonStyle"
                        android:background="@drawable/chord_button_spacer"/>
                </LinearLayout>
            </LinearLayout>
            <LinearLayout
                android:layout_width="0dp"
                android:layout_weight=".15"
                android:layout_height="match_parent"
                android:background="@drawable/chord_button"
                android:orientation="horizontal"
                android:id="@+id/Chord1Key3"
                android:padding="0dp"
                android:weightSum="1">
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_weight=".1"
                    android:layout_height="match_parent"
                    android:orientation="vertical"
                    android:weightSum="1"
                    >
                    <ImageButton
                        android:layout_width="match_parent"
                        android:layout_height="0dp"
                        android:layout_weight=".6"
                        style="?android:attr/borderlessButtonStyle"
                        android:background="@drawable/chord_button_spacer"/>
                </LinearLayout>
                <Button
                    android:id="@+id/Chord1Button3"
                    android:layout_width="0dp"
                    android:layout_weight=".8"
                    android:layout_height="match_parent"
                    android:background="@android:color/transparent"
                    android:onClick="chordClick"
                    android:textSize="33sp"
                    style="?android:attr/borderlessButtonStyle"
                    android:textAllCaps="false"
                    />
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_weight=".1"
                    android:layout_height="match_parent"
                    android:orientation="vertical"
                    android:weightSum="1"
                    >
                    <ImageButton
                        android:layout_width="match_parent"
                        android:layout_height="0dp"
                        android:layout_weight=".6"
                        style="?android:attr/borderlessButtonStyle"
                        android:background="@drawable/chord_button_spacer"/>
                </LinearLayout>
            </LinearLayout>
            <LinearLayout
                android:layout_width="0dp"
                android:layout_weight=".15"
                android:layout_height="match_parent"
                android:background="@drawable/chord_button"
                android:orientation="horizontal"
                android:id="@+id/Chord1Key4"
                android:padding="0dp"
                android:weightSum="1">
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_weight=".1"
                    android:layout_height="match_parent"
                    android:orientation="vertical"
                    android:weightSum="1"
                    >
                    <ImageButton
                        android:layout_width="match_parent"
                        android:layout_height="0dp"
                        android:layout_weight=".6"
                        style="?android:attr/borderlessButtonStyle"
                        android:background="@drawable/chord_button_spacer"/>
                </LinearLayout>
                <Button
                    android:id="@+id/Chord1Button4"
                    android:layout_width="0dp"
                    android:layout_weight=".9"
                    android:layout_height="match_parent"
                    android:background="@android:color/transparent"
                    android:onClick="chordClick"
                    android:textSize="33sp"
                    style="?android:attr/borderlessButtonStyle"
                    android:textAllCaps="false"
                    />
            </LinearLayout>
            <ImageButton
                android:layout_width="wrap_content"
                android:layout_height="match_parent"
                android:layout_weight=".05"
                android:background="@drawable/chord_button_spacer"/>
            <LinearLayout
                android:layout_width="0dp"
                android:layout_weight=".25"
                android:layout_height="match_parent"
                android:orientation="horizontal"
                android:gravity="center"
                android:background="@drawable/chord_button"
                >
                <ImageButton
                    android:id="@+id/Chord1Play"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:minWidth="40dp"
                    android:minHeight="40dp"
                    android:layout_gravity="center"
                    android:onClick="playProgression"
                    android:background="@drawable/circle_button"
                    android:src="@drawable/ic_play_arrow_black_24dp">
                </ImageButton>
                <ImageButton
                    android:id="@+id/Chord1Save"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:minWidth="40dp"
                    android:minHeight="40dp"
                    android:layout_marginStart="8px"
                    android:layout_gravity="center"
                    android:onClick="saveProgression"
                    android:background="@drawable/circle_button"
                    android:src="@drawable/ic_file_download_black_24dp">
                </ImageButton>
            </LinearLayout>
        </LinearLayout>
     */

}
