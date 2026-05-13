package com.hervekakiang.logbook.ue;

import java.io.Serializable;

public class UE implements Serializable {
    private int id;
    private String code;
    private String nom;

    public UE() {
    }

    public UE(String code, String nom) {
        this.code = code;
        this.nom = nom;
    }

    public UE(int id, String code, String nom) {
        this.id = id;
        this.code = code;
        this.nom = nom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
