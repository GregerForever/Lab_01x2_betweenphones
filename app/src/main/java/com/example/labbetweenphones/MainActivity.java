package com.example.labbetweenphones;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    DatagramSocket socket;

    EditText ipText;
    EditText portSendText;
    EditText portReceiveText;
    EditText messageText;
    EditText nameText;

    TextView chatText;

    String sendS;
    String receiveS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipText = findViewById(R.id.IPBox);
        portSendText = findViewById(R.id.SendBox);
        portReceiveText = findViewById(R.id.RecBox);
        messageText = findViewById(R.id.ContentBox);
        nameText = findViewById(R.id.NickBox);
        chatText = findViewById(R.id.ChatText);

        int portReceive = Integer.parseInt(portReceiveText.getText().toString());

        try {
            InetAddress localNetwork = InetAddress.getByName("0.0.0.0");
            SocketAddress localAddress = new InetSocketAddress(localNetwork, portReceive);
            socket = new DatagramSocket(localAddress);
        }
        catch (UnknownHostException | SocketException e)
        {
            e.printStackTrace();
        }

        Runnable receiver = () ->{
            byte[] receiveBuffer = new byte[500];
            DatagramPacket receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            while (true)
            {
                try {
                    socket.receive(receivedPacket);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                receiveS = new String(receiveBuffer, 0, receivedPacket.getLength());

                String name;
                String message;
                String[] s = receiveS.split("%", 2);

                name = s[0];
                message = s[1];

                runOnUiThread(() ->
                {
                    String allReception = chatText.getText().toString();

                    chatText.setText(allReception + "\n" + name + ": " + message);
                });
                Log.e("MESSAGE",receiveS);
            }
        };

        Thread receivingThread = new Thread(receiver);
        receivingThread.start();

        chatText.setMovementMethod(new ScrollingMovementMethod());
    }

    DatagramPacket sendPacket;

    public void onClick(View v)
    {
        String name = nameText.getText().toString();
        String mess = messageText.getText().toString();
        String ip = ipText.getText().toString();

        int portSend = Integer.parseInt(portSendText.getText().toString());

        sendS = name + '%' + mess;

        byte[] sendBuffer = sendS.getBytes(StandardCharsets.UTF_8);

        try
        {
            InetAddress remoteAddress = InetAddress.getByName(ip);
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, remoteAddress, portSend);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }

        sendPacket.setLength(sendS.length());

        Runnable r = () -> {
            try {
                socket.send(sendPacket);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        };

        Thread sendingThread = new Thread(r);
        sendingThread.start();

        String allReception = chatText.getText().toString();
        chatText.setText(allReception + "\n" + name + ": " + mess);

    }

}