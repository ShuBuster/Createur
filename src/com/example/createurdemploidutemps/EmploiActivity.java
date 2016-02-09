package com.example.createurdemploidutemps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import modele.EmploiDuTemps;
import modele.HeuresMarquees;
import modele.Task;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import boutons.Bouton;

import com.example.basededonnees.EmploiDAO;
import com.example.basededonnees.HeuresDAO;
import com.example.basededonnees.TaskDAO;
import composants.MyLayoutParams;
import composants.Utile;

public class EmploiActivity extends Activity {

	private String nomEdt;

	private EmploiDuTemps emploi;

	/* liste triee des activites. */
	private ArrayList<Task> tasks;

	/* liste triee des heures marquees. */
	private ArrayList<Double> heures;

	private LinearLayout liste;

	private LinearLayout liste_heures;

	private final int ID = 1;

	private int H;

	private float textSize = 40;

	private final int unit = TypedValue.COMPLEX_UNIT_FRACTION;

	private final Activity a = this;

	private Button ajouter;

	private TextView nom;

	private View parent;

	private EditText edit_heures;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// full screen
		// Utile.fullScreen(this);
		setContentView(R.layout.activity_emplois);

		// taille ecran
		H = Utile.getScreenSize(a)[1];
		final int wDpi = Utile.getScreenSizePI(a)[0];
		if (wDpi > 300) {
			textSize = textSize + 20;
		}
		if (H < 1000) {
			textSize = 30;
		}

		// ///////////////////////////////////////////////////////
		// / Recuperation du nom de l'emploi du temps
		// ///////////////////////////////////////////////////////

		final Intent intent = getIntent();
		if (intent.getSerializableExtra("extra") != null) {
			emploi = (EmploiDuTemps) intent.getSerializableExtra("extra");
			nomEdt = emploi.getNomEnfant();

		}

		// / /////////////////////////////////////////
		// / IHM
		// //////////////////////////////////////////

		nom = (TextView) findViewById(R.id.nom);
		ajouter = (Button) findViewById(R.id.ajouter_edt);
		parent = findViewById(R.id.parent);
		liste = (LinearLayout) findViewById(R.id.liste_task);
		liste_heures = (LinearLayout) findViewById(R.id.heures_marquees);
		edit_heures = (EditText) findViewById(R.id.mes_heures);

		if (nomEdt != null) {
			nom.setText(nomEdt);
			nom.setTextSize(unit, textSize + 20);
		}
		final Drawable d = Bouton.roundedDrawable(a, R.color.light_blue3, 0.5f);
		ajouter.setBackgroundDrawable(d);

		// le listener d'ajout d'activites et d'heures
		ajouter.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				// ajout d'une activite
				if (edit_heures.getText().toString().equals("")) {
					final Intent secondActivity = new Intent(a,
							TaskActivity.class);

					// une activite vide avec pour heure de debut l'heure de
					// fin de la derniere activite
					double heureFin = -1;
					if (tasks.size() != 0) {
						heureFin = tasks.get(tasks.size()-1)
								.getHeureFin();
					}
					final Serializable extra = new Task(ID, nomEdt, "", "", heureFin,
							-1, "", -1);
					secondActivity.putExtra("extra", extra);
					a.startActivity(secondActivity);
				}
				// ajout d'une heure marquee
				else {

					if (HeuresMarquees.isValidHeure(edit_heures.getText()
							.toString())) {
						final double h = HeuresMarquees
								.traductionHeure(edit_heures.getText()
										.toString());
						// pas de doublons
						if (!heures.contains(h)) {
							// ajout de l'IHM
							addHeure(h);
							// ajout dans la base de donnees
							final int id = heures.size() + 1;
							new HeuresDAO(getApplicationContext())
									.ajouter(new HeuresMarquees(id, h, nomEdt));

						} else {
							Toast.makeText(
									getApplicationContext(),
									getResources().getString(
											R.string.hmarque_unique),
									Toast.LENGTH_LONG).show();
						}
						edit_heures.setText("");
					} else {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(
										R.string.hmarque_format),
								Toast.LENGTH_LONG).show();
					}

				}

			}
		});

	}

	/**
	 *
	 */
	@Override
	public void onResume() {
		super.onResume();
		// //////////////////////////////////////////////
		// / remet a jour l'IHM avec la base de donnees
		// //////////////////////////////////////////////
		liste.removeAllViews();
		liste_heures.removeAllViews();
		tasks = new TaskDAO(getApplicationContext()).getAllTasks(nomEdt);
		heures = new HeuresDAO(getApplicationContext()).getHeures(nomEdt);
		verification();

		// On tri les heures
		Collections.sort(heures);
		// rempli la scrollView avec toutes les activites
		addSpace();

		for (final Task task : tasks) {
			addTask(task);
			addSpace();
		}

		// renseigne toutes les heures marquees
		for (final Double heure : heures) {
			addHeure(heure);
		}

	}

	/**
	 * Verifie que les activites sont coherentes et non vide. Valide ou non
	 * l'emploi du temps associe. Trie les activites sur l'ecran si elle sont
	 * coherentes.
	 * 
	 * @return True si les activites sont coherentes : l'emploi est valide
	 */
	public boolean verification() {
		// pas d'activites
		if (tasks.isEmpty()) {
			emploi.setValid(false);
		}
		// l'emploi du temps est valide, pas de chevauchement d'activites
		else if (Task.trierTask(tasks) != null) {
			tasks = Task.trierTask(tasks);
			parent.setBackgroundColor(getResources().getColor(
					R.color.blanc_casse));
			nom.setTextColor(getResources().getColor(R.color.indigo7));
			emploi.setValid(true);
		}
		// l'emploi du temps n'est pas valide, des activites se chevauchent
		else {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.chevauche),
					Toast.LENGTH_LONG).show();
			nom.setTextColor(getResources().getColor(R.color.red1));
			parent.setBackgroundColor(getResources().getColor(R.color.grey4));
			emploi.setValid(false);
		}
		// on precise dans la base de donnees que l'emploi est valide ou non
		new EmploiDAO(getApplicationContext()).modifier(emploi.getNomEnfant(),
				emploi);
		return emploi.isValid();
	}

	/**
	 * @return some space
	 */
	protected void addSpace() {
		final LinearLayout space = new LinearLayout(this);
		final LayoutParams spaceParams = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, H / 40);
		space.setLayoutParams(spaceParams);
		liste.addView(space);

	}

	/**
	 * Ajoute un layout contenant la task dans la scrollView
	 * 
	 * @param task
	 */
	private void addTask(final Task task) {

		// le layout global
		final RelativeLayout layout = new RelativeLayout(
				getApplicationContext());
		liste.addView(layout);
		final LayoutParams params = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		layout.setLayoutParams(params);

		// le layout du contenut de l'activite

		final RelativeLayout contenut = new RelativeLayout(
				getApplicationContext());
		layout.addView(contenut);
		contenut.setId(1);
		final RelativeLayout.LayoutParams contenut_params = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		contenut.setPadding(10, 10, 20, 10);
		contenut.setLayoutParams(contenut_params);
		contenut.setBackgroundColor(getResources().getColor(task.getCouleur()));

		final Button contenut_d = Bouton.createButton(this, task.getCouleur());
		contenut.setBackgroundDrawable(contenut_d.getBackground());

		// le clic listenner du contenut pour etre modifie
		contenut.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				final Intent secondActivity = new Intent(a, TaskActivity.class);
				final Serializable extra = task;
				if (extra != null) {
					secondActivity.putExtra("extra", extra);
				}
				a.startActivity(secondActivity);
			}
		});

		// Tout ce que contient l'activite
		final int color = getResources().getColor(R.color.indigo7);

		final TextView nom = new TextView(getApplicationContext());
		contenut.addView(nom);
		nom.setText(task.getNom());
		nom.setId(2);
		nom.setTextSize(unit, textSize);
		nom.setTextColor(color);
		final MyLayoutParams nom_params = new MyLayoutParams();
		nom_params.marginLeft(30);
		nom.setLayoutParams(nom_params);

		final TextView heures = new TextView(getApplicationContext());
		contenut.addView(heures);
		heures.setTextSize(unit, textSize);
		heures.setTextColor(color);
		heures.setText(HeuresMarquees.toString(task.getHeureDebut()) + " - "
				+ HeuresMarquees.toString(task.getHeureFin()));
		final MyLayoutParams heures_params = new MyLayoutParams();
		heures_params.addRule(RelativeLayout.RIGHT_OF, 2);
		heures_params.marginLeft(30);
		heures.setLayoutParams(heures_params);

		/*
		 * final TextView img = new TextView(getApplicationContext());
		 * contenut.addView(img); img.setId(3); img.setTextSize(unit, textSize);
		 * img.setTextColor(color); img.setText("Image : " + task.getImage());
		 * final MyLayoutParams img_params = new MyLayoutParams();
		 * img_params.addRule(RelativeLayout.BELOW, 2);
		 * img_params.marginLeft(30); img.setLayoutParams(img_params);
		 */

		final TextView desc = new TextView(getApplicationContext());
		contenut.addView(desc);
		desc.setText(task.getDescription());
		desc.setTextSize(unit, textSize);
		desc.setTextColor(color);
		final MyLayoutParams desc_params = new MyLayoutParams();
		desc_params.addRule(RelativeLayout.BELOW, nom.getId());
		desc_params.marginLeft(30);
		desc.setLayoutParams(desc_params);

		// bouton pour supprimer l'activite
		final Button suppr = new Button(getApplicationContext());
		layout.addView(suppr);
		final RelativeLayout.LayoutParams suppr_params = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		suppr_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		suppr_params.addRule(RelativeLayout.ALIGN_BOTTOM, 1);
		suppr.setLayoutParams(suppr_params);
		suppr.setBackgroundDrawable(getResources()
				.getDrawable(R.drawable.close));

		suppr.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				liste.removeView(layout);
				new TaskDAO(getApplicationContext()).supprimer(task.getId());
				tasks.remove(task);
				verification();
			}
		});

	}

	/**
	 * Ajoute les heures marquees en bas
	 * 
	 * @param heure
	 */
	private void addHeure(final double heure) {
		// ajout IHM
		final TextView text = new TextView(getApplicationContext());
		liste_heures.addView(text);
		text.setId(3);
		text.setTextSize(unit, textSize);
		text.setTextColor(getResources().getColor(R.color.indigo7));
		text.setText(HeuresMarquees.toString(heure));
		final LayoutParams text_params = new LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		text_params.setMargins(30, 0, 0, 0);
		text.setLayoutParams(text_params);

		// ajout dans la base de donne se fait lorsque l'on valide(voir
		// onCreate())

		// listener de suppression de l'heure
		text.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				liste_heures.removeView(text);
				new HeuresDAO(getApplicationContext()).supprimer(heure);
			}
		});

	}

}
