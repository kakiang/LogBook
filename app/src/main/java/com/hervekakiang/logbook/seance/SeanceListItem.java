package com.hervekakiang.logbook.seance;

import com.hervekakiang.logbook.matiere.Matiere;

public class SeanceListItem {
    public static final int TYPE_MATIERE = 0;
    public static final int TYPE_SEANCE = 1;

    private final int type;
    private String matiereNom;
    private int matiereId;
    private Seance seance;
    private boolean isExpanded = true;

    public SeanceListItem(String matiereNom, int matiereId) {
        this.type = TYPE_MATIERE;
        this.matiereId = matiereId;
        this.matiereNom = matiereNom;
    }

    public SeanceListItem(Seance seance) {
        this.type = TYPE_SEANCE;
        this.seance = seance;
    }

    public int getType() {
        return type;
    }

    public String getMatiereNom() {
        return matiereNom;
    }

    public int getMatiereId() {
        return matiereId;
    }

    public Seance getSeance() {
        return seance;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
