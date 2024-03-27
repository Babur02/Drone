import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.mavlink.messages.MAVLinkMessage;
import org.mavlink.messages.MAV_CMD;
import org.mavlink.messages.MAV_RESULT;
import org.mavlink.messages.MAV_TYPE;
import org.mavlink.messages.common.msg_command_long;
import org.mavlink.messages.common.msg_heartbeat;
import org.mavlink.messages.common.msg_statustext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DroneCommunicationManager {

    private static final String TAG = "DroneCommunication";

    private static final String PREFS_NAME = "DroneDetailsPrefs";

    private String name;
    private String ipAddress;
    private String port;

    public DroneCommunicationManager(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        name = prefs.getString("name", "");
        ipAddress = prefs.getString("ipAddress", "");
        port = prefs.getString("port", "");
    }

    public void connectToDrone() {
        // Establish TCP connection to the drone
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ipAddress, Integer.parseInt(port)), 5000);
            Log.d(TAG, "Connected to drone");
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to drone: " + e.getMessage());
            return;
        }

        // Send MAVLink heartbeat message
        msg_heartbeat heartbeat = new msg_heartbeat();
        heartbeat.type = MAV_TYPE.MAV_TYPE_GCS;
        heartbeat.autopilot = 1;
        heartbeat.system_status = 3;
        heartbeat.base_mode = 0;
        heartbeat.custom_mode = 0;
        heartbeat.mavlink_version = 3;
        MAVLinkMessage heartbeatMsg = heartbeat;
        try {
            socket.getOutputStream().write(heartbeatMsg.encodePacket());
            Log.d(TAG, "Sent MAVLink heartbeat message");
        } catch (IOException e) {
            Log.e(TAG, "Error sending heartbeat message: " + e.getMessage());
            return;
        }

        // Send an example command (you can replace this with actual commands)
        msg_command_long command = new msg_command_long();
        command.target_system = 1; // System ID of the drone
        command.target_component = 1; // Component ID of the drone
        command.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
        command.confirmation = 0;
        command.param1 = 0;
        command.param2 = 0;
        command.param3 = 0;
        command.param4 = 0;
        command.param5 = 0;
        command.param6 = 0;
        command.param7 = 0;
        MAVLinkMessage commandMsg = command;
        try {
            socket.getOutputStream().write(commandMsg.encodePacket());
            Log.d(TAG, "Sent MAVLink command message");
        } catch (IOException e) {
            Log.e(TAG, "Error sending command message: " + e.getMessage());
        }

        // Receive and process incoming messages (for demonstration)
        new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead = socket.getInputStream().read(buffer);
                    if (bytesRead > 0) {
                        MAVLinkMessage message = MAVLinkMessage.createMessage(buffer, bytesRead);
                        if (message instanceof msg_statustext) {
                            msg_statustext statusText = (msg_statustext) message;
                            Log.d(TAG, "Received status text: " + statusText.getText());
                        } else {
                            Log.d(TAG, "Received message: " + message);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error receiving message: " + e.getMessage());
            }
        }).start();
    }
}
