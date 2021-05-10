package cs;

import mypackage.AdvancedEnemyBot;
import robocode.*;
import robocode.util.Utils;

import java.awt.geom.Point2D;

//import java.awt.Color;

public class Bro extends AdvancedRobot {
    public static final int THRESHOLD = 200;
    private byte moveDirection = 1;
    private boolean meleeMode = false;
    private AdvancedEnemyBot enemy = new AdvancedEnemyBot();

    public void run() {
        meleeMode = (getOthers() > 1);

        // setColors(Color.red,Color.blue,Color.green); // body,gun,radar
        while (true) {
            if(meleeMode){
                setAhead(getBattleFieldWidth());
            }else{
                doMove();
            }
            if (getRadarTurnRemaining() == 0.0) setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            execute();
        }

    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if (enemy.none() || e.getDistance() < enemy.getDistance() - 70 ||
                e.getName().equals(enemy.getName())) {

            // track him using the NEW update method
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
        setTurnGunRight(normalizeBearing(turn));

        // calculate firepower based on distance
        double firePower = Math.min(500 / e.getDistance(), 3);
        double bulletSpeed = 20 - firePower * 3;
        long time = (long) (e.getDistance() / bulletSpeed);

        // if distance > threshold
        if(!meleeMode && e.getDistance() > THRESHOLD){
            setTurnRight(e.getBearing());
            setAhead(30);
        }

        //setFire(Math.min(400 / e.getDistance(), 3));
        doGun();
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        //turnLeft(90 - e.getBearing());
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        if (meleeMode) {
            setTurnLeftRadians(Math.cos(e.getBearingRadians()));
            execute();
        } else {
            double bearing = e.getBearing(); //get the bearing of the wall
            turnRight(-bearing); //This isn't accurate but release your robot.
            ahead(100); //The robot goes away from the wall.
        }
    }

    double normalizeBearing(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    public void doMove() {
        if(getTime() % 20 == 0) {
            setTurnRight(enemy.getBearing()+90);
            // strafe by changing direction every 20 ticks
            moveDirection *= -1;
            setAhead(150 * moveDirection);
        }
    }

    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        meleeMode = (getOthers() > 1);

        // see if the robot we were tracking died
        if (e.getName().equals(enemy.getName())) {
            enemy.reset();
        }
    }

    void doGun() {

        // don't shoot if I've got no enemy
        if (enemy.none())
            return;

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
        // non-predictive firing can be done like this:
        //double absDeg = absoluteBearing(getX(), getY(), enemy.getX(), enemy.getY());

        // turn the gun to the predicted x,y location
        setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));

        // if the gun is cool and we're pointed in the right direction, shoot!
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(firePower);
        }
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
            bearing = 360 + arcSin; // arcsin is negative here, actually 360 - ang
        } else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
            bearing = 180 - arcSin;
        } else if (xo < 0 && yo < 0) { // both neg: upper-right
            bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
        }

        return bearing;
    }

}
