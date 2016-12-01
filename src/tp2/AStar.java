package tp2;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import lejos.nxt.Button;
import lejos.nxt.LCD;


public class AStar
{
	/**
	 * Fourni la liste des positions par lesquelles il faut passer pour retourner ˆ la position initiale<br>
	 * La position courante ne fait pas parti du chemin<br>
	 * Algo : A* A REMPLIR POUR LA PARTIE 2 DU TP
	 * 
	 * @return
	 */
	
	public static Stack<Noeud> findTheHomeWay(Position start, Position end, Graphe graphe)	{

		// Queue contenant le rŽsultat final i.e. le chemin
		Stack<Position> theWay = new Stack<Position>();
		
		// dans A* on manipule des Noeuds repréŽsentant les noeuds de l'espace des Žtats
		// Un TreeSet aurait éŽtéŽ un meilleur choix mais ce n'est pas supporter par Lejos...
		List<Noeud> open = new ArrayList<Noeud>();
		List<Noeud> closed = new ArrayList<Noeud>();
		// Le premier noeud (currentNoeud) est la position initiale du robot
		Noeud currentNoeud = graphe.getNode(start.getX(), start.getY());
		
		ArrayList <Noeud> nodes;
		nodes = graphe.getNodes();
		// Initialise les noeuds
		for(int i = 0; i < graphe.getSize(); i++){
			nodes.get(i).setCouleur(Noeud.Couleur.BLANC);
			nodes.get(i).setParent(null);
			nodes.get(i).setG(0.0);
			nodes.get(i).setH(0.0);	
			nodes.get(i).setF(0.0);
		}
		
		// calcul d'A* ...
		// Commence avec la première case 
		open.add(currentNoeud);
		boolean success = currentNoeud.getPosition().equals(end);

			
		// open = list grey 
		Double curr_g_cost;
		
		while(!open.isEmpty()){

			currentNoeud = getBestNoeud(open);
			success = currentNoeud.getPosition().equals(end);
			if (success)
				break;

			open.remove(currentNoeud);
			
			// Parcours des voisins de la case
			for(int i = 0; i < currentNoeud.noeudsVoisins.size(); i++) {
				Noeud noeudVoisin = currentNoeud.noeudsVoisins.get(i) ;
				if (noeudVoisin == null){
					continue;
				}

				// Case déjà visitée
				if(noeudVoisin.getCouleur()==Noeud.Couleur.NOIR) {
					continue;
				}
								
				noeudVoisin.setH(heuristic(noeudVoisin, end));
				
				//La distance depuis le départ du noeud courant					
				curr_g_cost = currentNoeud.getG() 
					+ cost(currentNoeud, noeudVoisin);	
				
				// Cette case voisine n'a pas encore été visitée ou offre un coût inférieur 
				if(!(noeudVoisin.getCouleur()==Noeud.Couleur.GRIS) || 
					curr_g_cost < noeudVoisin.getG()) {
				
					// On met a jour la distance g
					noeudVoisin.setG(curr_g_cost);
				
					if(!(noeudVoisin.getCouleur()==Noeud.Couleur.GRIS)) {
						noeudVoisin.setF(currentNoeud.getG() + currentNoeud.getH());
						open.add(noeudVoisin);
					}

					noeudVoisin.setParent(currentNoeud);
					noeudVoisin.setCouleur(Noeud.Couleur.GRIS);		
				}
			}
			currentNoeud.setCouleur(Noeud.Couleur.NOIR);
			closed.add(currentNoeud);
		}

		// ...
		// fin du calcul d'A*
		
		
		// Passage des noeuds de A* vers des positions dans la Queue i.e. le chemin de retour ˆ suivre
		// Inverser pour passer de queue a stack?
		Stack<Noeud> stackTMP = new Stack<Noeud>();
		Noeud n = new Noeud(0,0);
		stackTMP.push(n);
		if (success)
		{
			while (currentNoeud.getParent() != null)
			{
				currentNoeud = currentNoeud.getParent();
				stackTMP.push(currentNoeud);
			}	
		}
		return stackTMP;
	}

	/**
	 * Retourne le meilleur noeud de la liste des ouverts pour A*
	 * 
	 * @return
	 */
	private static Noeud getBestNoeud(List<Noeud> open)
	{
		Noeud best = null;
		for (Noeud n : open)
		{
			if (best == null || n.compareTo(best) < 0)
			{
				best = n;
			}
		}
		return best;
	}
	
	/**
	 * Pour l'instant, aucune heuristique
	 * Algo : A* A MODIFIER POUR LA PARTIE 4 DU TP
	 * 
	 * @param next
	 * @return
	 */
	private static Double heuristic(Noeud next, Position end)
	{
		int x_start = next.getAbscisse();
		int y_start = next.getOrdonnee();
		
		int x_end = end.getX();
		int y_end = end.getY();
		 
		Double dist = Math.sqrt((x_end - x_start)*(x_end - x_start) + (y_end - y_start)*(y_end - y_start));
		
		return dist;
	}

	/**
	 * Pour l'instant, le cout de passage d'un noeud ˆ un autre est fixe : 1.0
	 * Algo : A* A MODIFIER POUR LA PARTIE 4 DU TP
	 * 
	 * @param currentNoeud
	 * @param next
	 * @return
	 */
	private static Double cost(Noeud currentNoeud, Noeud next)
	{
		return 1.0;
	}
}
