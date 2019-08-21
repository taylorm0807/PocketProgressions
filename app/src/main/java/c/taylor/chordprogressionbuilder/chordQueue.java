package c.taylor.chordprogressionbuilder;

import android.content.Context;
import android.media.SoundPool;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class chordQueue {

    private ArrayList<ArrayList<String>> queue;
    private int endOfQueue;

    public chordQueue(int size){
        endOfQueue = 0;
        queue = new ArrayList<>();
        for(int r = 0; r < size; r++){
            queue.add(new ArrayList<String>());
        }
    }

    public int getSize(){
        return endOfQueue;
    }

    public boolean isFull(){
        return endOfQueue > queue.size() - 1;
    }

    public int addChord(ArrayList<String> chord){
        return addChord(chord, endOfQueue);
    }

    public int addChord(ArrayList<String> chord, int pos){
        if(!isFull()){
            endOfQueue++;
        }
        queue.set(pos, chord);
        return 1;
    }

    public ArrayList<String> getChord(int pos){
        return queue.get(pos);
    }

    public void removeChord(int pos){
        queue.remove(pos);
        endOfQueue--;
    }

    public String toString(){
        StringBuilder allChord = new StringBuilder();
        for(int r = 0; r < queue.size()-1; r++){
            ArrayList<String> curChord = queue.get(r);
            StringBuilder oneChord = new StringBuilder();
            for(int c = 0; c < curChord.size(); c++){
                oneChord.append(curChord.get(c) + ' ');
            }
            allChord.append(oneChord + ", ");
        }
        return allChord.toString();
    }

}
