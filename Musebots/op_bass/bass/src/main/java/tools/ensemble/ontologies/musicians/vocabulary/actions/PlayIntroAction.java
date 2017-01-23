package tools.ensemble.ontologies.musicians.vocabulary.actions;

import jade.content.onto.basic.Action;

/**
 * Created by OscarAlfonso on 1/21/2017.
 */
public class PlayIntroAction extends Action {
    private int lenght;
    private boolean now;
    private Float duration;

    public void setLenght(int lenght)
    {
        this.lenght = lenght;
    }

    public int getLenght()
    {
        return this.lenght;
    }

    public void setNow(boolean now)
    {
      this.now = now;
    }

    public boolean getNow()
    {
        return this.now;
    }

    public void setDuration(float duration)
    {
        this.duration = duration;
    }

    public float getDuration()
    {
        return duration;
    }
}
