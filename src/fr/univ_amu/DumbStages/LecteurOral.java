package fr.univ_amu.DumbStages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.Vector;

import fr.univ_amu.DumbStages.donnees.Oral;
import fr.univ_amu.DumbStages.donnees.Responsable;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet; //?
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class LecteurOral {

    private XSSFWorkbook monExcel;

    public LecteurOral(String path) throws IOException, InvalidFormatException {
        monExcel = new XSSFWorkbook(new java.io.File(path));
    }

    public XSSFWorkbook getFichier() {
        return monExcel;
    }

    public void generateResponsables() {
        XSSFSheet mySheet = this.getFichier().getSheetAt(1);

        // à ajouter si le ArrayList de la classe Responsable ne fonctionne pas comme prévu
        //ArrayList<Responsable> responsables = new ArrayList<Responsable>();

        for (Row row : mySheet) {
            Cell diminutiveCell = row.getCell(0);
            Cell nameCell = row.getCell(1);
            if ((diminutiveCell.getStringCellValue().isEmpty() || diminutiveCell.getStringCellValue().isBlank()) &&
                (nameCell.getStringCellValue().isEmpty() || nameCell.getStringCellValue().isBlank())
            ) continue;

            new Responsable(nameCell.getStringCellValue(), diminutiveCell.getStringCellValue());
            System.out.println("Nouveau Responsable généré: "+nameCell.getStringCellValue()+" "+diminutiveCell.getStringCellValue());
        }
    }

    //Classe à appeler après avoir généré les responsables
    public ArrayList<Oral> generateOraux() {
        XSSFSheet mySheet = this.getFichier().getSheetAt(2);

        ArrayList<Oral> oraux = new ArrayList<>();

        for (Row row : mySheet) {
            if (row.getRowNum() > 0) {
                Cell lastnameCell = row.getCell(0);
                Cell firstnameCell = row.getCell(1);
                Cell isValide = row.getCell(2);
                Cell tuteur = row.getCell(3);
                Cell auditeur = row.getCell(4);
                Cell entreprise = row.getCell(5);

                if ((lastnameCell.getStringCellValue().isEmpty() || lastnameCell.getStringCellValue().isBlank()) ||
                    (firstnameCell.getStringCellValue().isEmpty() || firstnameCell.getStringCellValue().isBlank() ||
                    isValide.getStringCellValue() == "validé")
                ) continue;

                Oral monOral = new Oral(
                        lastnameCell.getStringCellValue(),
                        firstnameCell.getStringCellValue(),
                        (!entreprise.getStringCellValue().isEmpty() ? entreprise.getStringCellValue() : "Inconnu"),
                        tuteur.getStringCellValue(),
                        auditeur.getStringCellValue()
                );
                monOral.Afficher();
            }
        }
        return oraux;
    }

    public static void ThirdStep() throws IOException, InvalidFormatException {
        try {
            String path = "D:/Soutenances.xlsx";
            LecteurOral lecteurTest = new LecteurOral(path);
            //lecteurTest.generateResponsables();
            //System.out.println("Debug (Doit afficher 'Alain Casali'): "+ Objects.requireNonNull(Responsable.findResponsableByDiminutif("AC")).getNom());
        } catch (Exception e) {
            System.out.println("erreur "+e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("programme terminé");
        }
    }
}
