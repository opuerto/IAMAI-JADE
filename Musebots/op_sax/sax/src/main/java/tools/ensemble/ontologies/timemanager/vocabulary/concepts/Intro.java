package tools.ensemble.ontologies.timemanager.vocabulary.concepts;

import jade.content.Concept;

import java.util.Date;

/**
 * Created by OscarAlfonso on 1/30/2017.
 */
public class Intro implements Concept {

    private float introLenght;
    private Date introStartedAt;

    public void setIntroLenght (float introLenght)
    {
        this.introLenght = introLenght;
    }

    public float getIntroLenght()
    {
        return introLenght;
    }

    public void setIntroStartedAt(Date introStartedAt)
    {
        this.introStartedAt = introStartedAt;
    }

    public Date getIntroStartedAt()
    {
        return introStartedAt;
    }
}
