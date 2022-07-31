package com.example.pdfreader;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.Activity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;



import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PDVView extends Activity {

    private PDFAdapter pdfAdapter;
    private List<File> pdfList;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayPdf();
    }



    public ArrayList<File> findPdf (File file)
    {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();


            for (int i=0;i<files.length;i++) {

                File newFile = files[i];
                    if (newFile.getPath().endsWith("pdf")) {
                        arrayList.add(newFile);
                    }
                }


        return arrayList;
        }




    private void displayPdf() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        pdfList = new ArrayList<>();
        File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (downloadFolder.exists()) {
            pdfList.addAll(findPdf(downloadFolder));
        }
        else{
            Toast.makeText(this, "Directory not found", Toast.LENGTH_SHORT).show();
        }


        pdfAdapter = new PDFAdapter(this, pdfList);
        recyclerView.setAdapter(pdfAdapter);
    }
}