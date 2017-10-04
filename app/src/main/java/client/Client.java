package client;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.androidsrc.server.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import static android.content.ContentValues.TAG;

public class Client extends AsyncTask<Void, Void, Void> {

	String dstAddress;
	int dstPort;
	String response = "";
	TextView textResponse;
	FileOutputStream fos = null;
	BufferedOutputStream bos = null;
	Activity activity;

	Client(String addr, int port,TextView textResponse,Activity aActivity) {
		dstAddress = addr;
		activity = aActivity;
		dstPort = port;
		this.textResponse=textResponse;
	}

	@Override
	protected Void doInBackground(Void... arg0) {

		Socket socket;
		File backupFile;
		File appFolder;

		try {

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
			backupFile = new File(appFolder, "Video.mp4");
			Log.e(TAG, "file @" + backupFile.getAbsolutePath());

            socket = new Socket(dstAddress, dstPort);

			int bufferSize;

			InputStream is;
			try {
				is = socket.getInputStream();
				bufferSize = socket.getReceiveBufferSize();
				System.out.println("Buffer size: " + bufferSize);
				fos = new FileOutputStream(backupFile);
				bos = new BufferedOutputStream(fos);
				byte[] bytes = new byte[bufferSize];
				int count;
				while ((count = is.read(bytes)) >= 0) {
					bos.write(bytes, 0, count);
					Log.e("writing","yes");
				}
				bos.flush();
				bos.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.e("file write :","completed");

		} catch (UnknownHostException e) {
			e.printStackTrace();
			response = "UnknownHostException: " + e.toString();
		} catch (IOException e) {
			e.printStackTrace();
			response = "IOException: " + e.toString();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		textResponse.setText(response);
		super.onPostExecute(result);
	}

}
