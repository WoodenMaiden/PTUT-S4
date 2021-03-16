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

import fr.univ_amu.DumbStages.donnees.Responsable;
import fr.univ_amu.DumbStages.donnees.Etudiant;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet; //?
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.filechooser.FileSystemView;

public class LecteurOral {

    private XSSFWorkbook monExcel;

    public LecteurOral(String path) throws IOException, InvalidFormatException {
        monExcel = new XSSFWorkbook(new java.io.File(path));
    }

    public XSSFWorkbook getFichier() {
        return monExcel;
    }

    public void generateResponsables() {
            XSSFSheet mySheet = this.getFichier().getSheetAt(0);

            ArrayList<Responsable> responsables = new ArrayList<Responsable>();

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

    public void generateEtudiant() {

    }

    public static void ThirdStep() throws IOException, InvalidFormatException {
        try {
            String path = "D:/Download/Soutenances.xlsx";
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

    public static void Start() throws IOException, InvalidFormatException {
        ThirdStep();
    }
}
