package tp2;

import java.util.ArrayList;

public class Noeud {
	
	public enum Couleur {BLANC, GRIS, NOIR}
	private int abscisse;
	private int ordonnee;
	private Position position;
	
	private Noeud ouest;
	private Noeud est;
	private Noeud nord;
	private Noeud sud;

	// Pour le parcours en profondeur de l'aller;
	private Noeud parent;
	public ArrayList<Noeud> noeudsVoisins;	
	private Couleur couleur;

	// Pour trouver la sortie comme pour ASTAR, réinitialisation des valeurs entre les 2 procédures
	boolean estConnue; // Equivalent case blanche : On sait qu'il y a une case mais elle n'a pas encoré été explorée
	boolean estExploree; // Equivalent case grise : La case est connue et explorée
	boolean estVerifiee; // Equivalent case noire : La case est connue, explorée et tous ses voisins sont explorés

	//Le coût des noeuds
	//cout reel
	private Double g;
	//cout heuristique
	private Double h;	
	//somme
	private Double f;

	public Noeud(int x, int y) {
		this.abscisse = x;
		this.ordonnee = y;

		this.ouest = null;
		this.est = null;
		this.nord = null;
		this.sud = null;

		this.estConnue = false;
		this.estExploree = false;
		this.estVerifiee = false;

		noeudsVoisins = new ArrayList<>();
		position = new Position(abscisse, ordonnee);
	}

	public Noeud(Position position) {
		this.position = position;
		g = 0.0;
		f = 0.0;
	}

	/**
	 * METHODE COMPARETO Permet de trier deux noeuds par ordre croissant de f et
	 * descroissant de g Renvoie -1 / 0 / 1
	 */
	public int compareTo(Noeud o) {
		int r = this.f.compareTo(o.f);
		if (r == 0) {
			r = -1 * this.g.compareTo(o.g);
		}
		return r;
	}

	public int getAbscisse() {
		return abscisse;
	}

	public void setAbscisse(int abscisse) {
		this.abscisse = abscisse;
	}

	public int getOrdonnee() {
		return ordonnee;
	}

	public void setOrdonnee(int ordonnee) {
		this.ordonnee = ordonnee;
	}

	public Noeud getOuest() {
		return ouest;
	}

	public void setOuest(Noeud ouest) {
		this.ouest = ouest;
	}

	public Noeud getEst() {
		return est;
	}

	public void setEst(Noeud est) {
		this.est = est;
	}

	public Noeud getNord() {
		return nord;
	}

	public void setNord(Noeud nord) {
		this.nord = nord;
	}

	public Noeud getSud() {
		return sud;
	}

	public void setSud(Noeud sud) {
		this.sud = sud;
	}
	
	public Noeud getNoeudParAzimuth(Azimuth a) {
		switch (a) {
		case NORTH:
			return getNord();
		case SOUTH:
			return getSud();
		case EAST:
			return getEst();
		case WEST:
			return getOuest();
		
		default:
			return null;
		}
	}

	public boolean isEstVisitee() {
		return estConnue;
	}

	public void setEstVisitee(boolean estVisitee) {
		this.estConnue = estVisitee;
	}

	public boolean isEstVerifiee() {
		return estVerifiee;
	}

	public void setEstVerifiee(boolean estVerifiee) {
		this.estVerifiee = estVerifiee;
	}

	public boolean isEstExploree() {
		return estExploree;
	}

	public void setEstExploree(boolean estExploree) {
		this.estExploree = estExploree;
	}

	public Noeud getParent() {
		return parent;
	}

	public void setParent(Noeud parent) {
		this.parent = parent;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public Double getG() {
		return g;
	}

	public void setG(Double g) {
		this.g = g;
	}

	public Double getF() {
		return f;
	}

	public void setF(Double f) {
		this.f = f;
	}

	public Double getH() {
		return h;
	}

	public void setH(Double h) {
		this.h = h;
	}

	public Couleur getCouleur() {
		return couleur;
	}

	public void setCouleur(Couleur couleur) {
		this.couleur = couleur;
	}
}
