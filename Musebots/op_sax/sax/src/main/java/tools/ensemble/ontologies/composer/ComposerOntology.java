package tools.ensemble.ontologies.composer;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PrimitiveSchema;
import jade.content.schema.TermSchema;
import tools.ensemble.ontologies.composer.vocabulary.concepts.AccompanimentConcepts;
import tools.ensemble.ontologies.composer.vocabulary.concepts.IntroConcepts;
import tools.ensemble.ontologies.composer.vocabulary.concepts.SoloConcepts;


/**
 * Created by OscarAlfonso on 2/22/2017.
 */
public class ComposerOntology extends Ontology {

    /**
     A symbolic constant, containing the name of this ontology.
     */
    public static final String NAME = "composer-ontology";
    // VOCABULARY

    // Concepts INTRO
    public static final String INTRO = "INTRO";
    public static final String INTRO_TEMPO = "introTempo";
    public static final String INTRO_SCORE_SIGNATURE_NUMERATOR = "introNumerator";
    public static final String INTRO_SCORE_SIGNATURE_DENOMITAOR = "introDenominator";
    public static final String INTRO_LENGTH = "introLength";
    public static final String INTRO_DURATION = "introDuration";
    public static final String INTRO_STARTED_AT = "introStartedAt";

    // Concepts SOLO
    public static final String SOLO = "SOLO";
    public static final String SOLO_TEMPO = "soloTempo";
    public static final String SOLO_SIGNATURE_NUMERATOR = "soloNumerator";
    public static final String SOLO_SIGNATURE_DENOMITAOR = "soloDenominator";
    public static final String SOLO_FORM = "soloForm";
    public static final String SOLO_CURRENT_SECTION = "soloCurrentSection";
    public static final String SOLO_DURATION = "soloDuration";
    public static final String SOLO_STARTED_AT = "soloStartedAt";

    // Concepts Accompaniment
    public static final String ACCOMPANIMENT = "ACCOMPANIMENT";
    public static final String ACCOMPANIMENT_TEMPO = "accompanimentTempo";
    public static final String ACCOMPANIMENT_SIGNATURE_NUMERATOR = "accompanimentNumerator";
    public static final String ACCOMPANIMENT_SIGNATURE_DENOMITAOR = "accompanimentDenominator";
    public static final String ACCOMPANIMENT_FORM = "accompanimentForm";
    public static final String ACCOMPANIMENT_CURRENT_SECTION = "accompanimentCurrentSection";
    public static final String ACCOMPANIMENT_DURATION = "accompanimentDuration";
    public static final String ACCOMPANIMENT_STARTED_AT = "accompanimentStartedAt";

    private static Ontology theInstance = new ComposerOntology();
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

    public ComposerOntology()
    {
        super(NAME, BasicOntology.getInstance());
        try
        {
            add(new ConceptSchema(INTRO), IntroConcepts.class);
            add(new ConceptSchema(SOLO), SoloConcepts.class);
            add(new ConceptSchema(ACCOMPANIMENT), AccompanimentConcepts.class);

            ConceptSchema cs = (ConceptSchema) getSchema(INTRO);
            cs.add(INTRO_TEMPO, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(INTRO_SCORE_SIGNATURE_NUMERATOR, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(INTRO_SCORE_SIGNATURE_DENOMITAOR, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(INTRO_LENGTH, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(INTRO_DURATION, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
            cs.add(INTRO_STARTED_AT, (PrimitiveSchema) getSchema(BasicOntology.DATE), ObjectSchema.OPTIONAL);

            //SOLO
            cs = (ConceptSchema) getSchema(SOLO);
            cs.add(SOLO_TEMPO, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(SOLO_SIGNATURE_NUMERATOR, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(SOLO_SIGNATURE_DENOMITAOR, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(SOLO_FORM, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
            cs.add(SOLO_CURRENT_SECTION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
            cs.add(SOLO_DURATION, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
            cs.add(SOLO_STARTED_AT, (PrimitiveSchema) getSchema(BasicOntology.DATE), ObjectSchema.OPTIONAL);

            //ACCOMPANIMENT
            cs = (ConceptSchema) getSchema(ACCOMPANIMENT);
            cs.add(ACCOMPANIMENT_TEMPO, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(ACCOMPANIMENT_SIGNATURE_NUMERATOR, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(ACCOMPANIMENT_SIGNATURE_DENOMITAOR, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
            cs.add(ACCOMPANIMENT_FORM, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
            cs.add(ACCOMPANIMENT_CURRENT_SECTION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
            cs.add(ACCOMPANIMENT_DURATION, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
            cs.add(ACCOMPANIMENT_STARTED_AT, (PrimitiveSchema) getSchema(BasicOntology.DATE), ObjectSchema.OPTIONAL);
        }
        catch(OntologyException oe) {
        oe.printStackTrace();
        }
    }

}
