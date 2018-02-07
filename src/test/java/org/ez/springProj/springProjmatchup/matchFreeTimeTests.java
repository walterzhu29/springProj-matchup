package org.ez.springProj.springProjmatchup;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.FreeBusyCalendar;
import com.google.api.services.calendar.model.FreeBusyResponse;
import com.google.api.services.calendar.model.TimePeriod;
import org.ez.springProj.springProjmatchup.query.GoogleCalendarService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.omg.CORBA.Any;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class matchFreeTimeTests {
    private GoogleCalendarService mockito;
    private String calendarId1;
    private String calendarId2;
    @Before
    public void setUp() {
        mockito = mock(GoogleCalendarService.class);
        calendarId1 = "u9udoeo3tukabp92990sbe3c2s@group.calendar.google.com";
        calendarId2 = "v2qv94guse9j2f79iq5tgiqm0s@group.calendar.google.com";
    }

    /**
     * test result when there is no busy infos
     */
    @Test
    public void testEmptyResponse() throws IOException, ParseException {
        //input params
        String timeMin = "2018-01-08 06:00:00";
        String timeMax = "2018-01-08 10:00:00";
        //mock
        FreeBusyResponse fbResponse = new FreeBusyResponse();
        List<TimePeriod> emptyList= new ArrayList<TimePeriod>();
        FreeBusyCalendar emptyCalendar = new FreeBusyCalendar().setBusy(emptyList);
        Map<String, FreeBusyCalendar> calendars = new HashMap<String, FreeBusyCalendar>();
        calendars.put(calendarId1, emptyCalendar);
        calendars.put(calendarId2, emptyCalendar);
        fbResponse.setCalendars(calendars);
        Date mockDateTimeMin = new Date(118, 0, 8, 6, 0, 0);
        Date mockDateTimeMax = new Date(118, 0, 8, 10, 0, 0);
        DateTime mockTimeMin = new DateTime(mockDateTimeMin);
        DateTime mockTimeMax = new DateTime(mockDateTimeMax);
        fbResponse.setTimeMin(mockTimeMin);
        fbResponse.setTimeMax(mockTimeMax);
        when(mockito.checkBusyInfos(calendarId1, timeMin, timeMax, "EST")).thenReturn(fbResponse);
        when(mockito.checkBusyInfos(calendarId2, timeMin, timeMax, "EST")).thenReturn(fbResponse);
        when(mockito.convertTime(Matchers.any(DateTime.class), anyString())).thenCallRealMethod();
        when(mockito.matchFreeTime(calendarId1, calendarId2, timeMin, timeMax, "EST")).thenCallRealMethod();
        //test method
        List<String> result = mockito.matchFreeTime(calendarId1, calendarId2, timeMin, timeMax, "EST");
        String expectString = "From " + timeMin + " to " + timeMax;
        List<String> expectResult = new ArrayList<String>();
        expectResult.add(expectString);
        //verify result
        assertThat(result, is(expectResult));
    }

    /**
     * test invalid time interval input case
     */
    @Test
    public void testInvalidTimeIntervalCase() throws IOException, ParseException {
        //input params
        String timeMax = "2018-01-08 06:00:00";
        String timeMin = "2018-01-08 10:00:00";
        //mock
        FreeBusyResponse fbResponse = new FreeBusyResponse();
        //"2018-01-08 06:00:00 EST"
        Date mockDateTimeMax = new Date(118, 0, 8, 6, 0, 0);
        //"2018-01-08 10:00:00 EST"
        Date mockDateTimeMin = new Date(118, 0, 8, 10, 0, 0);
        DateTime mockTimeMin = new DateTime(mockDateTimeMin);
        DateTime mockTimeMax = new DateTime(mockDateTimeMax);
        fbResponse.setTimeMin(mockTimeMin);
        fbResponse.setTimeMax(mockTimeMax);
        when(mockito.checkBusyInfos(calendarId1, timeMin, timeMax, "EST")).thenReturn(fbResponse);
        when(mockito.checkBusyInfos(calendarId2, timeMin, timeMax, "EST")).thenReturn(fbResponse);
        when(mockito.convertTime(Matchers.any(DateTime.class), anyString())).thenCallRealMethod();
        when(mockito.matchFreeTime(calendarId1, calendarId2, timeMin, timeMax, "EST")).thenCallRealMethod();
        //test method
        List<String> result = mockito.matchFreeTime(calendarId1, calendarId2, timeMin, timeMax, "EST");
        String expectString = "Invalid Time Interval!";
        List<String> expectResult = new ArrayList<String>();
        expectResult.add(expectString);
        //verify result
        assertThat(result, is(expectResult));
    }
}
