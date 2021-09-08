/** BlueBeacon: Sender/Empfänger Beacon Klasse
*
*   Über diese Klasse werden die BlueBeacons(iBeacon Standard) angelegt und gesteuert.
*   Unterschied von Sender und Empfänger BlueBeacon.java -> veränderter Konstuktor für Klasse.
*
*   @author David Juraske
*   @version final Build 17.11.16
*/

import java.io.*;
import java.lang.String.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

//iBeacon Standard

class BlueBeacon
{

      private String[] advdata = new String[47];
      public String advertising="";
      public String uuid="";
      public String key="";
      public String mac="";
      public String tx;
      public Byte rssi;
      public String distance="";
      public String major="";
      public String minor="";
      public String adv="";
      public String err="12";

      Runtime r = Runtime.getRuntime();


      public static void main( String args []) {

        try {
          System.out.println("Main");
        }


	     catch(Exception e){e.printStackTrace();}
      }

      /**
      * BlueBeacon Konstuktor Aufruf aus Main.java
      * @param uuid
      * @param major
      * @param minor
      */
      BlueBeacon(String uuid, String major, String minor) {
       this.uuid = uuid;
       this.major = major;
       this.minor = minor;
       this.advertising = "0x08 0x0008 1E 02 01 1A 1A FF 4C 00 02 15 "+ uuid + major + minor;
      }

      BlueBeacon(){}

      /**
      * getAdvertising(): Scannt nach LE Beacon Data und erstellt ein neues BlueBeacon Objekt
      * Aufruf des hcitool/hcidump über exec-Befehl
      * Hauptbestandteil in BleScanner Klasse
      * Liest die Konsolenausgabe des hcidump aus und splittet diese in die einzelnen Komponenten auf
      Dient nur noch zu Testzwekcen in dieser Klasse
      */
      public void getAdvertising() throws Exception {

        ArrayList<String> advinput = new ArrayList<String>();

        try {

          //sudo hcitool lescan --duplicates
	        //sudo hcitool lescan --duplicates & sudo hcidump --raw -x
	        BufferedReader br = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("sudo hcidump --raw -x").getInputStream()));

          String inputLine;

          // i als Begrenzung, da das AD-Package durchgehen gescannt wird --> i<=4 für ein AD-Package
          // steht im Bezug zu der hcidump Konsolenausgabe

          int i=0;
          while ((inputLine=br.readLine()) != null && i<=4) {
            System.out.println(inputLine);
            advinput.add(inputLine);
            i++;
          }

	        br.close();

          //Set Advertising
          for(i=2;i<advinput.size();i+=3) {
            this.advertising = advinput.get(i)+advinput.get(i+1)+advinput.get(i+2);
          }

	        //Split Space and ">" -> Zuweisung AD-Bystes üebr ArrayString
	        advdata = this.advertising.split("\\s+");


	        for(i=24;i<40;i++) {//UUID 24.-40. Byte
            this.uuid=this.uuid+advdata[i];
          }

	        for(i=40;i<42;i++) {//Major 40.-42. Byte
            this.major=this.major+advdata[i];
          }

	        for(i=42;i<44;i++) {//Minor 42.-44. Byte
            this.minor=this.minor+advdata[i];
          }

	          //this.rssi=Byte.parseByte(advdata[45]);   //noch nicht implementiert
	          //this.tx=Byte.parseByte(advdata[46],16);  //noch nciht implementiert

	    }


	    catch(Exception e){
	        e.printStackTrace();
	        this.err="RuntimeException getAdvertising()";
        }
    }

    /**
    * Liest die aktuelle Systemzeit aus und rundet diese auf das nächste Zeitinterval (hier 1/4h) auf
    * Für BlueBeacons unwichtig, Bestandteil des SecBeacon
    * @return dateSting
    */
     public static String getTime() {

       Date sysDate = new Date();
       Calendar calendar = Calendar.getInstance();
       calendar.setTime(sysDate);

       int unroundedMinutes = calendar.get(Calendar.MINUTE);
       int time = ((15 - unroundedMinutes % 15) + unroundedMinutes); //auf nächste 1/4h runden

       if(time==60) { //Bei einer vollen Stunde wird HOUR +1 addiert und MINUTE auf 0 gesetzt
         int unroundedHour = calendar.get(Calendar.HOUR)+1;
         calendar.set(calendar.MINUTE, 0);
         calendar.set(calendar.HOUR, unroundedHour);
       }

       else {
         calendar.set(calendar.MINUTE, time);
       }

       calendar.set(Calendar.SECOND, 0);
       calendar.set(Calendar.MILLISECOND, 0);

       String dateString = calendar.getTime().toString();

       return dateString;
     }


     /**
     * Sendet die für ein BlueBeacon Objekt festgelegten Parameter (Bluebeacon.advertising)
     */
     public void sendAdv() throws Exception {
            // System.out.println(this.advertising);
            try{
              r.exec("sudo hciconfig hci0 noleadv");
              r.exec("sudo hciconfig hci0 leadv");
	            r.exec("sudo hcitool -i hci0 cmd " + this.advertising);}

            catch(Exception e) {
              e.printStackTrace();
            }

     }

     /**
     * Konsolenausgabe des aktuellen BlueBeacons
     */
    public void printBeacon() {

      System.out.println("\n"+this.getClass().getName());
      System.out.println("UUID:           "+this.uuid);
      System.out.println("Minor:         "+this.minor);
      System.out.println("Major:         "+this.major+"\n");

    }

    /**
    * Fügt nach jeden 2ten Byte ein Leerzeichen ein
    * um hciconfig Syntax einzuhalten "AD8F55C3..." ->"AD 8F 55 C3 ..."
    * @param inputValue String, dem Leerzeichen hinzugefügt werden soll
    * @return sb.toString().toUpperCase() gesplitteter Sting mit Großbuchstaben
    */
    public static String addSpace (String inputValue){

      char[] inputValueArray = inputValue.toCharArray();
      StringBuilder sb = new StringBuilder();

      for(int i = 0;i<inputValueArray.length; i++) {
              if(i % 2 == 0) {
                sb.append(" ").append(inputValueArray[i]);
              }

              else {
                sb.append(inputValueArray[i]);
              }
      }

      return sb.toString().toUpperCase();
    }

}
