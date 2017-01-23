package tools.ensemble.ontologies.musicelements;

import jade.content.Concept;

/**
 * Created by OscarAlfonso on 1/16/2017.
 */
public class ScoreElements implements Concept  {
    private int 	_tempo;
    private int    _numerator;
    private int 	_denominator;
    private String  _form;

    // Methods required to use this class to represent the ADDRESS role
    public void setTempo(int tempo) {
        _tempo=tempo;
    }
    public int getTempo() {
        return _tempo;
    }
    public void setNumerator(int numerator) {
        _numerator=numerator;
    }
    public int getNumerator() {
        return _numerator;
    }
    public void setDenominator(int denominator) {
        _denominator=denominator;
    }
    public int getDenominator() {
        return _denominator;
    }
    public void setForm (String form){_form=form;}
    public String getForm(){return _form;}

}
