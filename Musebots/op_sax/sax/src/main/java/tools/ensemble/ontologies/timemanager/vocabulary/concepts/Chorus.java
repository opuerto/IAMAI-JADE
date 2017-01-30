package tools.ensemble.ontologies.timemanager.vocabulary.concepts;

import jade.content.Concept;

/**
 * Created by OscarAlfonso on 1/30/2017.
 */
public class Chorus implements Concept {

    private String currentSection;
    private float sectionStartedAt;
    private float sectionLenght;
    private float updatedTimestamp;

    public void setCurrentSection (String currentSection)
    {
        this.currentSection = currentSection;
    }

    public String getCurrentSection()
    {
        return this.currentSection;
    }

    public void setSectionStartedAt (float sectionStartedAt)
    {
        this.sectionStartedAt = sectionStartedAt;
    }

    public float getSectionStartedAt()
    {
        return this.sectionStartedAt;
    }

    public void setSectionLenght (float sectionLenght)
    {
        this.sectionLenght = sectionLenght;
    }

    public float getSectionLenght ()
    {
        return this.sectionLenght;
    }

    public void setUpdatedTimestamp (float updatedTimestamp)
    {
        this.updatedTimestamp = updatedTimestamp;
    }

    public  float getUpdatedTimestamp ()
    {
        return this.updatedTimestamp;
    }
}
