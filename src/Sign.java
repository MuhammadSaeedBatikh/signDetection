import com.leapmotion.leap.Hand;
import com.leapmotion.leap.*;

/**
 * Created by Muhammad on 17/07/2017.
 */
public class Sign {
    // static enum Group {BACKHAND, FOREHAND}

    boolean staticGesture;
    boolean phalangesAreImportant;
    float letterFrequency = 0;
    Vector[] fingersDirections = new Vector[5];
    Vector[] distalPhalangesDirections = new Vector[5];
    Vector[] proximalPhalangesDirections = new Vector[5];
    float[] handTofingertipsDistances = new float[5]; // distance from the center of the hand to each fingertip normalized to each finger length

    //cos(angle) = dot(a,b)/(||a|| ||b||), a and b are the proximal Vectors
    float[] anglesBetweenAdjacentFingers = new float[4]; // a[0]=angle between pinky and ring, a[1] = angle between ring and middle etc
    /* float pitch;   //angle around the x-axis
    float yaw;     //angle around the y-axis
     float roll;    //angle around the z-axis
     // The Vector class defines functions for getting the pitch, yaw, and roll
 */
    float grabAngle;
     /*The angle is computed by looking at the angle between the direction of the 4 fingers and the direction of the hand.
     Thumb is not considered when computing the angle.
     The angle is 0 radian for an open hand, and reaches pi radians when the pose is a tight fist.*/

    float pinchDistance; // the shortest distance between the last 2 phalanges of the thumb and those of the index finger
    float[] signFeatures = new float[52];

    public Sign(Vector[] fingersDirections, Vector[] distalPhalangesDirections,
                Vector[] proximalPhalangesDirections, Vector armDirection) {

        this.fingersDirections = fingersDirections;
        this.distalPhalangesDirections = distalPhalangesDirections;
        this.proximalPhalangesDirections = proximalPhalangesDirections;
    }

    public Sign(Vector[] fingersDirections, Vector[] distalPhalangesDirections,
                Vector[] proximalPhalangesDirections, float grabAngle, float pinchDistance) {

        this.fingersDirections = fingersDirections;
        this.distalPhalangesDirections = distalPhalangesDirections;
        this.proximalPhalangesDirections = proximalPhalangesDirections;
        this.grabAngle = grabAngle;
        this.pinchDistance = pinchDistance;
    }

    public Sign(boolean staticGesture, boolean phalangesAreImportant, float letterFrequency,
                Vector[] fingersDirections, Vector[] proximalPhalangesDirections, Vector[] distalPhalangesDirections,
                Vector armDirection, float[] handTofingertipsDistances, float[] anglesBetweenAdjacentFingers,
                float grabAngle, float pinchDistance) {

        this.staticGesture = staticGesture;
        this.phalangesAreImportant = phalangesAreImportant;
        this.letterFrequency = letterFrequency;
        this.fingersDirections = fingersDirections;
        this.distalPhalangesDirections = distalPhalangesDirections;
        this.proximalPhalangesDirections = proximalPhalangesDirections;
        this.handTofingertipsDistances = handTofingertipsDistances;
        this.anglesBetweenAdjacentFingers = anglesBetweenAdjacentFingers;
        this.grabAngle = grabAngle;
        this.pinchDistance = pinchDistance;
    }

    public Sign(Sign sign) {
        this.staticGesture = sign.staticGesture;
        this.phalangesAreImportant = sign.phalangesAreImportant;
        this.letterFrequency = sign.letterFrequency;
        this.fingersDirections = sign.fingersDirections;
        this.distalPhalangesDirections = sign.distalPhalangesDirections;
        this.proximalPhalangesDirections = sign.proximalPhalangesDirections;
        this.handTofingertipsDistances = sign.handTofingertipsDistances;
        this.anglesBetweenAdjacentFingers = sign.anglesBetweenAdjacentFingers;
        this.grabAngle = sign.grabAngle;
        this.pinchDistance = sign.pinchDistance;
    }

    public Sign(Hand hand) {
        int count = hand.fingers().count();
        Vector d = hand.direction();
        Vector n = hand.palmNormal();
        Vector r = d.cross(n).normalized();
//        this.grabAngle = hand.grabAngle();
//        this.pinchDistance = hand.pinchDistance();
        for (int i = 0; i < count; i++) {
            Finger finger = hand.finger(i);
            this.fingersDirections[i] = changeBasis(d, r, n, finger.direction());
            this.distalPhalangesDirections[i] = changeBasis(d, r, n, finger.bone(Bone.Type.TYPE_DISTAL).direction());
            this.proximalPhalangesDirections[i] = changeBasis(d, r, n, finger.bone(Bone.Type.TYPE_PROXIMAL).direction());
            this.handTofingertipsDistances[i] = finger.tipPosition().minus(hand.palmPosition()).magnitude() / finger.length();
        }
       /* for (int i = 0; i < 4; i++) {
            Vector current = proximalPhalangesDirections[i];
            Vector next = proximalPhalangesDirections[i + 1];
            this.anglesBetweenAdjacentFingers[i] = current.angleTo(next);
        }*/
        fillFeaturesArray();
    }

    public static Vector changeBasis(Vector d, Vector r, Vector n, Vector v) {
        float[] c = {d.getX(), d.getY(), d.getZ(), r.getX(), r.getY(), r.getZ(), n.getX(), n.getY(), n.getZ()};
        // h = 1/det
        float h = 1 / (c[0] * (c[4] * c[8] - c[5] * c[7]) + c[1] * (c[5] * c[6] - c[3] * c[8]) + c[2] * (c[3] * c[7] - c[4] * c[6]));
        float[] inverseOfBasis = {

                h * (c[4] * c[8] - c[5] * c[7]), h * (c[2] * c[7] - c[1] * c[8]), h * (c[1] * c[5] - c[2] * c[4]),

                h * (c[5] * c[6] - c[3] * c[8]), h * (c[0] * c[8] - c[2] * c[6]), h * (c[2] * c[3] - c[0] * c[5]),

                h * (c[3] * c[7] - c[4] * c[6]), h * (c[1] * c[6] - c[0] * c[7]), h * (c[0] * c[4] - c[1] * c[3])
        };
        Vector column1 = new Vector(inverseOfBasis[0], inverseOfBasis[1], inverseOfBasis[2]);
        Vector column2 = new Vector(inverseOfBasis[3], inverseOfBasis[4], inverseOfBasis[5]);
        Vector column3 = new Vector(inverseOfBasis[6], inverseOfBasis[7], inverseOfBasis[8]);
        //column1*x + column2*y +column3*z
        return column1.times(v.getX()).plus(column2.times(v.getY())).plus(column3.times(v.getZ()));
    }

    public String toCSV() {
        System.out.println("here");
        String result = "";
        for (int i = 0; i < this.signFeatures.length - 1; i++) {
            result += signFeatures[i] + ",";
        }
        result += this.signFeatures[this.signFeatures.length - 1];
        return result;
    }

    public static String vectorAnglestoCSV(Vector vector) {
        String st = (int) Math.toDegrees(vector.pitch()) + "," + (int) Math.toDegrees(vector.yaw()) + "," + (int) Math.toDegrees(vector.roll());
        return st;
    }

    public static String vectorCoordinatesToCSV(Vector vector) {
        String st = vector.getX() + "," + vector.getY() + "," + vector.getZ();
        return st;
    }

    public static int[] vectorAnglesToArray(Vector vector) {
        int[] angles = {(int) Math.toDegrees(vector.pitch()), (int) Math.toDegrees(vector.yaw()), (int) Math.toDegrees(vector.roll())};
        return angles;
    }


    public static int[] subtractAngles(Vector v1, Vector v2) {
        int[] angles1 = vectorAnglesToArray(v1);
        int[] angles2 = vectorAnglesToArray(v2);
        int[] anglesDif = {angles1[0] - angles2[0], angles1[1] - angles2[1], angles1[2] - angles2[2]};
        return anglesDif;
    }

    public void fillFeaturesArray() {
        for (int i = 0; i < 15; i += 3) {
            System.out.println("i = " + i + " this.fingersDirections[" + i + "].getX() " + this.fingersDirections[i].getX() + "\n " +
                    "signFeatures[" + i + "] = " + signFeatures[i]);
            this.signFeatures[i] = this.fingersDirections[i].getX();
            this.signFeatures[i + 1] = this.fingersDirections[i].getY();
            this.signFeatures[i + 2] = this.fingersDirections[i].getZ();
        }
        for (int i = 15; i < 30; i += 3) {
            this.signFeatures[i] = this.distalPhalangesDirections[i].getX();
            this.signFeatures[i + 1] = this.distalPhalangesDirections[i].getY();
            this.signFeatures[i + 2] = this.distalPhalangesDirections[i].getZ();
        }
        for (int i = 30; i < 45; i++) {
            this.signFeatures[i] = this.proximalPhalangesDirections[i].getX();
            this.signFeatures[i + 1] = this.proximalPhalangesDirections[i].getY();
            this.signFeatures[i + 2] = this.proximalPhalangesDirections[i].getZ();
        }
        for (int i = 45; i < 50; i++) {
            this.signFeatures[i] = this.handTofingertipsDistances[i];
        }
        // this.signFeatures[50] = this.grabAngle;
        //  this.signFeatures[51] = this.pinchDistance;
    }
    /*
    *  boolean staticGesture;
    boolean phalangesAreImportant;
    float letterFrequency = 0;
    Vector[] fingersDirections = new Vector[5];
    Vector[] distalPhalangesDirections = new Vector[5];
    Vector[] proximalPhalangesDirections = new Vector[5];
    Vector armDirection = new Vector(); //The normalized direction in which the arm is pointing (from elbow to wrist)
    float[] handTofingertipsDistances = new float[5]; // distance from the center of the hand to each fingertip normalized to each finger length
   float[] anglesBetweenAdjacentFingers = new float[4]; // a[0]=angle between pinky and ring, a[1] = angle between ring and middle etc
    float grabAngle;

    float pinchDistance; // the shortest distance between the last 2 phalanges of the thumb and those of the index finger
  */
    /*public float compareSign(Sign sign){

    }*/
}