package com.example.zenbo_prac_server;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.asus.robotframework.API.RobotCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.asus.robotframework.API.RobotCommand;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.WheelLights;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends RobotActivity {
    TextView ip_tt;
    String name, tmp_ip, tmp_portm, ip;
    List<Socket> clients = new ArrayList<Socket>();
    ServerSocket server;

    public MainActivity(RobotCallback robotCallback, RobotCallback.Listen robotListenCallback) {
        super(robotCallback, robotListenCallback);
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    System.out.println("ip1--:" + inetAddress);
                    System.out.println("ip2--:" + inetAddress.getHostAddress());

                    // for getting IPV4 format
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("test", "5616");
        ip = getLocalIpAddress();
        Log.d("test", "5616");
        ip_tt = (TextView) findViewById(R.id.ip_t);
//        ip = "123";
        ip_tt.setText(ip);
        Log.d("test", "5616");


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server = new ServerSocket(7100);
                    robotAPI.robot.setExpression(RobotFace.HIDEFACE);

                    while (!server.isClosed()) {
                        Log.d("test", "1");
                        Socket client = null;
                        try{
                            client = server.accept();// 使服务端处于监听状态
                            robotAPI.robot.setExpression(RobotFace.HAPPY, "hello");
                        }catch (Exception e){
                            Log.d("test", e.toString());
                        }

                        Log.d("test", "0");

                        new ChatThread(client).start();
                    }

                } catch(Exception e) {
                    Log.d("connection", e.toString());
                }
            }
        });
        thread.start();
    }

    public class ChatThread extends Thread {
        Socket client;
        BufferedReader br;
        public ChatThread(Socket c) {
            super();
            this.client = c;
            try {
                br = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            super.run();
            String content = null;
            try {
                while ((content = br.readLine()) != null) {
                    JSONObject jsonObj = new JSONObject(content); //轉JSON物件
                    String message = jsonObj.getString("message");
                    String err = jsonObj.getString("error");
                    if(err.equals("")){
                        if(message.equals("shy")){
                            robotAPI.robot.setExpression(RobotFace.SHY);
                        }
                        else if(message.equals("lazy")){
                            robotAPI.robot.setExpression(RobotFace.LAZY);
                        }
                        else if(message.equals("proud")){
                            robotAPI.robot.setExpression(RobotFace.PROUD);
                        }
                        else if (message.equals("red")) {
                            robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0xFF0000);
                        }
                        else if (message.equals("blue")) {
                            robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0xFFFF00);
                        }
                        else if (message.equals("yellow")) {
                            robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0x0000FF);
                        }
                        else if (message.equals("green")) {
                            robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0x008000);
                        }
                        else if (message.equals("orange")) {
                            robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0xFFA500);
                        }
                        else if (message.equals("indigo")) {
                            robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0x4B0082);
                        }
                        else if (message.equals("purple")) {
                            robotAPI.wheelLights.setColor(WheelLights.Lights.SYNC_BOTH, 0xff, 0x800080);
                        }

                    }
                    else{
                        clients.remove(client);

                        robotAPI.robot.setExpression(RobotFace.HIDEFACE, "byebye");
                        client.close();
                        break;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

    }


    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);

            Log.d("RobotDevSample", "onResult:"
                    + RobotCommand.getRobotCommand(cmd).name()
                    + ", serial:" + serial + ", err_code:" + err_code
                    + ", result:" + result.getString("RESULT"));
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }
    };


    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {

        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {

        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };
    public MainActivity() {
        super(robotCallback, robotListenCallback);
    }
}