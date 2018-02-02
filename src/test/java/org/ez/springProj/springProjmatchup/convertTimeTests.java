package org.ez.springProj.springProjmatchup;

import com.google.api.client.util.DateTime;
import org.ez.springProj.springProjmatchup.query.GoogleCalendarService;

import org.ez.springProj.springProjmatchup.query.GoogleCalendarService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

@RunWith(SpringRunner.class)
@SpringBootTest
public class convertTimeTests {

    @Autowired
    private GoogleCalendarService googleCalendarService;
    private TimeZone EST;
    private TimeZone PST;
    private TimeZone GMT;
    private Date date;

    @Before
    public void setUp() {
        date = new Date(118, 0, 1, 0, 0, 0);
        EST = TimeZone.getTimeZone("EST");
        PST = TimeZone.getTimeZone("PST");
        GMT = TimeZone.getTimeZone("GMT");
    }

    @Test
    public void ESTToEST() throws ParseException {
        String rightResult = "2018-01-01 00:00:00";
        DateTime dateTime = new DateTime(date, EST);
        String testResult = googleCalendarService.convertTime(dateTime, "EST");
        Assert.assertEquals(testResult, rightResult);
    }

    @Test
    public void ESTToPST() throws ParseException {
        String rightResult = "2017-12-31 21:00:00";
        DateTime dateTime = new DateTime(date, EST);
        String testResult = googleCalendarService.convertTime(dateTime, "PST");
        Assert.assertEquals(testResult, rightResult);
    }

    @Test
    public void ESTToGMT() throws ParseException {
        String rightResult = "2018-01-01 05:00:00";
        DateTime dateTime = new DateTime(date, EST);
        String testResult = googleCalendarService.convertTime(dateTime, "GMT");
        Assert.assertEquals(testResult, rightResult);
    }
}
