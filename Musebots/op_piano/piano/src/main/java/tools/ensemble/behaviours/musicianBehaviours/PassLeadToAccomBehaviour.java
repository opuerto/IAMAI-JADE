package tools.ensemble.behaviours.musicianBehaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import sun.swing.plaf.synth.SynthIcon;

/**
 * Created by OscarAlfonso on 3/27/2017.
 */
public class PassLeadToAccomBehaviour extends OneShotBehaviour {

    private int transition = 0;
    private int firstTimeHere = 0;
    private int state = 0;

    public PassLeadToAccomBehaviour(Agent a)
    {
        super(a);
    }

    public void action()
    {
        if (firstTimeHere < 1)
        {
            System.out.println("Pass Lead To accompaniment");
        }
    }

    public int onEnd()
    {
        firstTimeHere++;
        return transition;
    }
}
