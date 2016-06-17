package com.packtpub.masteringopencvandroid.objectdetect;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    private int keypointsObject1;
    private int keypointsObject2;
    private int keypointMatches;

    // Maximum number of match to draw
    private final int MAX_MATCHES = 50;

    private TextView tvTime;

    private Mat src1;
    private Mat src2;

    // Action Mode from HomeActivity
    private static int ACTION_MODE = 0;

    // Permission Flag
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

        // Loading OpenCV
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mOpenCVCallBack);

        // Linking view component
        tvTime = (TextView) findViewById(R.id.tvTime);
        ivImage1 = (ImageView)findViewById(R.id.ivImage1);
        tvKeyPointsObject1 = (TextView) findViewById(R.id.tvKeyPointsObject1);
        tvKeyPointsObject2 = (TextView) findViewById(R.id.tvKeyPointsObject2);
        tvKeyPointsMatches = (TextView) findViewById(R.id.tvKeyPointsMatches);

        // TODO: Not clear the purpose of those variables
        keypointsObject1 = keypointsObject2 = keypointMatches = -1;

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(HomeActivity.ACTION_MOE)) {
            ACTION_MODE = intent.getIntExtra(HomeActivity.ACTION_MOE, 0);
            Log.d(TAG, "ACTION_MODE: " + ACTION_MODE);
        }
    }

//    @Override
//    protected void onResume() {
//        Log.d(TAG, "onResume is called");
//        super.onResume();
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this,
//                mOpenCVCallBack);
//    }

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
                            //ivImage1.setImageBitmap(selectedImage);
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
            Toast.makeText(MainActivity.this, src1Selected + " " + src2Selected, Toast.LENGTH_SHORT).show();

            if (src1Selected && src2Selected) {
                Log.d(TAG, "Before execute");
                new AsyncTask<Void, Void, Bitmap>() {

                    private long startTime;
                    private long endTime;

                    @Override
                    protected void onPreExecute() {
                        Log.d(TAG, "onPreExecute begin");
                        super.onPreExecute();
                        startTime = System.currentTimeMillis();
                    }

                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        Log.d(TAG, "doInBackground begin");
                        return executeTask();
                        //return null;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        Log.d(TAG, "onPostExecute begin");
                        super.onPostExecute(bitmap);
                        endTime = System.currentTimeMillis();

                        ivImage1.setImageBitmap(bitmap);
                        tvKeyPointsObject1.setText("Object 1 : " + keypointsObject1);
                        tvKeyPointsObject2.setText("Object 2 : " + keypointsObject2);
                        tvKeyPointsMatches.setText("Key point Matches : " + keypointMatches);

                        tvTime.setText("Time taken: " + (endTime-startTime) + "(ms)");
                    }

                }.execute();
            }
        }
    }

    // Task will be execute in background
    private Bitmap executeTask() {
        Log.d(TAG, "executeTask begin");
        FeatureDetector detector;
        MatOfKeyPoint keyPoints1, keyPoints2;
        DescriptorExtractor descriptorExtractor;
        Mat descriptors1, descriptors2;
        DescriptorMatcher descriptorMatcher;

        MatOfDMatch matches  = new MatOfDMatch();

        keyPoints1 = new MatOfKeyPoint();
        keyPoints2 = new MatOfKeyPoint();

        descriptors1 = new Mat();
        descriptors2 = new Mat();

        Log.d(TAG, "before switch ACTION_MODE " + ACTION_MODE);

        switch (ACTION_MODE) {
            case HomeActivity.MODE_SIFT:
                Log.d(TAG, "MODE_SIFT");
                detector = FeatureDetector.create(FeatureDetector.SIFT);
                descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
                descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_SL2);
                Log.d(TAG, "Got descriptorMatcher");
                break;
            default:
                Log.d(TAG, "default case");
                detector = FeatureDetector.create(FeatureDetector.FAST);
                descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.BRIEF);
                descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
                break;
        }

        Log.d(TAG, "before detect");
        Log.d(TAG, "image 1 " + src1.cols() + " / " + src1.rows());
        Log.d(TAG, "image 2 " + src1.cols() + " / " + src1.rows());

        // Get key points
        Log.d(TAG, "start get key points");
        detector.detect(src2, keyPoints2);
        detector.detect(src1, keyPoints1);

        Log.d(TAG, CvType.typeToString(src1.type()) + " " + CvType.typeToString(src2.type()));
        Log.d(TAG, keyPoints1.toArray().length + " key points");
        Log.d(TAG, keyPoints2.toArray().length + " key points");

        keypointsObject1 = keyPoints1.toArray().length;
        keypointsObject2 = keyPoints2.toArray().length;

        Log.d(TAG, "start get key point descriptor");
        descriptorExtractor.compute(src1, keyPoints1, descriptors1);
        descriptorExtractor.compute(src2, keyPoints2, descriptors2);

        Log.d(TAG, "matching two descriptor");
        descriptorMatcher.match(descriptors1, descriptors2, matches);

        keypointMatches = matches.toArray().length;

        Log.d(TAG, "number of match " + keypointMatches);

        Collections.sort(matches.toList(), new Comparator<DMatch>() {
            @Override
            public int compare(DMatch o1, DMatch o2) {
                if (o1.distance < o2.distance ) {
                    return -1;
                }
                if (o1.distance > o2.distance) {
                    return 1;
                }
                return 0;
            }
        });

        List<DMatch> listOfDMatch = matches.toList();

        if(listOfDMatch.size() > MAX_MATCHES){
            matches.fromList(listOfDMatch.subList(0, MAX_MATCHES));
        }

        Log.d(TAG, "start to draw match");
        Mat src3 = drawMatches(src1, keyPoints1, src2, keyPoints2, matches, false);

        Bitmap image1 = Bitmap.createBitmap(src3.cols(), src3.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src3, image1);
        Imgproc.cvtColor(src3, src3, Imgproc.COLOR_BGR2RGB);

        boolean bool = Highgui.imwrite(Environment.getExternalStorageDirectory()+"/Download/" + ACTION_MODE+".png", src3);
        Log.d(TAG, bool + " " + Environment.getExternalStorageDirectory()+"/Download/" + ACTION_MODE + ".png");

        return image1;

    }

    private static Mat drawMatches(Mat img1, MatOfKeyPoint key1, Mat img2, MatOfKeyPoint key2,
                                   MatOfDMatch matches, boolean imageOnly){
        Mat out = new Mat();
        Mat im1 = new Mat();
        Mat im2 = new Mat();

        Imgproc.cvtColor(img1, im1, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(img2, im2, Imgproc.COLOR_BGR2RGB);

        if ( imageOnly){
            MatOfDMatch emptyMatch = new MatOfDMatch();
            MatOfKeyPoint emptyKey1 = new MatOfKeyPoint();
            MatOfKeyPoint emptyKey2 = new MatOfKeyPoint();
            Features2d.drawMatches(im1, emptyKey1, im2, emptyKey2, emptyMatch, out);
        } else {
            Features2d.drawMatches(im1, key1, im2, key2, matches, out);
        }

        Imgproc.cvtColor(out, out, Imgproc.COLOR_BGR2RGB);

        Core.putText(out, "FRAME", new Point(img1.width() / 2,30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0,255,255),3);

        Core.putText(out, "MATCHED", new Point(img1.width() + img2.width() / 2,30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255,0,0),3);

        return out;
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
