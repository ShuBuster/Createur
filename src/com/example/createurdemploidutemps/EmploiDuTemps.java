package com.example.createurdemploidutemps;

import java.io.Serializable;
import java.util.ArrayList;

public class EmploiDuTemps implements Serializable{
	
	
	private ArrayList<Task> emploi;
	private String nomEnfant;
	private Double[] marqueTemps;
	
	public Double[] getMarqueTemps() {
		return marqueTemps;
	}

	public void setMarqueTemps(Double[] marqueTemps) {
		this.marqueTemps = marqueTemps;
	}

	public EmploiDuTemps(ArrayList<Task> myTasks, String nom,Double[] marqueTemps){
		nomEnfant = nom;
		emploi = myTasks;
		this.marqueTemps = marqueTemps;
	}

	public ArrayList<Task> getEmploi() {
		return emploi;
	}

	public String getNomEnfant() {
		return nomEnfant;
	}

	public void setEmploi(ArrayList<Task> emploi) {
		this.emploi = emploi;
	}

	public void setNomEnfant(String nomEnfant) {
		this.nomEnfant = nomEnfant;
	}
	
	public boolean isPlanningChevauche(){
		for(int i=0;i<emploi.size()-1;i++){
			if(emploi.get(i).getHeureFin()>emploi.get(i+1).getHeureDebut()){
				return true;
			}
		}
		return false;
	}
	
	public void fillHoles(){
		for(int i=0;i<emploi.size()-1;i++){
			double hDebut = emploi.get(i).getHeureFin();
			double hFin =emploi.get(i+1).getHeureDebut();
			if(hDebut<hFin){
				Task libre = new Task("Temps libre", "On fait ce qu'on veut", hFin-hDebut, hDebut, "etoile",14);
				emploi.add(i+1,libre);
			}
		}
	}
}
