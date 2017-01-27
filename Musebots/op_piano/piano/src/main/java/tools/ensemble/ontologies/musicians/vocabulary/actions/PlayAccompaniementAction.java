package tools.ensemble.ontologies.musicians.vocabulary.actions;

import jade.content.onto.basic.Action;

/**
 * Created by OscarAlfonso on 1/25/2017.
 */
public class PlayAccompaniementAction extends Action {
    //Time left that the musician have to start to play the accompaniement
    private float timeLeft;

    public void setTimeleft(float timeLeft)
    {
        this.timeLeft = timeLeft;
    }

    public float getTimeLeft()
    {
        return this.timeLeft;
    }
}
