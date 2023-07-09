package com.indialone.server_socketprogrammingdemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static InputStreamReader inputStreamReader;
    private static BufferedReader bufferedReader;

    private Socket socket;
    private PrintWriter printWriter;
    @Nullable private static String message = "";
    private static final int PORT = 8080;

    private TextView tvMessages, tvServerDetails;

    private EditText etMessage;
    private Button btnSendMessage;

    private static Thread thread1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        try {
            serverSocket = new ServerSocket(PORT);
            tvServerDetails.append("Address: " + getLocalIpAddress() + "\nPort: " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        thread1 = new Thread(new Thread1());
        thread1.start();

        btnSendMessage.setOnClickListener((view) -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                new Thread(new SendMessageThread(message)).start();
            }
        });

    }

    private void initializeViews() {
        etMessage = findViewById(R.id.et_message);
        btnSendMessage = findViewById(R.id.btn_send_message);
        tvServerDetails = findViewById(R.id.tv_server_details);
        tvMessages = findViewById(R.id.tv_messages);
        tvMessages.setMovementMethod(new ScrollingMovementMethod());
    }

    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipInt = wifiInfo.getIpAddress();
            return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
        }
        return null;
    }

    class Thread1 implements Runnable {
        @Override
        public void run() {
            while (!message.equalsIgnoreCase("over")) {
                try {
                    clientSocket = serverSocket.accept();
                    inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                    bufferedReader = new BufferedReader(inputStreamReader);
                    message = bufferedReader.readLine();
                    runOnUiThread(() -> {
                        tvMessages.append(message + "\n");
                    });
                    inputStreamReader.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class SendMessageThread implements Runnable {

        private String messageValue;

        public SendMessageThread(String message) {
            messageValue = message;
        }

        @Override
        public void run() {
            try {
                String ipAddress = getLocalIpAddress();
                if (ipAddress != null && !ipAddress.isEmpty()) {
                    Socket socket = new Socket(ipAddress, PORT);
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    writer.write("Server: " + messageValue);
                    writer.flush();
                    writer.close();
                    socket.close();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}