package tools.ensemble.ontologies.timemanager.vocabulary.concepts;

import jade.content.Concept;

import java.util.Date;

/**
 * Created by OscarAlfonso on 1/30/2017.
 */
public class Section implements Concept {

    private String accompanimentCurrentSection;
    private Date timeLeft;
    private int sectionIndex;
    private Date sectionStartedAt;

    public void setAccompanimentCurrentSection(String section)
    {
        this.accompanimentCurrentSection = section;
    }

    public String getAccompanimentCurrentSection()
    {
        return this.accompanimentCurrentSection;
    }

    public void setTimeLeft(Date timeLeft)
    {
        this.timeLeft = timeLeft;
    }

    public Date getTimeLeft()
    {
        return this.timeLeft;
    }

    public void setSectionIndex(int sectionIndex)
    {
        this.sectionIndex = sectionIndex;
    }

    public int getSectionIndex()
    {
        return sectionIndex;
    }

    public void setSectionStartedAt(Date started)
    {
        this.sectionStartedAt = started;
    }
    public Date getSectionStartedAt()
    {
        return sectionStartedAt;
    }


}
