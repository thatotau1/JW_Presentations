package com.example.JW_Presentations;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class Socket_Handler {
    private Socket mSocket ;
    private static Socket_Handler instance;
    private Socket_Handler(){

    }
    public static Socket_Handler getInstance(){
        if(instance==null){
            instance = new Socket_Handler();
        }
        return instance;
    }

    public void setmSocket() {
        {
            try {
                if(mSocket==null) {
                    mSocket = IO.socket("http://"+Global_var.global_ip_address);
                }else{
                    mSocket.close();
                    mSocket = IO.socket("http://"+Global_var.global_ip_address);
                }
            } catch (URISyntaxException e) {
            }
        }
    }
    public Socket getmSocket(){
        return mSocket;
    }
}
