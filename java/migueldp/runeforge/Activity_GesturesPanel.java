package migueldp.runeforge;

import android.app.Activity;
import android.app.AlertDialog;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Activity_GesturesPanel extends Activity {

    private GestureOverlayView gestureOverlay;
    private GestureLibrary gestureLibrary;
    private String[] gestures;
    private String gestureName;
    private TextView tv_counter_gestures;
    private Button b_insert;
    private Button b_clear;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesturespanel);

        gestureOverlay = findViewById(R.id.gestures);
        tv_counter_gestures = findViewById(R.id.tv_gesture_panel);
        b_insert = findViewById(R.id.b_new_gesture);
        b_clear = findViewById(R.id.b_clean_overlay);
        ListView listView = findViewById(R.id.list_view);

        /*
         * IMPORTANT: the next line returns a object of FileGestureLibrary who is a son of GestureLibrary
         * that is why the methods load and save have code and they aren't abstract.
         */
        gestureLibrary = GestureLibraries.fromFile(getExternalFilesDir(null) + "/" + "gestures.txt");
        gestureLibrary.load();
        gestures = gestureLibrary.getGestureEntries().toArray(new String[0]);

        tv_counter_gestures.setText(R.string.gesturePanel_numberGestures);
        tv_counter_gestures.append(" " + gestures.length);
        b_insert.setOnClickListener(this::init_create_gesture);
        b_clear.setOnClickListener(v -> gestureOverlay.clear(false));

        arrayAdapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.textView, gestures);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> read_gesture(position));

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            delete_gesture(position);
            return true;
        });

        gestureOverlay.setGestureStrokeAngleThreshold(90.0f);
        gestureOverlay.addOnGesturePerformedListener((overlay, gesture) -> {

            ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);

            // one prediction needed TODO: return the gesture with biggest score!!
            if (predictions.size() > 0 && predictions.get(0).score > 2.0) {
                String message = getResources().getString(R.string.gesturePanel_gestureDetected) + " " + predictions.get(0).name;
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void reload_UI() {
        tv_counter_gestures.setText(R.string.gesturePanel_numberGestures);
        tv_counter_gestures.append(" " + gestures.length);
    }

    public void read_gesture(int positionGesture) {
        String gestureEntry = gestures[positionGesture];
        Gesture gesture = gestureLibrary.getGestures(gestureEntry).get(0);
        gestureOverlay.setGesture(gesture);
    }

    public void delete_gesture(int positionGesture) {
        Toast.makeText(getApplicationContext(), "Deleted: " + gestures[positionGesture], Toast.LENGTH_SHORT).show();
        gestureLibrary.removeEntry(gestures[positionGesture]);
        gestureLibrary.save();
        gestures = gestureLibrary.getGestureEntries().toArray(new String[0]);
        arrayAdapter.notifyDataSetChanged(); // TODO: not working
        reload_UI();
    }

    private void create_gesture(String gestureName) {
        Gesture gesture = gestureOverlay.getGesture();

        if (gesture == null || gesture.getStrokesCount() == 0) {
            // Check if the gesture is empty
            Toast.makeText(getApplicationContext(), R.string.addGesture_checkGestureEmpty, Toast.LENGTH_SHORT).show();
        } else {
            gestureLibrary.addGesture(gestureName, gesture);

            Toast.makeText(this,
                    getResources().getString(R.string.addGesture_gestureSaved).concat(" ").concat(gestureName),
                    Toast.LENGTH_SHORT).show();
            gestureLibrary.save();
            gestures = gestureLibrary.getGestureEntries().toArray(new String[0]);
            arrayAdapter.notifyDataSetChanged(); // TODO: not working
            reload_UI();
        }
    }

    private void getGestureName_AlertDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.addGesture_DialogTitle);

        final EditText editText = new EditText(this);
        dialog.setView(editText);
        dialog.setPositiveButton(R.string.addGesture_save, (dialogInterface, i) -> {
            gestureName = editText.getText().toString();

            // Check if the name of the gesture is empty
            if (gestureName.equals("")) {
                Toast.makeText(this, R.string.addGesture_checkGestureName, Toast.LENGTH_SHORT).show();
                getGestureName_AlertDialog();
            } else {
                create_gesture(gestureName);
            }

            gestureOverlay.clear(false);
        });
        dialog.setNegativeButton(R.string.addGesture_cancel, (dialogInterface, i) -> {
            dialogInterface.cancel();
            gestureOverlay.clear(false);
        });
        dialog.show();
    }

    public void init_create_gesture(View view) {

        gestureOverlay.clear(false);
        gestureOverlay.setFadeEnabled(false);
        gestureOverlay.setFadeOffset(100000);

        Button button = (Button) view;
        button.setText(R.string.gesturePanel_saveGesture);
        button.setOnClickListener(this::save_create_gesture);

        b_clear.setVisibility(View.VISIBLE);
    }

    public void save_create_gesture(View view) {

        gestureOverlay.setFadeEnabled(true);
        gestureOverlay.setFadeOffset(420); //default value

        // Show dialog to ask gesture name
        getGestureName_AlertDialog();

        Button button = (Button) view;
        button.setText(R.string.gesturePanel_createGesture);
        button.setOnClickListener(this::init_create_gesture);

        b_clear.setVisibility(View.GONE);
    }

}