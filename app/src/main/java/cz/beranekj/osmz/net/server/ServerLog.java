package cz.beranekj.osmz.net.server;

import java.util.ArrayList;
import java.util.List;

public class ServerLog
{
    private final StringBuilder logger = new StringBuilder();
    private final List<LogObserver> observers = new ArrayList<>();

    public void log(String message)
    {
        this.logger.append(message);
        this.logger.append("\n");

        for (LogObserver observer : this.observers)
        {
            observer.handleMessage(message);
        }
    }

    public void addListener(LogObserver observer)
    {
        this.observers.add(observer);
    }
}
