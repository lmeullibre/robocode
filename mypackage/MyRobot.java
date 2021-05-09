package mypackage;

import robocode.*;
import robocode.Robot;

public class MyRobot extends AdvancedRobot {

    public void run() {
        while (true) {
            // todo how to set at beggining of battle?
            //if (getOthers() > 1){
                // follow wall
                ahead(getBattleFieldWidth());

            //}else{
                // one-on-one strategy
                ahead(100);
                //turnGunRight(360);
            //}

        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        fire(1);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        setTurnLeftRadians( Math.cos(event.getBearingRadians()) );
        execute();
    }

}
