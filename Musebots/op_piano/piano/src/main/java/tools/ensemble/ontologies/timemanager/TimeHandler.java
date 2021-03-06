package tools.ensemble.ontologies.timemanager;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PrimitiveSchema;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Section;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Intro;
import tools.ensemble.ontologies.timemanager.vocabulary.concepts.Song;

/**
 * Created by OscarAlfonso on 1/30/2017.
 */
public class TimeHandler extends Ontology {

    /**
     A symbolic constant, containing the name of this ontology.
     */
    public static final String NAME = "time-handler";
    // VOCABULARY

    //concept
    public static final String SECTION = "SECTION";
    public static final String CURRENT_SECTION = "accompanimentCurrentSection";
    public static final String SECTION_TIME_LEFT = "timeLeft";
    public static final String SECTION_INDEX = "sectionIndex";
    public static final String SECTION_STARTED_AT = "sectionStartedAt";


    //concept
    public static final String INTRO = "INTRO";
    public static final String INTRO_LENGHT = "introLenght";
    public static final String INTRO_STARTED_AT = "introStartedAt";

    //concept
    public static final String SONG = "SONG";
    public static final String SONG_STARTED_AT = "songStartedAt";


    private static Ontology theInstance = new TimeHandler();

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

    public TimeHandler()
    {
        super(NAME, BasicOntology.getInstance());
        try {
            //add(new AgentActionSchema(PLAY_INTRO), PlayIntroAction.class);
            add(new ConceptSchema(SECTION), Section.class);
            add(new ConceptSchema(INTRO), Intro.class);
            add(new ConceptSchema(SONG), Song.class);

            //AgentActionSchema as = (AgentActionSchema)getSchema(PLAY_INTRO);
            //as.add(LENGHT,(PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);

            ConceptSchema cs =(ConceptSchema)getSchema(SECTION);
            cs.add(CURRENT_SECTION,(PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
            cs.add(SECTION_TIME_LEFT,(PrimitiveSchema)getSchema(BasicOntology.DATE));
            cs.add(SECTION_INDEX,(PrimitiveSchema)getSchema(BasicOntology.INTEGER));
            cs.add(SECTION_STARTED_AT,(PrimitiveSchema)getSchema(BasicOntology.DATE));


            cs = (ConceptSchema)getSchema(INTRO);
            cs.add(INTRO_LENGHT,(PrimitiveSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
            cs.add(INTRO_STARTED_AT,(PrimitiveSchema)getSchema(BasicOntology.DATE), ObjectSchema.MANDATORY);

            cs = (ConceptSchema)getSchema(SONG);
            cs.add(SONG_STARTED_AT,(PrimitiveSchema)getSchema(BasicOntology.FLOAT));
        }
        catch(OntologyException oe) {
        oe.printStackTrace();
        }
    }


}
