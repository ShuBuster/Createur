package com.example.basededonnees;

import java.util.ArrayList;

import modele.Task;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TaskDAO extends DAOBase {

	/**
	 * 
	 * @param pContext
	 */
	public TaskDAO(Context pContext) {
		super(pContext);
	}

	/**
	 * @param t
	 *            l'activite a ajouter la base.
	 */
	public void ajouter(Task t) {

		SQLiteDatabase mdb = open();

		ContentValues value = new ContentValues();
		value.put(DbSchema.EDT_NOM, t.getEdt_name());
		value.put(DbSchema.TASK_NOM, t.getNom());
		value.put(DbSchema.TASK_DEBUT, t.getHeureDebut());
		value.put(DbSchema.TASK_FIN, t.getHeureFin());
		value.put(DbSchema.TASK_IMAGE, t.getImage());
		value.put(DbSchema.TASK_COULEUR, t.getCouleur());
		value.put(DbSchema.TASK_DESCRIPTION, t.getDescription());

		if (!mdb.isReadOnly()) {
			mdb.insert(DbSchema.TASK_TABLE_NAME, DbSchema.TASK_NOM, value);
		}

	}

	/**
	 * @param id
	 *            l'identifiant de l'activite a supprimer
	 */
	public void supprimer(long id) {
		SQLiteDatabase mdb = open();
		mdb.delete(DbSchema.TASK_TABLE_NAME, DbSchema.ID + " = ?",
				new String[] { String.valueOf(id) });
	}

	/**
	 * Remplace l'activite existante (meme ID) par la nouvelle.
	 * 
	 * @param t
	 *            l'activite modifiee
	 */
	public void modifier(Task t) {
		SQLiteDatabase mdb = open();
		ContentValues value = new ContentValues();
		value.put(DbSchema.EDT_NOM, t.getEdt_name());
		value.put(DbSchema.TASK_NOM, t.getNom());
		value.put(DbSchema.TASK_DEBUT, t.getHeureDebut());
		value.put(DbSchema.TASK_FIN, t.getHeureFin());
		value.put(DbSchema.TASK_IMAGE, t.getImage());
		value.put(DbSchema.TASK_COULEUR, t.getCouleur());
		value.put(DbSchema.TASK_DESCRIPTION, t.getDescription());
		mdb.update(DbSchema.TASK_TABLE_NAME, value, DbSchema.ID + " = ?",
				new String[] { String.valueOf(t.getId()) });
	}

	/**
	 * 
	 * @param nom
	 * @return toutes les activites de l'emploi du temps ou une arrayList<Task>
	 *         vide si l'emploi du temps n'a pas de donnees
	 */
	public ArrayList<Task> getAllTasks(String nom) {

		SQLiteDatabase mdb = open();
		ArrayList<Task> tasks = new ArrayList<Task>();
		// curseur contenant toutes les activites d'un meme edt

		Cursor c = mdb.rawQuery("select " + DbSchema.ID + ", "
				+ DbSchema.EDT_NOM + ", " + DbSchema.TASK_NOM + ", "
				+ DbSchema.TASK_DEBUT + ", " + DbSchema.TASK_FIN + ", "
				+ DbSchema.TASK_IMAGE + ", " + DbSchema.TASK_COULEUR + ", "
				+ DbSchema.TASK_DESCRIPTION + " from "
				+ DbSchema.TASK_TABLE_NAME + " where " + DbSchema.EDT_NOM
				+ " LIKE ?", new String[] { nom });

		// parcours toutes les activites
		while (c.moveToNext()) {
			int id = c.getInt(0);
			String nomTask = c.getString(2);
			double heureDebut = c.getDouble(3);
			double heureFin = c.getDouble(4);
			String image = c.getString(5);
			int couleur = c.getInt(6);
			String description = c.getString(7);

			tasks.add(new Task(id, nom, nomTask, description, heureDebut,
					heureFin, image, couleur));
		}
		c.close();
		return tasks;
	}
}
