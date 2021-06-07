package com.example.studienarbeit_kugellabyrinth;

public class DatenbankTemplate {

    final String TAG = "DatenbankTemplate";


    public long id;
    public String name;
    public String time;

    /**
     * Konstruktor
     */
    public DatenbankTemplate(String name, String time) {
        this.name          = name;
        this.time        = time;

        id = -1; // wird erst beim Einfuegen in die Datenbank erzeugt
    }

    /**
     * Konstruktor
     */
    public DatenbankTemplate() {
    }
}
