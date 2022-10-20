package com.example.JW_Presentations;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class PDFViewHolder extends RecyclerView.ViewHolder {
    public TextView tv_Name;
    public CardView container;


    public PDFViewHolder(@NonNull View itemView) {
        super(itemView);

        tv_Name = itemView.findViewById(R.id.pdfName);
        container = itemView.findViewById(R.id.Container);
    }
}
