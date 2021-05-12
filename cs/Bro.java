package cs;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.Point2D;

import java.awt.Color;

public class Bro extends AdvancedRobot {
    public static final int THRESHOLD = 200;
    private byte moveDirection = 1;
    private boolean meleeMode = false;
    private final Enemy enemy = new Enemy();

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
        if (enemy.isEmpty() || e.getDistance() < enemy.getDistance() - 70 ||
                e.getName().equals(enemy.getName())) {
            enemy.updateEnemyRef(e, this);
        }
        double turnoRadar = Utils.normalRelativeAngle((getHeadingRadians() + e.getBearingRadians()) - getRadarHeadingRadians());
        double turnoExtra = Math.min(Math.atan(36.0 / e.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);
        if (turnoRadar < 0) {
            turnoRadar -= turnoExtra;
        }
        else {
            turnoRadar += turnoExtra;
        }
        setTurnRadarRightRadians(turnoRadar);
        setTurnGunRight(Utils.normalRelativeAngleDegrees(getHeading() - getGunHeading() + e.getBearing()));
        if(!meleeMode && e.getDistance() > THRESHOLD){
            setTurnRight(e.getBearing());
            setAhead(30);
        }
        if (enemy.isEmpty()) return;
        double potencia = Math.min(500 / enemy.getDistance(), 3);
        long temps = (long)(enemy.getDistance() / (20 - potencia * 3));
        double theta = calcAnguloAbsoluto(getX(), getY(), enemy.getXAfter(temps), enemy.getYAfter(temps));
        setTurnGunRight(Utils.normalRelativeAngleDegrees(theta - getGunHeading()));
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(potencia);
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
            double absDeg = calcAnguloAbsoluto(getX(), getY(), enemy.getXAfter(0), enemy.getYAfter(0));
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
            turnRight(-e.getBearing());
            ahead(100);
        }
    }

    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        meleeMode = (getOthers() > 1);
        if (e.getName().equals(enemy.getName())) {
            enemy.resetEnemyRef();
        }
    }

    double calcAnguloAbsoluto(double x1, double y1, double x2, double y2) {
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

    public static class Enemy {

        private volatile double bearing;
        private volatile double distance;
        private volatile double heading;
        private volatile String name = "";
        private volatile double velocity;
        private double x;
        private double y;

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getBearing() {
            return bearing;
        }

        public double getHeading() {
            return heading;
        }

        public double getVelocity() {
            return velocity;
        }

        public void resetEnemyRef() {
            bearing = 0.0;
            distance = 0.0;
            heading = 0.0;
            name = "";
            velocity = 0.0;
            x = 0.0;
            y = 0.0;
        }

        public boolean isEmpty() {
            return "".equals(name);
        }

        public void updateEnemyRef(ScannedRobotEvent e, Robot robot) {
            bearing = e.getBearing();
            distance = e.getDistance();
            heading = e.getHeading();
            name = e.getName();
            velocity = e.getVelocity();
            double absBearingDeg = (robot.getHeading() + e.getBearing());
            if (absBearingDeg < 0) absBearingDeg += 360;
            x = robot.getX() + Math.sin(Math.toRadians(absBearingDeg)) * e.getDistance();
            y = robot.getY() + Math.cos(Math.toRadians(absBearingDeg)) * e.getDistance();
        }

        public double getXAfter(long when){
            return x + Math.sin(Math.toRadians(getHeading())) * getVelocity() * when;
        }

        public double getYAfter(long when){
            return y + Math.cos(Math.toRadians(getHeading())) * getVelocity() * when;
        }
    }


}
