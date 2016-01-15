package com.example.basededonnees;

import java.util.ArrayList;

import modele.HeuresMarquees;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class HeuresDAO extends DAOBase {

	public HeuresDAO(final Context pContext) {
		super(pContext);
	}

	/**
	 * @param h
	 *            les heures a ajouter a la base
	 */
	public void ajouter(final HeuresMarquees h) {
		final SQLiteDatabase mdb = open();

		final ContentValues value = new ContentValues();
		value.put(DbSchema.EDT_NOM, h.getNomEdt());
		value.put(DbSchema.HEUREM_HEURE, h.getHeure_marquee());

		if (!mdb.isReadOnly()) {
			mdb.insert(DbSchema.HEUREM_TABLE_NAME, DbSchema.EDT_NOM, value);
		}
	}
	
	/**
	 * modifie l'heure marquee qui possede le meme ID que le parametre.
     * @param heure
     * l'heure modifiee
     */
    public void modifier(final HeuresMarquees heure)
    {
        final SQLiteDatabase mdb = open();
        final ContentValues value = new ContentValues();
        value.put(DbSchema.EDT_NOM, heure.getNomEdt());
        value.put(DbSchema.HEUREM_HEURE, heure.getHeure_marquee());
        mdb.update(DbSchema.HEUREM_TABLE_NAME, value, DbSchema.ID + " = ?", new String[] {String.valueOf(heure.getId())});

    }

	/**
	 * @param heure
	 *            l'heure marquee a supprimer
	 */
	public void supprimer(final double heure) {
	    final SQLiteDatabase mdb = open();
        mdb.delete(DbSchema.HEUREM_TABLE_NAME,DbSchema.HEUREM_HEURE + " = ?",
                new String[] { String.valueOf(heure)});
	}


	/**
	 *
	 * @param nom
	 * @return Toutes les heures (double) marquees de l'emploi du temps
	 */
	public ArrayList<Double> getHeures(final String nom) {

		final SQLiteDatabase mdb = open();
		final ArrayList<Double> heures = new ArrayList<Double>();
		// curseur contenant toutes les heures marquees d'un meme edt

		final Cursor c = mdb.rawQuery("select " + DbSchema.ID + ", " + DbSchema.EDT_NOM + ", " + DbSchema.HEUREM_HEURE
				+ " from " + DbSchema.HEUREM_TABLE_NAME + " where " + DbSchema.EDT_NOM
				+ " LIKE ?", new String[] { nom });

		// parcours toutes les activites
		while (c.moveToNext()) {
			final double heure = c.getDouble(2);
			heures.add(heure);
		}
		c.close();
		return heures;
	}

	/**
    *
    * @param nom
    * @return Toutes les heures marquees (objet cette fois) de l'emploi du temps
    */
   public ArrayList<HeuresMarquees> getHeuresMarquees(final String nom) {

       final SQLiteDatabase mdb = open();
       final ArrayList<HeuresMarquees> heures = new ArrayList<HeuresMarquees>();
       // curseur contenant toutes les heures marquees d'un meme edt

		final Cursor c = mdb.rawQuery("select " + DbSchema.ID + ", "
				+ DbSchema.EDT_NOM + ", " + DbSchema.HEUREM_HEURE + " from "
				+ DbSchema.HEUREM_TABLE_NAME + " where " + DbSchema.EDT_NOM
				+ " LIKE ?", new String[] { nom });

       // parcours toutes les activites
       while (c.moveToNext()) {
           final int id = c.getInt(0);
           final double heure = c.getDouble(2);
           heures.add(new HeuresMarquees(id,heure,nom));
       }
       c.close();
       return heures;
   }

}
