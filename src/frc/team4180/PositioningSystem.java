package frc.team4180;

import com.analog.adis16448.frc.ADIS16448_IMU;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class PositioningSystem {
    private final ADIS16448_IMU bigBoard;
    private final BuiltInAccelerometer roboRio;
    private final Timer time;
    private double lastTime = 0;

    private Vector accleration;
    private Vector velocity;
    private Vector position;

    private static final double ERROR_THRESHOLD = 0.05;

    public PositioningSystem(ADIS16448_IMU bigBoard, BuiltInAccelerometer roboRio) {
        accleration = new Vector();
        velocity = new Vector();
        position = new Vector();
        this.bigBoard = bigBoard;
        this.roboRio = roboRio;
        bigBoard.calibrate();
        time = new Timer();
        time.start();
    }

    public void increment() {
        final double currentTime = time.get();
        final double dTime = currentTime - lastTime;
        lastTime = currentTime;
        if(dTime > 0.5) {
            return;
        }
        final Vector currentAccel = new Vector(bigBoard.getAccelX(), bigBoard.getAccelY(), bigBoard.getAccelZ());
        final Vector oldAccel = accleration;
        accleration = currentAccel;
        final Vector dVelocity = Vector.average(currentAccel, oldAccel).scalarTimes(dTime);
        final Vector oldVelocity = velocity;
        if(Math.abs(dVelocity.x) > ERROR_THRESHOLD || Math.abs(dVelocity.y) > ERROR_THRESHOLD) {
            velocity = Vector.add(dVelocity, velocity);
        }
        final Vector avgVelocity = Vector.average(oldVelocity, velocity).scalarTimes(dTime);
        position = Vector.add(position, avgVelocity);
        SmartDashboard.putString("DB/String 0", Double.toString(position.x));
        SmartDashboard.putString("DB/String 1", Double.toString(position.y));
        SmartDashboard.putString("DB/String 2", Double.toString(position.z));
    }

    private static class Vector {
        private final double x, y, z;

        private Vector(final double x, final double y, final double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private Vector() {
            this(0, 0, 0);
        }

        private static Vector add(final Vector v, final Vector w) {
            return new Vector(v.x + w.x, v.y + w.y, v.z + w.z);
        }

        private Vector scalarTimes(final double d) {
            return new Vector(d * x, d * y, d * z);
        }

        private static Vector average(final Vector v, final Vector w) {
            return add(v, w).scalarTimes(0.5);
        }
    }
}