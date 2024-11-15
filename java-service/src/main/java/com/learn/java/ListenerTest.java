package com.learn.java;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Getter
@AllArgsConstructor
class Event {
    private String message;
}

interface EventListener {
    void onEvent(Event event);
}

class EventListenerImpl implements EventListener {
    @Override
    public void onEvent(Event event) {
        System.out.println("Received event: " + event.getMessage());
    }
}

class EventSource {
    private final List<EventListener> listeners = new ArrayList<>();

    public void addListener(EventListener listener) {
        listeners.add(listener);
    }

    public void triggerEvent(String message) {
        Event event = new Event(message);
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}

public class ListenerTest {
    public static void main(String[] args) {
        EventSource eventSource = new EventSource();
        EventListener listener = new EventListenerImpl();
        eventSource.addListener(listener);
        eventSource.triggerEvent("Hello, Event!");
    }
}