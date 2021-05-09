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
            if (meleeMode) {
                // simple wall follow
                setAhead(getBattleFieldWidth());
                execute();
            } else {
                doMove();
                if (getRadarTurnRemaining() == 0.0) setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
                execute();
            }
        }

    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if(meleeMode){
            return;
        }
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
        if(e.getDistance() > THRESHOLD){
            setTurnRight(e.getBearing());
            setAhead(30);
        }

        setFire(Math.min(400 / e.getDistance(), 3));
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        //turnLeft(90 - e.getBearing());
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        // Replace the next line with any behavior you would like
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



    private void goTo(int x, int y) {
        double a;
        setTurnRightRadians(Math.tan(
                a = Math.atan2(x -= (int) getX(), y -= (int) getY())
                        - getHeadingRadians()));
        setAhead(Math.hypot(x, y) * Math.cos(a));
    }
}
