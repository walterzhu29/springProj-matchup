package org.ez.springProj.springProjmatchup.query;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;
import com.google.api.services.calendar.model.TimePeriod;
import org.ez.springProj.springProjmatchup.rest.MatchupREST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class GoogleCalendarService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCalendarService.class);
    private static final String APPLICATION_NAME =
            "springProj-matchup";

    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), "desktop/springProj-matchup");
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR_READONLY);

    static {
        System.out.println("This is a static block");
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                MatchupREST.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public com.google.api.services.calendar.Calendar
    getCalendarService() throws IOException {
        Credential credential = authorize();
        LOGGER.info("authorized Calendar client service builded");
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static FreeBusyRequest getFreeBusyRequest(final String calendarId,
                                                     final String timeMin,
                                                     final String timeMax,
                                                     final String timeZone) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        df.setTimeZone(tz);
        Date timeMi = df.parse(timeMin);
        DateTime startTime = new DateTime(timeMi, tz);
        Date timeMa = df.parse(timeMax);
        DateTime endTime = new DateTime(timeMa, tz);
        FreeBusyRequest req = new FreeBusyRequest();
        List<FreeBusyRequestItem> items = new ArrayList<FreeBusyRequestItem>();
        FreeBusyRequestItem item = new FreeBusyRequestItem();
        item.setId(calendarId);
        items.add(item);
        req.setItems(items);
        req.setTimeMin(startTime);
        req.setTimeMax(endTime);
        req.setTimeZone(timeZone);
        return req;
    }

    public FreeBusyResponse checkBusyInfos(final String calendarId,
                                           final String timeMin,
                                           final String timeMax,
                                           final String timeZone) throws IOException, ParseException {
        com.google.api.services.calendar.Calendar service = getCalendarService();
        FreeBusyRequest req = getFreeBusyRequest(calendarId, timeMin, timeMax, timeZone);
        Calendar.Freebusy.Query fbq = service.freebusy().query(req);
        FreeBusyResponse fbResponse = fbq.execute();
        return fbResponse;
    }

    /**
     *
     * @param inputDate
     * @param outputTimeZone
     * @return
     * @throws ParseException
     */
    public String convertTime(final DateTime inputDate,
                              final String outputTimeZone) throws ParseException {
        TimeZone timeZoneOp = TimeZone.getTimeZone(outputTimeZone);
        Date date = new Date();
        date.setTime(inputDate.getValue());
        DateTime outputDate = new DateTime(date, timeZoneOp);
        StringBuilder result = new StringBuilder(outputDate.toString().substring(0, 19));
        result.setCharAt(10, ' ');
        return result.toString();
    }

    /**
     * match up free time from two calendars
     * @param calendarId1
     * @param calendarId2
     * @param timeMin
     * @param timeMax
     * @param timeZone
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public List<String> matchFreeTime(final String calendarId1,
                                      final String calendarId2,
                                      final String timeMin,
                                      final String timeMax,
                                      final String timeZone) throws IOException, ParseException {
        FreeBusyResponse fbResponse1 = checkBusyInfos(calendarId1, timeMin, timeMax, timeZone);
        FreeBusyResponse fbResponse2 = checkBusyInfos(calendarId2, timeMin, timeMax, timeZone);
        DateTime startTime = fbResponse1.getTimeMin();
        DateTime endTime = fbResponse1.getTimeMax();
        ArrayList<TimePeriod> busyList = new ArrayList<TimePeriod>();
        busyList.addAll(fbResponse1.getCalendars().get(calendarId1).getBusy());
        busyList.addAll(fbResponse2.getCalendars().get(calendarId2).getBusy());
        //
        List<String> resultList = new ArrayList<String>();
        List<TimePeriod> freeTimeList = new ArrayList<TimePeriod>();
        //if there are no busy item
        if(busyList.size() == 0) {
            String Result = "From "
                    + convertTime(startTime, timeZone)
                    + " to "
                    + convertTime(endTime, timeZone);
            resultList.add(Result);
            return resultList;
        }
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
        DateTime searchTime = startTime;
        for(int i = 0; i < busyList.size(); i++) {
            if(busyList.get(i).getStart().getValue() > searchTime.getValue()) {
                TimePeriod curr = new TimePeriod();
                curr.setStart(searchTime);
                curr.setEnd(busyList.get(i).getStart());
                freeTimeList.add(curr);
            }
            if(searchTime.getValue() < busyList.get(i).getEnd().getValue())
                searchTime = busyList.get(i).getEnd();
        }
        if(searchTime.getValue() < endTime.getValue()) {
            TimePeriod curr = new TimePeriod();
            curr.setStart(searchTime);
            curr.setEnd(endTime);
            freeTimeList.add(curr);
        }
        //build resultList
        for(int i = 0; i < freeTimeList.size(); i++) {
            resultList.add(
                    "From "
                    + convertTime(freeTimeList.get(i).getStart(), timeZone)
                    + " to "
                    + convertTime(freeTimeList.get(i).getEnd(), timeZone)
            );
        }
        return resultList;
    }
}
