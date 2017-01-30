package tools.ensemble.ontologies.timemanager.vocabulary.concepts;

import jade.content.Concept;

/**
 * Created by OscarAlfonso on 1/30/2017.
 */
public class Song implements Concept {

    private double songStartedAt;

    public void setSonStartedAt(double songStartedAt)
    {
        this.songStartedAt = songStartedAt;
    }

    public double getSongStartedAt()
    {
        return this.songStartedAt;
    }
}
