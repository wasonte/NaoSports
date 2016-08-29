package tfg.jorgealcolea.naosports;

import android.graphics.Bitmap;
import android.util.Log;

import com.aldebaran.qi.CallError;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.EventCallback;
import com.aldebaran.qi.helper.proxies.ALMemory;
import com.aldebaran.qi.helper.proxies.ALMotion;
import com.aldebaran.qi.helper.proxies.ALRedBallDetection;
import com.aldebaran.qi.helper.proxies.ALTextToSpeech;
import com.aldebaran.qi.helper.proxies.ALTracker;
import com.aldebaran.qi.helper.proxies.ALVideoDevice;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by george on 18/08/16.
 */
public class RobotSession {

    private static final String TAG = "RobotSession";

    // Singleton
    private static RobotSession singleton = new RobotSession();

    private RobotSession(){}

    public static RobotSession getInstance(){
        return singleton;
    }

    // Game elements
    private String playerName;
    private String rivalName;

    private int playerScore;
    private int rivalScore;

    private String mode;


    // Nao elements
    public static final String HeadYaw = "HeadYaw";
    public static final String HeadPitch = "HeadPitch";

    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;

    private Session session;
    private String ip;

    private ALMotion alMotion = null;
    private ALTextToSpeech alSpeech;

    private ALMemory alMemory;
    private ALRedBallDetection alRedBall;
    private ALTracker alTracker;
    private boolean subscribedToRedBall;

    private ALVideoDevice video;
    private String moduleName;

    // Accelerometer movements
    private List<Float> movementListX = null;
    private List<Float> movementListY = null;


    ////////////////////
    //
    // Rutine methods
    //
    ////////////////////

    public void startServiceRoutine(String ip, boolean mode) throws Exception {
        this.ip = ip;
        this.mode = (mode)?"versus":"solo";
        session = new Session();


        String ipAddress = ip;
        if (!ipAddress.contains(".")) {
            InetAddress[] inets = InetAddress.getAllByName(ipAddress);
            if (inets != null && inets.length > 0)
                ipAddress = inets[0].getHostAddress();
        }
        Log.i(TAG, "Ip address : " + ipAddress);
        session.connect("tcp://" + ipAddress + ":9559").get();

        // Modules
        alMotion = new ALMotion(session);
        alMotion.wakeUp();

        alSpeech = new ALTextToSpeech(session);

        alMemory = new ALMemory(session);
        alRedBall = new ALRedBallDetection(session);
        alRedBall.subscribe("redBallDetected");
        subscribedToRedBall = true;
        alTracker = new ALTracker(session);
        alTracker.setMode("Move");
        alTracker.registerTarget("RedBall", 0.12);
        //alTracker.track("RedBall");
        alMemory.subscribeToEvent("redBallDetected", new EventCallback() {
            @Override
            public void onEvent(Object event) throws InterruptedException, CallError {

                if (subscribedToRedBall == true) {
                    subscribedToRedBall = false;
                    //alRedBall.unsubscribe("redBallDetected");
                    if (event != null) {
                        alSpeech.say("I have found the ball");
                    }
                }
            }
        });

        // Camera
        int topCamera = 0;
        int resolution = 0; // 640 x 4807
        int colorspace = 11; // RGB
        int frameRate = 10; // FPS

        video = new ALVideoDevice(session);
        moduleName = video.subscribeCamera("demoAndroid", topCamera, resolution, colorspace, frameRate);
    }


    ////////////////////
    //
    // Video Methods
    //
    ////////////////////
    public void unsuscribeToVideo(){
        try {
            video.unsubscribe(moduleName);
        } catch (CallError callError) {
            callError.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public Bitmap getVideo() throws Exception {

        List<Object> image = (List<Object>) video.getImageRemote(moduleName);
        video.releaseImage(moduleName);
        ByteBuffer buffer = (ByteBuffer)image.get(6);
        byte[] rawData = buffer.array();

        int[] intArray = new int[HEIGHT * WIDTH];
        for (int i = 0; i < HEIGHT * WIDTH; i++) {
//            ((255 & 0xFF) << 24) | // alpha
            intArray[i] =
                    ((rawData[(i * 3)] & 0xFF) << 16) | // red
                            ((rawData[i * 3 + 1] & 0xFF) << 8) | // green
                            ((rawData[i * 3 + 2] & 0xFF)); // blue
        }

        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565);
        bitmap.setPixels(intArray, 0, WIDTH, 0, 0, WIDTH, HEIGHT);

        return bitmap;
    }

    ////////////////////
    //
    // BallTracker
    //
    ////////////////////

    public void registerBallTracker(){
        try {
            alTracker.track("RedBall");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void unRegisterBallTracker(){

        try {
            alTracker.stopTracker();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    ////////////////////
    //
    // Movement methods
    //
    ////////////////////
    public void moveHead(String joint, float newMovement){
        if (getHeadMovementList(joint) == null){
            initHeadMovementList(joint, true);
        }
        if (Math.abs(newMovement) > 0.3){
            getHeadMovementList(joint).add(newMovement);
        }
        if (Math.abs(newMovement) < 0.3 && getHeadMovementList(joint).size() > 0){
            float positive = 0;
            float negative = 0;
            boolean sign = (getHeadMovementList(joint).get(0) > 0)?true:false;

            for (Float move : getHeadMovementList(joint)){
                if (move > 0){
                    positive += move;
                } else {
                    negative += Math.abs(move);
                }
            }
            if (positive > 0 && negative > 0){
                try {
                    if (sign){
                        lookTo(joint, -headMovementToRadians(joint, positive));
                    } else {
                        lookTo(joint, headMovementToRadians(joint, negative));
                    }
                } catch(Exception except) {
                    except.printStackTrace();
                }
            }
            initHeadMovementList(joint, false);
        }
    }


    public List<Float> getHeadMovementList(String joint){
        if (joint.equals(HeadYaw)){
            return movementListX;
        } else if (joint.equals(HeadPitch)){
            return movementListY;
        }
        return null;
    }

    public void initHeadMovementList(String joint, boolean init){
        List<Float> empty = new ArrayList<>();
        if (joint.equals(HeadYaw)){
            movementListX = (init)?empty:null;
        } else if (joint.equals(HeadPitch)){
            movementListY = (init)?empty:null;
        }
    }

    public void lookTo(String joint,float delta) throws InterruptedException, CallError{
        if (joint.equals(HeadYaw)){
            lookRightOrLeft(delta);
        } else if (joint.equals(HeadPitch)){
            lookUpAndDown(delta);
        }
    }

    public void lookUpAndDown(Float delta) throws InterruptedException, CallError {
        alMotion.changeAngles(HeadPitch, delta, 0.5F);
    }

    public void lookRightOrLeft(Float delta) throws InterruptedException, CallError {
        alMotion.changeAngles(HeadYaw, delta, 0.5F);
    }

    public float headMovementToRadians(String joint,float movement){
        float maxRadians = 0F;
        float maxMovement = 0F;

        if (joint.equals(HeadYaw)){
            maxRadians = 2*2.0857F;
            maxMovement = 6.0F;
        } else if (joint.equals(HeadPitch)){
            maxRadians = 1.1869F;
            maxMovement = 4F;
        }

        if (Math.abs(movement) > maxMovement){
            return maxRadians;
        } else {
            return maxRadians*movement/maxMovement;
        }
    }

    private float velocityX = 0f;
    private float velocityY = 0f;


    public void onGoToFront() throws InterruptedException, CallError {
        velocityX = 1f;
        velocityY = 0f;
        alMotion.moveToward(velocityX, velocityY, 0f);
    }

    public void onStop() throws InterruptedException, CallError {
        velocityX = 0f;
        velocityY = 0f;
        alMotion.moveToward(velocityX, velocityY, 0f);
    }


    public void onGoToLeft() throws InterruptedException, CallError {
        velocityX = 0f;
        velocityY = 0f;
        alMotion.moveToward(velocityX, velocityY, 0.5f);
    }

    public void onGoToRight() throws InterruptedException, CallError {
        velocityX = 0f;
        velocityY = 0f;
        alMotion.moveToward(velocityX, velocityY, -0.5f);
    }

    public void onGoToBack() throws InterruptedException, CallError {
        velocityX = -1f;
        velocityY = 0f;
        alMotion.moveToward(velocityX, velocityY, 0f);
    }

    ////////////////////
    //
    // Getters and Setters
    //
    ////////////////////

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean isSubscribedToRedBall() {
        return subscribedToRedBall;
    }

    public void setSubscribedToRedBall(boolean subscribedToRedBall) {
        this.subscribedToRedBall = subscribedToRedBall;
    }

    public Boolean isAlMotion() {
        return (alMotion != null)?true:false;
    }

    public String getMode() {
        return mode;
    }


    public String getRivalName() {
        return rivalName;
    }

    public void setRivalName(String rivalName) {
        this.rivalName = rivalName;
    }

    public int getRivalScore() {
        return rivalScore;
    }

    public void setRivalScore(int rivalScore) {
        this.rivalScore = rivalScore;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public void setPlayerScore(int playerScore) {
        this.playerScore = playerScore;
    }
}
