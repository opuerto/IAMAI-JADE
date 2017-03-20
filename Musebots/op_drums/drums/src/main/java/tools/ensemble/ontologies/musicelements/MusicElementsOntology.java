package tools.ensemble.ontologies.musicelements;

/**
 * Created by OscarAlfonso on 1/16/2017.
 */
import jade.content.onto.*;
import jade.content.schema.*;
import tools.ensemble.ontologies.musicelements.vocabulary.concepts.ChordsAttributes;
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

    public static final String CHORDS_ATTRIBUTES = "CHORDS_ATTRIBUTES";
    public static final String ROOT_PITCH = "rootPitch";
    public static final String MAJOR_OR_MINOR = "majorOrMinor";
    public static final String CHORD_EXTENSION = "extension";



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
            add(new ConceptSchema(CHORDS_ATTRIBUTES), ChordsAttributes.class);
            ConceptSchema cs = (ConceptSchema)getSchema(SCORE_ELEMENTS);
            cs.add(SCORE_TEMPO, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
            cs.add(SCORE_SIGNATURE_NUMERATOR, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(SCORE_SIGNATURE_DENOMITAOR, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(SCORE_FORM, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
            cs.add(SECTION_A_CHORDS, (ConceptSchema) getSchema(CHORDS_ATTRIBUTES),1, ObjectSchema.UNLIMITED);
            cs.add(SECTION_B_CHORDS, (ConceptSchema) getSchema(CHORDS_ATTRIBUTES),1, ObjectSchema.UNLIMITED);
            cs.add(SECTION_C_CHORDS, (ConceptSchema) getSchema(CHORDS_ATTRIBUTES),1, ObjectSchema.UNLIMITED);
            // Structure of the schema for the Chord_Attributes concept
            cs = (ConceptSchema)getSchema(CHORDS_ATTRIBUTES);
            cs.add(ROOT_PITCH,(PrimitiveSchema)getSchema(BasicOntology.INTEGER),ObjectSchema.MANDATORY);
            cs.add(MAJOR_OR_MINOR,(PrimitiveSchema)getSchema(BasicOntology.STRING),ObjectSchema.MANDATORY);
            cs.add(CHORD_EXTENSION,(PrimitiveSchema)getSchema(BasicOntology.INTEGER),ObjectSchema.OPTIONAL);
        }
        catch(OntologyException oe) {
            oe.printStackTrace();
        }

    }

}
