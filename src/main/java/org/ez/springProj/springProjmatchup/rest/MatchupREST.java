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
import org.springframework.web.bind.annotation.CrossOrigin;

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
     *
     * @param calendarId1
     * @param calendarId2
     * @param timeMin
     * @param timeMax
     * @param timeZone
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @CrossOrigin(origins = "http://localhost:63342")
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
        List<String> resultList = new ArrayList<String>();
        resultList = googleCalendarService.matchFreeTime(calendarId1, calendarId2, timeMin, timeMax, timeZone);
        if(resultList.size() == 0)
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NO_CONTENT);
        else
            return new ResponseEntity<>(resultList, HttpStatus.OK);
    }
}
