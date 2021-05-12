package cs;

import mypackage.AdvancedEnemyBot;
import robocode.*;
import robocode.util.Utils;

import java.awt.geom.Point2D;

import java.awt.Color;

public class Elver_Galarga extends AdvancedRobot {
    public static final int THRESHOLD = 200;
    private byte moveDirection = 1;
    private boolean meleeMode = false;
    private AdvancedEnemyBot enemy = new AdvancedEnemyBot();

    @Override
    public void run() {
        setColors(Color.pink,Color.black,Color.black); // body,gun,radar

        meleeMode = (getOthers() > 1);

        while (true) {
            // strafe
            if(getTime() % 20 == 0) {
                setTurnRight(enemy.getBearing()+90);
                moveDirection *= -1;
                setAhead(150 * moveDirection);
            }
            if (getRadarTurnRemaining() == 0.0) setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            execute();
        }

    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // set to track this new enemy
        if (enemy.none() || e.getDistance() < enemy.getDistance() - 70 ||
                e.getName().equals(enemy.getName())) {
            enemy.update(e, this);
        }

        double angleToEnemy = getHeadingRadians() + e.getBearingRadians();
        double radarTurn = Utils.normalRelativeAngle(angleToEnemy - getRadarHeadingRadians());
        double extraTurn = Math.min(Math.atan(36.0 / e.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);
        if (radarTurn < 0)
            radarTurn -= extraTurn;
        else
            radarTurn += extraTurn;
        setTurnRadarRightRadians(radarTurn);
        double turn = getHeading() - getGunHeading() + e.getBearing();
        // normalize the turn to take the shortest path there
        setTurnGunRight(Utils.normalRelativeAngleDegrees(turn));

        // FOLLOW THE MF
        if(!meleeMode && e.getDistance() > THRESHOLD){
            setTurnRight(e.getBearing());
            setAhead(30);
        }

        // don't shoot if I've got no enemy
        if (enemy.none()) return;

        // calculate firepower based on distance
        double firePower = Math.min(500 / enemy.getDistance(), 3);
        // calculate speed of bullet
        double bulletSpeed = 20 - firePower * 3;
        // distance = rate * time, solved for time
        long time = (long)(enemy.getDistance() / bulletSpeed);

        // calculate gun turn to predicted x,y location
        double futureX = enemy.getFutureX(time);
        double futureY = enemy.getFutureY(time);
        double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);

        // turn the gun to the predicted x,y location
        setTurnGunRight(Utils.normalRelativeAngleDegrees(absDeg - getGunHeading()));

        // if the gun is cool and we're pointed in the right direction, shoot!
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(firePower);
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        if(meleeMode) {
            setTurnGunRight(Utils.normalRelativeAngleDegrees(e.getBearing()));
            setTurnLeft(90 - e.getBearing());
            setAhead(200);
            execute();
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        if(meleeMode) {
            // point to robot we just hit, fire at max power, then move away
            double absDeg = absoluteBearing(getX(), getY(), enemy.getFutureX(0), enemy.getFutureY(0));
            setTurnGunRight(Utils.normalRelativeAngleDegrees(absDeg - getGunHeading()));
            setFire(Rules.MAX_BULLET_POWER);
            setTurnLeft(90 - event.getBearing());
            setAhead(100);
            execute();
        }
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        if (meleeMode) {
            setTurnLeftRadians(Math.cos(e.getBearingRadians()));
        } else {
            turnRight(-e.getBearing()); //This isn't accurate but release your robot.
            ahead(100); //The robot goes away from the wall.
        }
    }

    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        meleeMode = (getOthers() > 1);
        if (e.getName().equals(enemy.getName())) {
            enemy.reset();
        }
    }

    double absoluteBearing(double x1, double y1, double x2, double y2) {
        double xo = x2-x1;
        double yo = y2-y1;
        double hyp = Point2D.distance(x1, y1, x2, y2);
        double arcSin = Math.toDegrees(Math.asin(xo / hyp));
        double bearing = 0;

        if (xo > 0 && yo > 0) {
            bearing = arcSin;
        } else if (xo < 0 && yo > 0) {
            bearing = 360 + arcSin;
        } else if (xo > 0 && yo < 0) {
            bearing = 180 - arcSin;
        } else if (xo < 0 && yo < 0) {
            bearing = 180 - arcSin;
        }

        return bearing;
    }

}

