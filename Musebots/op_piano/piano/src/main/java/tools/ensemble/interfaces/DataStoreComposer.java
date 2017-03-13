package tools.ensemble.interfaces;

/**
 * Created by OscarAlfonso on 2/28/2017.
 */
public interface DataStoreComposer {

    public static final String INTRO_COMPOSER_INSTANCE = "introComposerInstance";
    public static final String SOLO_COMPOSER_INSTANCE = "soloComposerInstance";
    public static final String ACCOMPANIMENT_COMPOSER_INSTANCE = "accompanimentComposerInstance";
    public static final String INTRO_SCORE = "introScore";
    public static final String CURRENT_MESSAGE = "currentMessage";
    public static final String INTRO_COMPOSITION = "introComposition";
    public static final String COMPOSER_MY_INTERNAL_MUSICIAN = "composerMyInternalMusician";
    public static final String INTRO_TIME_LEFT = "introTimeLeft";
    public static final String ACCOMPANIMENT_SCORE = "accompanimentScore";
    public static final String FROM_PLAY_TO_COMPOSE = "fromPlayToCompose";

    //Flag to put in hold the composition process while Im in the play accompaniment state.
    public static final String HOLD_COMPOSITION = "holdComposition";

    //Flag to put in hold the play process while Im in the composition state state.
    public static final String HOLD_PLAYBACK = "holdPlayBack";
}
