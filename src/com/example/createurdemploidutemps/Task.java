package com.example.createurdemploidutemps;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

@SuppressWarnings("serial")
public class Task implements Serializable{

	private String nom; // nom de l'activite
	private String description; // description de l'activite en quelques mots
	private double duree; // duree de l'activite en heure
	private double heureDebut; // heure du debut de l'activite
	private String nomImage; // une image illustrant l'activite
	private int couleur; // la couleur de l'activite dans la frise
	
	public Task(String nom, String description, double duree, double heureDebut,
			String nomImage,int couleur) {
		this.nom = nom;
		this.description = description;
		this.duree = duree;
		this.heureDebut = heureDebut;
		this.nomImage = nomImage;
		this.couleur = couleur;
	}

	public String getNom() {
		return nom;
	}

	public String getDescription() {
		return description;
	}

	public double getDuree() {
		return duree;
	}

	public double getHeureDebut() {
		return heureDebut;
	}

	public double getHeureFin() {
		return heureDebut+duree;
	}
	
	public String getImage() {
		return nomImage;
	}
	
	public int getCouleur(){
		return couleur;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDuree(int duree) {
		this.duree = duree;
	}

	public void setHeureDebut(int heureDebut) {
		this.heureDebut = heureDebut;
	}

	public void setImage(String nomImage) {
		this.nomImage = nomImage;
	}
	
	public void setCouleur(int couleur){
		this.couleur = couleur;
	}
	
	/**
	 * Donne la position en pixel du debut de l'activite sur la frise
	 * @param W longueur de la frise en pixel
	 * @param h0 heure de debut de la frise
	 * @param h1 heure de fin de la frise
	 * @return la position du debut
	 */
	public int getXbegin(int W, double h0, double h1){
		return (int) ((W/(h1-h0))*heureDebut - (h0*W)/(h1-h0));
	}
	
	/**
	 * Donne la largeur en pixel de l'activite sur la frise
	 * @param W longueur de la frise en pixel
	 * @param h0 heure de debut de la frise
	 * @param h1 heure de fin de la frise
	 * @return la largeur
	 */
	public int getXwidth(int W,double h0,double h1){
		return (int) ((duree*W)/(h1-h0));
	}
	
	/**
	 * Donne la position en pixel du milieu de l'activite sur la frise
	 * @param W longueur de la frise en pixel
	 * @param h0 heure de debut de la frise
	 * @param h1 heure de fin de la frise
	 * @return la largeur du milieu
	 */
	public int getMiddle(int W,double h0,double h1){
		int begin = getXbegin(W, h0, h1);
		int width = getXwidth(W, h0, h1);
		return begin + width/2;
	}
	

	
	/**
	 * Trouve une activite dans un planning relativement a une activite donne
	 * @param myTasks Le planning
	 * @param t L'activite de reference
	 * @param pas la distance relative a laquelle on veut trouver l'autre activite
	 * @return
	 */
	public static Task findRelativeTask(ArrayList<Task> myTasks,Task t,int pas){
		
		/* Retrouve l'indice de l'activite de reference dans le planning   */
		int id = indexOfTask(myTasks, t);
		
		/* Renvoie la bonne activite */
		if((id+pas>=0) && (id+pas<myTasks.size())){
		  return myTasks.get(id+pas);
		}
		/* Renvoie null si l'activite n'existe pas */
		else{
			return null;
		}
	}
	
	/**
	 * Trouve l'indice d'une activite dans un planning
	 * @param myTasks Le planning
	 * @param t L'activite
	 * @return l'indice
	 */
	public static int indexOfTask(ArrayList<Task> myTasks,Task t){
		
		/* Retrouve l'indice de l'activite de reference dans le planning */
		int id = 0;
		for(int i=0;i<myTasks.size();i++){
			if(myTasks.get(i)==t) id = i;
		}
		
		/* Renvoie la bonne activite */
		return id;
	}

}
