/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gcm.play.android.samples.com.gcmsender;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// NOTE:
// This class emulates a server for the purposes of this sample,
// but it's not meant to serve as an example for a production app server.
// This class should also not be included in the client (Android) application
// since it includes the server's API key. For information on GCM server
// implementation see: https://developers.google.com/cloud-messaging/server
public class GcmSender {

    public static final String API_KEY = "AIzaSyBK1uR_FSBXHh6n-iE_himhz47buZVVI7g";

    public static String playerToken;
    public static String rivalToken;
    public static String mode;

    public static void main(String[] args) {

        // TODO this fields should be setted by the clients
        playerToken = "fv3H7A4KzkQ:APA91bHFs3H4ezmD9_yoMxZCGq-67RyEtplo5Ry95pJZ5DQ7l0MTin8Hp-ed7sAuZkVQ7A1-AMm9MiVRrinTc_DxicJcjoKTMdb4_jqLKdP8vDG8X31LnvJV0PRUu3vh2kegfWLl56R0";
        rivalToken  = "";
        //mode = "solo";
        mode = "versus";

        if (args.length < 1) {
            System.exit(1);
        }

        if (mode.equals("solo") && args.length > 2){
            System.exit(1);
        }

        if(mode.equals("versus") && args[0].trim().equals("start") && args.length > 1){
            System.exit(1);
        } else if (mode.equals("versus") && args[0].trim().equals("score") && args.length != 3){
            System.exit(1);
        }


        /*

        { "data": {
                "score": "5x1",
                "time": "15:10"
            },
            "registration_ids": ["4", "8", ...]
        }


        ///////// Script exameples
        ./gradlew run -Paction="start"
        ./gradlew run -Paction="score" -Pplayer="3"
        ./gradlew run -Paction="score" -Pplayer="3" -Prival="1"

         */

        //What to send
        JSONObject jData = new JSONObject();
        jData.put("action", args[0].trim());

        // Where to send
        JSONObject jGcmData = new JSONObject();
        List<String> idList = new ArrayList<>();

        if (mode.equals("solo")){
            jData.put("playerScore", args[1].trim());
            idList.add(playerToken);
        } else {
            if(args[0].equals("score")){
                jData.put("playerScore", args[1].trim());
                jData.put("rivalScore", args[2].trim());
            }
            idList.add(playerToken);
            idList.add(rivalToken);
        }
        jGcmData.put("data", jData);
        jGcmData.put("registration_ids", idList);

        sendPushNotification(jGcmData);
    }

    public static void sendPushNotification(JSONObject gcmData){
        try {
            // Create connection to send GCM Message request.
            URL url = new URL("https://android.googleapis.com/gcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "key=" + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Send GCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(gcmData.toString().getBytes());

            // Read GCM response.
            InputStream inputStream = conn.getInputStream();
            String resp = IOUtils.toString(inputStream);
            System.out.println(resp);
            System.out.println("Check your device/emulator for notification or logcat for " +
                    "confirmation of the receipt of the GCM message.");
        } catch (IOException e) {
            System.out.println("Unable to send GCM message.");
            System.out.println("Please ensure that API_KEY has been replaced by the server " +
                    "API key, and that the device's registration token is correct (if specified).");
            e.printStackTrace();
        }
    }

}
