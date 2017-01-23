package tools.ensemble.interfaces;

/**
 * Created by OscarAlfonso on 1/18/2017.
 */
public interface MusicianStates {
    //States
    public final static String STATE_START = "Start";
    public final static String STATE_REGISTER = "Register";
    public final static String STATE_GET_MEMBERS = "Get_members";
    public final static String STATE_LEADER = "Leader";
    public final static String STATE_LEADER_GET_SONG_STRUCTURE = "LeaderGetSongStructure";
    public final static String STATE_SHARE_STRUCTURE = "ShareStructure";
    public final static String STATE_REQUEST_INTRO = "RequestIntro";
    public final static String STATE_REQUEST_SOLO = "RequestSolo";
    public final static String STATE_PASS_LEAD = "PassLead";
    public final static String STATE_SILENT = "Silent";
    public final static String STATE_REQUEST_END = "RequestEnd";
    public final static String STATE_END ="End";
    public final static String STATE_ACCOMPANIST="Accompanist";
    public final static String STATE_GET_STRUCTURE="GetStructure";
    public final static String STATE_INTRO ="Intro";
    public final static String STATE_ACCEPT_INTRO="AcceptIntro";
    public final static String STATE_REFUSE_INTRO="RefuseIntro";
    public final static String STATE_ACCEPT_ACCOMPANIMENT="AcceptAccompaniment";
    public final static String STATE_REQUEST_ACCOMPANIMENT="RequestAccompaniment";
    public final static String STATE_ACCOMPANIENT_SILENT="AccompanientSilent";
    public final static String STATE_WAITING_LEADERSHIP="WaitingLeadership";
    public final static String STATE_ACCEPT_ENDING="AcceptEnding";
}
