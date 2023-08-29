package com.example.zenbo_prac_client;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btn_send, btn_leave, btn_connect;
    EditText message_t, ip_t;
    TextView chatroom_t;
    String name, ip, port;
    Socket server;

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
        Log.d("test", "10");
        Log.d("test", String.valueOf(server));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViewElement();

        port = "7100";
//        port = "6100";
//        Log.d("tt", ip);


        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ip = ip_t.getText().toString();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("test", ip);

                            server = new Socket(ip, Integer.parseInt(port));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatroom_t.setText(chatroom_t.getText() + "\nsuccess");
                                }
                            });
//                            server = new Socket("10.0.2.2", Integer.parseInt(port));
                            Log.d("test", "1");
                        } catch (IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatroom_t.setText(chatroom_t.getText() + "\nfail " + e.toString());
                                }
                            });
                            Log.d("test", "111");
                            Log.d("test", e.toString());
                            throw new RuntimeException(e);
                        }
                    }
                });
                if(server == null || server.isClosed()){
                    Log.d("test", "reg");
                    chatroom_t.setText(chatroom_t.getText() + "\nconnect");
                    thread.start();
                }
                else {
                    chatroom_t.setText(chatroom_t.getText() + "\nno connect");
                }
            }
        });



        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = message_t.getText().toString();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("connection", "s");
                        try {
                            JSONObject jsonObj = new JSONObject();
                            try {
                                jsonObj.put("message", message);
                                jsonObj.put("error", "");
                                // 添加更多的键值对
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatroom_t.setText(chatroom_t.getText() + "\n: " + message);
                                    message_t.setText("");
                                }
                            });
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
                            bw.write(jsonObj.toString());
                            bw.newLine();
                            bw.flush();
                            Log.d("connection", "sd");
                        } catch (Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatroom_t.setText(chatroom_t.getText() + "\nfail " + e.toString());
                                }
                            });
                            Log.d("connection", e.toString());
                        }
                    }
                });

                if(server == null && server.isClosed()){
                    chatroom_t.setText(chatroom_t.getText() + "\nServer closed. Please press leave button.");
                    message_t.setText("");
                }
                else {
                    thread.start();
                }
            }
        });

        btn_leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(server != null && !server.isClosed()){
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d("connection", "l");
                                JSONObject jsonObj = new JSONObject();
                                try {
                                    jsonObj.put("message", "");
                                    jsonObj.put("error", "close");
                                    // 添加更多的键值对
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
                                bw.write(jsonObj.toString());
                                bw.newLine();
                                bw.flush();
                                server.close();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        chatroom_t.setText(chatroom_t.getText() + "\nsuccess");
                                    }
                                });
                                Log.d("connection", "ld");
                            }
                            catch (Exception ex){
                                Log.d("connection", ex.toString());
                            }
                        }
                    });
                    thread.start();
                }
            }
        });
    }

    private void initViewElement(){
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_leave = (Button) findViewById(R.id.btn_leave);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        message_t = (EditText) findViewById(R.id.message_t);
        ip_t = (EditText) findViewById(R.id.ip_t);
        chatroom_t = (TextView) findViewById(R.id.chatroom_t);
    }

}