package com.example.ex07_09;

/**
 * Created by RENESYS on 2016-05-08.
 */


public class SensorData {
    final static int VECTOR_SIZE = 2;

    double[] currrentVector = {0, 0, 0, 0, 0, 0};
    double[][] standardVector = {
            {0, 6, 7, 0, 0, 0},
            //{0, 6, 7, 400, 0, 0},
            {5, 0, 6, 0, 0, 0},
            //{5, 0, 6, 1000, 0, 0}
    };

    public SensorData(){

    }


    public void setAccX(int accX) {
        this.currrentVector[0] = (double)accX;
    }

    public void setAccY(int accY) {
        this.currrentVector[1] = (double)accY;
    }

    public void setAccZ(int accZ) {
        this.currrentVector[2] = (double)accZ;
    }

    public void setGyroX(int gyroX) {
        this.currrentVector[3] = (double)gyroX;
    }

    public void setGyroY(int gyroY) {
        this.currrentVector[4] = (double)gyroY;
    }

    public void setGyroZ(int gyroZ) {
        this.currrentVector[5] = (double)gyroZ;
    }

    private int detectAccX(){
        int stat = 0;
        if(currrentVector[0] < -15 || currrentVector[0] > 15){
            stat = 1;
        }
        if(currrentVector[0] > 8000 || currrentVector[0] < -8000){
            stat = 3;
        }
        return stat;
    }

    private int detectAccY(){
        int stat = 0;
        if(currrentVector[1] < -15 || currrentVector[1] > 15){
            stat = 1;
        }
        if(currrentVector[1] > 5000 || currrentVector[1] < -5000){
            stat = 3;
        }
        return stat;
    }

    private int detectAccZ(){
        int stat = 0;
        if(currrentVector[2] < -15 || currrentVector[2] > 15){
            stat = 1;
        }
        if(currrentVector[2] > 3500 || currrentVector[2] < -3500){
            stat = 3;
        }
        return stat;
    }

    private int detectGyroX(){
        return currrentVector[3] >= -700  && currrentVector[3] <= 700 ? 0 : 1;
    }

    private int detectGyroY(){
        return currrentVector[4] >= -800 && currrentVector[4] <= 800 ? 0 : 1;
    }

    private int detectGyroZ(){
        return currrentVector[5] >= -1000 && currrentVector[5] <= 1000 ? 0 : 1;
    }

    public double cosineSim(double[] stdVector){
        double res = 0;
        double qi = 0, q = 0, i = 0;

        for(int it = 0; it < 6; it++){
        	System.out.println("+++" + currrentVector[it] + "____" +  stdVector[it]);
            qi += (currrentVector[it] * stdVector[it])+1;
            q += currrentVector[it] * currrentVector[it];
            i += stdVector[it] * stdVector[it];
            System.out.println("+++++++" + qi + "____" + q + "_____" + i);
        }
        System.out.println("q"+q+"//qi "+qi+"//i"+i);
        res = qi / (Math.sqrt(q) * Math.sqrt(i));
        
        return res;
    }

    public boolean checkEmergency(){
        int status = detectAccX() + detectAccY() + detectAccZ()
                + detectGyroX() +detectGyroY() +detectGyroZ();
        return status >= 4;
    }


    public boolean checkEmergency2(){
        boolean check = false;
        for(int i = 0; i < VECTOR_SIZE; i++) {
            double d = cosineSim(standardVector[i]);
            System.out.println(d);
            if (d < 3.0e-4){
                check = true;
                break;
            }
        }
        System.out.println("-------------------");
        return check;
    }

    public String getAllData(){
        String str = "";
        for(int i = 0; i < 5; i++){
            str += currrentVector[i] + " ";
        }
        str += (currrentVector[5] + "\n");
        return str;
    }


}