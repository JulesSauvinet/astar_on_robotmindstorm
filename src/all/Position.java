package all;


public class Position {

	private int x;
	private int y;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Azimuth azimuthTo(Position current, Position next) {
		if (current.getX() == next.getX()) {
			return (current.getY() > next.getY()) ? Azimuth.NORTH : Azimuth.SOUTH;
		}
		else 
			return (current.getX() > next.getX()) ? Azimuth.WEST : Azimuth.EAST;
		}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
}
