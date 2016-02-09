package com.example.basededonnees;

import java.util.ArrayList;

import modele.EmploiDuTemps;
import modele.HeuresMarquees;
import modele.Task;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class EmploiDAO extends DAOBase {

	public EmploiDAO(final Context pContext) {
		super(pContext);
	}

	/**
	 * @param edt
	 *            l'emploi du temps a ajouter a la base
	 * @return
	 */
	public long ajouter(final EmploiDuTemps edt) {
		final SQLiteDatabase mdb = open();

		final ContentValues value = new ContentValues();
		value.put(DbSchema.EDT_NOM, edt.getNomEnfant());
		value.put(DbSchema.EDT_VALIDE, edt.isValid() ? 1 : 0);

		if (!mdb.isReadOnly()) {
			return mDb.insert(DbSchema.EDT_TABLE_NAME, null, value);
		}
		return -1;
	}

	/**
	 * @param edt
	 *            l'emploi du temps a supprimer supprime aussi les activites et
	 *            les heures
	 */
	public void supprimer(final EmploiDuTemps edt) {
		final SQLiteDatabase mdb = open();
		String nom = edt.getNomEnfant();
		mdb.delete(DbSchema.EDT_TABLE_NAME, DbSchema.EDT_NOM + " LIKE ?",
				new String[] { nom });
		// toutes les activites doivent etre enlevees
		mdb.delete(DbSchema.TASK_TABLE_NAME, DbSchema.EDT_NOM + " LIKE ?",
				new String[] { nom });
		// toutes les heures marquees doivent etre enlevees
		mdb.delete(DbSchema.HEUREM_TABLE_NAME, DbSchema.EDT_NOM + " LIKE ?",
				new String[] { nom });
	}

	/**
	 * supprime tous les emplois du temps avec leurs activites et heures
	 */
	public void supprimerTous() {
		final SQLiteDatabase mdb = open();
		mdb.delete(DbSchema.EDT_TABLE_NAME, null, null);
		// toutes les activites doivent etre enlevees
		mdb.delete(DbSchema.TASK_TABLE_NAME, null, null);
		// toutes les heures marquees doivent etre enlevees
		mdb.delete(DbSchema.HEUREM_TABLE_NAME, null, null);

	}

	/**
	 * @param ancientNom
	 * @param emploi
	 *            l'emploi du temps modifie,
	 * 
	 */
	public void modifier(String ancientNom, final EmploiDuTemps emploi) {
		final SQLiteDatabase mdb = open();
		final ContentValues value = new ContentValues();
		value.put(DbSchema.EDT_NOM, emploi.getNomEnfant());
		value.put(DbSchema.EDT_VALIDE, emploi.isValid() ? 1 : 0);
		mdb.update(DbSchema.EDT_TABLE_NAME, value, DbSchema.ID + " = ?",
				new String[] { String.valueOf(emploi.getId()) });

		// modifie le nom de l'edt pour toutes les taches et les heures qui lui
		// etaient liees
		if (!emploi.getNomEnfant().equals(ancientNom)) {

			final ContentValues task_value = new ContentValues();
			task_value.put(DbSchema.EDT_NOM, emploi.getNomEnfant());
			mdb.update(DbSchema.TASK_TABLE_NAME, task_value, DbSchema.EDT_NOM
					+ " LIKE ?", new String[] { ancientNom });

			final ContentValues heure_value = new ContentValues();
			heure_value.put(DbSchema.EDT_NOM, emploi.getNomEnfant());
			mdb.update(DbSchema.HEUREM_TABLE_NAME, heure_value,
					DbSchema.EDT_NOM + " LIKE ?", new String[] { ancientNom });

		}

	}

	/**
	 * @param taskdao
	 * @param heuredao
	 * @return Selectionne tous les emplois du temps avec leurs activites et
	 *         heures
	 */
	public ArrayList<EmploiDuTemps> getEmplois(final TaskDAO taskdao,
			final HeuresDAO heuredao) {

		final SQLiteDatabase mdb = open();
		final ArrayList<EmploiDuTemps> emplois = new ArrayList<EmploiDuTemps>();
		// curseur contenant tous les edt
		final Cursor c = mdb.rawQuery("select " + DbSchema.ID + ",  "
				+ DbSchema.EDT_NOM + ",  " + DbSchema.EDT_VALIDE + " from "
				+ DbSchema.EDT_TABLE_NAME, null);

		// parcours tous les edt
		while (c.moveToNext()) {
			final int id = c.getInt(0);
			final String nom = c.getString(1);
			final int valide = c.getInt(2);
			final ArrayList<Task> tasks = taskdao.getAllTasks(nom);
			final ArrayList<HeuresMarquees> heures = heuredao
					.getHeuresMarquees(nom);
			final EmploiDuTemps edt = new EmploiDuTemps(id, tasks, nom, heures);
			edt.setValid(valide == 1);
			emplois.add(edt);
		}
		c.close();

		return emplois;
	}

}
