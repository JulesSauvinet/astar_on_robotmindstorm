package tp2;

/**
 * Classe AZIMUTH Valeur de l'azimut pris dans une liste énumérée
 */
public enum Azimuth {
	WEST, NORTH, EAST, SOUTH;

	public Azimuth turnLeft() {
		switch (this) {
		case WEST:
			return SOUTH;
		case EAST:
			return NORTH;
		case NORTH:
			return WEST;
		case SOUTH:
			return EAST;
		}
		return null;
	}

	public Azimuth turnRight() {
		switch (this) {
		case WEST:
			return NORTH;
		case EAST:
			return SOUTH;
		case NORTH:
			return EAST;
		case SOUTH:
			return WEST;
		}
		return null;
	}

	public Azimuth UTurn() {
		switch (this) {
		case WEST:
			return EAST;
		case EAST:
			return WEST;
		case NORTH:
			return SOUTH;
		case SOUTH:
			return NORTH;
		}
		return null;
	}
	
	private boolean hasAtLeft(Azimuth azimuth) {
		switch (this) {
		case WEST:
			return azimuth.equals(SOUTH);
		case EAST:
			return azimuth.equals(NORTH);
		case NORTH:
			return azimuth.equals(WEST);
		case SOUTH:
			return azimuth.equals(EAST);
		}
		return false;
	}

	private int getLeftAngleTo(Azimuth target) {
		int a = 90;
		if (this.hasAtLeft(target)) {
			return a;
		} else {
			return a + this.turnLeft().getLeftAngleTo(target);
		}
	}

	public int getAngleTo(Azimuth target) {
		int angle = this.getLeftAngleTo(target);
		switch (angle) {
		case 270:
			angle = -90;
			break;
		case 360:
			angle = 0;
			break;
		default:
			break;
		}
		return angle;
	}
	
	public int getAngleTo2(Azimuth target) {
		switch (this) {
		case WEST:
			switch (target) {
			case WEST:
				return 0;
			case EAST:
				return 180;
			case NORTH:
				return 90;
			case SOUTH:
				return -90;
			}
		case EAST:
			switch (target) {
			case WEST:
				return 180;
			case EAST:
				return 0;
			case NORTH:
				return -90;
			case SOUTH:
				return 90;
			}
		case NORTH:
			switch (target) {
			case WEST:
				return -90;
			case EAST:
				return 90;
			case NORTH:
				return 0;
			case SOUTH:
				return 180;
			}
		case SOUTH:
			switch (target) {
			case WEST:
				return -90;
			case EAST:
				return 90;
			case NORTH:
				return 180;
			case SOUTH:
				return 0;
			}
		}
		return 0;
	}
}
