package com.example.createurdemploidutemps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import boutons.Bouton;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import composants.AnimatedText;
import composants.Animer;
import composants.Ecran;

public class MainActivity extends Activity {

	boolean firstTime = true;
	ArrayList<EmploiDuTemps> emplois = new ArrayList<EmploiDuTemps>();
	LinearLayout liste;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// full screen
		Ecran.fullScreen(this);

		setContentView(R.layout.activity_main);

		// Les emplois du temps
		chargerEmplois(); // charge les emplois du temps present dans le fichier
							// texte
		liste = (LinearLayout) findViewById(R.id.liste);
		for (EmploiDuTemps emploi : emplois) {
			TextView txt_activite = new TextView(getApplicationContext());
			txt_activite.setText(emploi.getNomEnfant());
			txt_activite.setTextColor(getResources().getColor(R.color.indigo5));
			txt_activite.setTextSize(25f);
			liste.addView(txt_activite);
			//txt_activite.setOnClickListener(suprimerListener);
		}

		// taille ecran
		final int H = Ecran.getSize(this)[1];

		// titre
		LinearLayout layout_titre = (LinearLayout) findViewById(R.id.titre);
		int[] colors = { R.color.light_green3, R.color.light_green4,
				R.color.light_green5, R.color.green4, R.color.green5,
				R.color.blue3, R.color.blue5, R.color.red2, R.color.pink2,
				R.color.pink3, R.color.red3, R.color.pink4, R.color.red4,
				R.color.red5, R.color.red6 };
		AnimatedText.add(this, layout_titre, "Creation de frise", colors, 80);

			/* Apparition du logo bouton */
			Button logo_bouton = (Button) findViewById(R.id.logo_bouton);
			RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
			RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
			ImageView shadow= (ImageView) findViewById(R.id.slide_top_shadow);
			Animer.activityApparitionAnimation(logo_bouton,slide_bottom,slide_top,shadow,H);
		
		
		// skin des boutons
		Button ajouter = (Button) findViewById(R.id.ajouter);
		Drawable ajouter_d = Bouton.roundedDrawable(this, R.color.light_blue3, 1f);
		ajouter.setBackground(ajouter_d);
		Button valider = (Button) findViewById(R.id.valider);
		Drawable valider_d = Bouton.roundedDrawable(this,R.color.orange3, 1f);
		valider.setBackground(valider_d);
		Button supprimer = (Button) findViewById(R.id.supprimer);
		Drawable supprimer_d = Bouton.roundedDrawable(this, R.color.red3, 0.5f);
		supprimer.setBackground(supprimer_d);

		// Listener boutons
		ajouter.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				sauvegarderEmplois(); // save les emplois du temps dans le fichier
				Intent intent = new Intent(getApplicationContext(),
						EmploiActivity.class);
				startActivity(intent);
			}
		});
		valider.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				sauvegarderEmplois();
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
		supprimer.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				alert("Attention",
						"Etes-vous sur de vouloir supprimer tous les emplois du temps ?");
			}
		});

		// recuperation d'un nouvel emploi du temps
		Intent i = getIntent();
		EmploiDuTemps new_emploi = (EmploiDuTemps) i
				.getSerializableExtra("sampleObject");
		if (new_emploi != null) {
			// ajout a la liste des emplois
			emplois.add(new_emploi);

			// ajout a la liste du nouvel enfant
			TextView txt_activite = new TextView(getApplicationContext());
			txt_activite.setText(new_emploi.getNomEnfant());
			txt_activite.setTextColor(getResources().getColor(R.color.indigo3));
			txt_activite.setTextSize(25f);
			liste.addView(txt_activite);
		}

	}

	@Override
	/* L'activite revient sur le devant de la scene */
	public void onResume() {
		super.onResume();
		firstTime = true;
		executeDelayed();

	}

	private void executeDelayed() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// execute after 500ms
				hideNavBar();
			}
		}, 500);
	}

	private void hideNavBar() {
		if (Build.VERSION.SDK_INT >= 19) {
			View v = getWindow().getDecorView();
			v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	/**
	 * Ecrit le fichier
	 * 
	 * @param texte
	 */
	public void writeFriseFile(String texte) {
		// Get the directory for the user's public text document directory.
		try {
			if (isExternalStorageWritable()) {

				deleteFriseFile();
				// This will get the SD Card directory and create a folder named
				// MyFiles in it.
				File sdCard = Environment.getExternalStorageDirectory();
				File directory = new File(sdCard.getAbsolutePath()
						+ "/FilesFrise");
				directory.mkdirs();

				// Now create the file in the above directory and write the
				// contents into it
				File file = new File(directory, "frise.txt");
				FileOutputStream fOut = new FileOutputStream(file);
				OutputStreamWriter osw = new OutputStreamWriter(fOut);
				osw.write(texte);
				osw.flush();
				osw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Lit le fichier
	 * 
	 * @param texte
	 */
	public File readFriseFile() {
		try {
			if (isExternalStorageReadable()) {
				File sdCard = Environment.getExternalStorageDirectory();
				File directory = new File(sdCard.getAbsolutePath()
						+ "/FilesFrise");
				directory.mkdirs();
				File file = new File(directory, "frise.txt");
				if (file.exists()) {
					return file;
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	void deleteFriseFile() {
		File sdCard = Environment.getExternalStorageDirectory();
		File directory = new File(sdCard.getAbsolutePath() + "/FilesFrise");
		File file = new File(directory, "frise.txt");
		if (file.exists()) {
			file.delete();
		}

	}

	/**
	 * Ajoute les emplois du temps présents sur le fichier texte
	 */
	public void chargerEmplois() {
		File frise = readFriseFile();
		this.emplois = TaskReader.read(frise, this);
	}

	public void sauvegarderEmplois() {
		StringBuilder sb = new StringBuilder();
		for (EmploiDuTemps emploi : emplois) { // pour chaque emploi du temps
												// d'enfant
			sb.append(emploi.getNomEnfant() + "\n");
			for (Double heure_marque : emploi.getMarqueTemps()) { // toutes les
																	// marques
																	// de temps
				sb.append(String.valueOf(heure_marque) + "\n");
			}
			sb.append("\n"); // saut de ligne
			for (Task task : emploi.getEmploi()) { // pour toutes les activites
				if(task.getCouleur()>=0 && !task.getNom().contains("_")){
					sb.append(task.getNom()+"_"+task.getCouleur() + "\n");
				}else{
					sb.append(task.getNom()+"\n");
				}
				sb.append(task.getDescription() + "\n");
				sb.append(task.getDuree() + "\n");
				sb.append(task.getHeureDebut() + "\n");
				sb.append(task.getImage() + "\n");
				sb.append("\n");
			}
			sb.append("\n");
		}
		String texte = sb.toString();
		writeFriseFile(texte);
	}

	public void alert(String titre, String message) {
		new AlertDialog.Builder(this)
				.setTitle(titre)
				.setMessage(message)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								emplois = new ArrayList<EmploiDuTemps>();
								liste.removeAllViews();
								deleteFriseFile();
								dialog.cancel();
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						}).setIcon(android.R.drawable.ic_delete).show();
	}
	
	/*View.OnClickListener suprimerListener = new View.OnClickListener() {

		@Override
		public void onClick(final View activite) {
			new AlertDialog.Builder(context)
			.setTitle("Attention")
			.setMessage("Voulez-vous supprimer cet emploi du temps ?")
			.setNegativeButton("non", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int which) {
					dialog.cancel();
				}})
			.setPositiveButton("oui",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
								int indice = activite.getId();
								myTasks.remove(indice);
								liste_activite.removeView(activite);
							}}))
		}};*/

}
