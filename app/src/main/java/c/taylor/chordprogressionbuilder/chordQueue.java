package c.taylor.chordprogressionbuilder;

import android.content.Context;
import android.media.SoundPool;
import android.widget.Toast;

public class chordQueue {

    private String[][] queue;
    private int endOfQueue;

    public chordQueue(int size, int sizeOfChords){
        endOfQueue = 0;
        queue = new String[size][sizeOfChords];
            for(int r = 0; r < queue.length; r++){
                for(int c  = 0; c < queue[0].length; c++){
                    queue[r][c] = " ";
                }
            }
    }

    public int getSize(){
        return endOfQueue;
    }

    public boolean isFull(){
        return endOfQueue > queue.length - 1;
    }

    public int addChord(String[] chord){
        return addChord(chord, endOfQueue);
    }

    public int addChord(String[] chord, int pos){
        if(isFull()) {
            return 0;
        }
        else {
            for (int c = 0; c < queue[0].length; c++) {
                queue[pos][c] = chord[c];
            }
            if(pos > endOfQueue) {
                endOfQueue = pos;
            }
            return 1;
        }
    }

    public String[] getChord(int pos){
        String[] chord = new String[queue[pos].length];
        for(int c = 0; c < chord.length; c++){
            chord[c] = queue[pos][c];
        }
        return chord;
    }

    public void removeChord(int pos){
        for(int c = 0; c < queue[0].length; c++){
            queue[pos][c] = " ";
        }
        endOfQueue = reSize();
    }

    public int reSize(){
        for(int r = queue.length - 1; r >= 0; r--){
            if(!(queue[r][0].equals(" ")))
                return r;
        }
        return 0;
    }

    public String toString(){
        StringBuilder allChord = new StringBuilder();
        for(int r = 0; r < queue.length-1; r++){
            StringBuilder oneChord = new StringBuilder();
            for(int c = 0; c < queue[0].length; c++){
                oneChord.append(queue[r][c]);
            }
            allChord.append(oneChord + ", ");
        }
        StringBuilder oneChord = new StringBuilder();
        for(int c = 0; c < queue[0].length; c++){
            oneChord.append(queue[queue.length-1][c]);
        }
        allChord.append(oneChord);
        return allChord.toString();
    }

}
