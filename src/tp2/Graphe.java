package tp2;

import java.util.ArrayList;

public class Graphe {
	public ArrayList<Noeud> nodes;

	public Graphe() {
		nodes = new ArrayList<>();
	}

	public void ajouterNoeud(Noeud node) {
		nodes.add(node);
	}

	public void creerNoeud(int x, int y) {
		Noeud node = new Noeud(x, y);
		nodes.add(node);
	}

	// Verifie si le graphe est totalement parcouru
	public boolean grapheVerifie() {
		for (Noeud node : nodes) {
			if (node.isEstVerifiee() == false) {
				return false;
			}
		}
		return true;
	}

	// Verifie si une node existe dans le graphe via ses coordonnées
	public boolean existeCoordonnees(int x, int y) {
		for (Noeud node : nodes) {
			if (x == node.getAbscisse() && y == node.getOrdonnee()) {
				return true;
			}
		}
		return false;
	}

	// Renvoie la node dont les coordonnées sont (x,y)
	public Noeud getNode(int x, int y) {
		for (Noeud node : nodes) {
			if (x == node.getAbscisse() && y == node.getOrdonnee()) {
				return node;
			}
		}
		return null;
	}

	public int getSize() {
		return nodes.size();
	}

	public ArrayList<Noeud> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<Noeud> nodes) {
		this.nodes = nodes;
	}
}
