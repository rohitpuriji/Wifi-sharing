package com.androidsrc.server;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import static android.content.ContentValues.TAG;

public class Server {
    MainActivity activity;
    ServerSocket serverSocket;
    String message = "";
    static final int socketServerPORT = 8080;
    SocketServerReplyThread socketServerReplyThread;
    Socket clientSocket;

    public Server(MainActivity activity) {
        this.activity = activity;
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    public int getPort() {
        return socketServerPORT;
    }

    public void onDestroy() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SocketServerThread extends Thread {

        int count = 0;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(socketServerPORT);
                clientSocket = serverSocket.accept();
                FileTxThread fileTxThread = new FileTxThread(clientSocket);
                fileTxThread.run();

/*                while (true) {
                    clientSocket = serverSocket.accept();

                    count++;
                    message += "#" + count + " from "
                            + clientSocket.getInetAddress() + ":"
                            + clientSocket.getPort() + "\n";

                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            activity.msg.setText(message);
                        }
                    });
     *//*               socketServerReplyThread = new SocketServerReplyThread(clientSocket);
                    socketServerReplyThread.run();*//*

                    FileTxThread fileTxThread = new FileTxThread(clientSocket);
                    fileTxThread.run();

                }*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class SocketServerReplyThread extends Thread {

        private Socket hostThreadSocket;

        SocketServerReplyThread(Socket socket) {
            hostThreadSocket = socket;
        }

        @Override
        public void run() {
            OutputStream outputStream;
            String msgReply = "Hello Client";

            try {
                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(msgReply);
                printStream.close();

                message += "replayed: " + msgReply + "\n";

                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        activity.msg.setText(message);
                    }
                });

                FileTxThread fileTxThread = new FileTxThread(hostThreadSocket);
                fileTxThread.run();

            } catch (IOException e) {
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.msg.setText(message);
                }
            });
        }

    }


    public class FileTxThread extends Thread {
        Socket socket;
        File backupFile;
        File appFolder;

        FileTxThread(Socket socket){
            this.socket= socket;
        }

        @Override
        public void run() {

            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
                appFolder = new File(Environment.getExternalStorageDirectory(), activity.getResources().getString(R.string.app_name));
                if (!appFolder.exists())
                    appFolder.mkdir();
                Log.d(TAG,"In External");
            }else {
                ContextWrapper cw = new ContextWrapper(activity);
                appFolder = cw.getDir(activity.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
                if (!appFolder.exists())
                    appFolder.mkdir();
                Log.d(TAG,"In internal");
            }
            backupFile = new File(appFolder.getAbsolutePath(), "/iQuirkpuzzle_video.mp4");
            Log.e(TAG, "file @" + backupFile.getAbsolutePath());

           /* try {
                FileOutputStream f = new FileOutputStream(backupFile);
                PrintWriter pw = new PrintWriter(f);
                pw.println("Hi , How are you");
                pw.println("Hello");
                pw.flush();
                pw.close();
                f.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i(TAG, "******* File not found. Did you" +
                        " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            FileInputStream fis = null;
            BufferedInputStream bis = null;
            BufferedOutputStream out = null;
            byte[] buffer = new byte[8192];
            try {
                fis = new FileInputStream(backupFile);
                bis = new BufferedInputStream(fis);
                out = new BufferedOutputStream(socket.getOutputStream());
                int count;
                while ((count = bis.read(buffer)) > 0) {
                    out.write(buffer, 0, count);
                }
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    out.close();
                    fis.close();
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Server running at : "
                                + inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }
}
