package com.example.mediapipemultihandstracking;

// Please check the document for hand detection and tracking
// https://google.github.io/mediapipe/solutions/hands.html

import android.util.Log;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;

import java.util.List;

public class MyHand {

    private static final String TAG = "MyHand";
    private boolean activeHand = false;
    private String whichHand = "";
    private String activeGesture = "";
    private List<NormalizedLandmark> landmarkList;

    public MyHand(String whichHand){      //constructor 1
        this.whichHand = whichHand;
    }

    public void updateLandMarks(List<NormalizedLandmark> landmarkList)  {
        this.landmarkList = landmarkList;
    }

    public void setActive(boolean YesNo)  {
        this.activeHand = YesNo;
    }

    public boolean isActive()  {
        return this.activeHand;
    }

    public void setGesture(List<NormalizedLandmark> landmarkList)  {
        this.activeHand = true;
        this.landmarkList = landmarkList;
        String gestureText = this.handGestureCalculator();
        this.activeGesture = gestureText;
    }

    public String getGesture()  {
        return this.activeGesture;
    }

    public String handGestureCalculator() {

        //if (multiHandLandmarks.isEmpty()) {
        //    return "No hand deal";
       // }

        landmarkList = this.landmarkList;

        boolean thumbIsOpen = false;
        boolean firstFingerIsOpen = false;
        boolean secondFingerIsOpen = false;
        boolean thirdFingerIsOpen = false;
        boolean fourthFingerIsOpen = false;


        float pseudoFixKeyPoint = landmarkList.get(2).getX();
        if (pseudoFixKeyPoint < landmarkList.get(9).getX()) {
            if (landmarkList.get(3).getX() < pseudoFixKeyPoint && landmarkList.get(4).getX() < pseudoFixKeyPoint) {
                thumbIsOpen = true;
            }
        }
        if (pseudoFixKeyPoint > landmarkList.get(9).getX()) {
            if (landmarkList.get(3).getX() > pseudoFixKeyPoint && landmarkList.get(4).getX() > pseudoFixKeyPoint) {
                thumbIsOpen = true;
            }
        }

        //Log.d(TAG, "pseudoFixKeyPoint == " + pseudoFixKeyPoint + "\nlandmarkList.get(2).getX() == " + landmarkList.get(2).getX()
        //        + "\nlandmarkList.get(4).getX() = " + landmarkList.get(4).getX());

        pseudoFixKeyPoint = landmarkList.get(6).getY();
        if (landmarkList.get(7).getY() < pseudoFixKeyPoint && landmarkList.get(8).getY() < landmarkList.get(7).getY()) {
            firstFingerIsOpen = true;
        }
        pseudoFixKeyPoint = landmarkList.get(10).getY();
        if (landmarkList.get(11).getY() < pseudoFixKeyPoint && landmarkList.get(12).getY() < landmarkList.get(11).getY()) {
            secondFingerIsOpen = true;
        }
        pseudoFixKeyPoint = landmarkList.get(14).getY();
        if (landmarkList.get(15).getY() < pseudoFixKeyPoint && landmarkList.get(16).getY() < landmarkList.get(15).getY()) {
            thirdFingerIsOpen = true;
        }
        pseudoFixKeyPoint = landmarkList.get(18).getY();
        if (landmarkList.get(19).getY() < pseudoFixKeyPoint && landmarkList.get(20).getY() < landmarkList.get(19).getY()) {
            fourthFingerIsOpen = true;
        }

        // Hand gesture recognition
        if (thumbIsOpen && firstFingerIsOpen && secondFingerIsOpen && thirdFingerIsOpen && fourthFingerIsOpen) {
            return "FIVE";
        } else if (!thumbIsOpen && firstFingerIsOpen && secondFingerIsOpen && thirdFingerIsOpen && fourthFingerIsOpen) {
            return "FOUR";
        } else if (thumbIsOpen && firstFingerIsOpen && secondFingerIsOpen && !thirdFingerIsOpen && !fourthFingerIsOpen) {
            return "TREE";
        } else if (thumbIsOpen && firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && !fourthFingerIsOpen) {
            return "TWO";
        } else if (!thumbIsOpen && firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && !fourthFingerIsOpen) {
            return "ONE";
        } else if (!thumbIsOpen && firstFingerIsOpen && secondFingerIsOpen && !thirdFingerIsOpen && !fourthFingerIsOpen) {
            return "YEAH";
        } else if (!thumbIsOpen && firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && fourthFingerIsOpen) {
            return "ROCK";
        } else if (thumbIsOpen && firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && fourthFingerIsOpen) {
            return "Spider-Man";
        } else if (!thumbIsOpen && !firstFingerIsOpen && !secondFingerIsOpen && !thirdFingerIsOpen && !fourthFingerIsOpen) {
            return "FIST";
        } else if (!firstFingerIsOpen && secondFingerIsOpen && thirdFingerIsOpen && fourthFingerIsOpen && isNearDistance(landmarkList.get(4), landmarkList.get(8), 0.1)) {
            return "OK";
        } else {
            String info = "thumbIsOpen " + thumbIsOpen + "firstFingerIsOpen" + firstFingerIsOpen
                    + "secondFingerIsOpen" + secondFingerIsOpen +
                    "thirdFingerIsOpen" + thirdFingerIsOpen + "fourthFingerIsOpen" + fourthFingerIsOpen;
            Log.d(TAG, "handGestureCalculator: == " + info);
            return "";
        }

    }


    public boolean connectWith(MyHand anotherHand){

        if (!this.activeHand || !anotherHand.activeHand) return false;

        try {
            return isNearDistance(this.landmarkList.get(8),anotherHand.landmarkList.get(8), 0.05);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isNearDistance(LandmarkProto.NormalizedLandmark point1, LandmarkProto.NormalizedLandmark point2, double minDistance) {
        double distance = getEuclideanDistanceAB(point1.getX(), point1.getY(), point2.getX(), point2.getY());
        return distance < minDistance;
    }

    private double getEuclideanDistanceAB(double a_x, double a_y, double b_x, double b_y) {
        double dist = Math.pow(a_x - b_x, 2) + Math.pow(a_y - b_y, 2);
        return Math.sqrt(dist);
    }

}

