/** Main.Java: Sender Beacon
*
*   Über diese Klasse wird der Sender Beacon gesteuert. Es wird festgelegt welcher Beacon Standard angewendet werden soll.
*   BlueBeacon(IBeacon Standard) oder SecBeacon(eigener Standard mit gekürzter UUID und MAC) wurden Implementiert.
*
*   @author David Juraske
*   @version final Build 17.11.16
*/

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.Date;


class Main extends TimerTask {

static int setKeyPressed;


    /**
    * TimerTask Methode überprüft die Systemzeit
    * Bei überschreitung des Zeitintervalls (hier zu jeder vollen 1/4h) wird die Mac neu berechnet und das Advertising Package neu gesendet
    * Gilt nur für SecBeacon Standard
    */
    @Override
    public void run() {

        Calendar cal = Calendar.getInstance();
        Date curDate = new Date();
        cal.setTime(curDate);
        int curMinutes = cal.get(Calendar.MINUTE);

        if(curMinutes % 15 == 0 && setKeyPressed == 2){ //setKeyPressed==2 -> SecBeacon
          try{
          System.out.println("Send SecBeacon data...");
          SecBeacon sec1 = new SecBeacon("E2 0A 39 F4");
          sec1.sendAdv();
          sec1.printBeacon();
          }
          catch(Exception e){e.printStackTrace();}
        }

    }

      /**
      * Main Methode
      * Steuerung des Beacons
      */
      public static void main (String args[]) {

        Scanner keyInput = new Scanner(System.in);
        Runtime r = Runtime.getRuntime();

        TimerTask timerTask = new Main();
        Timer refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(timerTask, 0, 10000); //10000ms=1 minuten -> jede minute Timer timerTask


	try {

      System.out.println("BLE Sender - Broadcast Beacons");

      while(true){
      System.out.println("\n\nSet Beacon to:");
      System.out.println("1. BlueBeacon");
      System.out.println("2. SecBeacon");
      System.out.println("3. Stop broadcasting");
      System.out.println("4. Exit");


      Integer input = keyInput.nextInt();

        switch(input){

              case 1:
                 System.out.println("Send BlueBeacon data...");
  	             BlueBeacon beacon1 = new BlueBeacon("E2 0A 39 F4 FF C8 00 00 FF FF D0 19 21 00 41 70", " 0E 04", " 00 00");
                 beacon1.sendAdv();
                 beacon1.printBeacon();
                 setKeyPressed=1;
                 break;

              case 2:
                 System.out.println("Send SecBeacon data...");
                 SecBeacon sec1 = new SecBeacon("E2 0A 39 F4");
                 sec1.sendAdv();
                 sec1.printBeacon();
                 setKeyPressed=2;
                 break;

              case 3:
                r.exec("sudo hciconfig hci0 noleadv");
                System.out.println("Stopped...");
                setKeyPressed=3;
                break;

              case 4:
                r.exec("sudo hciconfig hci0 noleadv");
                System.exit(0);
                setKeyPressed=4;
                break;

          }
        }
	  }

	   catch(Exception e){
        e.printStackTrace();
	   }
}
}
