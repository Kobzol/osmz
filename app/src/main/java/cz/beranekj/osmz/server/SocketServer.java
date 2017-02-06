package cz.beranekj.osmz.server;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer extends Thread
{
	private ServerSocket serverSocket;
	private final int port = 8080;
	private boolean bRunning;

    private final HttpHandler handler;

    public SocketServer(HttpHandler handler)
    {
        this.handler = handler;
    }

    public boolean isRunning()
    {
        return this.bRunning;
    }

    public void close()
    {
		try
        {
			serverSocket.close();
		}
        catch (IOException e)
        {
			Log.d("SERVER", "Error, probably interrupted in accept(), see log");
			e.printStackTrace();
		}
		bRunning = false;
	}
	
	public void run()
    {
        try
        {
        	Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(port);
            bRunning = true;
            while (bRunning)
            {
            	Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept(); 
                Log.d("SERVER", "Socket Accepted");

                this.handler.handleConnection(s.getInputStream(), s.getOutputStream());
	            
                s.close();
                Log.d("SERVER", "Socket Closed");
            }
        } 
        catch (IOException e)
        {
            if (serverSocket != null && serverSocket.isClosed())
            	Log.d("SERVER", "Normal exit");
            else
            {
            	Log.d("SERVER", "Error");
            	e.printStackTrace();
            }
        }
        finally
        {
        	serverSocket = null;
        	bRunning = false;
        }
    }
}
