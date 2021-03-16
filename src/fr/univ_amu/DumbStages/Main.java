package fr.univ_amu.DumbStages;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;

//Classe servant uniquement à lancer l'Application
public class Main {
    public static void main(String[] args) throws IOException, InvalidFormatException {
        //App.launch(App.class, args);
        LecteurOral.Start();
    }
}
