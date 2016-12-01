package all;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import lejos.nxt.*;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.PilotProps;

public class Robot {

	// LES CONSTANTES
	/**
	 * angle de rotation du robot lorsqu'il tourne
	 */
	public static final int ANGLE = 90;
	/**
	 * taille d'une case "en mesure ultrason"
	 */
	private static final int MIN_CASE_LENGTH = 38;
	/**
	 * distance à partir de laquelle le robot détecte un mur
	 */
	private static final int OBSTACLE_DIST = 28;
	/**
	 * distance à partir de laquelle le robot s'arrête devant un mur
	 */
	private static final int OPTIMAL_DIST = 10;
	/**
	 * distance "infinie" pour détecter la sortie
	 */
	private static final int EXIT_THRESHOLD = 180;
	/**
	 * vitesse de rotation du robot (en degre per sec)
	 */
	private static final int ROTATION_SPEED = 70;
	/**
	 * vitesse de deplacement du robot
	 */
	//private static final double TRAVEL_SPEED = 150.0; // Sauvegarde valeur fonctionnelle
	private static final double TRAVEL_SPEED = 250.0;
	/**
	 * diamètre des roues du robot
	 */
	private static final String DIAMETRE_ROUE = "35";
	/**
	 * diamètre du robot
	 */
	private static final String DIAMETRE_ROBOT = "181";

	// LES VARIABLES
	/**
	 * capteur ultrason
	 */
	private static UltrasonicSensor sonar;
	/**
	 * position de départ du robot (0,0)
	 */
	private static Position initialPosition;
	/**
	 * position courante du robot calculée sur un repère orthonormé dont le
	 * centre est la position initiale du robot
	 */
	private static Position position;
	/**
	 * angle du robot par rapport au réferentiel (Nord / Sud / Est / Ouest)
	 */
	private static Azimuth currentAzimuth;
	/**
	 * mesure de distance mise à jour quand une case est atteinte (ou rotation)
	 */
	private static int odometer;
	/**
	 * booleen à vrai quand le robot a trouvé la sortie et est revenu à son
	 * point de départ
	 */

	// Autres variables de classe
	private DifferentialPilot pilote;
	private RegulatedMotor moteurDroit;
	private RegulatedMotor moteurGauche;
	private float diametreRoue;
	private float largeurRobot;
	private boolean reverse;	
	private boolean isOut = false;
	private static Graphe graphe;
	private Noeud noeudCourant;

	// Pour le parcours en profondeur de l'aller
	private ArrayList<Noeud> noeudsExplores;

	public Robot() throws IOException {
		
		PilotProps pp = new PilotProps();
		pp.loadPersistentValues();

		this.diametreRoue = Float.parseFloat(pp.getProperty(PilotProps.KEY_WHEELDIAMETER, DIAMETRE_ROUE));
		this.largeurRobot = Float.parseFloat(pp.getProperty(PilotProps.KEY_TRACKWIDTH, DIAMETRE_ROBOT));

		this.moteurGauche = PilotProps.getMotor(pp.getProperty(PilotProps.KEY_LEFTMOTOR, "A"));
		this.moteurDroit = PilotProps.getMotor(pp.getProperty(PilotProps.KEY_RIGHTMOTOR, "C"));
		boolean reverse = Boolean.parseBoolean(pp.getProperty(PilotProps.KEY_REVERSE, "false"));

		this.pilote = new DifferentialPilot(diametreRoue, largeurRobot, moteurGauche, moteurDroit, reverse);
		position = new Position(0, 0);
	}

	public void init() {

		// Configuration du robot et pilote
		sonar = new UltrasonicSensor(SensorPort.S3);
		this.pilote.setRotateSpeed(ROTATION_SPEED);
		this.pilote.setTravelSpeed(TRAVEL_SPEED);

		// Positionnement du robot dans l'espace (0,0 orienté Nord)
		initialPosition = new Position(0, 0);
		position = initialPosition;
		currentAzimuth = Azimuth.NORTH;

		// Création du graphe et de sa première case
		graphe = new Graphe();
		Noeud noeudInitial = new Noeud(0, 0);
		noeudInitial.estConnue = true;
		noeudInitial.estExploree = false;
		graphe.ajouterNoeud(noeudInitial);
		noeudCourant = noeudInitial;

		// Pour le parcours aller - et retour
		noeudsExplores = new ArrayList<>();

		// Lancement du programme principal de recherche & retour
		process();
	}

	/**
	 * METHODE PROCESS fait avancer le robot tant qu'il n'a pas trouvé la sortie
	 * et qu'il n'est pas revenu au point de départ
	 */
	private void process() {
		// On lance le parcours en profondeur depuis (0,0)
		visiter_pp(noeudCourant);

		// On lance A*
		demiTour(); // Facultatif
		Button.waitForAnyPress();
		goHome();
	}

	// Parcours en profondeur. Quand on trouve la sortie, la branche s'arrête.
	// Chaque récursions antérieures prend fin grace au if(isOut) {return;} et on retourne dans process() pour A*
	private void visiter_pp(Noeud n) {
		
		// On prépare un eventuel backtrack et marque le noeud exploré
		n.setParent(noeudCourant);
		noeudCourant = n;
		position = noeudCourant.getPosition();
		noeudCourant.estExploree = true;
		noeudsExplores.add(n);		
		
		// TODO : Je crois que resetOdometer() ne sert à rien : voir walldetected()
		resetOdometer();
		if (exitDetected()) {
			noeudCourant.noeudsVoisins.add(noeudCourant.getParent());
			isOut = true;
		} else {
			
			// On définit les voisins
			setVoisins();

			// Pour chaque direction non nulle et non visitée
			// Si la sortie est trouvée, la prochaine itération sera annulée grâce au return
			for (Azimuth a : Azimuth.values()) {
				Noeud nvoisin = getNoeudCourant().getNoeudParAzimuth(a);
				if (nvoisin != null && !nvoisin.estExploree) {
					if (!nvoisin.estExploree) {
						goTo(nvoisin);
						visiter_pp(nvoisin);
					}
				}
				if (isOut) { 
					return;
				}
			}

			// Si il n'y a plus rien à visiter à partir de la case courante, on backtrack
			noeudCourant.estVerifiee = true;
			Noeud p = noeudCourant.getParent();
			if (p != noeudCourant && !p.estVerifiee) {
				goTo(p);
				noeudCourant = p;
				position = noeudCourant.getPosition();
			} else {
				// On est bloqués dans un labyrinthe sans issue
				if (graphe.grapheVerifie()) {
					display("RITO PLZ", 0, false);
					display("Liberez moi...", 1, false);
					Button.waitForAnyPress();
					System.exit(0);
				}
			}
		}
	}

	// Permet au robot d'aller sur une case voisine donnée
	// On commence par tourner le robot dans la bonne direction
	private void goTo(Noeud p) {
		if (!position.equals(p.getPosition())) {
			Azimuth a = position.azimuthTo(position, p.getPosition());

			int angle = currentAzimuth.getAngleTo(a);
			if (angle != 0) {
				tourner(angle);
			}
			avancer();
		}
	}

	private void setVoisins() {
		for (Azimuth a : Azimuth.values()) {
			if (getNoeudCourant().getNoeudParAzimuth(a) == null) {
				int angle = currentAzimuth.getAngleTo(a);
				if (angle != 0) {
					tourner(angle);
				}

				// TODO : Je crois que resetOdometer() ne sert à rien : voir walldetected()
				resetOdometer();
				if (!wallDetected()) {
					decouvrirNoeud();
				}
			}
		}
	}

	private void goHome() {
		// Calcul via A* du chemin pour aller de la position courante à la position initiale
		Stack<Noeud> homeWay = null;
		homeWay = AStar.findTheHomeWay(position, initialPosition, graphe);

		// Faire suivre au robot le chemin calculé
		followTheWay(homeWay);
	}

	/**
	 * METHODE FOLLOWTHEWAY
	 * 
	 * @param homeWay
	 *            Fait avancer le robot en suivant le chemin fourni dans la
	 *            Queue homeWay
	 */
	private void followTheWay(Stack<Noeud> homeWay) {
		position = (Position) homeWay.pop().getPosition();
		while (!homeWay.isEmpty()) {
			Noeud nextPosition = (Noeud) homeWay.pop();			
			goTo(nextPosition);
			position = nextPosition.getPosition();
		}
		if (position.equals(initialPosition)) {
			display("Home sweet home!", 0, false);
			Sound.playNote(Sound.XYLOPHONE, 660, 200);
			Sound.playNote(Sound.XYLOPHONE, 660, 300);
			Sound.playNote(Sound.XYLOPHONE, 660, 300);
			Sound.playNote(Sound.XYLOPHONE, 523, 200);
			Sound.playNote(Sound.XYLOPHONE, 660, 400);
			Sound.playNote(Sound.XYLOPHONE, 784, 500);
			Sound.playNote(Sound.XYLOPHONE, 392, 600);
		}
	}

	// Creation dynamique du graphe
	// - On regarde notre azimuth et on en déduit les coordonnees de la case devant
	// - On vérifie si elle existe dans le graphe
	// - Si elle n'existe pas, on la créé et on la link aux autres
	// - Sinon, on met juste à jour les links
	private void decouvrirNoeud() {
		int nodeX;
		int nodeY;

		switch (currentAzimuth) {
		case NORTH:
			nodeX = position.getX();
			nodeY = position.getY() - 1;
			break;
		case SOUTH:
			nodeX = position.getX();
			nodeY = position.getY() + 1;
			break;
		case EAST:
			nodeX = position.getX() + 1;
			nodeY = position.getY();
			break;
		case WEST:
			nodeX = position.getX() - 1;
			nodeY = position.getY();
			break;
		default:
			nodeX = 0;
			nodeY = 0;
			break;
		}

		if (!graphe.existeCoordonnees(nodeX, nodeY)) {
			Noeud nouveauNoeud = new Noeud(nodeX, nodeY);
			nouveauNoeud.estConnue = true;
			graphe.ajouterNoeud(nouveauNoeud);
			nodeLinker(nouveauNoeud);
		} else {
			Noeud noeudVoisin = graphe.getNode(nodeX, nodeY);
			nodeLinker(noeudVoisin);
		}
	}

	private void nodeLinker(Noeud nouveauNoeud) {
		switch (currentAzimuth) {
		case NORTH:
			nouveauNoeud.setSud(noeudCourant);
			noeudCourant.setNord(nouveauNoeud);
			noeudCourant.noeudsVoisins.add(nouveauNoeud);
			nouveauNoeud.noeudsVoisins.add(noeudCourant);
			break;
		case SOUTH:
			nouveauNoeud.setNord(noeudCourant);
			noeudCourant.setSud(nouveauNoeud);
			noeudCourant.noeudsVoisins.add(nouveauNoeud);
			nouveauNoeud.noeudsVoisins.add(noeudCourant);
			break;
		case EAST:
			nouveauNoeud.setOuest(noeudCourant);
			noeudCourant.setEst(nouveauNoeud);
			noeudCourant.noeudsVoisins.add(nouveauNoeud);
			nouveauNoeud.noeudsVoisins.add(noeudCourant);
			break;
		case WEST:
			nouveauNoeud.setEst(noeudCourant);
			noeudCourant.setOuest(nouveauNoeud);
			noeudCourant.noeudsVoisins.add(nouveauNoeud);
			nouveauNoeud.noeudsVoisins.add(noeudCourant);
			break;
		}
	}

	
	  /***************************************************************************************/
	 /******************************** Marche/Arret/Gestion Sensor **************************/
	/***************************************************************************************/
	
	private void resetOdometer() {
		sonar.ping();
		odometer = sonar.getDistance();
	}

	private static boolean exitDetected() {
		sonar.ping();
		return sonar.getDistance() > EXIT_THRESHOLD;
	}

	private boolean wallDetected() {
		sonar.ping();
		int d = sonar.getDistance();
		boolean detection = d < OBSTACLE_DIST;
		if (detection) {
			// ajustement pour se remettre à la bonne distance du mur (milieu de case)
			if (d > OPTIMAL_DIST) {
				forwardCorrection();
			} else if (d < OPTIMAL_DIST) {
				backwardCorrection();
			}
		}
		return detection;
	}

	private void backwardCorrection() {
		this.pilote.setTravelSpeed(TRAVEL_SPEED / 2);
		backward();
		sonar.ping();
		int d = sonar.getDistance();
		while (d < OPTIMAL_DIST) {
			sonar.ping();
			d = sonar.getDistance();
		}
		stop();
		this.pilote.setTravelSpeed(TRAVEL_SPEED);
	}

	private void forwardCorrection() {
		this.pilote.setTravelSpeed(TRAVEL_SPEED / 2);
		forward();
		sonar.ping();
		int d = sonar.getDistance();
		while (d > OPTIMAL_DIST) {
			sonar.ping();
			d = sonar.getDistance();
		}
		stop();
		this.pilote.setTravelSpeed(TRAVEL_SPEED);
	}

	public void forward() {
		pilote.forward();
	}

	public void backward() {
		pilote.backward();
	}

	public void stop() {
		pilote.stop();
	}
	

	  /***************************************************************************************/
	 /************************************ Mouvements du robot ******************************/
	/***************************************************************************************/

	public void avancer() {
		pilote.travel(380);
	}

	public void reculer() {
		pilote.travel(-380);
	}

	public void tournerDroite() {
		pilote.rotate(ANGLE);
		currentAzimuth = currentAzimuth.turnRight();
	}

	public void tournerGauche() {
		pilote.rotate(-ANGLE);
		currentAzimuth = currentAzimuth.turnLeft();
	}

	public void demiTour() {
		pilote.rotate(2 * ANGLE);
		currentAzimuth = currentAzimuth.UTurn();
	}

	public void tourner(int angle) {
		switch (angle) {
		case 90:
			tournerGauche();
			break;
		case 180:
			demiTour();
			break;
		case -90:
			tournerDroite();
			break;
		default:
			break;
		}
	}
	

	  /***************************************************************************************/
	 /************************************ Getters & Setters ********************************/
	/***************************************************************************************/

	public DifferentialPilot getPilote() {
		return pilote;
	}

	public void setPilote(DifferentialPilot pilote) {
		this.pilote = pilote;
	}

	public RegulatedMotor getMoteurDroit() {
		return moteurDroit;
	}

	public void setMoteurDroit(RegulatedMotor moteurDroit) {
		this.moteurDroit = moteurDroit;
	}

	public RegulatedMotor getMoteurGauche() {
		return moteurGauche;
	}

	public void setMoteurGauche(RegulatedMotor moteurGauche) {
		this.moteurGauche = moteurGauche;
	}

	public float getDiametreRoue() {
		return diametreRoue;
	}

	public void setDiametreRoue(float diametreRoue) {
		this.diametreRoue = diametreRoue;
	}

	public float getLargeurRobot() {
		return largeurRobot;
	}

	public void setLargeurRobot(float largeurRobot) {
		this.largeurRobot = largeurRobot;
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public Graphe getGraphe() {
		return graphe;
	}

	public Noeud getNoeudCourant() {
		return noeudCourant;
	}

	public void setNoeudCourant(Noeud noeudCourant) {
		this.noeudCourant = noeudCourant;
	}

	/**
	 * FOR display
	 * 
	 * @param msg
	 * @param y
	 *            line index
	 * @param waiting
	 */
	public static void display(String msg, int y, boolean waiting) {
		LCD.drawString(msg, 0, y);
		if (waiting) {
			Button.waitForAnyPress();
			try {
				Thread.sleep(1000); // délai après l'appui sur bouton pour
									// redémarrer le robot
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
