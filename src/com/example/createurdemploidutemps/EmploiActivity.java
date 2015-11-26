package com.example.createurdemploidutemps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class EmploiActivity extends Activity
{

    private int RESULT_LOAD_IMAGE;

    private PopupWindow pick_color;

    private PopupWindow existing_color;

    private String nomImage = "image_dejeuner.png";

    private int couleur = -1;

    private int couleur_picked = -1;

    private int[] colorsInt;

    private int[] boutonsID;

    private int i;

    private String heure_derniere_activite;

    private final Context context = this;

    final ArrayList<Task> myTasks = new ArrayList<Task>();

    LinearLayout liste_activite;

    private TextView modif;

    Button choix_couleur;

    
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emploi);

        modif = new TextView(this);
        // liste des heures a marquer
        final ArrayList<Double> heure_marques = new ArrayList<Double>();
        // les clics sur les images
        imageButtons();
        // layout qui contient les activites
        liste_activite = (LinearLayout) findViewById(R.id.liste_activite);
        // bouton qui sert a ajouter une activite au layout
        final Button ajouter_activite = (Button) findViewById(R.id.ajouter_activite);
        ajouter_activite.setOnClickListener(new View.OnClickListener()
        {

            
            public void onClick(final View v)
            {
                final EditText nom_activite = (EditText) findViewById(R.id.nom_activite);
                final EditText description_activite =
                    (EditText) findViewById(R.id.description_activite);
                final EditText fin_activite = (EditText) findViewById(R.id.fin);
                final EditText debut_activite = (EditText) findViewById(R.id.debut);
                // les 4 champs qui ont ete rempli

                final String nom = nom_activite.getText().toString();
                final String description = description_activite.getText().toString();
                double heureDebut = 1;
                double heureFin = 2;
                if (fin_activite.getText().toString().equals("")
                    || debut_activite.getText().toString().equals(""))
                {
                    alert("Activite non creee",
                        "Vous devez saisir une heure de debut et une heure de fin");
                }
                else
                {
                    heureFin = traductionHeure(fin_activite.getText().toString());
                    heureDebut = traductionHeure(debut_activite.getText().toString());
                    heure_derniere_activite = fin_activite.getText().toString();

                }
                final double duree = heureFin - heureDebut;
                if (heureFin <= heureDebut)
                {
                    alert("Activite non creee",
                        "Vous devez saisir une heure de debut anterieure a " + "l'heure de fin");
                }
                else if (nom.equals("") || description.equals(""))
                {
                    alert("Activite non creee",
                        "Vous devez saisir un nom et une description non vide");
                }
                else
                {
                    // on ajoute la nouvelle activite aux taches
                    final Task activite =
                        new Task(nom, description, duree, heureDebut, nomImage, couleur);
                    myTasks.add(activite);
                    // on ajoute le texte de l'activite au layout pour avoir
                    // un apercu de toutes les activites deja definies
                    final TextView txt_activite = new TextView(getApplicationContext());
                    final LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                    
                        params.setMargins(0, 50, 0, 0);
                    
                    txt_activite.setLayoutParams(params);
                    txt_activite.setText(nom + "  " + debut_activite.getText().toString() + " - "
                        + fin_activite.getText().toString());
                    txt_activite.setTextColor(getResources().getColor(R.color.indigo5));
                    // l'id de cette activite est celle de l'indice de la tache dans la liste
                    // myTasks
                    txt_activite.setId(myTasks.indexOf(activite));
                    txt_activite.setTextSize(20f);
                    // on ajoute cette activite a la position de l'indice
                    liste_activite.addView(txt_activite, txt_activite.getId());
                    // un textView qui sera utile s'il faut modifier cette activite
                    modif = txt_activite;
                    // on vide les champs
                    nom_activite.getText().clear();
                    description_activite.getText().clear();
                    fin_activite.getText().clear();
                    debut_activite.getText().clear();
                    if (heure_derniere_activite != null)
                    {
                        ;
                    }
                    // on rempli le champs heure de debut avec l'heure de fin de cette activite :
                    // gain de temps pour utilisateur
                    debut_activite.setText(heure_derniere_activite);
                    // le text est clickable pour etre modifie
                    txt_activite.setOnClickListener(modifierListener);
                }
            }
        });

        final Button ajouter_heure = (Button) findViewById(R.id.ajouter_heure_marque);
        final LinearLayout liste_heure = (LinearLayout) findViewById(R.id.liste_heure_marques);
        ajouter_heure.setOnClickListener(new View.OnClickListener()
        {

            
            public void onClick(final View v)
            {
                final EditText edit_heure_marque = (EditText) findViewById(R.id.edit_heure_marque);
                final TextView txt_heure_marque = new TextView(getApplicationContext());
                final LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

                    params.setMargins(0, 50, 0, 0);
                
                txt_heure_marque.setLayoutParams(params);
                if (!edit_heure_marque.getText().toString().equals(""))
                {
                    txt_heure_marque.setText(edit_heure_marque.getText().toString());
                    txt_heure_marque.setTextColor(getResources().getColor(R.color.indigo5));
                    txt_heure_marque.setTextSize(20f);
                    liste_heure.addView(txt_heure_marque);
                    heure_marques.add(traductionHeure(edit_heure_marque.getText().toString()));
                    edit_heure_marque.getText().clear();
                }
                else
                {
                    alert("Heure non marquee",
                        "Vous devez saisir une heure a marquer au bon format ");

                }

            }
        });

        final Button valider = (Button) findViewById(R.id.valide_enfant);
        valider.setOnClickListener(new View.OnClickListener()
        {

            
            public void onClick(final View v)
            {
                final EditText nom_enfant = (EditText) findViewById(R.id.nom_enfant);
                final String nom = nom_enfant.getText().toString();
                final Double[] mt = new Double[heure_marques.size()];
                heure_marques.toArray(mt);
                final EmploiDuTemps emploi = new EmploiDuTemps(myTasks, nom, mt);
                if (emploi.isPlanningChevauche())
                {
                    alert("Emploi du temps non valide", "Certaines activites se chevauchent");
                }
                else if (nom.equals(""))
                {
                    alert("Emploi du temps non valide", "Veuillez rentrer un nom pour l'enfant");
                }
                else
                {
                    new AlertDialog.Builder(context)
                        .setTitle("Attention")
                        .setMessage("Etes-vous sur de vouloir valider cette frise de journee ?")
                        .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener()
                            {

                                
                                public void onClick(final DialogInterface dialog, final int which)
                                {
                                    emploi.fillHoles(); // rempli les
                                    // trous de
                                    // l'emploi du
                                    // temps
                                    final Intent i =
                                        new Intent(getApplicationContext(), MainActivity.class);
                                    i.putExtra("sampleObject", emploi);
                                    i.putExtra("firstTime", false);
                                    startActivity(i);
                                    dialog.cancel();
                                }
                            })
                        .setNegativeButton(android.R.string.no,
                            new DialogInterface.OnClickListener()
                            {

                                
                                public void onClick(final DialogInterface dialog, final int which)
                                {
                                    dialog.cancel();
                                }
                            }).setIcon(android.R.drawable.ic_delete).show();

                }
            }
        });

        // Prealable aux choix des couleurs
        fillColorArrays();

        // Choix des couleurs
        choix_couleur = (Button) findViewById(R.id.changer_couleur);
        choix_couleur.setOnClickListener(new View.OnClickListener()
        {

            
            public void onClick(final View v)
            {
                // TODO
                final EditText nom_activite = (EditText) findViewById(R.id.nom_activite);
                final String nom = nom_activite.getText().toString();
                if (nom.equals(""))
                {
                    alert("Pas tout de suite !",
                        "Entrez d'abord un nom d'activite avant de choisir la couleur");
                }
                else
                {
                    final int activity_color = findColorActivity(nom);
                    if (activity_color >= 0)
                    { // Si une couleur est deja definie pour cette activite
                        final LayoutInflater inflater =
                            (LayoutInflater) getApplicationContext().getSystemService(
                                Context.LAYOUT_INFLATER_SERVICE);
                        existing_color =
                            new PopupWindow(inflater.inflate(R.layout.fixed_color, null, false),
                                600, 250, true);
                        final RelativeLayout existing_layout =
                            (RelativeLayout) existing_color.getContentView();

                        final ImageView la_couleur =
                            (ImageView) existing_layout.findViewById(R.id.la_couleur);
                        la_couleur.setBackgroundColor(getApplicationContext().getResources()
                            .getColor(colorsInt[activity_color]));
                        final Button ex_oui = (Button) existing_layout.findViewById(R.id.oui);
                        ex_oui.setOnClickListener(new View.OnClickListener()
                        {

                            
                            public void onClick(final View v)
                            {
                                openPickColor();
                                couleur = activity_color;
                                existing_color.dismiss();
                            }
                        });
                        final ImageButton close =
                            (ImageButton) existing_layout.findViewById(R.id.close);
                        close.setOnClickListener(new View.OnClickListener()
                        {

                            
                            public void onClick(final View v)
                            {
                                couleur = activity_color;
                                existing_color.dismiss();
                            }
                        });

                        existing_color.showAtLocation(findViewById(R.id.parent_view),
                            Gravity.CENTER, 0, 0);
                    }
                    else
                    { // si aucune couleur n'est encore definie
                        openPickColor();
                    }
                }

            }
        });

    }

    public String heureToString(final double heure)
    {
        final String h = String.valueOf((int) heure);
        final int m = (int) (60 * (heure - (int) heure));
        if (m == 0)
        {
            return h + "h";
        }
        final String min = String.valueOf(m);
        return h + "h" + min;
    }

    public double traductionHeure(final String h)
    {
        final String[] sep = h.split("h");
        final int heure = Integer.valueOf(sep[0]);
        double minute = 0;
        if (sep.length >= 2)
        {
            final int valeur = Integer.valueOf(sep[1]);
            minute = valeur / 60.0;
        }

        return heure + minute;
    }

    public void alert(final String titre, final String message)
    {
        new AlertDialog.Builder(this).setTitle(titre).setMessage(message)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
            {

                
                public void onClick(final DialogInterface dialog, final int which)
                {
                    dialog.cancel();
                }
            }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    public void imageButtons()
    {
        final ImageButton dejeuner = (ImageButton) findViewById(R.id.dejeuner);
        dejeuner.setTag("dejeuner");
        final ImageButton bob = (ImageButton) findViewById(R.id.bob);
        bob.setTag("bob");
        final ImageButton cours = (ImageButton) findViewById(R.id.cours);
        cours.setTag("cours");
        final ImageButton dodo = (ImageButton) findViewById(R.id.dodo);
        dodo.setTag("dodo");
        final ImageButton maison = (ImageButton) findViewById(R.id.maison);
        maison.setTag("maison");
        final ImageButton recre = (ImageButton) findViewById(R.id.recre);
        recre.setTag("recre");
        final ImageButton jeu = (ImageButton) findViewById(R.id.jeu);
        jeu.setTag("jeu");
        final Button autre = (Button) findViewById(R.id.autre);

        dejeuner.setOnClickListener(clic);
        bob.setOnClickListener(clic);
        cours.setOnClickListener(clic);
        dodo.setOnClickListener(clic);
        jeu.setOnClickListener(clic);
        maison.setOnClickListener(clic);
        recre.setOnClickListener(clic);

        // on prend des photos de la galerie de la tablette
        autre.setOnClickListener(new OnClickListener()
        {

            
            public void onClick(final View v)
            {
                // on accede a la galerie
                final Intent i =
                    new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);

            }
        });

    }

    @SuppressWarnings("deprecation")
    
    /**
     * on recupere l'image que l'on donne en parametre de l'attribut de img_choisie
     */
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data)
        {
            final Uri selectedImage = data.getData();
            final String[] filePathColumn = {MediaColumns.DATA};
            final Cursor cursor =
                getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            final int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            final String picturePath = cursor.getString(columnIndex);
            cursor.close();
            final ImageView img_choisie = (ImageView) findViewById(R.id.image_choisie);
            final Bitmap bm = BitmapFactory.decodeFile(picturePath);
            final Drawable d = new BitmapDrawable(getResources(), bm);

            if (Build.VERSION.SDK_INT >= 16)
            {
                setBackground(img_choisie,d);
            }
            else
            {
                img_choisie.setBackgroundDrawable(d);
            }

            // on rajoute un caractere pour differencier cette image qui vient
            // de la galerie et non des ressources
            nomImage = "@" + picturePath;
            Log.d("chemin", nomImage);
        }
    }

    /**
     * Applique les listeners aux boutons dans la fenetre de choix de la couleur
     * @param grid
     */
    void setColorListeners(final GridLayout grid)
    {
        final ImageView choix =
            (ImageView) pick_color.getContentView().findViewById(R.id.choix_couleur);
        for (i = 0; i < colorsInt.length; i++)
        {
            final Button color_bouton = (Button) grid.findViewById(boutonsID[i]);
            color_bouton.setTag(i);
            color_bouton.setOnClickListener(new View.OnClickListener()
            {

                
                public void onClick(final View v)
                {
                    final int tag = (Integer) v.getTag();
                    choix.setBackgroundColor(getApplicationContext().getResources().getColor(
                        colorsInt[tag]));
                    couleur_picked = tag;
                }
            });

        }
    }

    void fillColorArrays()
    {
        colorsInt = new int[15];
        boutonsID = new int[15];

        colorsInt[11] = R.color.deep_orange2;
        colorsInt[12] = R.color.orange2;
        colorsInt[13] = R.color.amber2;
        colorsInt[14] = R.color.yellow2;
        colorsInt[0] = R.color.light_green2;
        colorsInt[1] = R.color.green2;
        colorsInt[2] = R.color.teal2;
        colorsInt[3] = R.color.cyan2;
        colorsInt[4] = R.color.light_blue2;
        colorsInt[5] = R.color.blue2;
        colorsInt[6] = R.color.indigo2;
        colorsInt[7] = R.color.deep_purple2;
        colorsInt[8] = R.color.purple2;
        colorsInt[9] = R.color.pink2;
        colorsInt[10] = R.color.red2;

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
    }

    View.OnClickListener modifierListener = new View.OnClickListener()
    {

        
        public void onClick(final View activite)
        {
            new AlertDialog.Builder(context).setTitle("Attention")
                .setMessage("Voulez-vous modifier, supprimer ou ne rien faire ?")
                .setNeutralButton("ne rien faire", new DialogInterface.OnClickListener()
                {

                    
                    public void onClick(final DialogInterface dialog, final int which)
                    {
                        dialog.cancel();
                    }
                })
                // oui on souhaire modifier l'activite representee par son textView
                .setPositiveButton("Modifier activite", new DialogInterface.OnClickListener()
                {

                    
                    public void onClick(final DialogInterface dialog, final int which)
                    {
                        // on retrouve l'indice du textView :
                        // c'est sa position dans le layout
                        // et c'est aussi la position de l'activite dans myTasks
                        final int indice = activite.getId();
                        // on modifie les boutons de validation
                        final Button ajouter_activite =
                            (Button) findViewById(R.id.ajouter_activite);
                        ajouter_activite.setVisibility(View.INVISIBLE);
                        ajouter_activite.setEnabled(false);
                        final Button modifier_activite =
                            (Button) findViewById(R.id.modifier_activite);
                        modifier_activite.setVisibility(View.VISIBLE);
                        modifier_activite.setEnabled(true);
                        // les 4 champs sont preremlis avec les valeurs de l'activite a modifier
                        final EditText nom_activite = (EditText) findViewById(R.id.nom_activite);
                        final EditText description_activite =
                            (EditText) findViewById(R.id.description_activite);
                        final EditText fin_activite = (EditText) findViewById(R.id.fin);
                        final EditText debut_activite = (EditText) findViewById(R.id.debut);
                        nom_activite.setText(myTasks.get(indice).getNom());
                        description_activite.setText(myTasks.get(indice).getDescription());
                        fin_activite.setText(heureToString(myTasks.get(indice).getHeureFin()));
                        debut_activite.setText(heureToString(myTasks.get(indice).getHeureDebut()));
                        // on valide les modification :
                        modifier_activite.setOnClickListener(new View.OnClickListener()
                        {

                            
                            public void onClick(final View v)
                            {
                                // les boutons changent de nouveau pour revenir a l'etat d'origine
                                modifier_activite.setVisibility(View.INVISIBLE);
                                modifier_activite.setEnabled(false);
                                ajouter_activite.setVisibility(View.VISIBLE);
                                ajouter_activite.setEnabled(true);
                                // on recupere les valeurs des champs modifies
                                final String nom = nom_activite.getText().toString();
                                final String description =
                                    description_activite.getText().toString();
                                double heureDebut = 1;
                                double heureFin = 2;
                                if (fin_activite.getText().toString().equals("")
                                    || debut_activite.getText().toString().equals(""))
                                {
                                    alert("Activite non creee",
                                        "Vous devez saisir une heure de debut et une heure de fin");
                                }
                                else
                                {
                                    heureFin = traductionHeure(fin_activite.getText().toString());
                                    heureDebut =
                                        traductionHeure(debut_activite.getText().toString());
                                    final double duree = heureFin - heureDebut;
                                    if (heureFin <= heureDebut)
                                    {
                                        alert("Activite non modifiee",
                                            "Vous devez saisir une heure de debut anterieure a "
                                                + "l'heure de fin");
                                    }
                                    else if (nom.equals("") || description.equals(""))
                                    {
                                        alert("Activite non modifiee",
                                            "Vous devez saisir un nom et une description non vide");
                                    }
                                    else
                                    {
                                        // on recree une activite que l'on met dans myTasks
                                        // a la place de l'ancienne
                                        final Task activite =
                                            new Task(nom, description, duree, heureDebut, nomImage,
                                                couleur);
                                        myTasks.remove(indice);
                                        myTasks.add(indice, activite);
                                        // on remet la nouvelle activite dans le texte du textView
                                        // du layout
                                        modif.setText(nom + "  "
                                            + debut_activite.getText().toString() + " - "
                                            + fin_activite.getText().toString());
                                        // on vide les 4 champs
                                        nom_activite.getText().clear();
                                        description_activite.getText().clear();
                                        fin_activite.getText().clear();
                                        debut_activite.getText().clear();

                                    }
                                }
                            }
                        });

                        dialog.cancel();
                    }
                }).setNegativeButton("supprimer activite", new DialogInterface.OnClickListener()
                {

                    
                    public void onClick(final DialogInterface dialog, final int which)
                    {
                        final int indice = activite.getId();
                        myTasks.remove(indice);
                        liste_activite.removeView(activite);
                        dialog.cancel();
                    }
                }).setIcon(android.R.drawable.ic_delete).show();
        }
    };

    View.OnClickListener clic = new View.OnClickListener()
    {

        
        public void onClick(final View v)
        {
            nomImage = (String) v.getTag();
            Log.d("chemin", nomImage);
            final ImageView img_choisie = (ImageView) findViewById(R.id.image_choisie);
            final Drawable d = v.getBackground();
            if (Build.VERSION.SDK_INT >= 16)
            {
                setBackground(img_choisie,d);
            }
            else
            {
                img_choisie.setBackgroundDrawable(d);
            }
        }
    };

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
     * Cherche si une activite a deja ete cree avec ce nom et donne la couleur de celle-ci
     * @param nom_activite
     * @return le nom de la couleur correspondant
     */
    public int findColorActivity(final String nom_activite)
    {
        final File file = readFriseFile();
        if (file != null)
        {
            try
            {
                final InputStream ips = new FileInputStream(file);
                final InputStreamReader ipsr = new InputStreamReader(ips);
                final BufferedReader br = new BufferedReader(ipsr);
                String ligne;

                while ((ligne = br.readLine()) != null)
                { // balaie
                    if (ligne.startsWith(nom_activite))
                    { // trouve l'activite du meme nom
                        if (ligne.length() > nom_activite.length())
                        { // si il y a une couleur dessus
                            final String sub = ligne.substring(nom_activite.length() + 1);
                            return Integer.valueOf(sub);
                        }
                    }
                }
            }
            catch (final FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public void openPickColor()
    {
        final LayoutInflater inflater =
            (LayoutInflater) getApplicationContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        pick_color =
            new PopupWindow(inflater.inflate(R.layout.pick_color, null, false), 400, 500, true);
        setColorListeners((GridLayout) pick_color.getContentView().findViewById(R.id.color_grid));
        pick_color.showAtLocation(findViewById(R.id.parent_view), Gravity.CENTER, 0, 0);

        final RelativeLayout pick_layout = (RelativeLayout) pick_color.getContentView();
        final ImageButton close_pick = (ImageButton) pick_layout.findViewById(R.id.close);
        close_pick.setOnClickListener(new View.OnClickListener()
        {

            
            public void onClick(final View v)
            {
                pick_color.dismiss();
            }
        });
        final Button color_valider = (Button) pick_layout.findViewById(R.id.color_valider);
        color_valider.setOnClickListener(new View.OnClickListener()
        {

            
            public void onClick(final View v)
            {
                pick_color.dismiss();
                couleur = couleur_picked;
                choix_couleur.setText("Couleur choisie");
                if (couleur >= 0 && couleur < colorsInt.length)
                {
                    choix_couleur.setBackgroundColor(getResources().getColor(colorsInt[couleur]));
                }
            }
        });
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

}
