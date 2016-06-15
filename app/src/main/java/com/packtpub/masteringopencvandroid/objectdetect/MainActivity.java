package com.packtpub.masteringopencvandroid.objectdetect;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends Activity {

    public static final String TAG = "ObjectDetect::MainActivity";

    private final int SELECT_PHOTO_1 = 1;
    private final int SELECT_PHOTO_2 = 2;

    private boolean src1Selected = false, src2Selected = false;
    Mat src1, src2;

    private ImageView ivImage1;

    private boolean isAllowed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate is called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_load_first_image) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO_1);
            return true;
        } else if (id == R.id.action_load_second_image) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO_2);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        // TODO: Permission checking implementation
        if (resultCode == RESULT_OK && (requestCode == SELECT_PHOTO_1 || requestCode == SELECT_PHOTO_2)
                && imageReturnedIntent != null) {
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission is not allowed. Request permission");
            } else {
                Log.d(TAG, "Permission is granted");
                isAllowed = true;
            }
        }

        if (isAllowed) {

            Log.d(TAG, "Handle the action of select image");

            switch (requestCode) {
                case SELECT_PHOTO_1:
                    if (resultCode == RESULT_OK) {
                        try {
                            final Uri imageUri = imageReturnedIntent.getData();
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            //src1 = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC4);
                            ivImage1.setImageBitmap(selectedImage);
                            Utils.bitmapToMat(selectedImage, src1);
                            src1Selected = true;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case SELECT_PHOTO_2:
                    if (resultCode == RESULT_OK) {
                        try {
                            final Uri imageUri = imageReturnedIntent.getData();
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            //src2 = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC4);
                            Utils.bitmapToMat(selectedImage, src2);
                            src2Selected = true;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

}
