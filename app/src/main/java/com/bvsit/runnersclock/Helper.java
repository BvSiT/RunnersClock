package com.bvsit.runnersclock;

import java.util.ArrayList;
import java.util.List;

class Helper {
    static long[] vibrationPattern(long val,int msDefaultVib,int msDefaultPause,boolean bIsMinutes) {
        /* Translate number of minutes into a pattern of vibrations */
        boolean addEndPause;
        long[] fullPattern;
        int hours=0;
        ArrayList<Integer> listPattern = new ArrayList<>();
        listPattern.add(0); //first element of a vibration pattern represents a delay

        //if val represents minutes start vibration pattern with hours
        if (bIsMinutes){
            hours= (int) val/60;
            val%=60;
            //System.out.println("val= "+val); //debug
            //System.out.println("hours= "+hours); //debug
        }

        int posDigit=0;
        while(val>0){
            byte digit = (byte) (val % 10);
            val/=10; //discards most right digit
            //System.out.println("val= "+val); //debug
            addEndPause=(posDigit>0); //don't add last pause for pattern at the end
            int digitPattern[]=createVibPattern(digit, posDigit,msDefaultVib,msDefaultPause, addEndPause);
            //System.out.println(24+": "+Arrays.toString(digitPattern)); //debug
            List<Integer> intList = new ArrayList<>();
            for (int i : digitPattern) {intList.add(i); }
            listPattern.addAll(1,intList); //add pattern at the beginning but after the leading element (=0,represents delay)
            posDigit++;
        }

        if (bIsMinutes && hours>0){
            //create vibration pattern for hours. For hours multiply the vibration duration for minutes  by 4
            int[] pattern=createVibPattern(hours,msDefaultVib*4,msDefaultPause*4, true);
            List<Integer> intList = new ArrayList<>();
            for (int i=0;i<pattern.length;i++){
                if (i==pattern.length-1){
                    intList.add(pattern[i]+(msDefaultVib*4)+(msDefaultPause*4));//add extra long pause at end vibrations hours
                }
                else {
                    intList.add(pattern[i]);
                }
            }
            listPattern.addAll(1,intList); //add pattern at the beginning but after the leading element (=0,represents delay)
        }

        fullPattern = new long[listPattern.size()];
        for (int i = 0 ; i<listPattern.size();i++){
            fullPattern[i]=listPattern.get(i);
        }
        return fullPattern;
    }

    private static int[] createVibPattern(byte digitValue, int digitPosition,int msDefaultVib,int msDefaultPause,boolean addEndPause) {
        /* Translate a number into a pattern of vibrations. For every next digit to the left double the duration  */
        int[] pattern;
        if (digitValue>0){
            //digitPosition starts with 0 for most right digit in a number which expresses a value <10
            msDefaultVib<<= digitPosition; //each bit shift left doubles the value
            msDefaultPause<<=digitPosition;
            pattern=new int[addEndPause?digitValue*2:(digitValue*2)-1];
            for (int i=0;i<pattern.length;i++){
                if (i % 2 ==0){
                    pattern[i]=msDefaultVib; //every even element represents a vibration
                }
                else { //uneven element represents a pause
                    // element [9] is the 5th uneven element, represents the pause after the 5th vibration
                    if (i==9){  //extra long pause between 5th and next vibrations
                        pattern[i]=msDefaultPause+msDefaultVib+msDefaultPause;
                    }
                    else {
                        pattern[i]=msDefaultPause;
                    }
                }
            }
            return pattern;
        }
        return new int[]{};
    }

    private static int[] createVibPattern(int value,int msDefaultVib,int msDefaultPause,boolean addEndPause) {
        /* Translate a number into a pattern of the same number of sets of a vibration and a pause */
        int[] pattern;
        if (value>0){
            pattern=new int[addEndPause?value*2:(value*2)-1];
            for (int i=0;i<pattern.length;i++){
                if (i % 2 ==0){
                    pattern[i]=msDefaultVib; //every even element represents a vibration
                }
                else { //uneven element represents a pause
                    pattern[i]=msDefaultPause;
                }
            }
            return pattern;
        }
        return new int[]{};
    }
}