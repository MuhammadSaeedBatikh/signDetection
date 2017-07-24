import com.leapmotion.leap.*;

import java.io.*;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.Scanner;

public class Main {
    static boolean listen = true;
    static Controller controller = new Controller();
    static MyListener myListener;


    public static void main(String[] args) throws IOException {
        myListener = new MyListener();
        controller.addListener(myListener);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line = "";


        while (true) {
            line = in.readLine();
            if (line.equalsIgnoreCase("q")) {
                break;
            }
            if (!listen) {
                System.out.println("Listening= " + !controller.removeListener(myListener));
                listen = !listen;
                try {
                    myListener.save();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else {
                System.out.println("Listening= " + controller.addListener(myListener));
                listen = !listen;
            }
        }

    }

    //basis are d r n
}

class MyListener extends Listener {
    long frameCounter = 0;
    long prevTime = System.currentTimeMillis();
    int resultsCounter = 0;
    FileWriter log = new FileWriter(new File("distance from each finger to palm log.txt"), true);
    FileWriter trainingSet = new FileWriter(new File("training set.csv"), true);
    Vector[] fingersDirections = new Vector[5];
    Vector[] distalPhalangesDirections = new Vector[5];
    Vector[] proximalPhalangesDirections = new Vector[5];
    float[] handTofingertipsDistances = new float[5]; // distance from the center of the hand to each fingertip normalized to each finger length
    float[] signFeatures = new float[52];
    float grabAngle;
    float pinchDistance;
    // FileWriter computationLog = new FileWriter(new File("distance from each finger to palm statistics.txt"), true);

    /* double[] fingerToDistalArr = new double[100];
     double[] distalToIntermediateArr = new double[100];
     double[] intermediateToProximalArr = new double[100];
     double[] fingerToProximalArr = new double[100];
     double[] fingerToIntermediateArr = new double[100];
     double[] distaltoProximalArr = new double[100];*/

    MyListener() throws IOException {
    }


    @Override
    public void onInit(Controller controller) {
        System.out.println("initialized");
    }

    @Override
    public void onConnect(Controller controller) {
        System.out.println("connected");
    }

    @Override
    public void onDisconnect(Controller controller) {
        System.out.println("disconnected");
    }

    @Override
    public void onFrame(Controller controller) {

        long currTime = System.currentTimeMillis();
        Frame frame = controller.frame();
        if (frameCounter % 100 == 0) {
            if (!controller.frame().hands().isEmpty()) {
                Hand hand = frame.hands().rightmost();
                int count = hand.fingers().count();
                Vector d = hand.direction();
                Vector n = hand.palmNormal();
                Vector r = d.cross(n).normalized();
                //System.out.println(Arrays.toString(fingersDirections));
                //grabAngle = hand.grabAngle();
                System.out.println(hand.fingers().get(2).direction());
                /* for (int i = 0; i < 2; i++) {
                    Finger finger = hand.finger(1);
                    this.fingersDirections[0] = finger.direction();
                    this.distalPhalangesDirections[i] = changeBasis(d, r, n, finger.bone(Bone.Type.TYPE_DISTAL).direction());
                    this.proximalPhalangesDirections[i] = changeBasis(d, r, n, finger.bone(Bone.Type.TYPE_PROXIMAL).direction());
                     this.handTofingertipsDistances[i] = finger.tipPosition().minus(hand.palmPosition()).magnitude() / finger.length();
                }*/
                //  pinchDistance = hand.pinchDistance();
                String st = toCSV();
                System.out.println(st);
                try {
                    trainingSet.write(st + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
             /*  Vector pinkyPosition = hand.fingers().fingerType(Finger.Type.TYPE_PINKY).get(0).tipPosition();
                    Vector ringPosition = hand.fingers().fingerType(Finger.Type.TYPE_RING).get(0).tipPosition();
                    Vector middlePosition = hand.fingers().fingerType(Finger.Type.TYPE_MIDDLE).get(0).tipPosition();
                    Vector indexPosition = hand.fingers().fingerType(Finger.Type.TYPE_INDEX).get(0).tipPosition();
                    Vector thumbPosition = hand.fingers().fingerType(Finger.Type.TYPE_THUMB).get(0).tipPosition();
                    Vector handPosition = hand.palmPosition();
                    float pinkyLength = hand.fingers().fingerType(Finger.Type.TYPE_PINKY).get(0).length();
                    float ringLength = hand.fingers().fingerType(Finger.Type.TYPE_RING).get(0).length();
                    float middleLength = hand.fingers().fingerType(Finger.Type.TYPE_MIDDLE).get(0).length();
                    float indexLength = hand.fingers().fingerType(Finger.Type.TYPE_INDEX).get(0).length();
                    float thumbLength = hand.fingers().fingerType(Finger.Type.TYPE_THUMB).get(0).length();
                    float pinchDistance = hand.pinchDistance();
                    double grabAngle = Math.toDegrees(hand.grabAngle());
                    double[] distancesToHand = {pinkyPosition.distanceTo(handPosition), ringPosition.distanceTo(handPosition),
                            middlePosition.distanceTo(handPosition), indexPosition.distanceTo(handPosition),
                            thumbPosition.distanceTo(handPosition)};
                    try {
                        String description =Arrays.toString(distancesToHand) + "\n"+
                                "pinch distance: "+pinchDistance+"mm       grab angle:  "+grabAngle+" degree   \n\n";
                        System.out.println(description);
                        log.write(description);
                        save();
                        //  computationLog.write("\n\n\n=========\n\n\n new results at " + resultsCounter + "\n\n\n=========\n\n\n");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                resultsCounter = 0;



                    /*
                    tests for each phalange

                    Finger finger = hand.fingers().fingerType(Finger.Type.TYPE_MIDDLE).get(0);
                    Vector fingerDirection = Sign.changeBasis(d, r, n, finger.direction()).opposite();
                    Vector distalPhalangeDirection = Sign.changeBasis(d, r, n, finger.bone(Bone.Type.TYPE_DISTAL).direction());      //tips directions
                    Vector intermediatePhalangeDirection = Sign.changeBasis(d, r, n, finger.bone(Bone.Type.TYPE_INTERMEDIATE).direction());
                    Vector proximalPhalangeDirectios = Sign.changeBasis(d, r, n, finger.bone(Bone.Type.TYPE_PROXIMAL).direction());
                    String descripton = "\n";
                    float fingerToDistal = fingerDirection.distanceTo(distalPhalangeDirection);
                    float distalToIntermediate = distalPhalangeDirection.distanceTo(intermediatePhalangeDirection);
                    float intermediateToProximal = intermediatePhalangeDirection.distanceTo(proximalPhalangeDirectios);
                    float fingerToProximal = fingerDirection.distanceTo(proximalPhalangeDirectios);
                    float fingerToIntermediate = fingerDirection.distanceTo(intermediatePhalangeDirection);
                    float distaltoProximal = distalPhalangeDirection.distanceTo(proximalPhalangeDirectios);
                    if (resultsCounter < 100) {
                        fingerToDistalArr[resultsCounter] = fingerToDistal;
                        fingerToProximalArr[resultsCounter] = fingerToProximal;
                        fingerToIntermediateArr[resultsCounter] = fingerToIntermediate;
                        distalToIntermediateArr[resultsCounter] = distalToIntermediate;
                        intermediateToProximalArr[resultsCounter] = intermediateToProximal;
                        distaltoProximalArr[resultsCounter] = distaltoProximal;

                    } else {
                        try {
                            save();
                            this.fingerToDistalArr = new double[100];
                            this.distalToIntermediateArr = new double[100];
                            this.intermediateToProximalArr = new double[100];
                            this.fingerToProximalArr = new double[100];
                            this.fingerToIntermediateArr = new double[100];
                            this.distaltoProximalArr = new double[100];
                            log.write("\n\n\n=========\n\n\n new results at " + resultsCounter + "\n\n\n=========\n\n\n");
                            computationLog.write("\n\n\n=========\n\n\n new results at " + resultsCounter + "\n\n\n=========\n\n\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        resultsCounter = 0;
                    }
                    descripton +=
                            "-finger direction        " + fingerDirection + "\n" +
                                    "distal direction         " + distalPhalangeDirection + "\n" +
                                    "intermediate direction   " + intermediatePhalangeDirection + "\n" +
                                    "proximal direction       " + proximalPhalangeDirectios + "\n" +
                                    "-finger to distal        " + fingerDirection.distanceTo(distalPhalangeDirection) + "\n" +
                                    "distal to intermediate   " + distalPhalangeDirection.distanceTo(intermediatePhalangeDirection) + "\n" +
                                    "intermediate to proximal " + intermediatePhalangeDirection.distanceTo(proximalPhalangeDirectios) + "\n" +
                                    "-finger to proximal      " + fingerDirection.distanceTo(proximalPhalangeDirectios) + "\n" +
                                    "distal to Proximal       " +distaltoProximal+"\n"+
                                    "-finger to intermediate  " + fingerDirection.distanceTo(intermediatePhalangeDirection) + "\n\n";
                    System.out.println("\n");
                    System.out.println(descripton);
                    try {
                        log.write(descripton.toCharArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                System.out.println("============ \n\n");
                prevTime = System.currentTimeMillis();
                resultsCounter++;
            }
        }

    }


    public void save() throws IOException {
        log.close();
        log = new FileWriter(new File("distance from each finger to palm log.txt"), true);
        trainingSet.close();
        trainingSet = new FileWriter(new File("training set.csv"));
        /*computationLog.write(Arrays.toString(sum)+"\n");
        computationLog.close();
        computationLog=new FileWriter(new File("distance from each finger to palm statistics.txt"),true);*/

      /*  double distalToIntermediateAverage = Arrays.stream(this.distalToIntermediateArr).average().getAsDouble();
        computationLog.write("distal To Intermediate Average   " + Double.valueOf(distalToIntermediateAverage) + "\n");
        double fingerToDistalAverage = Arrays.stream(this.fingerToDistalArr).average().getAsDouble();
        computationLog.write("finger To Distal Average         " + Double.valueOf(fingerToDistalAverage) + "\n");
        double fingerToIntermediateAverage = Arrays.stream(this.fingerToIntermediateArr).average().getAsDouble();
        computationLog.write("finger To Intermediate Average   " + Double.valueOf(fingerToIntermediateAverage) + "\n");
        double fingerToProximalAverage = Arrays.stream(this.fingerToProximalArr).average().getAsDouble();
        computationLog.write("finger To Proximal Average       " + Double.valueOf(fingerToProximalAverage) + "\n");
        double intermediateToProximalAverage = Arrays.stream(this.intermediateToProximalArr).average().getAsDouble();
        computationLog.write("intermediate To Proximal Average " + Double.valueOf(intermediateToProximalAverage) + "\n");
        double distalToProximalAverage = Arrays.stream(this.distaltoProximalArr).average().getAsDouble();
        computationLog.write("distal To Proximal Average       " + Double.valueOf(distalToProximalAverage) + "\n");*/

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
        this.signFeatures[50] = this.grabAngle;
        this.signFeatures[51] = this.pinchDistance;
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

}