//comment avoir le range du bleu: https://gurus.pyimagesearch.com/object-tracking-in-video/

package website.timrobinson.opencvtutorial;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.objdetect.CascadeClassifier;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2HSV;
import static org.opencv.imgproc.Imgproc.COLOR_HSV2RGB_FULL;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2HSV_FULL;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.resize;

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
        Mat frame = new Mat();
        Size sz = new Size(600,600);
        Mat blurred=new Mat();

        GaussianBlur(mRgba, mRgba,new Size(11, 11), 0);
        Imgproc.cvtColor(mRgba,mRgba ,Imgproc.COLOR_BGR2HSV);

       // cvtColor(mRgba,mRgba , COLOR_BGR2HSV);


        Core.inRange(mRgba, new Scalar(57, 68, 0),new Scalar(151, 255, 255),mRgba );


        erode(mRgba, mRgba, getStructuringElement(MORPH_RECT, new Size(5, 5)));
        dilate(mRgba, mRgba, getStructuringElement(MORPH_RECT, new Size(5, 5)));

        //finding countours:
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mRgba.clone(),contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //max contour area:
        double area =0.;
        double tmp=0.;
        int maxindex = -1;
        float[] radius = new float[1];
        Point center = new Point();
        for (int i = 0; i < contours.size(); i++)
        {
            tmp = Imgproc.contourArea(contours.get(i));
           if (tmp>area ) {
              area = tmp;
              maxindex=i;
           }

        }
        Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(maxindex).toArray()),center, radius);
        if (radius[0]>10) {
            //
            Moments M = Imgproc.moments(contours.get(maxindex), true);
            double cX = M.get_m10() /M.get_m00();
            double cY = M.get_m01() / M.get_m00();

            Imgproc.circle( mRgba, center, 4, new Scalar(0,255,255), 2);
            String colorName="bleu";
            Imgproc.putText(mRgba, colorName, new Point(cX, cY), FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(0, 255, 255), 2);

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
        cvtColor(touchedRegionRgba, touchedRegionHsv, COLOR_RGB2HSV_FULL);




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
        cvtColor(pointMatHsv, pointMatRgba, COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

}
