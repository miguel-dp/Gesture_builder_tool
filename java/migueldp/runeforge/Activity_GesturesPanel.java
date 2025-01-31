package migueldp.runeforge;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;

public class Activity_GesturesPanel extends AppCompatActivity {

    // primitive
    private String gestureName;
    private ArrayList<String> gesturesArrayList;

    // UI
    private TextView tv_counter_gestures;
    private TextView tv_last_gesture;
    private Button b_insert;
    private Button b_clear;
    private GestureOverlayView gestureOverlay;

    // Libraries
    private GestureLibrary gestureLibrary;
    private ArrayAdapter<String> arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesturespanel);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.topMargin = insets.top;
            mlp.leftMargin = insets.left;
            mlp.bottomMargin = insets.bottom;
            mlp.rightMargin = insets.right;
            v.setLayoutParams(mlp);

            // Return CONSUMED if you don't want the window insets to keep passing
            // down to descendant views.
            return WindowInsetsCompat.CONSUMED;
        });

        gestureOverlay = findViewById(R.id.gestures);
        tv_counter_gestures = findViewById(R.id.tv_gesture_panel);
        tv_last_gesture = findViewById(R.id.tv_last_gesture);
        b_insert = findViewById(R.id.b_new_gesture);
        b_clear = findViewById(R.id.b_clean_overlay);
        ImageButton ib_help = findViewById(R.id.ib_help);
        ListView listView = findViewById(R.id.list_view);

        ib_help.setOnClickListener(v -> dialog_showHelp());
        b_insert.setOnClickListener(this::init_create_gesture);
        b_clear.setOnClickListener(v -> gestureOverlay.clear(false));

        /*
         * IMPORTANT: the next line returns a object of FileGestureLibrary who is a son of GestureLibrary
         * that is why the methods load and save have code and they aren't abstract.
         */
        gestureLibrary = GestureLibraries.fromFile(getExternalFilesDir(null) + "/" + "gestures.txt");
        gestureLibrary.load();

        String[] gesturesArray = gestureLibrary.getGestureEntries().toArray(new String[0]);
        gesturesArrayList = new ArrayList<>();
        gesturesArrayList.addAll(Arrays.asList(gesturesArray));

        arrayAdapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.textView, gesturesArrayList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> read_gesture(position));
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            dialog_confirmDelete(position);
            return true;
        });

        refresh_UI();

        gestureOverlay.setGestureStrokeAngleThreshold(90.0f);
        gestureOverlay.addOnGesturePerformedListener(onGesturePerformedListener);

    }

    final GestureOverlayView.OnGesturePerformedListener onGesturePerformedListener = (overlay, gesture) -> {
        ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);

        // one prediction needed
        // I guess score of 5 is the minimum of a good predict
        // predictions array is sorted by default, by score
        if (predictions.size() > 0 && predictions.get(0).score > 5.0) {
            String message = getResources().getString(R.string.gesturePanel_gestureDetected) + " " + predictions.get(0).name;
            message = message + "\n" + getResources().getString(R.string.gesturePanel_gestureScore) + " " + predictions.get(0).score;

            tv_last_gesture.setText(message);
        }
    };

    public void dialog_showHelp() {
        String message = getResources().getString(R.string.dialog_help_message).concat("\n").concat(getExternalFilesDir(null) + "/" + "gestures.txt");

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.help)
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void refresh_UI() {
        tv_counter_gestures.setText(R.string.gesturePanel_numberGestures);
        tv_counter_gestures.append(" " + gesturesArrayList.size());
        arrayAdapter.notifyDataSetChanged();
        gestureOverlay.clear(false);
    }

    public void read_gesture(int positionGesture) {
        String gestureEntry = gesturesArrayList.get(positionGesture);
        Gesture gesture = gestureLibrary.getGestures(gestureEntry).get(0);
        gestureOverlay.setGesture(gesture);
    }

    private void create_gesture(String gestureName, Gesture gesture) {
        gestureLibrary.addGesture(gestureName, gesture);
        gestureLibrary.save();
        gesturesArrayList.add(gestureName);
    }

    public void delete_gesture(int positionGesture) {
        gestureLibrary.removeEntry(gesturesArrayList.get(positionGesture));
        gestureLibrary.save();
        gesturesArrayList.remove(positionGesture);
    }

    private void dialog_confirmDelete(int positionGesture) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.gesturePanel_confirmDelete)
                .setNeutralButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .setPositiveButton(R.string.accept, (dialog, which) ->
                {
                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(R.string.deleted) + ": " + gesturesArrayList.get(positionGesture),
                            Toast.LENGTH_SHORT).show();
                    delete_gesture(positionGesture);
                    refresh_UI();
                })
                .show();
    }

    private void dialog_askNameGesture() {
        Gesture gesture = gestureOverlay.getGesture();

        if (gesture == null || gesture.getStrokesCount() == 0) { // Check if the gesture is empty
            Toast.makeText(this, R.string.addGesture_checkGestureEmpty, Toast.LENGTH_SHORT).show();
            return;
        }

        EditText editText = new EditText(this);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.addGesture_DialogTitle)
                .setView(editText)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.cancel();
                    gestureOverlay.clear(false);
                })
                .setPositiveButton(R.string.save, (dialog, which) ->
                {
                    String ToastMessage;
                    gestureName = editText.getText().toString();

                    // CONTROL OF ERRORS
                    if (gestureName.equals("")) { // Check if the name of the gesture is empty
                        ToastMessage = getResources().getString(R.string.addGesture_checkGestureName);
                        dialog_askNameGesture();
                    } else {
                        create_gesture(gestureName, gesture);
                        ToastMessage = getResources().getString(R.string.addGesture_gestureSaved).concat(" ").concat(gestureName);
                        refresh_UI();
                    }

                    Toast.makeText(getApplicationContext(), ToastMessage, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    public void init_create_gesture(View view) {

        gestureOverlay.clear(false);
        gestureOverlay.setFadeEnabled(false);
        gestureOverlay.setFadeOffset(100000);

        b_insert.setText(R.string.gesturePanel_saveGesture);
        b_insert.setOnClickListener(this::save_create_gesture);

        b_clear.setVisibility(View.VISIBLE);
    }

    public void save_create_gesture(View view) {

        gestureOverlay.setFadeEnabled(true);
        gestureOverlay.setFadeOffset(420); //default value

        dialog_askNameGesture();

        b_insert.setText(R.string.gesturePanel_createGesture);
        b_insert.setOnClickListener(this::init_create_gesture);

        b_clear.setVisibility(View.GONE);
    }

}