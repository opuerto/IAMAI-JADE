package tools.ensemble.ontologies.musicelements;

import jade.content.Concept;

/**
 * Created by OscarAlfonso on 1/16/2017.
 */
public class ScoreElements implements Concept  {
    private int 	tempo;
    private int    numerator;
    private int 	denominator;
    private String  form;

    // Methods required to use this class to represent the ADDRESS role
    public void setTempo(int tempo) {
        this.tempo=tempo;
    }
    public int getTempo() {
        return tempo;
    }
    public void setNumerator(int numerator) {
        this.numerator=numerator;
    }
    public int getNumerator() {
        return numerator;
    }
    public void setDenominator(int denominator) {
        this.denominator=denominator;
    }
    public int getDenominator() {
        return denominator;
    }
    public void setForm (String form){this.form=form;}
    public String getForm(){return form;}

}
