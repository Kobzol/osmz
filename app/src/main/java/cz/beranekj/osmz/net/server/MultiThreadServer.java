package cz.beranekj.osmz.net.server;

import android.app.ExpandableListActivity;
import android.util.Log;

import com.annimon.stream.function.Consumer;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

import cz.beranekj.osmz.net.http.HttpHandler;

public class MultiThreadServer extends NetServer
{
    private class ClientThread extends Thread
    {
        private final Socket socket;
        private final HttpHandler handler;
        private final Consumer<ClientThread> onExit;
        private final ServerLog log;

        private ClientThread(Socket socket, HttpHandler handler, Consumer<ClientThread> onExit, ServerLog log)
        {
            this.socket = socket;
            this.handler = handler;
            this.onExit = onExit;
            this.log = log;
        }

        @Override
        public void run()
        {
            try
            {
                this.handler.handleConnection(this.socket.getInputStream(), this.socket.getOutputStream(), this.log);
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
    private final int maxNumberOfConnections;
    private final Semaphore semaphore;

    private HashSet<ClientThread> connections = new HashSet<>();
    private ServerSocket listenerSocket = null;
    private Thread listenerThread = null;

    public MultiThreadServer(HttpHandler handler, int port, int maxNumberOfConnections)
    {
        super(handler);
        this.port = port;
        this.maxNumberOfConnections = maxNumberOfConnections;
        this.semaphore = new Semaphore(maxNumberOfConnections);
    }

    @Override
    public void start()
    {
        if (this.isRunning()) return;

        this.listenerThread = new Thread(this::listenerLoop);
        this.listenerThread.start();

        this.isRunning = true;

        this.log.log("Server started");
    }

    @Override
    public void stop() throws IOException
    {
        this.isRunning = false;
        this.log.log("Server stopped");

        // close server
        if (this.listenerSocket != null)
        {
            try
            {
                this.listenerSocket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        // close clients
        for (ClientThread thread : this.connections)
        {
            thread.getSocket().close();
        }

        // wait for thread
        if (this.listenerThread != null)
        {
            try
            {
                this.listenerThread.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        this.log.log("Server stopped");
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

                this.log.log("Client accepted: " + client.getRemoteSocketAddress().toString());

                boolean hasThread = this.semaphore.tryAcquire();
                Log.d("SERVER", "Creating thread, available threads: " + String.valueOf(this.semaphore.availablePermits()));

                if (hasThread)
                {
                    this.createClientConnection(client);
                }
                else
                {
                    try
                    {
                        PrintWriter ps = new PrintWriter(client.getOutputStream());
                        ps.write("HTTP/1.0 500 DRAINED");
                        ps.flush();
                        client.getInputStream().close();
                        client.getOutputStream().close();
                        client.close();
                    }
                    catch (Exception e)
                    {
                        try
                        {
                            client.close();
                        }
                        catch (Exception ignored) { }
                    }
                }
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
            this.isRunning = false;
        }
    }

    private void createClientConnection(final Socket client)
    {
        ClientThread clientThread = new ClientThread(client, this.handler, this::removeThread, this.log);
        clientThread.setDaemon(true);
        clientThread.start();
        this.connections.add(clientThread);
    }

    private synchronized void removeThread(ClientThread clientThread)
    {
        this.connections.remove(clientThread);
        this.semaphore.release();
        Log.d("SERVER", "Destroyed thread, available threads: " + String.valueOf(this.semaphore.availablePermits()));
    }
}
