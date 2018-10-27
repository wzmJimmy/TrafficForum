package com.example.wzm.matrix._practice_fragments_activities_intents.config;

import com.example.wzm.matrix._practice_fragments_activities_intents.model.Event;

import java.util.ArrayList;
import java.util.List;

public class DataService {
    /**
     * Fake all the event data for now.
     */
    public static List<Event> getEventData() {
        List<Event> eventData = new ArrayList<Event>();
        for (int i = 1; i <= 10; ++i) {
            eventData.add(
                    new Event("Event"+i, "1184 W valley Blvd, CA 90101",
                            "This is a huge event"));
        }
        return eventData;
    }

}
