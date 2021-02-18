package fr.univ_amu.DumbStages.donnees;

public class Etudiant {
    private final String Nom;
    private final String Prenom;
    private final String Groupe;

    public Etudiant(String nom, String prenom, String groupe) {
        Nom = nom;
        Prenom = prenom;
        Groupe = groupe;
    }

    public final String getNom() {
        return Nom;
    }

    public final String getPrenom() {
        return Prenom;
    }

    public final String getGroupe() {
        return Groupe;
    }
}
