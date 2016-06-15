package com.packtpub.masteringopencvandroid.objectdetect;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends Activity {

    public static final String TAG = "ObjectDetect::MainActivity";

    // Request code definition
    private final int SELECT_PHOTO_1    = 1;
    private final int SELECT_PHOTO_2    = 2;
    private final int READ_PERMISSION   = 1;

    private boolean src1Selected = false;
    private boolean src2Selected = false;

    private ImageView ivImage1;

    private TextView tvKeyPointsObject1;
    private TextView tvKeyPointsObject2;
    private TextView tvKeyPointsMatches;
    private TextView tvTime;

    private Mat src1;
    private Mat src2;

    private boolean isAllowed = false;

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            Log.d(TAG, "onManagedConnected is called");
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.d(TAG, "OpenCV is loaded successfully");
                    // Loading native library
                    System.loadLibrary("nonfree");
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate is called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Linking view component
        ivImage1 = (ImageView)findViewById(R.id.ivImage1);
        tvKeyPointsObject1 = (TextView) findViewById(R.id.tvKeyPointsObject1);
        tvKeyPointsObject2 = (TextView) findViewById(R.id.tvKeyPointsObject2);
        tvKeyPointsMatches = (TextView) findViewById(R.id.tvKeyPointsMatches);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this,
                mOpenCVCallBack);
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

                ActivityCompat.requestPermissions(this, new String[]
                        {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION);

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
                            src1 = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC4);
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
                            src2 = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC4);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_PERMISSION: {
                // If request is cancelled, the result arrays are empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission is granted");
                    isAllowed = true;
                } else {
                    Log.d(TAG, "Permission is Not granted");
                }
            }
        }
    }

}
