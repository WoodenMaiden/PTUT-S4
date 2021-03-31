package fr.univ_amu.DumbStages.donnees;

import java.util.ArrayList;
import java.util.List;

public class Oral {
    private List<String> stagiaire;
    private String entreprise;
    private Responsable tuteur;
    private Responsable auditeur;
    static ArrayList<Oral> oraux;

    public Oral (String nom, String prenom, String entreprise, String dim_tuteur, String dim_auditeur) {
        this.stagiaire = new ArrayList<String>();
        this.stagiaire.add(nom);
        this.stagiaire.add(prenom);
        this.entreprise = entreprise;
        this.tuteur = Responsable.findResponsableByDiminutif(dim_tuteur);
        this.auditeur = Responsable.findResponsableByDiminutif(dim_auditeur);
    }


    public List<String> getStagiaire() {
        return stagiaire;
    }

    public void setStagiaire(String nom, String prenom) {
        this.stagiaire.set(0, nom);
        this.stagiaire.set(1, prenom);
    }

    public String getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    public Responsable getTuteur() {
        return tuteur;
    }

    public void setTuteur(String diminutif) {
        Responsable.findResponsableByDiminutif(diminutif);
    }

    public Responsable getAuditeur() {
        return auditeur;
    }

    public void setAuditeur(String diminutif) {
        Responsable.findResponsableByDiminutif(diminutif);
    }

    public void Afficher() {
        System.out.println(this.toString());
    }

    public String toString() {
        String str = "Etudiant: " + this.stagiaire.get(0) + this.stagiaire.get(1) + '\n';
        str += "Entreprise: " + this.entreprise + '\n';
        str += "Tuteur: " + this.tuteur.getNom() + '\n';
        str += "Auditeur: " + this.auditeur.getNom() + '\n';

        return str;
    }
}