package tools.ensemble.ontologies.musicelements;

/**
 * Created by OscarAlfonso on 1/16/2017.
 */
import jade.content.onto.*;
import jade.content.schema.*;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ScoreElements;

public class MusicElementsOntology extends Ontology  {

    /**
     A symbolic constant, containing the name of this ontology.
     */
    public static final String NAME = "musicElements-ontology";

    // VOCABULARY
    // Concepts
    public static final String SCORE_ELEMENTS = "SCORE_ELEMENTS";
    public static final String SCORE_TEMPO = "tempo";
    public static final String SCORE_SIGNATURE_NUMERATOR = "numerator";
    public static final String SCORE_SIGNATURE_DENOMITAOR = "denominator";
    public static final String SCORE_FORM = "form";
    public static final String SECTION_A_CHORDS = "sectionAchords";
    public static final String SECTION_B_CHORDS = "sectionBchords";
    public static final String SECTION_C_CHORDS = "sectionCchords";



    private static Ontology theInstance = new MusicElementsOntology();

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

    private MusicElementsOntology() {
        super(NAME, BasicOntology.getInstance());
        try {
            add(new ConceptSchema(SCORE_ELEMENTS), ScoreElements.class);
            ConceptSchema cs = (ConceptSchema)getSchema(SCORE_ELEMENTS);
            cs.add(SCORE_TEMPO, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
            cs.add(SCORE_SIGNATURE_NUMERATOR, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(SCORE_SIGNATURE_DENOMITAOR, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(SCORE_FORM, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
            cs.add(SECTION_A_CHORDS, (PrimitiveSchema)getSchema(BasicOntology.INTEGER),0, ObjectSchema.UNLIMITED);
            cs.add(SECTION_B_CHORDS, (PrimitiveSchema)getSchema(BasicOntology.INTEGER),0, ObjectSchema.UNLIMITED);
            cs.add(SECTION_C_CHORDS, (PrimitiveSchema)getSchema(BasicOntology.INTEGER),0, ObjectSchema.UNLIMITED);
        }
        catch(OntologyException oe) {
            oe.printStackTrace();
        }

    }

}
