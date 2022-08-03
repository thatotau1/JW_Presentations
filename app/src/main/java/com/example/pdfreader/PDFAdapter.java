package com.example.pdfreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class PDFAdapter extends RecyclerView.Adapter<PDFViewHolder> {
    private Context context;
    private List<File> pdfFiles;
    private PDFSelectedListener pdfSelectedListener;

    public PDFAdapter(Context context, List<File> pdfFiles, PDFSelectedListener pdfSelectedListener) {
        this.context = context;
        this.pdfFiles = pdfFiles;
        this.pdfSelectedListener = pdfSelectedListener;
    }

    @NonNull
    @Override
    public PDFViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PDFViewHolder(LayoutInflater.from(context).inflate(R.layout.pdf_holder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PDFViewHolder holder, int position) {
        holder.tv_Name.setText(pdfFiles.get(position).getName());
        holder.tv_Name.setSelected(true);
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfSelectedListener.onPDFSelected(pdfFiles.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfFiles.size();
    }
}
