package com.example.createurdemploidutemps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
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
import boutons.Bouton;

import composants.AnimatedText;
import composants.Animer;
import composants.Ecran;
import composants.Utile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class MainActivity extends Activity
{

    boolean firstTime = true;

    ArrayList<EmploiDuTemps> emplois = new ArrayList<EmploiDuTemps>();

    LinearLayout liste;
    
    final Activity a = this;

    
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // full screen
        Ecran.fullScreen(this);

        setContentView(R.layout.activity_main);

        // Les emplois du temps
        chargerEmplois(); // charge les emplois du temps present dans le fichier
                          // texte
        liste = (LinearLayout) findViewById(R.id.liste);
        for (final EmploiDuTemps emploi : emplois)
        {
            final TextView txt_activite = new TextView(getApplicationContext());
            txt_activite.setText(emploi.getNomEnfant());
            txt_activite.setTextColor(getResources().getColor(R.color.indigo5));
            txt_activite.setTextSize(25f);
            liste.addView(txt_activite);
            // txt_activite.setOnClickListener(suprimerListener);
        }

        // taille ecran
        final int H = Ecran.getSize(this)[1];

        // titre
        final LinearLayout layout_titre = (LinearLayout) findViewById(R.id.titre);
        final int[] colors =
            {R.color.light_green3, R.color.light_green4, R.color.light_green5, R.color.green4,
                R.color.green5, R.color.blue3, R.color.blue5, R.color.red2, R.color.pink2,
                R.color.pink3, R.color.red3, R.color.pink4, R.color.red4, R.color.red5,
                R.color.red6};
        AnimatedText.add(this, layout_titre, "Creation de frise", colors, 80);

        /* Apparition du logo bouton */
        final Button logo_bouton = (Button) findViewById(R.id.logo_bouton);
        final RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
        final RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
        final ImageView shadow = (ImageView) findViewById(R.id.slide_top_shadow);
        Animer.activityApparitionAnimation(logo_bouton, slide_bottom, slide_top, shadow, H);

        // skin des boutons
        final Button ajouter = (Button) findViewById(R.id.ajouter);
        final Drawable ajouter_d = Bouton.roundedDrawable(this, R.color.light_blue3, 1f);
        final Button valider = (Button) findViewById(R.id.valider);
        final Drawable valider_d = Bouton.roundedDrawable(this, R.color.orange3, 1f);
        final Button supprimer = (Button) findViewById(R.id.supprimer);
        final Drawable supprimer_d = Bouton.roundedDrawable(this, R.color.red3, 0.5f);

        if (Build.VERSION.SDK_INT >= 16)
        {
            setBackground(ajouter,ajouter_d);
            setBackground(valider,valider_d);
            setBackground(supprimer,supprimer_d);
        }
        else
        {
            ajouter.setBackgroundDrawable(ajouter_d);
            valider.setBackgroundDrawable(valider_d);
            supprimer.setBackgroundDrawable(supprimer_d);
        }

        // Listener boutons
        ajouter.setOnClickListener(new View.OnClickListener()
        {

            
            public void onClick(final View v)
            {
                sauvegarderEmplois(); // save les emplois du temps dans le fichier
                final Intent intent = new Intent(getApplicationContext(), EmploiActivity.class);
                startActivity(intent);
            }
        });
        valider.setOnClickListener(new View.OnClickListener()
        {

            
            public void onClick(final View v)
            {
                sauvegarderEmplois();
                final Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        supprimer.setOnClickListener(new View.OnClickListener()
        {

            
            public void onClick(final View v)
            {
                alert("Attention", "Etes-vous sur de vouloir supprimer tous les emplois du temps ?");
            }
        });

        // recuperation d'un nouvel emploi du temps
        final Intent i = getIntent();
        final EmploiDuTemps new_emploi = (EmploiDuTemps) i.getSerializableExtra("sampleObject");
        if (new_emploi != null)
        {
            // ajout a la liste des emplois
            emplois.add(new_emploi);

            // ajout a la liste du nouvel enfant
            final TextView txt_activite = new TextView(getApplicationContext());
            txt_activite.setText(new_emploi.getNomEnfant());
            txt_activite.setTextColor(getResources().getColor(R.color.indigo3));
            txt_activite.setTextSize(25f);
            liste.addView(txt_activite);
        }

    }

    
    /* L'activite revient sur le devant de la scene */
    public void onResume()
    {
        super.onResume();
        firstTime = true;
        executeDelayed();

    }

    private void executeDelayed()
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {

            
            public void run()
            {
                // execute after 500ms
                Utile.hideNavBar(a);
            }
        }, 500);
    }

    
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable()
    {
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable()
    {
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)
            || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {
            return true;
        }
        return false;
    }

    /**
     * Ecrit le fichier
     * @param texte
     */
    public void writeFriseFile(final String texte)
    {
        // Get the directory for the user's public text document directory.
        try
        {
            if (isExternalStorageWritable())
            {

                deleteFriseFile();
                // This will get the SD Card directory and create a folder named
                // MyFiles in it.
                final File sdCard = Environment.getExternalStorageDirectory();
                final File directory = new File(sdCard.getAbsolutePath() + "/FilesFrise");
                directory.mkdirs();

                // Now create the file in the above directory and write the
                // contents into it
                final File file = new File(directory, "frise.txt");
                final FileOutputStream fOut = new FileOutputStream(file);
                final OutputStreamWriter osw = new OutputStreamWriter(fOut);
                osw.write(texte);
                osw.flush();
                osw.close();
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Lit le fichier
     * @param texte
     */
    public File readFriseFile()
    {
        try
        {
            if (isExternalStorageReadable())
            {
                final File sdCard = Environment.getExternalStorageDirectory();
                final File directory = new File(sdCard.getAbsolutePath() + "/FilesFrise");
                directory.mkdirs();
                final File file = new File(directory, "frise.txt");
                if (file.exists())
                {
                    return file;
                }
                else
                {
                    return null;
                }
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    void deleteFriseFile()
    {
        final File sdCard = Environment.getExternalStorageDirectory();
        final File directory = new File(sdCard.getAbsolutePath() + "/FilesFrise");
        final File file = new File(directory, "frise.txt");
        if (file.exists())
        {
            file.delete();
        }

    }

    /**
     * Ajoute les emplois du temps pr�sents sur le fichier texte
     */
    public void chargerEmplois()
    {
        final File frise = readFriseFile();
        emplois = TaskReader.read(frise, this);
    }

    public void sauvegarderEmplois()
    {
        final StringBuilder sb = new StringBuilder();
        for (final EmploiDuTemps emploi : emplois)
        { // pour chaque emploi du temps
            // d'enfant
            sb.append(emploi.getNomEnfant() + "\n");
            for (final Double heure_marque : emploi.getMarqueTemps())
            { // toutes les
                // marques
                // de temps
                sb.append(String.valueOf(heure_marque) + "\n");
            }
            sb.append("\n"); // saut de ligne
            for (final Task task : emploi.getEmploi())
            { // pour toutes les activites
                if (task.getCouleur() >= 0 && !task.getNom().contains("_"))
                {
                    sb.append(task.getNom() + "_" + task.getCouleur() + "\n");
                }
                else
                {
                    sb.append(task.getNom() + "\n");
                }
                sb.append(task.getDescription() + "\n");
                sb.append(task.getDuree() + "\n");
                sb.append(task.getHeureDebut() + "\n");
                sb.append(task.getImage() + "\n");
                sb.append("\n");
            }
            sb.append("\n");
        }
        final String texte = sb.toString();
        writeFriseFile(texte);
    }

    public void alert(final String titre, final String message)
    {
        new AlertDialog.Builder(this).setTitle(titre).setMessage(message)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
            {

                
                public void onClick(final DialogInterface dialog, final int which)
                {
                    emplois = new ArrayList<EmploiDuTemps>();
                    liste.removeAllViews();
                    deleteFriseFile();
                    dialog.cancel();
                }
            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
            {

                
                public void onClick(final DialogInterface dialog, final int which)
                {
                    dialog.cancel();
                }
            }).setIcon(android.R.drawable.ic_delete).show();
    }
    
    /**
	 * sets the background of a view depending on the API
	 * 
	 * @param v
	 * @param d
	 */
	private static void setBackground(final View v, final Drawable d) {
		if (Build.VERSION.SDK_INT >= 16) {
			// v.setBackground(d);
			Method methodBackgroung;
			try {
				methodBackgroung = View.class.getMethod("setBackground",
						Drawable.class);
				methodBackgroung.invoke(v, d);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			v.setBackgroundDrawable(d);
		}
	}

    /*
     * View.OnClickListener suprimerListener = new View.OnClickListener() {
     * 
     * public void onClick(final View activite) {
     * new AlertDialog.Builder(context)
     * .setTitle("Attention")
     * .setMessage("Voulez-vous supprimer cet emploi du temps ?")
     * .setNegativeButton("non", new DialogInterface.OnClickListener() {
     * public void onClick(DialogInterface dialog,
     * int which) {
     * dialog.cancel();
     * }})
     * .setPositiveButton("oui",
     * new DialogInterface.OnClickListener() {
     * public void onClick(DialogInterface dialog,
     * int which) {
     * dialog.cancel();
     * int indice = activite.getId();
     * myTasks.remove(indice);
     * liste_activite.removeView(activite);
     * }}))
     * }};
     */
    
    

}
