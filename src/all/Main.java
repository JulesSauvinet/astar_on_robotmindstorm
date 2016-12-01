package all;

import java.io.IOException;

import lejos.nxt.*;

public class main {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Robot robot = new Robot();
		Button.waitForAnyPress();
		robot.init();
	}
}