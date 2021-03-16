package fr.univ_amu.DumbStages.donnees;

import java.util.ArrayList;

public class Responsable {
    private String nom;
    private String diminutif;
    static ArrayList<Responsable> responsables;

    public static Responsable findResponsableByDiminutif(String diminutif) {
        int i = 0;
        while (i < responsables.size()) {
            if (diminutif == responsables.get(i).getDiminutif())
                return responsables.get(i);
            ++i;
        }
        return null;
    }

    public Responsable(String nom, String diminutif) {
        this.nom = nom;
        this.diminutif = diminutif;
        responsables.add(this);
    }

    public String getNom() {
        return nom;
    }

    public String getDiminutif() {
        return diminutif;
    }
}