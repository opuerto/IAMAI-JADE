package tools.ensemble.ontologies.musicians;

import jade.content.onto.*;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PrimitiveSchema;
import tools.ensemble.ontologies.musicians.vocabulary.actions.PlayAccompaniementAction;
import tools.ensemble.ontologies.musicians.vocabulary.actions.PlayIntroAction;

/**
 * Created by OscarAlfonso on 1/21/2017.
 */
public class MusicianOntology extends Ontology {

    /**
     A symbolic constant, containing the name of this ontology.
     */
    public static final String NAME = "musicians-ontology";

    // VOCABULARY

    //concept

    //actions
    public static final String PLAY_INTRO = "PLAY_INTRO";
    public static final String LENGHT = "lenght";
    public static final String CUE_PLAY_NOW = "now";
    public static final String DURATION_OF_INTRO = "duration";
    public static final String PLAY_ACCOMPANIEMENT = "PLAY_ACCOMPANIEMENT";
    public static final String TIME_LEFT = "timeLeft";


    private static Ontology theInstance = new MusicianOntology();

    /**
     This method grants access to the unique instance of the
     ontology.
     @return An <code>Ontology</code> object, containing the concepts
     of the ontology.
     */
    public static Ontology getInstance() {
        return theInstance;
    }

    /**
     * Constructor
     */
    public MusicianOntology()
    {
        super(NAME, BasicOntology.getInstance());
        try {
            add(new AgentActionSchema(PLAY_INTRO), PlayIntroAction.class);
            add(new AgentActionSchema(PLAY_ACCOMPANIEMENT), PlayAccompaniementAction.class);

            AgentActionSchema as = (AgentActionSchema)getSchema(PLAY_INTRO);
                as.add(LENGHT,(PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
                as.add(CUE_PLAY_NOW,(PrimitiveSchema)getSchema(BasicOntology.BOOLEAN));
                as.add(DURATION_OF_INTRO,(PrimitiveSchema)getSchema(BasicOntology.FLOAT));

            as = (AgentActionSchema)getSchema(PLAY_ACCOMPANIEMENT);
                as.add(TIME_LEFT,(PrimitiveSchema)getSchema(BasicOntology.FLOAT));

            }
        catch(OntologyException oe) {
            oe.printStackTrace();
        }
    }


}
