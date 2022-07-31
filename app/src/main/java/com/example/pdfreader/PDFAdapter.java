package com.example.pdfreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class PDFAdapter extends RecyclerView.Adapter<PDFViewHolder> {
    private Context context;
    private List<File> pdfFiles;

    public PDFAdapter(Context context, List<File> pdfFiles) {
        this.context = context;
        this.pdfFiles = pdfFiles;
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
    }

    @Override
    public int getItemCount() {
        return pdfFiles.size();
    }
}
