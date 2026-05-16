package com.hervekakiang.logbook.matiere;

import java.io.Serializable;

public class Matiere implements Serializable {
    private int id;
    private int ueId;
    private String nom;
    private String enseignant;
    private int volumeHoraire;

    public Matiere() {
    }

    public Matiere(int id, int ueId, String nom, String enseignant, int volumeHoraire) {
        this.id = id;
        this.ueId = ueId;
        this.nom = nom;
        this.enseignant = enseignant;
        this.volumeHoraire = volumeHoraire;
    }

    public Matiere(int ueId, String nom, String enseignant, int volumeHoraire) {
        this.ueId = ueId;
        this.nom = nom;
        this.enseignant = enseignant;
        this.volumeHoraire = volumeHoraire;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUeId() {
        return ueId;
    }

    public void setUeId(int ueId) {
        this.ueId = ueId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(String enseignant) {
        this.enseignant = enseignant;
    }

    public int getVolumeHoraire() {
        return volumeHoraire;
    }

    public void setVolumeHoraire(int volumeHoraire) {
        this.volumeHoraire = volumeHoraire;
    }
}
