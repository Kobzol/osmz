package cz.beranekj.osmz2.util;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class SubscriptionManager
{
    private final List<Disposable> subscriptions = new ArrayList<>();

    public void add(Disposable subscription)
    {
        this.subscriptions.add(subscription);
    }

    public void unsubscribe()
    {
        Stream.of(this.subscriptions).forEach(Disposable::dispose);
        this.subscriptions.clear();
    }
}
