// Copyright 2019 The MediaPipe Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.mediapipemultihandstracking;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import com.example.mediapipemultihandstracking.basic.BasicActivity;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.formats.proto.ClassificationProto.ClassificationList;

import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main activity of MediaPipe multi-hand tracking app.
 */
public class MainActivity extends BasicActivity {
    private static final String TAG = "MainActivity";

    private List<NormalizedLandmarkList> multiHandLandmarks;

    private static final String INPUT_NUM_HANDS_SIDE_PACKET_NAME = "num_hands";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks";
    // Max number of hands to detect/process.
    private static final int NUM_HANDS = 2;

    private static final String OUTPUT_HANDEDNESS_PACKET_NAME = "handedness";

    private String currentHand;
    private TextView gestureLeft;
    private TextView gestureRight;
    private ImageView explosion;

    private MyHand leftHand = new MyHand("Left");
    private MyHand rightHand = new MyHand("Right");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gestureLeft = findViewById(R.id.gesture_left); // left hand gesture
        gestureRight = findViewById(R.id.gesture_right); //right hand gesture
        explosion = findViewById(R.id.image_explosion); // left and right hand collide

        explosion.setVisibility(View.GONE);
        getSupportActionBar().hide();

        AndroidPacketCreator packetCreator = processor.getPacketCreator();
        Map<String, Packet> inputSidePackets = new HashMap<>();
        inputSidePackets.put(INPUT_NUM_HANDS_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_HANDS));
        processor.setInputSidePackets(inputSidePackets);

        processor.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
                    Log.d(TAG, "Received multi-hand landmarks packet.");
                    multiHandLandmarks =
                            PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            int handIndex = 0;

                            if (multiHandLandmarks.size() ==1) { // one hand only,  either left or right
                                for (NormalizedLandmarkList landmarks : multiHandLandmarks) {

                                    if (currentHand.equals("Left")) {
                                        rightHand.setActive(false);
                                        leftHand.setGesture(landmarks.getLandmarkList());
                                        gestureLeft.setText(leftHand.getGesture());
                                    }

                                    if (currentHand.equals("Right")) {
                                        leftHand.setActive(false);
                                        rightHand.setGesture(landmarks.getLandmarkList());
                                        gestureRight.setText(rightHand.getGesture());
                                    }
                                }
                            }

                            if (multiHandLandmarks.size() >1) { // two hands
                                for (NormalizedLandmarkList landmarks : multiHandLandmarks) {

                                    Log.v(TAG, "[" + handIndex + "]: " + landmarks.getLandmarkCount() + "=>Last Hand:" + currentHand );

                                    // index 0, 1 is not always pointed to Left, Right Hand. have to look for previous captured hand and assign the priority
                                    if (currentHand.equals("Right")) {
                                        if (handIndex == 0) {
                                            leftHand.setGesture(landmarks.getLandmarkList());
                                            gestureLeft.setText(leftHand.getGesture());
                                        }
                                        if (handIndex == 1) {
                                            rightHand.setGesture(landmarks.getLandmarkList());
                                            gestureRight.setText(rightHand.getGesture());
                                        }
                                    }

                                    if (currentHand.equals("Left")) {
                                        if (handIndex == 0) {
                                            rightHand.setGesture(landmarks.getLandmarkList());
                                            gestureRight.setText(rightHand.getGesture());
                                        }
                                        if (handIndex == 1) {
                                            leftHand.setGesture(landmarks.getLandmarkList());
                                            gestureLeft.setText(leftHand.getGesture());
                                        }
                                    }

                                    ++handIndex;
                                }
                            }

                            // index fingers connected?
                            if (leftHand.connectWith(rightHand))
                                explosion.setVisibility(View.VISIBLE);
                            else
                                explosion.setVisibility(View.GONE);

                        }

                    });

                });

                processor.addPacketCallback(
                        OUTPUT_HANDEDNESS_PACKET_NAME,
                        (packet) -> {
                            Log.v(TAG, "Received multi-handness packet.");
                            List<ClassificationList> multiHandedness =
                                    PacketGetter.getProtoVector(packet, ClassificationList.parser());
                            if (multiHandedness.size() == 1){
                                for (ClassificationList handedness : multiHandedness) {
                                    currentHand = handedness.getClassificationList().get(0).getLabel().trim();
                                }
                            }
                        });

    }

    private String getMultiHandLandmarksDebugString(List<NormalizedLandmarkList> multiHandLandmarks) {
        if (multiHandLandmarks.isEmpty()) {
            return "No hand landmarks";
        }
        String multiHandLandmarksStr = "Number of hands detected: " + multiHandLandmarks.size() + "\n";
        int handIndex = 0;
        for (NormalizedLandmarkList landmarks : multiHandLandmarks) {
            multiHandLandmarksStr +=
                    "\t#Hand landmarks for hand[" + handIndex + "]: " + landmarks.getLandmarkCount() + "\n";
            int landmarkIndex = 0;
            for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {
                if (landmarkIndex==8) {
                    multiHandLandmarksStr +=
                            "\t\tLandmark ["
                                    + landmarkIndex
                                    + "]: ("
                                    + landmark.getX()
                                    + ", "
                                    + landmark.getY()
                                    + ", "
                                    + landmark.getZ()
                                    + ")\n";
                    ++landmarkIndex;
                }
            }
            ++handIndex;
        }
        return multiHandLandmarksStr;
    }

}
