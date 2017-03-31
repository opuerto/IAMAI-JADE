package tools.ensemble.interfaces;

/**
 * Created by OscarAlfonso on 2/28/2017.
 */
public interface DataStoreComposer {

    public static final String INTRO_COMPOSER_INSTANCE = "introComposerInstance";
    public static final String SOLO_COMPOSER_INSTANCE = "soloComposerInstance";
    public static final String ACCOMPANIMENT_COMPOSER_INSTANCE = "accompanimentComposerInstance";

   //store the intro score
    public static final String INTRO_SCORE = "introScore";

    //Store message send from the custom protocol
    public static final String CURRENT_MESSAGE = "currentMessage";

    public static final String INTRO_COMPOSITION = "introComposition";

    //store the ID of the Musician agent
    public static final String COMPOSER_MY_INTERNAL_MUSICIAN = "composerMyInternalMusician";
    //Store the ID of the Time Manager agent
    public static final String COMPOSER_MY_INTERNAL_SYNCHRONIZER = "composerMyInternalSynchronizer";

    //time left to play the next section
    public static final String PLAY_TIME_LEFT = "introTimeLeft";

    //Store the score send to the play accompaniement state
    public static final String ACCOMPANIMENT_SCORE = "accompanimentScore";

    public static final String FROM_PLAY_TO_COMPOSE = "fromPlayToCompose";

    //Flag to put in hold the composition process while Im in the play accompaniment state.
    public static final String HOLD_COMPOSITION = "holdComposition";

    //Flag to put in hold the play process while Im in the composition state state.
    public static final String HOLD_PLAYBACK = "holdPlayBack";

    //Section that the musician going to paly
    public static final String NEXT_SECTION_TO_PLAY = "nextSectionToPlay";

    //Store if is the first solo played in the sond
    public static final String FIRST_TIME_SOLO = "firstTimeSolo";

    //Store current message from musician
    public static final String CURRENT_MESSAGE_FOR_MUSICIAN = "currentMessageFromMusician";

    //Store current message from musician
    public static final String CURRENT_MESSAGE_FOR_SYN = "currentMessageFromSyn";

    // the index of the character that represent the song form in the string array
    public static final String NEXT_SECTION_INDEX = "nextSectionIndex";

    //Save the info about the sections sent by the syn, that we will use to play the solo.
    public static final String SECTION_INSTANCE_FOR_SYN_SOLO = "sectionInstanceForSyncSolo";

 //Save the info about the sections sent by the syn, that we will use to play the accompaniement.
 public static final String SECTION_INSTANCE_FOR_SYN_ACCOMP = "sectionInstanceForSyncAccom";

    //Save the internal Musician ID
    public static final String INTERNAL_MUSICIAN_AID = "internalMusicianID";
}
