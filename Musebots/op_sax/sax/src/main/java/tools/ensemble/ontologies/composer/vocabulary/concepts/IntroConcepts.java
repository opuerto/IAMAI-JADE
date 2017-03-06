package tools.ensemble.ontologies.composer.vocabulary.concepts;

import jade.content.Concept;

import java.util.Date;

/**
 * Created by OscarAlfonso on 2/22/2017.
 */
public class IntroConcepts implements Concept {

    private int 	introTempo;
    private int    introNumerator;
    private int 	introDenominator;
    private int introLength;
    private float introDuration;
    private Date introStartedAt;

    public void setIntroTempo(int tempo)
    {
        this.introTempo = tempo;
    }
    public int getIntroTempo()
    {
        return introTempo;
    }

    public void setIntroNumerator(int numerator)
    {
        this.introNumerator = numerator;
    }

    public int getIntroNumerator()
    {
        return introNumerator;
    }

    public void setIntroDenominator(int denominator)
    {
        this.introDenominator = denominator;
    }

    public int getIntroDenominator()
    {
        return introDenominator;
    }

    public void setIntroLength(int length)
    {
        this.introLength = length;
    }

    public int getIntroLength()
    {
        return introLength;
    }

    public void setIntroDuration(float duration)
    {
        this.introDuration = duration;
    }
    public float getIntroDuration()
    {
        return introDuration;
    }
    public void setIntroStartedAt(Date started)
    {
        this.introStartedAt = started;
    }
    public Date getIntroStartedAt()
    {
        return introStartedAt;
    }



}
