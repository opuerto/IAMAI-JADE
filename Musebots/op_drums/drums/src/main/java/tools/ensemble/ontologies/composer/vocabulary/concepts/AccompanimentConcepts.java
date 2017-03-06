package tools.ensemble.ontologies.composer.vocabulary.concepts;

import jade.content.Concept;

import java.util.Date;

/**
 * Created by OscarAlfonso on 2/22/2017.
 */
public class AccompanimentConcepts implements Concept {
    private int accompanimentTempo;
    private int accompanimentNumerator;
    private int accompanimentDenominator;
    private String accompanimentForm;
    private String accompanimentCurrentSection;
    private float accompanimentDuration;
    private Date accompanimentStartedAt;

    public void setAccompanimentTempo(int tempo)
    {
        this.accompanimentTempo = tempo;
    }

    public int getAccompanimentTempo()
    {
        return accompanimentTempo;
    }

    public void setAccompanimentNumerator(int numerator)
    {
        this.accompanimentNumerator = numerator;
    }
    public int getAccompanimentNumerator()
    {
        return accompanimentNumerator;
    }
    public void setAccompanimentDenominator(int denominator)
    {
        this.accompanimentDenominator = denominator;
    }
    public int getAccompanimentDenominator()
    {
        return accompanimentDenominator;
    }
    public void setAccompanimentForm(String form)
    {
        this.accompanimentForm = form;
    }
    public String getAccompanimentForm()
    {
        return accompanimentForm;
    }
    public void setAccompanimentCurrentSection(String currentSection)
    {
        this.accompanimentCurrentSection = currentSection;
    }
    public String getAccompanimentCurrentSection()
    {
        return accompanimentCurrentSection;
    }
    public void setAccompanimentDuration(float duration)
    {
        this.accompanimentDuration = duration;
    }
    public float getAccompanimentDuration()
    {
        return accompanimentDuration;
    }
    public void setAccompanimentStartedAt(Date startedAt)
    {
        this.accompanimentStartedAt = startedAt;
    }
    public Date getAccompanimentStartedAt()
    {
        return accompanimentStartedAt;
    }
}


