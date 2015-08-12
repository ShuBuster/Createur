package com.example.createurdemploidutemps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class TaskReader {

	public static ArrayList<EmploiDuTemps> read(File file,Context context) {
		
		Scanner s;
		ArrayList<Task> myTasks = null;
		ArrayList<Double> marqueTemps = null;
		ArrayList<EmploiDuTemps> lesEmploisDuTemps = new ArrayList<EmploiDuTemps>();
		
		try {

			InputStream ips = new FileInputStream(file);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String ligne;
			
			while(notEmpty(ligne = br.readLine())){ // tant qu'il y a des enfants
				myTasks = new ArrayList<Task>(); // creation d'une nouvelle liste
				marqueTemps = new ArrayList<Double>();
				String nomEnfant = ligne; // nom de l'enfant
					while(notEmpty(ligne = br.readLine())){ // tant qu'il y a des temps forts
						s = new Scanner(ligne);
						double marque = Double.valueOf(s.next()); // duree de l'activite
						marqueTemps.add(marque);
					}
					Double[] mt = new Double[marqueTemps.size()];
					marqueTemps.toArray(mt);
				//br.readLine(); // saute une ligne apres le nom de l'enfant
				
				while(notEmpty(ligne = br.readLine())) { // tant qu'il y a des taches
					String nom = ligne; // nom de l'activite
					int couleur = -1;
					if(nom.contains("_")){ // si il y a une couleur
						int index = nom.indexOf("_");
						String col = nom.substring(index+1);
						couleur = Integer.valueOf(col);
					}
	
					ligne = br.readLine();
					String description = ligne; // description de l'activite
					
					ligne = br.readLine();
					s = new Scanner(ligne);
					double duree = Double.valueOf(s.next()); // duree de l'activite
					s.close();
					
					ligne = br.readLine();
					s = new Scanner(ligne);
					double heureDebut = Double.valueOf(s.next()); // heure de debut de l'activite
					s.close();
					
					ligne = br.readLine();
					String nomImage = ligne;
					Task myTask = new Task(nom, description, duree, heureDebut,nomImage,couleur);
					myTasks.add(myTask);
					
					ligne = br.readLine(); // passe le saut de ligne entre les requetes
	
				}
				EmploiDuTemps emploi = new EmploiDuTemps(myTasks, nomEnfant,mt);
				lesEmploisDuTemps.add(emploi);
			}

		}catch (Exception e) {
			System.out.println(e.toString());
		}
		return lesEmploisDuTemps;
	}

	public static boolean notEmpty(String str){
		if(str!=null){
			if(!str.equals("")){
				return true;
			}
		}
		return false;
	}
}
