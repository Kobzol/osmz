package cz.beranekj.osmz.net.server;

import android.util.Log;

import com.annimon.stream.function.Consumer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import cz.beranekj.osmz.net.http.HttpHandler;

public class MultiThreadServer extends NetServer
{
    private class ConnectionData
    {
        private final Thread thread;
        private final Socket socket;

        ConnectionData(Thread thread, Socket socket)
        {
            this.thread = thread;
            this.socket = socket;
        }

        public Thread getThread()
        {
            return this.thread;
        }

        public Socket getSocket()
        {
            return this.socket;
        }
    }
    private class ClientThread extends Thread
    {
        private final Socket socket;
        private final HttpHandler handler;
        private final Consumer<ClientThread> onExit;

        private ClientThread(Socket socket, HttpHandler handler, Consumer<ClientThread> onExit)
        {
            this.socket = socket;
            this.handler = handler;
            this.onExit = onExit;
        }

        @Override
        public void run()
        {
            try
            {
                this.handler.handleConnection(this.socket.getInputStream(), this.socket.getOutputStream());
                this.socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                try
                {
                    this.socket.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
            finally
            {
                this.onExit.accept(this);
            }
        }

        public Socket getSocket()
        {
            return this.socket;
        }
    }

    private final int port;

    private HashSet<ClientThread> connections = new HashSet<>();
    private ServerSocket listenerSocket = null;
    private Thread listenerThread = null;

    public MultiThreadServer(HttpHandler handler, int port)
    {
        super(handler);
        this.port = port;
    }

    @Override
    public void start()
    {
        if (this.isRunning()) return;

        this.listenerThread = new Thread(this::listenerLoop);
        this.listenerThread.start();

        this.isRunning = true;
    }

    @Override
    public void stop() throws IOException
    {
        if (!this.isRunning()) return;

        this.isRunning = false;

        // close clients
        for (ClientThread thread : this.connections)
        {
            thread.getSocket().close();
            try
            {
                thread.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        // close server
        try
        {
            this.listenerSocket.close();
        }
        catch (IOException e)
        {
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

    private void listenerLoop()
    {
        try
        {
            Log.d("SERVER", "Creating Socket");
            this.listenerSocket = new ServerSocket(this.port);
            while (this.isRunning())
            {
                Log.d("SERVER", "Socket Waiting for connection");
                Socket client = this.listenerSocket.accept();
                Log.d("SERVER", "Socket Accepted, thread created");

                this.createClientConnection(client);
            }
        }
        catch (IOException e)
        {
            if (this.listenerSocket != null && this.listenerSocket.isClosed())
                Log.d("SERVER", "Normal exit");
            else
            {
                Log.d("SERVER", "Error");
                e.printStackTrace();
            }
        }
        finally
        {
            this.listenerThread = null;
            this.isRunning = false;
        }
    }

    private void createClientConnection(final Socket client)
    {
        ClientThread clientThread = new ClientThread(client, this.handler, this::removeThread);
        clientThread.start();
        this.connections.add(clientThread);
    }

    private void removeThread(ClientThread clientThread)
    {
        this.connections.remove(clientThread);
        try
        {
            clientThread.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
