package com.bgt.pdfdocumentdemo;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.media.Image;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hendrix.pdfmyxml.viewRenderer.AbstractViewRenderer;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    byte[] byteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tv);
        imageView = (ImageView) findViewById(R.id.iv);
        Glide.with(getApplicationContext()).load(R.drawable.electric)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(200, 200) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        resource.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byteArray = stream.toByteArray();
                        imageView.setImageBitmap(resource);
                    }
                });
    }

    public void mCreatePdf(View v) {

        //mCreatePdfDocument();
        //mCreatePDfDocumentLib();
        String text = textView.getText().toString();
        if (byteArray != null && byteArray.length != 0) {
            mCreatePDfDocumentItext(byteArray, text);
        }


    }

    private void mCreatePDfDocumentItext(byte[] byteArray, String text) {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "PDF IText");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("App", "failed to create directory");
            }
        }

        try {
            File f = new File(mediaStorageDir, "share.pdf");
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(f));
            document.open();
            Paragraph p = new Paragraph(text);
            document.add(p);
            com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(byteArray);
            document.add(image);
            document.close();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void mCreatePDfDocumentLib() {

        AbstractViewRenderer page = new AbstractViewRenderer(getApplicationContext(), R.layout.activity_main) {


            @Override
            protected void initView(View view) {
                TextView tv_hello = (TextView) view.findViewById(R.id.tv);
                Button button = (Button) view.findViewById(R.id.bt);
                button.setVisibility(View.GONE);
            }
        };
// you can reuse the bitmap if you want
        //page.setReuseBitmap(true);

        final com.hendrix.pdfmyxml.PdfDocument doc = new com.hendrix.pdfmyxml.PdfDocument(MainActivity.this);

// add as many pages as you have
        doc.addPage(page);

        doc.setRenderWidth(findViewById(R.id.tv).getWidth());
        doc.setRenderHeight(findViewById(R.id.tv).getHeight());
        doc.setOrientation(com.hendrix.pdfmyxml.PdfDocument.A4_MODE.PORTRAIT);
        doc.setProgressTitle(R.string.please);
        doc.setProgressMessage(R.string.gen_pdf_file);
        doc.setFileName("test");
        doc.setInflateOnMainThread(false);
        doc.setListener(new com.hendrix.pdfmyxml.PdfDocument.Callback() {
            @Override
            public void onComplete(File file) {
                Log.i(com.hendrix.pdfmyxml.PdfDocument.TAG_PDF_MY_XML, "Complete" + file.getPath());

            }

            @Override
            public void onError(Exception e) {
                Log.i(com.hendrix.pdfmyxml.PdfDocument.TAG_PDF_MY_XML, "Error");
            }
        });

        doc.createPdf(MainActivity.this);
    }

    private void mCreatePdfDocument() {

        PrintAttributes printAttrs = new PrintAttributes.Builder().
                setColorMode(PrintAttributes.COLOR_MODE_COLOR).
                setMediaSize(PrintAttributes.MediaSize.NA_LETTER).
                setResolution(new PrintAttributes.Resolution("zooey", PRINT_SERVICE, findViewById(R.id.scroll_view).getWidth(), findViewById(R.id.scroll_view).getHeight())).
                setMinMargins(PrintAttributes.Margins.NO_MARGINS).
                build();
        PdfDocument document = new PrintedPdfDocument(this, printAttrs);
        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(findViewById(R.id.scroll_view).getWidth(), findViewById(R.id.scroll_view).getHeight(), 1).create();
        // create a new page from the PageInfo
        PdfDocument.Page page = document.startPage(pageInfo);
        // repaint the user's text into the page
        View content = findViewById(R.id.scroll_view);
        content.draw(page.getCanvas());
        // do final processing of the page
        document.finishPage(page);
        // Here you could add more pages in a longer doc app, but you'd have
        // to handle page-breaking yourself in e.g., write your own word processor...
        // Now write the PDF document to a file; it actually needs to be a file
        // since the Share mechanism can't accept a byte[]. though it can
        // accept a String/CharSequence. Meh.

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "PDFDocument");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("App", "failed to create directory");
            }
        }

        try {
            File f = new File(mediaStorageDir, "share.pdf");
            FileOutputStream fos = new FileOutputStream(f);
            document.writeTo(fos);
            document.close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating file", e);
        }
    }
}
