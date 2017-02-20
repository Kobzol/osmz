package cz.beranekj.osmz.net.server;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import cz.beranekj.osmz.net.http.HttpHandler;

public class SingleThreadServer extends NetServer
{
	private ServerSocket serverSocket;
	private final int port;
    private HashSet<Socket> sockets = new HashSet<>();
    private Thread listenerThread;

    public SingleThreadServer(HttpHandler handler, int port)
    {
        super(handler);
        this.port = port;
    }

    @Override
    public void start()
    {
        if (this.isRunning()) return;

        this.listenerThread = new Thread(this::run);
        this.listenerThread.start();

        this.isRunning = true;
    }

    @Override
    public void stop()
    {
        if (!this.isRunning()) return;

        this.isRunning = false;

        // close clients
        try
        {
            for (Socket socket : this.sockets)
            {
                socket.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // close server
        try
        {
            this.serverSocket.close();
        }
        catch (IOException e)
        {
            Log.d("SERVER", "Error, probably interrupted in accept(), see log");
            e.printStackTrace();
        }

        // wait for thread
        try
        {
            this.listenerThread.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
	
	public void run()
    {
        try
        {
        	Log.d("SERVER", "Creating Socket");
            this.serverSocket = new ServerSocket(port);
            while (this.isRunning())
            {
            	Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept();
                this.sockets.add(s);
                Log.d("SERVER", "Socket Accepted");

                this.handler.handleConnection(s.getInputStream(), s.getOutputStream(), this.log);
	            
                s.close();
                this.sockets.remove(s);
                Log.d("SERVER", "Socket Closed");
            }
        } 
        catch (IOException e)
        {
            if (this.serverSocket != null && this.serverSocket.isClosed())
            	Log.d("SERVER", "Normal exit");
            else
            {
            	Log.d("SERVER", "Error");
            	e.printStackTrace();
            }
        }
        finally
        {
        	this.serverSocket = null;
            this.isRunning = false;
        }
    }
}
