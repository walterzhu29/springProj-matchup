package org.ez.springProj.springProjmatchup.rest;


import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.*;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.ez.springProj.springProjmatchup.query.GoogleCalendarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;


@RestController
@RequestMapping(value = "/matchup")
public class MatchupREST {
    private final static Logger logger = LoggerFactory.getLogger(MatchupREST.class);

    @Autowired
    private GoogleCalendarService googleCalendarService;

    /**
     * give a calendarID,
     *
     * @return a list of upcoming events.
     * @throws IOException
     */
    @ApiOperation(value = "upcoming-events", notes = "return upcoming events for the given calendar ID")
    @ApiImplicitParam(paramType = "query", name = "calendarId", dataType = "String", required = true)
    @RequestMapping(value = "/upcoming-events", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Event>> getUpcomingEvents(@RequestParam(name = "calendarId") String calendarId) throws IOException {
        // Build a new authorized API client service.
        // Note: Do not confuse this class with the
        //   com.google.api.services.calendar.model.Calendar class.
        com.google.api.services.calendar.Calendar service = googleCalendarService.getCalendarService();
        if (service != null) {
            // if service is down
        }

        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list(calendarId)
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            logger.info("No upcoming events found.");
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NO_CONTENT);
        } else {
            logger.info("Upcoming events:");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                logger.info("Event: {}, Starts: {}", event.getSummary(), start);
            }
        }
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    /**
     * give two calendarIDs and a time interval,
     *
     * @return a list free times
     * @throws IOException
     */
    @ApiOperation(value = "match-up", notes = "compare two calendars and return matching free times")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "calendarId1", dataType = "String", required = true),
            @ApiImplicitParam(paramType = "query", name = "calendarId2", dataType = "String", required = true),
            @ApiImplicitParam(paramType = "query", name = "timeMin", dataType = "String", required = true),
            @ApiImplicitParam(paramType = "query", name = "timeMax", dataType = "String", required = true),
            @ApiImplicitParam(paramType = "query", name = "timeZone", dataType = "String", required = true)
    })
    @RequestMapping(value = "/match-up", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<String>> matchUp(@RequestParam(name = "calendarId1") String calendarId1,
                                                           @RequestParam(name = "calendarId2") String calendarId2,
                                                           @RequestParam(name = "timeMin") String timeMin,
                                                           @RequestParam(name = "timeMax") String timeMax,
                                                           @RequestParam(name = "timeZone") String timeZone) throws IOException, ParseException {
        FreeBusyResponse fbResponse1 = googleCalendarService.checkBusyInfos(calendarId1, timeMin, timeMax, timeZone);
        FreeBusyResponse fbResponse2 = googleCalendarService.checkBusyInfos(calendarId2, timeMin, timeMax, timeZone);
        DateTime startTime = fbResponse1.getTimeMin();
        DateTime endTime = fbResponse1.getTimeMax();
        ArrayList<TimePeriod> busyList = new ArrayList<TimePeriod>();
        busyList.addAll(fbResponse1.getCalendars().get(calendarId1).getBusy());
        busyList.addAll(fbResponse2.getCalendars().get(calendarId2).getBusy());
        //sort busy times buy timeMin
        Collections.sort(busyList, new Comparator<TimePeriod>() {
            @Override
            public int compare(TimePeriod o1, TimePeriod o2) {
                long d1 = o1.getStart().getValue();
                long d2 = o2.getStart().getValue();
                if(d1 <= d2)
                    return -1;
                else
                    return 1;
            }
        });
        //search free times
        List<String> resultList = new ArrayList<String>();
        DateTime searchTime = startTime;
        for(int i = 0; i < busyList.size(); i++) {
            if(busyList.get(i).getStart().getValue() > searchTime.getValue()) {
                //change time zone of result
                String startTimeToString = searchTime.toString();
                String endTimeToString = busyList.get(i).getStart().toString();
                if(i == 0) {
                    String result = "From "
                            + googleCalendarService.convertTime(startTimeToString.substring(0, 18),
                            "GMT",
                            timeZone)
                            + " to "
                            + googleCalendarService.convertTime(endTimeToString.substring(0, 18),
                            "EST",
                            timeZone);
                    resultList.add(result);
                }
                else {
                    String result = "From "
                            + googleCalendarService.convertTime(startTimeToString.substring(0, 18),
                            "EST",
                            timeZone)
                            + " to "
                            + googleCalendarService.convertTime(endTimeToString.substring(0, 18),
                            "EST",
                            timeZone);
                    resultList.add(result);
                }
            }
            if(searchTime.getValue() < busyList.get(i).getEnd().getValue())
                searchTime = busyList.get(i).getEnd();
        }
        if(searchTime.getValue() < endTime.getValue()) {
            String startTimeToString = searchTime.toString();
            String endTimeToString = endTime.toString();
            String result = "From "
                    + googleCalendarService.convertTime(startTimeToString.substring(0, 18),
                    "EST",
                    timeZone)
                    + " to "
                    + googleCalendarService.convertTime(endTimeToString.substring(0, 18),
                    "GMT",
                    timeZone);
            resultList.add(result);
            searchTime = endTime;
        }
        if(resultList.size() == 0)
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NO_CONTENT);
        else
            return new ResponseEntity<>(resultList, HttpStatus.OK);
    }

}
