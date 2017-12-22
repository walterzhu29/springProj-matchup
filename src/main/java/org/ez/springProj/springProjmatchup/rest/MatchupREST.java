package org.ez.springProj.springProjmatchup.rest;


import com.google.api.client.util.Data;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.sun.xml.internal.xsom.impl.scd.Iterators;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.ez.springProj.springProjmatchup.query.GoogleCalendarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@RestController
@RequestMapping(value = "/matchup")
public class MatchupREST {
    private final static Logger logger = LoggerFactory.getLogger(MatchupREST.class);
    /**
     * give a calendarID,
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
        com.google.api.services.calendar.Calendar service =
                GoogleCalendarService.getCalendarService();

        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list(calendarId)
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.size() == 0) {
            logger.info("No upcoming events found.");
        } else {
            logger.info("Upcoming events:");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                logger.info(event.getSummary(), start);
            }
        }
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    /**
     * give calendarID and a time interval,
     * @return a list busy infos
     * @throws IOException
     */
    @ApiOperation(value = "busy-infos", notes = "return busy infos for the given time interval")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "calendarId", dataType = "String", required = true),
            @ApiImplicitParam(paramType = "query", name = "timeMin", dataType = "String", required = true),
            @ApiImplicitParam(paramType = "query", name = "timeMax", dataType = "String", required = true)
    })
    @RequestMapping(value = "/busy-infos", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<FreeBusyResponse> checkBusyInfos(@RequestParam(name = "calendarId") String calendarId,
                                                 @RequestParam(name = "timeMin") String timeMin,
                                                 @RequestParam(name = "timeMax") String timeMax) throws IOException, ParseException {


        com.google.api.services.calendar.Calendar service =
                GoogleCalendarService.getCalendarService();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date timeMi = df.parse(timeMin);
        DateTime startTime = new DateTime(timeMi, TimeZone.getDefault());
        Date timeMa = df.parse(timeMax);
        DateTime endTime = new DateTime(timeMa, TimeZone.getDefault());
        FreeBusyRequest req = new FreeBusyRequest();
        List<FreeBusyRequestItem> items= new ArrayList<FreeBusyRequestItem>();
        FreeBusyRequestItem item = new FreeBusyRequestItem();
        item.setId(calendarId);
        items.add(item);
        req.setItems(items);
        req.setTimeMin(startTime);
        req.setTimeMax(endTime);
        Calendar.Freebusy.Query fbq= service.freebusy().query(req);
        FreeBusyResponse fbResponse = fbq.execute();

        return new ResponseEntity<>(fbResponse, HttpStatus.OK);
    }

}
