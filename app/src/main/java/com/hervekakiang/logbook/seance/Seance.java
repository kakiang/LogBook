package com.hervekakiang.logbook.seance;

import java.io.Serializable;

public class Seance implements Serializable {
    private int id;
    private int matiereId;
    private String date;
    private String heureDebut;
    private int duree;
    private String contenuPedagogique;

    public Seance() {
    }

    public Seance(int id, int matiereId, String date, String heureDebut, int duree, String contenuPedagogique) {
        this.id = id;
        this.matiereId = matiereId;
        this.date = date;
        this.heureDebut = heureDebut;
        this.duree = duree;
        this.contenuPedagogique = contenuPedagogique;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMatiereId() {
        return matiereId;
    }

    public void setMatiereId(int matiereId) {
        this.matiereId = matiereId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public String getContenuPedagogique() {
        return contenuPedagogique;
    }

    public void setContenuPedagogique(String contenuPedagogique) {
        this.contenuPedagogique = contenuPedagogique;
    }
}
