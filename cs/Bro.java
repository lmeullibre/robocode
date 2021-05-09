package cs;
import robocode.*;
import robocode.util.Utils;
import java.awt.geom.Point2D;

import static robocode.util.Utils.normalRelativeAngleDegrees;
//import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Bro2 - a robot by (your name here)
 */
public class Bro extends AdvancedRobot
{
	private byte moveDirection = 1;


	/**
	 * run: Bro2's default behavior
	 */


	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar

		// Robot main loop


		while(true) {
			// Replace the next 4 lines with any behavior you would like
			doMove();
			if ( getRadarTurnRemaining() == 0.0 ) setTurnRadarRightRadians( Double.POSITIVE_INFINITY );
			execute();
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		double angleToEnemy = getHeadingRadians() + e.getBearingRadians();
		double radarTurn = Utils.normalRelativeAngle( angleToEnemy - getRadarHeadingRadians() );
		double extraTurn = Math.min( Math.atan( 36.0 / e.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS );
		if (radarTurn < 0)
			radarTurn -= extraTurn;
		else
			radarTurn += extraTurn;
		setTurnRadarRightRadians(radarTurn);
		double turn = getHeading() - getGunHeading() + e.getBearing();
		// normalize the turn to take the shortest path there
		setTurnGunRight(normalizeBearing(turn));

		// calculate firepower based on distance
		double firePower = Math.min(500 / e.getDistance(), 3);
		double bulletSpeed = 20 - firePower * 3;
		long time = (long)(e.getDistance() / bulletSpeed);


		setFire(Math.min(400 / e.getDistance(), 3));

	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		turnLeft(90 - e.getBearing());
	}


	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		back(20);
	}
	double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}

	double absoluteBearing(double x1, double y1, double x2, double y2) {
		double xo = x2-x1;
		double yo = y2-y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;

		if (xo > 0 && yo > 0) { // both pos: lower-Left
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
			bearing = 360 + arcSin; // arcsin is negative here, actuall 360 - ang
		} else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { // both neg: upper-right
			bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
		}

		return bearing;
	}

	public void doMove() {

		// always square off against our enemy

		// strafe by changing direction every 20 ticks
		if (getTime() % 20 == 0) {
			moveDirection *= -1;
			setAhead(150 * moveDirection);
		}
	}
}
