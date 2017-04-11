package tools.ensemble.ontologies.musicelements.vocabulary.concepts;

import jade.content.Concept;

import jade.util.leap.List;
import jade.util.leap.ArrayList;

/**
 * Created by OscarAlfonso on 1/16/2017.
 */
public class ScoreElements implements Concept  {
    private int 	tempo;
    private int    numerator;
    private int 	denominator;
    private String  form;
    private List sectionAchords = new ArrayList();
    private List sectionBchords = new ArrayList();
    private List sectionCchords = new ArrayList();

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
    public void setSectionAchords(List sectionAchords)
    {
        this.sectionAchords = sectionAchords;
    }
    public List getSectionAchords()
    {
        return this.sectionAchords;
    }
    public void setSectionBchords(List sectionBchords)
    {
        this.sectionBchords = sectionBchords;
    }
    public List getSectionBchords()
    {
        return this.sectionBchords;
    }
    public void setSectionCchords(List sectionCchords)
    {
        this.sectionCchords = sectionCchords;
    }
    public List getSectionCchords()
    {
        return this.sectionCchords;
    }

}
