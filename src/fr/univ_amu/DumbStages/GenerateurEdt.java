package fr.univ_amu.DumbStages;


import fr.univ_amu.DumbStages.donnees.Entreprise;
import fr.univ_amu.DumbStages.donnees.Etudiant;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public final class GenerateurEdt {

    public XSSFWorkbook Entree;
    public File RepertSortie; // on va (très probablement) générer plusieurs fichiers, autant les mettre dans un répertoire non ?

    private final int nombreHoraires;

    private final List<Etudiant> mesEtudiants = new ArrayList<Etudiant>(); 
    private final List<Entreprise> mesEntreprises = new ArrayList<Entreprise>();
    // c'est une map qui prend a pour clé les étudiants et en valeur un array de booléens, si array[i] == true alors l'étudiant est déjà placé à la colonne i+1 -> évite que les étudiants soient sur la même colomne
    private final Map<Etudiant, boolean[]>  map_Etudiant_Colonnes_Libres = new HashMap<Etudiant, boolean[]>();
    // idem, si array[i] == true alors l'étudiant est déjà3 placé pour l'entreprise à l'indice i de mesEntreprises -> évite que les étudiants soient sur la même ligne
    private final Map<Etudiant, boolean[]> map_Etudiant_Entreprises_Libres = new HashMap<Etudiant, boolean[]>();


    public GenerateurEdt(String chemin, String repertoire, int nombreHoraires) throws SecurityException {
        if (chemin == null || chemin == "") throw new SecurityException("entrez quelque chose");
        this.nombreHoraires = nombreHoraires;
        try {
            File FichierEntree = new File(chemin);
            this.Entree = new XSSFWorkbook(FichierEntree);
            this.RepertSortie = new File(repertoire);
            if (!RepertSortie.exists() && !RepertSortie.isDirectory()) RepertSortie.mkdirs();
            if (!FichierEntree.exists()) throw new SecurityException ("Une erreur d'entrée sortie est survenue :( \n Vérifiez si les fichier sont valides ou si vous avez des droits dessus");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }


    }

    private void remplirVecteurs(){

        // On parcours toutes les feuilles car 1 feuille = 1 groupe
        for(int indexFeuille = 0; indexFeuille < Entree.getNumberOfSheets(); ++indexFeuille) {
            final String groupe = Entree.getSheetAt(indexFeuille).getRow(1).getCell(0).getStringCellValue();
            int x = -1;
            int y = 3;

            String nom = null;
            String prenom = null;

            while (true) {
                boolean aLaFin = false;
                ++x;
                switch (x) {
                    case 0:
                        if (Entree.getSheetAt(indexFeuille).getRow(y) == null) {
                            aLaFin = true;
                            break;
                        }
                        Cell cetteCellule = Entree.getSheetAt(indexFeuille).getRow(y).getCell(x);
                        nom = cetteCellule.getStringCellValue();
                        break;

                    case 1:
                        Cell cetteCellule2 = Entree.getSheetAt(indexFeuille).getRow(y).getCell(x);
                        if (cetteCellule2 == null || cetteCellule2.getStringCellValue().isEmpty() || cetteCellule2.getStringCellValue().isBlank()) {
                            aLaFin = true;
                            break; // si c'est vide c'est que c'est la fin
                        }
                        prenom = cetteCellule2.getStringCellValue();
                        break;

                    case 2:
                        x = -1; // dans le cas ou c'est au dela de la 2eme colomne
                        ++y;
                        mesEtudiants.add(new Etudiant(nom, prenom, groupe));
                        break;
                }

                if (aLaFin) break;
            }
        }


        // ici on a pas besoin de parcourir toutes les feuilles : les entreprises sont les mêmes pour tout le monde
        int x = 1;
        while (true) {
            ++x;
            Cell cetteCellule = Entree.getSheetAt(0).getRow(2).getCell(x);
            if (cetteCellule != null) {
                if (cetteCellule.getStringCellValue().isEmpty() || cetteCellule.getStringCellValue().isBlank()) break; // si c'est vide c'est que c'est la fin
                mesEntreprises.add(new Entreprise(cetteCellule.getStringCellValue(), null, null));
            }
            else break;
        }


        for(Etudiant et : mesEtudiants){
            this.map_Etudiant_Colonnes_Libres.put(et, new boolean[this.nombreHoraires]);
            this.map_Etudiant_Entreprises_Libres.put(et, new boolean[this.mesEntreprises.size()]);
        }

        for (Etudiant et : mesEtudiants){
            final boolean[] mon_array = map_Etudiant_Colonnes_Libres.get(et);
            for (int i = 0; i < mon_array.length; ++i){
                mon_array[i] = false;
            }

            final boolean[] mon_array_entreprises = map_Etudiant_Entreprises_Libres.get(et);
            for (int i = 0; i < mon_array_entreprises.length; ++i){
                mon_array_entreprises[i] = false;
            }
        }
    }

    private void normaliser(short[] ligne_mat){
        // cette methode permet de vérifier si la ligne en question est valide, la normalisation a lieue si :
        // - Un nombre est trop élevé
        // - Un nombre est < 0,
        // - Un nombre est en double,
        // - Il n'y a pas de nombres écrit,
        // - Il y a une chaine de caractère au lieu du nombre (est converti sous forme d'entier négatif dans remplirChoix())
        // dans ces cas là on va mettre au hasard



        final List<Short> entiers_dispo = new ArrayList<Short>(); // liste des entiers de 1 à n entreprises disponibles quand un d'eux est présent dans ligne_mat il sera "consommé" (supprimé)
        for(short i = 1; i <= mesEntreprises.size(); ++i) entiers_dispo.add(i);


        final List<Integer> i_cases_invalides = new ArrayList<Integer>();

        boolean trouve = false; //booléen qui permet de savoir si on a trouvé la case de la matrice dans la liste des entiers disponibles

        int j = 0; // on va parcourir entiers_dispo avec ça

        for(int i = 0; i < ligne_mat.length; ++i){ //ici on détermine les entiers disponibles restants et les cases invalides

            while(j < entiers_dispo.size())
            {
                if (entiers_dispo.get(j) == ligne_mat[i]) { // si la case i de la matrice figure dans les entiers dispo on enlève cet entier de la liste, et trouve devient vrai...
                    entiers_dispo.remove(j);
                    trouve = true;
                    break;
                }
                ++j;
            }

            if (trouve == false){
                i_cases_invalides.add(i); // ... sinon on ajoute cette case à la liste des cases non valides
            }

            trouve = false;
            j = 0;

        }

        //maintenant on va remplir le tableau avec les entiers disponibles aux cases de ligne_mat non valides de manière aléatoire

        final Random gen_aleatoire = new Random();
        int indexAlea = 0;

        for (int i : i_cases_invalides) {
            indexAlea = gen_aleatoire.nextInt(entiers_dispo.size()); // on peut se permettre ça car nextInt(int bound) renvoie entre n € [0,bound[
            ligne_mat[i] = entiers_dispo.get(indexAlea);
            entiers_dispo.remove(indexAlea);
        }

    }



    private void remplirChoix(short[][] mat){


        short matx = 0;
        short maty = 0;

        for (int indexFeuille = 0; indexFeuille < Entree.getNumberOfSheets(); ++indexFeuille) {
//            maty = 0;
            for (Row R : Entree.getSheetAt(indexFeuille)){

                if (R.getRowNum() < 3 || R == null) continue;

                matx = 0;
                for (Cell C : R){
                    if (C.getColumnIndex() < 2 || C == null || C.getStringCellValue().isBlank() || C.getStringCellValue().isEmpty()) continue;

                    if (C.getCellType() != CellType.NUMERIC){
                        //on prends en compte le décalage du aux en têtes d'ou les soustractions
                        mat[maty][matx] = -1;
                    }
                    else if (C.getNumericCellValue() < -32768 || C.getNumericCellValue() > 32767){
                        mat[maty][matx] = -1;
                    }
                    else{
                        mat[maty][matx] = (short) C.getNumericCellValue();
                    }
                    ++matx;
                }

                normaliser(mat[maty]);
                ++maty;
            }

        }
    }

    private void placerEtudiant (int indice_et, int indice_ent, List<CellRangeAddress> mesCellFus, XSSFSheet feuille) {
        final CellRangeAddress range = mesCellFus.get(indice_ent);
        int i_colonne = range.getFirstColumn() + 1;
        int i_ligne = range.getFirstRow();
        final Etudiant et = mesEtudiants.get(indice_et);
        boolean vers_la_droite = true;
        XSSFCell cellule;

        //System.out.print(str_et + " : ");
        while (i_ligne <= range.getLastRow()){

            //TODO comprendre pourquoi le parcours ne continue pas sur d'autres lignes
            while (i_colonne < this.nombreHoraires + 1 && i_colonne > 0){
                cellule = feuille.getRow(i_ligne).getCell(i_colonne);
                //System.out.print(cellule.getAddress() + " c " + i_colonne + " l " + i_ligne + ", ");
                if (!map_Etudiant_Colonnes_Libres.get(et)[i_colonne - 1]){
                    if (cellule.getStringCellValue().isBlank() || cellule.getStringCellValue().isEmpty()){
                        map_Etudiant_Colonnes_Libres.get(et)[i_colonne - 1] = true;
                        cellule.setCellValue(et.getNom() + " " + et.getPrenom());
                        //System.out.println("placé en : " + cellule.getAddress());
                        return;
                    }
                }//(!map_Etudiant_Colonnes_Libres.get(et)[i_colonne - 1])
                if(vers_la_droite) ++i_colonne;
                else --i_colonne;
            }//while (i_colonne < this.nombreHoraires + 1 && i_colonne > 0)

            //on reset les valeurs
            if (i_colonne >= this.nombreHoraires + 1 ) i_colonne = this.nombreHoraires;
            else if (i_colonne <= 0) i_colonne = range.getFirstColumn() + 1;

            //System.out.println("sorti de la boucle i_colonne");
            vers_la_droite = !vers_la_droite;
            ++i_ligne;
        }//while (i_ligne <= range.getLastRow())
    }//placerEtudiant()


    public void run () throws Exception {
        remplirVecteurs();

        final short[][] matrice_de_choix = new short[mesEtudiants.size()][mesEntreprises.size()];

        remplirChoix(matrice_de_choix);

//        for (int i = 0; i < matrice_de_choix.length; ++i) {
//
//            for (int j = 0; j < matrice_de_choix[i].length; ++j)
//                //System.out.print(matrice_de_choix[i][j] + " ");
//
//            System.out.println(mesEtudiants.get(i).getNom() + " " + mesEtudiants.get(i).getPrenom());
//        }

//        for (Entreprise entr : mesEntreprises)
//            System.out.print(entr.getNom_en() + " | ");


        ///////////////////////////////////////
        // création de l'edt des entreprises //
        ///////////////////////////////////////


            final XSSFWorkbook ExcelEntreprises = new XSSFWorkbook();
            File EDTentreprises = new File(RepertSortie, "Emploi du temps entreprises "+Step2Controler.endFile+".xlsx");


            final XSSFCellStyle texte = ExcelEntreprises.createCellStyle();
            texte.setVerticalAlignment(VerticalAlignment.CENTER);
            texte.setAlignment(HorizontalAlignment.CENTER);


            final XSSFCellStyle titres = ExcelEntreprises.createCellStyle();
            titres.cloneStyleFrom(texte);
            titres.setBorderBottom(BorderStyle.THICK);
            titres.setBorderTop(BorderStyle.THICK);

            final XSSFCellStyle dernieresCellules = ExcelEntreprises.createCellStyle();
            dernieresCellules.cloneStyleFrom(texte);
            dernieresCellules.setBorderBottom(BorderStyle.THICK);


            final XSSFSheet maFeuille = ExcelEntreprises.createSheet("Entreprises");
            final XSSFRow mesHoraires = maFeuille.createRow(0);

            final XSSFCell A1 = mesHoraires.createCell(0);
            A1.setCellValue("Entreprises");
            A1.setCellStyle(titres);


            for (int h = 1; h < this.nombreHoraires +1; ++h) {

                XSSFCell c = mesHoraires.createCell(h);
                c.setCellStyle(titres);
                c.setCellValue("insérer une horaire");
                c.getSheet().autoSizeColumn(c.getColumnIndex());
            }

            // On a h horaires, de v places chacun, y entreprises, x élèves pouvant assister z fois (maximum h horaires)
            // on doit respecter : nb_places_totales ≥ nb_etudiants * nb_horaires
            // ∀ h,y,x ∈ N \ {0}, z ∈ N / z ∈ ]0,h]
            // h*v*y ≥ x*z


            int nb_places_necessaires = 6;

            /* représente le nombre d'entretiens auquels les étudiants vont assister : si on a un nombre d'entreprises E inférieures à un nombre d'horaires H alors
            l'étudiant ne va pas assister a H entreprises car cela impliquerais qu'il doit assister plusieurs fois à un entretien avec une entreprise (et un out of array aussi),
            dans ce cas on prend E horaires.


            Dans le cas contraire si E>=H, cela voudrait dire que l'étudiant doit assister à plusieurs entretiens en même temps, dans ce cas là on prends H horaires
             */

            int nb_entretiens_et;
            if (mesEntreprises.size() < this.nombreHoraires) nb_entretiens_et = mesEntreprises.size();
            else /*mesEntreprises.size() => this.nombreHoraires*/ nb_entretiens_et = this.nombreHoraires;


            while (true) {

                if (this.nombreHoraires * nb_places_necessaires * mesEntreprises.size() >= mesEtudiants.size() * nb_entretiens_et) {
                    break;
                } else if (nb_entretiens_et <= 3) {

                    ++nb_places_necessaires;
                } else {

                    --nb_entretiens_et;
                }
            }


            final List<CellRangeAddress> mesCellulesFusionnees = new ArrayList<CellRangeAddress>(); // représente les entreprises en ordonnées (leur range)


            //fusion des cellules

            int debut_fusion_cellules = -nb_places_necessaires + 1;
            for (int i = 0; i < mesEntreprises.size(); ++i) {

                debut_fusion_cellules += nb_places_necessaires;

                final CellRangeAddress maRange = new CellRangeAddress(debut_fusion_cellules, debut_fusion_cellules + nb_places_necessaires - 1, 0, 0);

                mesCellulesFusionnees.add(maRange);

                maFeuille.addMergedRegion(maRange);
            }


            // ici on remplit le tableau, on va d'abord faire une copie de la matrice de choix

            final List<List<Short>> matrice2 = new ArrayList<List<Short>>();
            for (short[] ligne : matrice_de_choix) {

                final List<Short> Vligne = new ArrayList<Short>();
                for (short s : ligne)
                    Vligne.add(s);

                matrice2.add(Vligne);
            }


            //créons TOUTES les cellules
            int indexEntreprises = 0;

            int cpt = 0;

            //System.out.println("\nnb entretiens : " + nb_entretiens_et);

            for (int i = 0; i < mesEntreprises.size() * nb_places_necessaires; ++i) {

                final XSSFRow rowDeRemplissage = maFeuille.createRow(i+1);

                for (int j = 0; j < this.nombreHoraires + 1; ++j) {

                    if (cpt >= nb_places_necessaires ){
                        ++indexEntreprises;
                        cpt = 0;
                    }

                    if (j == 0) {
                        final XSSFCell cellule_fusionee = rowDeRemplissage.createCell(0);

                        cellule_fusionee.setCellValue(mesEntreprises.get(indexEntreprises).getNom_en());

                        cellule_fusionee.setCellStyle(titres);
                        cellule_fusionee.getSheet().autoSizeColumn(cellule_fusionee.getColumnIndex());
                        continue;
                    }
                    final XSSFCell cellDeRemplissage = rowDeRemplissage.createCell(j);
                    cellDeRemplissage.setCellValue(" ");
                    cellDeRemplissage.setCellStyle(texte);
                    if ((i+1) % nb_places_necessaires == 0) cellDeRemplissage.setCellStyle(dernieresCellules);
                    cellDeRemplissage.getSheet().autoSizeColumn(cellDeRemplissage.getColumnIndex());
                }

                ++cpt;

            }

            //System.out.println(mesCellulesFusionnees.toString());
            //int debut = mesCellulesFusionnees.get(0).getFirstColumn() + 1;
            final Random aleaEtu = new Random();

            //entiers qui donnent les indexs des étudiants intéréssés par l'entreprise
            final List<Integer> etudiants_interesses = new ArrayList<>(5);

            //pour toutes les entreprises
            for (int i_entreprise = 0 ; i_entreprise < mesEntreprises.size(); ++i_entreprise){

                //on remplit la liste des étudiants intéréssés donc ceux <= nb_entreitens_et
                for (int et_matrice = 0; et_matrice < matrice_de_choix.length; ++et_matrice) {
                    if (matrice_de_choix[et_matrice][i_entreprise] <= nb_entretiens_et) {
                        etudiants_interesses.add(et_matrice);
                    }
                }

                //System.out.println(etudiants_interesses.toString());

                while(etudiants_interesses.size() != 0) {
                    final int indice = aleaEtu.nextInt(etudiants_interesses.size());
                    //index de l'etudiant a placer
                    final int i_etudiant = etudiants_interesses.get(indice);
                    placerEtudiant(i_etudiant, i_entreprise, mesCellulesFusionnees, maFeuille);
                    //System.out.println();
                    etudiants_interesses.remove(indice);
                }//while(etudiants_interesses.size() != 0)

            }//for (int entreprise = 0 ; entreprise < mesEntreprises.size(); ++entreprise)


            matrice2.clear();


        //////////////////////////////////////////////
        // fin de création de l'edt des entreprises //
        //////////////////////////////////////////////


        /////////////////////////////////////
        // création de l'edt des étudiants //
        /////////////////////////////////////

            final XSSFSheet feuille_et = ExcelEntreprises.createSheet("Etudiants");

            //on fait le header
            final XSSFRow mesHoraires_et = feuille_et.createRow(0);
            final XSSFCell A1_et = mesHoraires_et.createCell(0);
            A1_et.setCellValue("Étudiants");
            A1_et.setCellStyle(titres);

            for (int h = 1; h < this.nombreHoraires + 1; ++h) {
                final XSSFCell horaire = mesHoraires_et.createCell(h);
                horaire.setCellFormula("REPT(" + maFeuille.getSheetName() + "!" + new CellAddress(maFeuille.getRow(0).getCell(h)).toString() + ", 1)");
                horaire.setCellStyle(titres);
            }


            // on va créer toutes les cellules
            for (int et = 1; et < mesEtudiants.size() + 1; ++et) {
                XSSFRow maRow = feuille_et.createRow(et);
                for (int i_maCell = 0; i_maCell < this.nombreHoraires + 1; ++i_maCell) {
                    XSSFCell maCell = maRow.createCell(i_maCell);
                    maCell.setCellStyle(texte);
                }
            }

            // on va mettre les noms de nos étudiants
            for (Row R : feuille_et) {
                if (R.getRowNum() == 0) continue;
                R.getCell(0).setCellValue(mesEtudiants.get(R.getRowNum() - 1).getNom() + " " + mesEtudiants.get(R.getRowNum() - 1).getPrenom());
                R.getSheet().autoSizeColumn(0);

                //On regarde les changements de groupes tout en faisant gaffe à ne pas finir en dehors de l'array

                if ((R.getRowNum() <= (feuille_et.getLastRowNum() - 1)) && !(mesEtudiants.get(R.getRowNum() - 1).getGroupe().equals(mesEtudiants.get(R.getRowNum()).getGroupe()))) {
                    for (Cell c : R) {
                        c.setCellStyle(dernieresCellules);
                    }
                }
            }

            for (int h = 1; h < this.nombreHoraires + 1; ++h) {

                int y = 0;
                while (true) {
                    ++y;

                    //si la row est nulle est donc qu'on est à la fin du tableau on s'arrête là
                    if (maFeuille.getRow(y) == null) break;

                    //si la cellule est vide on l'ignore
                    if (maFeuille.getRow(y).getCell(h).getStringCellValue().isEmpty() || maFeuille.getRow(y).getCell(h).getStringCellValue().isBlank())
                        continue;

                    int y2 = 1;
                    // on défile les lignes jusqu'à ce qu'on arrive à l'étudiant concerné
                    //maFeuille.getRow(y).getCell(h).getStringCellValue() = Nom etudiant
                    for (; y2 < feuille_et.getLastRowNum() && 
                    		!(feuille_et.getRow(y2).getCell(0).getStringCellValue().equals(maFeuille.getRow(y).getCell(h).getStringCellValue())); ++y2);

                    //et on place l'entreprise
                    //maFeuille.getRow(y).getCell(0).getStringCellValue() = nom entreprise
                    feuille_et.getRow(y2).getCell(h).setCellValue(maFeuille.getRow(y).getCell(0).getStringCellValue());
                }

                feuille_et.autoSizeColumn(h);
            }

            final FileOutputStream fileOut = new FileOutputStream(EDTentreprises);
            ExcelEntreprises.write(fileOut);
            ExcelEntreprises.close();
            fileOut.close();
    }
}
