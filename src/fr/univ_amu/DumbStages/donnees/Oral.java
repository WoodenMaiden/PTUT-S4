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
        this.tuteur = this.tuteur.findResponsableByDiminutif(dim_tuteur);
        this.auditeur = this.auditeur.findResponsableByDiminutif(dim_auditeur);
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
        this.tuteur.findResponsableByDiminutif(diminutif);
    }

    public Responsable getAuditeur() {
        return auditeur;
    }

    public void setAuditeur(String diminutif) {
        this.auditeur.findResponsableByDiminutif(diminutif);
    }

    public void Afficher() {
        System.out.println("Etudiant: " + this.stagiaire.get(0) + this.stagiaire.get(1));
        System.out.println("Entreprise: " + this.entreprise);
        System.out.println("Tuteur: " + this.tuteur.getNom());
        System.out.println("Auditeur: " + this.auditeur.getNom());
    }
}