package org.ez.springProj.springProjmatchup.model;
import lombok.Data;
import java.util.Date;

@Data
public class MatchupRequestModel {

    private String matchupId;
    private String receiverEmail;
    private Date timeMin;
    private Date timeMax;
    private String timeZone;
    private String inviterId;
    private String location;
    private String status;
    private String summary;

    public MatchupRequestModel(String matchupId, String receiverEmail, Date timeMin, Date timeMax, String timeZone, String inviter, String location, String status, String summary) {
        this.matchupId = matchupId;
        this.receiverEmail = receiverEmail;
        this.timeMin = timeMin;
        this.timeMax = timeMax;
        this.timeZone = timeZone;
        this.inviterId = inviter;
        this.location = location;
        this.status = status;
        this.summary = summary;
    }
}
