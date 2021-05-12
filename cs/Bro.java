package cs;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.Point2D;

import java.awt.Color;

public class Bro extends AdvancedRobot {
    public static final int DIST_MINIMA = 200;
    private byte dirMovimiento = 1;
    private boolean multiplesEnemigos = false;
    private final Enemy enemigo = new Enemy();

    public void run() {
        setColors(Color.pink,Color.black,Color.black);

        multiplesEnemigos = (getOthers() > 1);

        while (true) {
            if(getTime() % 20 == 0) {
                setTurnRight(enemigo.getBearing()+90);
                dirMovimiento *= -1;
                setAhead(150 * dirMovimiento);
            }
            if (getRadarTurnRemaining() == 0.0) setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            execute();
        }

    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if (enemigo.isEmpty() || e.getDistance() < enemigo.getDistance() - 70 ||
                e.getName().equals(enemigo.getName())) {
            enemigo.updateEnemyRef(e, this);
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
        if(!multiplesEnemigos && e.getDistance() > DIST_MINIMA){
            setTurnRight(e.getBearing());
            setAhead(30);
        }
        if (enemigo.isEmpty()) return;
        double potencia = Math.min(500 / enemigo.getDistance(), 3);
        long temps = (long)(enemigo.getDistance() / (20 - potencia * 3));
        double theta = calcAnguloAbsoluto(getX(), getY(), enemigo.getXAfter(temps), enemigo.getYAfter(temps));
        setTurnGunRight(Utils.normalRelativeAngleDegrees(theta - getGunHeading()));
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(potencia);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        if(multiplesEnemigos) {
            setTurnGunRight(Utils.normalRelativeAngleDegrees(e.getBearing()));
            setTurnLeft(90 - e.getBearing());
            setAhead(200);
            execute();
        }
    }

    public void onHitRobot(HitRobotEvent event) {
        if(multiplesEnemigos) {
            double absDeg = calcAnguloAbsoluto(getX(), getY(), enemigo.getXAfter(0), enemigo.getYAfter(0));
            setTurnGunRight(Utils.normalRelativeAngleDegrees(absDeg - getGunHeading()));
            setFire(Rules.MAX_BULLET_POWER);
            setTurnLeft(90 - event.getBearing());
            setAhead(100);
            execute();
        }
    }

    public void onHitWall(HitWallEvent e) {
        if (multiplesEnemigos) {
            setTurnLeftRadians(Math.cos(e.getBearingRadians()));
        } else {
            turnRight(-e.getBearing());
            ahead(100);
        }
    }

    public void onRobotDeath(RobotDeathEvent e) {
        multiplesEnemigos = (getOthers() > 1);
        if (e.getName().equals(enemigo.getName())) {
            enemigo.resetEnemyRef();
        }
    }

    double calcAnguloAbsoluto(double x1, double y1, double x2, double y2) {
        double xo = x2-x1;
        double yo = y2-y1;
        double hipotenusa = Point2D.distance(x1, y1, x2, y2);
        double arcoseno = Math.toDegrees(Math.asin(xo / hipotenusa));

        if (xo > 0 && yo > 0) {
            return arcoseno;
        } else if (xo < 0 && yo > 0) {
            return 360 + arcoseno;
        } else if (xo > 0 && yo < 0) {
            return 180 - arcoseno;
        } else if (xo < 0 && yo < 0) {
            return 180 - arcoseno;
        }
        return 0;
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
