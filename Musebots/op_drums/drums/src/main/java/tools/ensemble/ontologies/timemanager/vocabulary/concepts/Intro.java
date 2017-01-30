package tools.ensemble.ontologies.timemanager.vocabulary.concepts;

import jade.content.Concept;

/**
 * Created by OscarAlfonso on 1/30/2017.
 */
public class Intro implements Concept {

    private float introLenght;
    private float introStartedAt;

    public void setIntroLenght (float introLenght)
    {
        this.introLenght = introLenght;
    }

    public float getIntroLenght()
    {
        return introLenght;
    }

    public void setIntroStartedAt(float introStartedAt)
    {
        this.introStartedAt = introStartedAt;
    }

    public float getIntroStartedAt()
    {
        return introStartedAt;
    }
}
