package tools.ensemble.interfaces;

/**
 * Created by OscarAlfonso on 2/22/2017.
 */
public interface ComposerStatesNames {

    //STATES FOR THE INTRO

    public final static String STATE_WAIT_FOR_INTRO_REQUEST = "waitForIntroRequest";
    public final static String STATE_COMPOSE_INTRO = "composeIntro";
    public final static String STATE_PLAY_INTRO = "playIntro";
    public final static String STATE_END_INTRO = "end";


    //STATES FOR SOLOS

    public final static String STATE_WAIT_FOR_SOLO_REQUEST = "waitForSoloRequest";
    public final static String STATE_REQUEST_INFO_SECTION = "requestInfoSection";
    public final static String STATE_GET_INFO_SECTION = "getInfoSection";
    public final static String STATE_CONFIRM_TO_MUSICIAN = "confirmToMusician";
    public final static String STATE_COMPOSE_SOLO = "composeSolo";
    public final static String STATE_PLAY_SOLO = "playSolo";
    public final static String STATE_END_SOLO = "end";

    //STATE FOR ACCOMPANIMENT

    public final static String STATE_WAIT_FOR_ACCOMP_REQUEST = "waitForAccompRequest";
    public final static String STATE_CONFIRM_COMPOSIION = "confirmComposition";
    public final static String STATE_COMPOSE_ACCOMP = "composeAccompaniment";
    public final static String STATE_PLAY_ACCOMP = "playAccomp";
    public final static String STATE_END_ACCOMP = "end";
}
