package fr.univ_amu.DumbStages.donnees;

public class Entreprise {

    private final String nom_en;
    private final String representants;
    private final String url;
    private String lienZoom;
    private String mdpZoom;

    public Entreprise(String nom, String rpz, String url) {
        this.nom_en = nom;
        this.representants = rpz;
        this.url = url;
    }

    public String getNom_en() {
        return this.nom_en;
    }

    public String getRepresentants() {
        return this.representants;
    }

    public String getLienZoom () {return lienZoom; }

    public void setLienZoom (String LienZoom) { this.lienZoom = LienZoom; }

    public String getMdpZoom () {return mdpZoom; }

    public void setMdpZoom (String MdpZoom) { this.mdpZoom = MdpZoom; }


    public String getUrl() {
        return this.url;
    }

    public void show()
    {
        System.out.println("Nom: "+this.getNom_en());
        System.out.println("Représentants: "+this.getRepresentants());
        System.out.println("URL: "+this.getUrl());
    }

/*
    private String RequeteWHOIS(){
        String os = System.getProperty("os.name");

        switch (os) {
            case "Windows" :

        }

    }
*/
}
