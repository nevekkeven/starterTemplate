//comment avoir le range du bleu: https://gurus.pyimagesearch.com/object-tracking-in-video/

package website.timrobinson.opencvtutorial;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.R.id.mask;
import static org.opencv.imgproc.Imgproc.GaussianBlur;

public class MainActivity extends AppCompatActivity implements OnTouchListener, CvCameraViewListener2 {
    private static final String TAG = "MyActivity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;
    private Scalar mBlobColorHsv;
    private Scalar mBlobColorRgba;


    private CascadeClassifier cascadeClassifier;
    private Mat grayscaleImage;
    private int absoluteFaceSize;


    TextView touch_coordinates;
    TextView touch_color;

    double x = -1;
    double y = -1;

    /* ******************opencv instanciation*************************************************** */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    /* check if opencv is installed */
    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    };
    /* **************end opencv instanciation************************************************* */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        touch_coordinates = (TextView) findViewById(R.id.touch_coordinates);
        touch_color = (TextView) findViewById(R.id.touch_color);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_tutorial_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }
    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        // The faces will be a 20% of the height of the screen
        absoluteFaceSize = (int) (height * 0.2);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }



    @Override
    //CameraBridgeViewBase.CvCameraViewFrame
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame  inputFrame) {


        mRgba = inputFrame.rgba();
        Mat resizeimage = new Mat();
        Size sz = new Size(600,600);
        Imgproc.resize( mRgba, resizeimage, sz );

        GaussianBlur(mRgba, mRgba,new Size(11, 11), 0);
        Imgproc.cvtColor(mRgba,mRgba ,Imgproc.COLOR_BGR2HSV);
        //loop over the color ranges
        //for (lower, upper, colorName) in colorRanges:
		//construct a mask for all colors in the current HSV range, then
		//perform a series of dilations and erosions to remove any small
		//blobs left in the mask
        Core.inRange(mRgba, new Scalar(57, 68, 0),new Scalar(151, 255, 255),mRgba );
        Imgproc.erode(mRgba, mRgba, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
        Imgproc.dilate(mRgba, mRgba, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));

        //finding countours:
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mRgba, contours,Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
         if (contours.size()>0){
             //https://gurus.pyimagesearch.com/object-tracking-in-video/

         }

        return mRgba ;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        double yLow = (double)mOpenCvCameraView.getHeight() * 0.2401961;
        double yHigh = (double)mOpenCvCameraView.getHeight() * 0.7696078;

        double xScale = (double)cols / (double)mOpenCvCameraView.getWidth();
        double yScale = (double)rows / (yHigh - yLow);

        /* coordonnees de la region de touche */
        x = event.getX();
        y = event.getY();
        y = y - yLow;
        x = x * xScale;
        y = y * yScale;
        /* fin coordonnees de la region de touche */

        if((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;
        //affihe les coordonnes de click a l'ecran:
        touch_coordinates.setText("X: " + Double.valueOf(x) + ", Y: " + Double.valueOf(y));

        //defini une region rectangulaire pour detecter la couleur
        Rect touchedRect = new Rect();
        touchedRect.x = (int)x;
        touchedRect.y = (int)y;
        touchedRect.width = 8;
        touchedRect.height = 8;
        Mat touchedRegionRgba = mRgba.submat(touchedRect);



        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);




        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = convertScalarHsv2Rgba(mBlobColorHsv);

        //recupere la couleur de la region touchee par l user:
        int mycolor = Color.rgb((int) mBlobColorRgba.val[0], (int) mBlobColorRgba.val[1], (int) mBlobColorRgba.val[2]);
        Log.v(TAG,"couleur: " + mycolor);
        touch_color.setText("Color: #" + String.format("%02X", (int)mBlobColorRgba.val[0])
                + String.format("%02X", (int)mBlobColorRgba.val[1])
                + String.format("%02X", (int)mBlobColorRgba.val[2]));


        //change la couleur de la region touchee par l'user:
        touch_color.setTextColor(mycolor);

        //change la couleur du text sur les coordonnees en la couleur de la region touchee:
        touch_coordinates.setTextColor(mycolor);

        return false;
    }

    private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

}
