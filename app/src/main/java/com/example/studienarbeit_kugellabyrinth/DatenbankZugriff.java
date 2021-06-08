package com.example.studienarbeit_kugellabyrinth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatenbankZugriff extends SQLiteOpenHelper {

    final String TAG = "DatenbankZugriff";

    private SQLiteDatabase db;
    private String tabellenSQL;
    private String tabelle;


    /**
     * Konstruktor
     * @param activity: aufrufende Activity
     * @param dbName: Name der Datenbank (wenn nicht vorhanden, dann wird sie neu erstellt)
     * @param tabellenSQL: SQL-Kommando zum Erzeugen der gew?nschten Tabelle (oder null bei ?ffnen
     *                      einer vorhandenen Datenbank)
     */
    public DatenbankZugriff(Context activity, String dbName, String tabellenSQL) {
        super(activity, dbName, null, 1);
        this.tabellenSQL = tabellenSQL;
        //bestimmeTabelle();

        db = this.getWritableDatabase();

        this.tabelle = dbName.replace(".dat", "").toUpperCase();
        db.execSQL(tabellenSQL);
    }

    @Override
    /**
     * Wird nur aufgerufen, wenn eine Datenbank neu erzeugt wird
     */
    public void onCreate(SQLiteDatabase db) {
        try {
            // Tabelle anlegen
            db.execSQL(tabellenSQL);
            Log.d("carpelibrum", "datenbank wird angelegt");
        }
        catch(Exception ex) {
            Log.e("carpelibrum", ex.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + tabelle);
        onCreate(db);
    }


    /**
     * Hier noch evtl. eigene Aufraeumarbeiten durchfuehren
     */
    @Override
    public synchronized void close() {
        if(db != null) {
            db.close();
            db = null;
        }

        super.close();
    }


    /**
     * Gegebenen Datensatz in die  Tabelle eingeben
     * @param datensatz
     * @return ID des neuen Datensatzes oder -1 bei Fehler
     */
    public long datensatzEinfuegen(DatenbankTemplate datensatz) {
        try {
            ContentValues daten = erzeugeDatenObjekt(datensatz);
            return db.insert(tabelle, null, daten); // id wird automatisch von SQLite gef?llt
        }
        catch(Exception ex) {
            Log.d("carpelibrum", ex.getMessage());
            return -1;
        }
    }

    public void changeCurrName(String name, long currID){
        try{
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            db.update(tabelle, cv, "_id = " + currID, null);
        }
        catch(Exception ex) {
            Log.d("carpelibrum", ex.getMessage());
        }
    }


    /**
     * Liefert Cursor zum Zugriff auf alle Eintr?ge, alphabetisch geordnet nach Spalte "Name"
     * @return
     */
    public Cursor erzeugeListViewCursor() {
        String[] spalten = new String[]{"_id", "Name", "Zeit"};
        return  db.query(tabelle, spalten, null, null, null, null, "Zeit", "10");
    }

    /**
     * Alle Datens?tze  liefern
     * @return Liste an Datens?tzen
     */
    public List<DatenbankTemplate> leseDatensaetze() {
        List<DatenbankTemplate> ergebnis = new ArrayList<DatenbankTemplate>();
        Cursor cursor = null;

        try {
            cursor = db.query(tabelle, null, null, null, null, null, null);
            int anzahl = cursor.getCount();
            cursor.moveToFirst();

            for(int i = 0; i < anzahl; i++) {
                DatenbankTemplate ds = erzeugeDatensatz(cursor);
                ergebnis.add(ds);
                cursor.moveToNext();
            }
        }
        catch(Exception ex) {
            Log.e("carpelibrum", ex.getMessage());
        }
        finally {
            // egal ob Erfolg oder Exception:: cursor schlie?en
            if(cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return ergebnis;
    }

    public int getDBLaenge(){
        Cursor cursor = null;
        int anzahl = 0;

        try {
            cursor = db.query(tabelle, null, null, null, null, null, null);
            anzahl = cursor.getCount();



        } catch(Exception ex) {
            Log.e("carpelibrum", ex.getMessage());
        }
        return anzahl;
    }


    /**
     * Existierenden Datensatz mit aktualisiertem Inhalt beschreiben
     */
    public void aktualisiereDatensatz(DatenbankTemplate ds) {
        try {
            ContentValues daten = erzeugeDatenObjekt(ds);
            db.update(tabelle, daten, "id = " + ds.id, null);
        }
        catch(Exception ex) {
            Log.e("carpelibrum", ex.getMessage());
        }
    }

    public void loescheDatensatz(DatenbankTemplate ds) {
        try {
            db.delete(tabelle, "id = " + ds.id, null);
        }
        catch(Exception ex) {
            Log.e("carpelibrum", ex.getMessage());
        }
    }


    /**
     * Aus dem Cursor an der aktuellen Position lesen und Datensatz erzeugen
     * @param cursor
     * @return
     */
    private DatenbankTemplate erzeugeDatensatz(Cursor cursor) {
        DatenbankTemplate ds        = new DatenbankTemplate();
        ds.id               = cursor.getLong(0);
        ds.name             = cursor.getString(1);
        ds.time          = cursor.getString(2);

        return ds;
    }

    /**
     * Erzeugt ein SQLite Datenobjekt
     * @param datensatz
     * @return
     */
    private ContentValues erzeugeDatenObjekt(DatenbankTemplate datensatz) {
        ContentValues daten = new ContentValues();
        daten.put("Name", datensatz.name);
        daten.put("Zeit", datensatz.time);

        return daten;
    }
}
