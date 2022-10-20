package com.example.JW_Presentations;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class ColourPicker extends Dialog implements View.OnClickListener {
    private ColourPickerSelectedListener listener;
    private final ArrayList<ColourButton> colourButtons;

    public ColourPicker(@NonNull Context context, int currentColour)
    {
        super(context);
        setOwnerActivity ((Activity) context);

        Window window = super.getWindow();

        if (window != null)
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        super.setContentView(R.layout.colour_picker);
        super.setCancelable(true);

        LinearLayout cColum1 = findViewById(R.id.columnLayout1);
        LinearLayout cColum2 = findViewById(R.id.columnLayout2);
        LinearLayout cColum3 = findViewById(R.id.columnLayout3);

        ArrayList<Integer> colours = new ArrayList<>();
        addColours(colours, cColum1);
        addColours(colours, cColum2);
        addColours(colours, cColum3);

        final int[] colourIDs = ColoursChoiceManager.getColourIDs();

        colourButtons = new ArrayList<>();

        for (int i = 0; i<colours.size(); i++)
        {
            View view = findViewById(colours.get(i));
            view.setOnClickListener(this);

            int colour = ContextCompat.getColor(context, colourIDs[i]);

            GradientDrawable gradientDrawable = (GradientDrawable) view.getBackground();
            gradientDrawable.setColor(ContextCompat.getColor(context, colourIDs[i]));
            
            int tickResourceID = getTickResourceID(colour);
            
            ColourButton colourButton = new ColourButton(colours.get(i), colourIDs[i], tickResourceID);
            colourButtons.add(colourButton);

            if (colour == currentColour)
                ((ImageButton) view).setImageResource(tickResourceID);
        }
        Button buttonCancel = findViewById(R.id.buttonCancel);
        Button buttonSelect = findViewById(R.id.buttonSelect);
        // Set the click listeners for the buttons
        buttonCancel.setOnClickListener(this);
        buttonSelect.setOnClickListener(this);
    }

    private int getTickResourceID(int colour) {
        int tickResourceID;
        tickResourceID = R.drawable.ic_done_white_24dp;
        if (ColoursChoiceManager.isSignificantlyLight(colour))
            tickResourceID = R.drawable.ic_done_black_24dp;
        return tickResourceID;

    }
    public void setOnDialogOptionSelectedListener (ColourPickerSelectedListener listener)
    {
        this.listener = listener;
    }
    private void addColours(ArrayList<Integer> colours, LinearLayout cColum) {
        for (int i = 0; i < cColum.getChildCount(); i++)
            colours.add(cColum.getChildAt(i).getId());
    }

    @Override
    public void onClick(View view) {
        // get the ID of the view object being clicked
        int viewID = view.getId();
        // prompt the listener depending on the option selected
        if (viewID == R.id.buttonCancel || viewID == R.id.buttonSelect)
        {
            dismiss();
        } else
        {
            // loop through the colour button drawables
            for (ColourButton colourButton : colourButtons)
            {
                // if the clicked view object is the same as the colour button
                if (colourButton.getViewID() == viewID)
                {
                    // get the colour of the button
                    int colour = ContextCompat.getColor(getContext(), colourButton.getColourID());
                    // set the "tick" icon overlay to show the colour is selected
                    ((ImageButton) view).setImageResource(colourButton.getSelectedID());
                    listener.onColourPickerOptionSelected(colour);
                } else
                {
                    // get the button as a view and remove any pre-existing "tick" drawable
                    View v = findViewById(colourButton.getViewID());
                    ((ImageButton) v).setImageDrawable(null);
                }
            }
        }

    }

    private static class ColourButton
    {
        private final int viewID;
        private final int colourID;
        private final int selectedID;


        public ColourButton(int viewID, int colourID, int selectedID)
        {
            this.viewID = viewID;
            this.colourID = colourID;
            this.selectedID = selectedID;
        }

        public int getViewID() {
            return viewID;
        }

        public int getColourID() {
            return colourID;
        }

        public int getSelectedID() {
            return selectedID;
        }
    }

    public interface ColourPickerSelectedListener
    {
        void onColourPickerOptionSelected (int colour);
    }
}
