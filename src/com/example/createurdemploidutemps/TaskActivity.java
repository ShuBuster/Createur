package com.example.createurdemploidutemps;

import modele.HeuresMarquees;
import modele.Task;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;
import boutons.Bouton;

import com.example.basededonnees.TaskDAO;

public class TaskActivity extends Activity {

	/** Vue de la palette definie dans un xml a part. */
	private static PopupWindow pick_color = null;

	/** Couleur de la palette qui a ete choisie. */
	private int couleur_picked = -1;

	private final int[] colorsTab = Task.getColorTab();

	private int RESULT_LOAD_IMAGE;

	private String nomImage;

	private Task task;

	/**
	 * Bouton qui valide l'activite. Verifie la validite et la coherence des
	 * champs.
	 */
	private Button valider;

	private final Activity a = this;

	private ImageView img_choisie;

	private EditText nom;

	private EditText desc;

	private EditText hDebut;

	private EditText hFin;

	/** bouton qui ouvre la palette de couleur. */
	private Button couleur_bouton;

	/** les ID de tous les boutons de la palette, voir fillColorArrays. */
	private int[] boutonsID;

	private boolean isNewTask;

	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task);

		// ///////////////////////////////////////////////
		// / Recuperation des donnees
		// ///////////////////////////////////////////////

		final Intent intent = getIntent();
		if (intent.getSerializableExtra("extra") != null) {
			task = (Task) intent.getSerializableExtra("extra");
		}

		// definition des boutons et editText
		imageButtons();
		nom = (EditText) findViewById(R.id.nom_activite);
		desc = (EditText) findViewById(R.id.description_activite);
		hDebut = (EditText) findViewById(R.id.debut);
		hFin = (EditText) findViewById(R.id.fin);
		img_choisie = (ImageView) findViewById(R.id.image_choisie);
		valider = (Button) findViewById(R.id.valider);
		couleur_bouton = (Button) findViewById(R.id.changer_couleur);
		final Drawable d = Bouton.roundedDrawable(this, R.color.light_blue3, 1);
		couleur_bouton.setBackgroundDrawable(d);
		valider.setBackgroundDrawable(d);

		// Prealable aux choix des couleurs
		fillColorArrays();

		// listener sur le choix des couleurs
		couleur_bouton.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {

				openPickColor();

			}

		});

		// si la task est deja remplie, on la supprime apres validation pour la
		// remplacer par celle
		// modifiee
		isNewTask = (task.getHeureFin() == -1);
		// listener sur la valisation/verification
		valider.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {

				if (isTaskCorrect()) {

					task.setImage(nomImage);
					// on ajoute une nouvelle activite dans la base de donnees
					if (isNewTask) {
						new TaskDAO(getApplicationContext()).ajouter(task);
					}
					// on remplace l'activite deja presente dans la base de
					// donnees
					else {
						new TaskDAO(getApplicationContext()).modifier(task);
					}

					finish();

				}

			}
		});

	}

	/**
	 * rempli prealablement les champs avec les donnees de la Task recue en
	 * intent si la task est vide, les champs restent vides.
	 */

	public void onStart() {
		super.onStart();
		
		// hdebut recoit l'heure de fin de l'activite precedente par commodite.
		if (task.getHeureDebut() != -1) {
			hDebut.setText(HeuresMarquees.toString(task.getHeureDebut()));
		}

		// charge une task deja remplie pour la modifier
		if (!isNewTask) {
			nom.setText(task.getNom());
			desc.setText(task.getDescription());
			hDebut.setText(HeuresMarquees.toString(task.getHeureDebut()));
			hFin.setText(HeuresMarquees.toString(task.getHeureFin()));
			couleur_picked = task.getCouleur();
			nomImage = task.getImage();

			// change la couleur du bouton de choix des couleurs
			if (couleur_picked >= 0) {
				final Drawable d = Bouton.roundedDrawable(a, couleur_picked, 1);
				couleur_bouton.setBackgroundDrawable(d);
			}
			// change l'image
			if (nomImage.startsWith("@")) {
				String picturePath = nomImage.substring(1);
				final Bitmap bm = BitmapFactory.decodeFile(picturePath);
				final Drawable d = new BitmapDrawable(getResources(), bm);
				img_choisie.setBackgroundDrawable(d);
			} else {
				final int imageId = getResources().getIdentifier(nomImage,
						"drawable", getPackageName());
				img_choisie.setBackgroundDrawable(getResources().getDrawable(
						imageId));
			}
		}
	}

	/**
	 * Une photo de la galerie a ete choisie pour image de l'activite, on
	 * recupere l'image que l'on donne en parametre de l'attribut de img_choisie
	 */

	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
				&& null != data) {
			final Uri selectedImage = data.getData();
			final String[] filePathColumn = { MediaColumns.DATA };
			final Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			final int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			final String picturePath = cursor.getString(columnIndex);
			cursor.close();

			final Bitmap bm = Bitmap.createScaledBitmap(
					BitmapFactory.decodeFile(picturePath), 120, 120, true);
			final Drawable d = new BitmapDrawable(getResources(), bm);

			img_choisie.setBackgroundDrawable(d);

			// on rajoute un caractere pour differencier cette image qui vient
			// de la galerie et non des ressources
			nomImage = "@" + picturePath;
			task.setImage(nomImage);
			Log.d("chemin", nomImage);
		}
	}

	/**
	 * Definit les listenners sur les images a choisir, permet de changer
	 * l'image de l'activite.
	 */
	public void imageButtons() {
		// Images preexistantes

		final LinearLayout images = (LinearLayout) findViewById(R.id.images);
		for (int i = 0; i < images.getChildCount(); i++) {
			View v = images.getChildAt(i);
			v.setOnClickListener(new View.OnClickListener() {

				public void onClick(final View v) {
					nomImage = (String) v.getTag();
					Log.d("chemin", nomImage);
					final Drawable d = v.getBackground();
					img_choisie.setBackgroundDrawable(d);
				}
			});
		}

		final Button autre = (Button) findViewById(R.id.autre);

		// on prend des photos de la galerie de la tablette
		autre.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				// on accede a la galerie
				final Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				// on change le background de img_choisie
				startActivityForResult(i, RESULT_LOAD_IMAGE);
			}
		});

	}

	/**
	 * ouvre la palette de couleur definie dans la vue xml pick_color
	 */
	public void openPickColor() {
		final LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		pick_color = new PopupWindow(inflater.inflate(R.layout.pick_color,
				null, false), android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, true);

		final RelativeLayout pick_layout = (RelativeLayout) pick_color
				.getContentView();
		// bouton qui valide la couleur choisie
		final Button color_valider = (Button) pick_layout
				.findViewById(R.id.color_valider);

		// listeners sur les boutons couleurs de la palette
		setColorListeners((GridLayout) pick_color.getContentView()
				.findViewById(R.id.color_grid), color_valider);
		pick_color.showAtLocation(findViewById(R.id.parent_view),
				Gravity.CENTER, 0, 0);

		// listener de fermeture de la palette validation de la couleur
		color_valider.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				pick_color.dismiss();
				couleur_bouton.setText("Couleur choisie");

				// change la couleur du bouton de choix des couleurs
				if (couleur_picked >= 0) {
					final Drawable d = Bouton.roundedDrawable(a,
							couleur_picked, 1);
					couleur_bouton.setBackgroundDrawable(d);
				}
			}
		});
	};

	/**
	 * Applique les listeners aux boutons dans la palette de choix de la couleur
	 * Les listeners changent la couleur du bouton de validation
	 * 
	 * @param grid
	 * @param color_valider
	 */
	void setColorListeners(final GridLayout grid, final Button color_valider) {

		for (int i = 0; i < colorsTab.length; i++) {
			final Button couleur_palette = (Button) grid
					.findViewById(boutonsID[i]);
			couleur_palette.setTag(i);
			couleur_palette.setOnClickListener(new View.OnClickListener() {

				public void onClick(final View v) {
					final int tag = (Integer) v.getTag();
					color_valider.setBackgroundColor(getApplicationContext()
							.getResources().getColor(colorsTab[tag]));
					couleur_picked = colorsTab[tag];
				}
			});

		}
	}

	/**
	 * rempli les boutons de la palette de couleur avec les bonnes couleurs.
	 */
	void fillColorArrays() {
		boutonsID = new int[16];

		boutonsID[0] = R.id.c1;
		boutonsID[1] = R.id.c2;
		boutonsID[2] = R.id.c3;
		boutonsID[3] = R.id.c4;
		boutonsID[4] = R.id.c5;
		boutonsID[5] = R.id.c6;
		boutonsID[6] = R.id.c7;
		boutonsID[7] = R.id.c8;
		boutonsID[8] = R.id.c9;
		boutonsID[9] = R.id.c10;
		boutonsID[10] = R.id.c11;
		boutonsID[11] = R.id.c12;
		boutonsID[12] = R.id.c13;
		boutonsID[13] = R.id.c14;
		boutonsID[14] = R.id.c15;
		boutonsID[15] = R.id.c16;
	}

	/**
	 * Verifie que tous les champs sont remplis correctement, informe des
	 * erreurs dans le cas echeant. Rempli la task si aucune erreur detectee.
	 * 
	 * @return
	 */
	private boolean isTaskCorrect() {
		if (nom.getText().toString().equals("")) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.nom_vide),
					Toast.LENGTH_SHORT).show();
			return false;
		}

		task.setNom(nom.getText().toString());

		/*
		 * if (desc.getText().toString().equals("")) {
		 * Toast.makeText(getApplicationContext(),
		 * getResources().getString(R.string.desc_vide),
		 * Toast.LENGTH_SHORT).show(); return false; }
		 */

		task.setDescription(desc.getText().toString());

		// validite de l'heure de debut
		if (hDebut.getText().toString().equals("")) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.debut_vide),
					Toast.LENGTH_SHORT).show();
			return false;
		}

		if (!HeuresMarquees.isValidHeure(hDebut.getText().toString())) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.debut_format),
					Toast.LENGTH_LONG).show();
			return false;
		}

		// validite de l'heure de debut
		if (hFin.getText().toString().equals("")) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.fin_vide),
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!HeuresMarquees.isValidHeure(hFin.getText().toString())) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.fin_format),
					Toast.LENGTH_LONG).show();
			return false;
		}

		final double heureFin = HeuresMarquees.traductionHeure(hFin.getText()
				.toString());
		final double heureDebut = HeuresMarquees.traductionHeure(hDebut
				.getText().toString());

		if (heureFin <= heureDebut) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.heure_coherent),
					Toast.LENGTH_SHORT).show();
			return false;
		}
		task.setHeureFin(heureFin);
		task.setHeureDebut(heureDebut);

		if (couleur_picked == -1) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.couleur_picked),
					Toast.LENGTH_SHORT).show();
			return false;
		}
		task.setCouleur(couleur_picked);
		return true;

	}

}
