package tools.ensemble.ontologies.composer.vocabulary.actions;

import java.util.Date;

/**
 * Created by OscarAlfonso on 3/29/2017.
 */
public class PreviousSection {
    private Character theSectionChar;
    private long timeLeft;
    private int index;
    private Date startedAt;
    private boolean on = false;

    public void setTheSectionChar(Character c)
    {
        this.theSectionChar = c;
    }
    public Character getTheSectionChar()
    {
        return theSectionChar;
    }

    public void setTimeLeft(long timeLeft)
    {
        this.timeLeft = timeLeft;
    }

    public long getTimeleft()
    {
        return this.timeLeft;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public int getIndex()
    {
        return index;
    }

    public void setStartedAt(Date startedAt)
    {
        this.startedAt = startedAt;
    }
    public Date getStartedAt()
    {
        return this.startedAt;
    }

    public void setOn(boolean o)
    {
        this.on = o;
    }

    public boolean getOn()
    {
        return this.on;
    }

}
