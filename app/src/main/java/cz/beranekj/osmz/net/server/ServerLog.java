package cz.beranekj.osmz.net.server;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class ServerLog
{
    private final Subject<String> messageLogStream = PublishSubject.create();
    private final StringBuilder logger = new StringBuilder();

    public void log(String message)
    {
        this.logger.append(message);
        this.logger.append("\n");

        this.messageLogStream.onNext(message);
    }

    public Observable<String> onMessageLogged()
    {
        return this.messageLogStream;
    }
}
