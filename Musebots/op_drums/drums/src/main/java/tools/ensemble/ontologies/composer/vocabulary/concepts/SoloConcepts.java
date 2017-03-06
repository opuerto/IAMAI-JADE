package tools.ensemble.ontologies.composer.vocabulary.concepts;

import jade.content.Concept;

import java.util.Date;

/**
 * Created by OscarAlfonso on 2/22/2017.
 */
public class SoloConcepts implements Concept {
    private int soloTempo;
    private int soloNumerator;
    private int soloDenominator;
    private String soloForm;
    private String soloCurrentSection;
    private float soloDuration;
    private Date soloStartedAt;

    public void setSoloTempo(int tempo)
    {
        this.soloTempo = tempo;
    }

    public int getSoloTempo()
    {
        return soloTempo;
    }

    public void setSoloNumerator(int numerator)
    {
        this.soloNumerator = numerator;
    }
    public int getSoloNumerator()
    {
        return soloNumerator;
    }
    public void setSoloDenominator(int denominator)
    {
        this.soloDenominator = denominator;
    }
    public int getSoloDenominator()
    {
        return soloDenominator;
    }
    public void setSoloForm(String form)
    {
        this.soloForm = form;
    }
    public String getSoloForm()
    {
        return soloForm;
    }
    public void setSoloCurrentSection(String currentSection)
    {
        this.soloCurrentSection = currentSection;
    }
    public String getSoloCurrentSection()
    {
        return soloCurrentSection;
    }
    public void setSoloDuration(float duration)
    {
        this.soloDuration = duration;
    }
    public float getSoloDuration()
    {
        return soloDuration;
    }
    public void setSoloStartedAt(Date started)
    {
        this.soloStartedAt = started;
    }
    public Date getSoloStartedAt()
    {
        return soloStartedAt;
    }

}

