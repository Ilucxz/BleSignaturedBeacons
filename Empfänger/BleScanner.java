/** BleScanner: Bluetooth Low Energy Scanner
*
*   Über diese Klasse wird nach LE Data gescannt. Werden Daten eines Beacons empfangen, werden diese in der Konsole aufgelistet.
*   Sollen Daten eine SecBeacons gescannt werden, werden diese zusätzlich in der Datei "AdvertisingScanData.txt" abgespeichert, wo
*   sie im nächsten Schritt verifiziert werden können.
*
*   @author David Juraske
*   @version final Build 17.11.16
*/

import java.io.*;
import java.lang.Math;
import java.lang.String.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

class BleScanner
{
      private String[] advdata = new String[47];
      private Integer idSec = 0;
      private Integer idApp = 0;


      //Scanner BLE
      public static void main (String args[]){
	    try{}

	    catch(Exception e){e.printStackTrace();}

     }

     /**
     * getAdvertising(): Scannt nach LE Beacon Data und erstellt ein neues SecBeacon Objekt
     * Aufruf des hcitool/hcidump über exec-Befehl
     * Hauptbestandteil in BleScanner Klasse
     * Liest die Konsolenausgabe des hcidump aus und splittet diese in die einzelnen Komponenten auf
     * und erkennt automatisch um welchen Standard es sich handelt.
     * Implementierte Standards: Ibeacon + Sec Beacon
     * Fehlende Standards: AltBeacon, EddystoneBeacon
     * SecBeacon Data wird zusätzlich in "AdvertisingScanData.txt" abgespeichert -> Verifizierung
     */
     public void getAdvertising() throws Exception{

       String uuid="";
       String mac="";
       String major="";
       String minor="";

       ArrayList<SecBeacon>  secBeaconList = new ArrayList<>();
       ArrayList<BlueBeacon> blueBeaconList = new ArrayList<>();

       ArrayList<String> advinput = new ArrayList<String>();

       try {
          //sudo hcitool lescan --duplicates
          //sudo hcitool lescan --duplicates & sudo hcidump --raw -x
          BufferedReader br = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("sudo hcidump --raw -x").getInputStream()));
          BufferedWriter bw = new BufferedWriter(new FileWriter("AdvertisingScanData.txt", true));
          String inputLine;

          //i als Begrenzung, da das AD-Package durchgehen gescannt wird --> i<=4 für ein AD-Package
          int i=0;
          while((inputLine=br.readLine()) != null && i<=4){
            System.out.println(inputLine);
            advinput.add(inputLine);
            i++;}

          br.close();

          //Set Advertising
          String scanAdvertising="";
          for(i=2;i<advinput.size();i+=3)
          {scanAdvertising = advinput.get(i)+advinput.get(i+1)+advinput.get(i+2);}


          //Split Space and ">" -> Zuweisung AD-Bystes üebr ArrayString
          advdata = scanAdvertising.split("\\s+");

          //Herstellerspezifikationen:
          //4C 00 -> Apple iBeacon
          //AA FF -> Eddystone Beacon
          //BE AC -> AltBeacon
          //4C 8B -> SecBeacon


          if(advdata[20].equals("4C") && advdata[21].equals("8B")){
            System.out.println("\nSecBeacon detected... Write to file.");

            for(i=24;i<28;i++) {//UUID 24.-28. Byte
              uuid=uuid+advdata[i];
            }

            for(i=28;i<44;i++) {//MAC
              mac=mac+advdata[i];
            }

            uuid = addSpace(uuid);
            mac = " "+addSpace(mac);

            //Ein Neues SecBeacon Objekt wird angelegt und ausgegeben
            secBeaconList.add(new SecBeacon (uuid, mac, scanAdvertising));
            secBeaconList.get(secBeaconList.size()-1).printBeacon();
            bw.append(uuid+" "+mac+" "+getRealTime()+"\r\n");}
            //Hier wird in die Datei geschrieben


          if(advdata[20].equals("4C") && advdata[21].equals("00")){
            System.out.println("\nIBeacon detected...");

            for(i=24;i<40;i++) {//UUID 24.-40. Byte
              uuid=uuid+advdata[i];
            }

            for(i=40;i<42;i++) {//Major 40.-42. Byte
              major=major+advdata[i];
            }

            for(i=42;i<44;i++) {//Minor 42.-44. Byte
              minor=minor+advdata[i];
            }

            //Ein Neues BlueBeacon Objekt wird angelegt und ausgegeben
            blueBeaconList.add(new BlueBeacon(uuid, major, minor, scanAdvertising));
            blueBeaconList.get(blueBeaconList.size()-1).printBeacon();
          }


          if(advdata[20].equals("AA") && advdata[21].equals("FF")){
            System.out.println("\nEddystone beacon detected...");

            for(i=24;i<40;i++) { //UUID 24.-40. Byte
              uuid=uuid+advdata[i];
            }

            for(i=40;i<42;i++) {//Major 40.-42. Byte
              major=major+advdata[i];
            }

            for(i=42;i<44;i++) {//Minor 42.-44. Byte
              minor=minor+advdata[i];
            }

            //Ein Neues BlueBeacon Objekt wird angelegt und ausgegeben
            blueBeaconList.add(new BlueBeacon(uuid, major, minor, scanAdvertising));
            blueBeaconList.get(blueBeaconList.size()-1).printBeacon();
          }


          if(advdata[20].equals("BE") && advdata[21].equals("AC")){
            System.out.println("\nAltBeacon detected...");

            for(i=24;i<40;i++) {//UUID 24.-40. Byte
              uuid=uuid+advdata[i];
            }

            for(i=40;i<42;i++) {//Major 40.-42. Byte
              major=major+advdata[i];
            }

            for(i=42;i<44;i++) {//Minor 42.-44. Byte
              minor=minor+advdata[i];
            }

            uuid = addSpace(uuid);
            major = addSpace(major);
            minor = addSpace(minor);

            //Ein Neues BlueBeacon Objekt wird angelegt und ausgegeben
            blueBeaconList.add(new BlueBeacon(uuid, major, minor, scanAdvertising));
            blueBeaconList.get(blueBeaconList.size()-1).printBeacon();
          }


          bw.close();


        }

      catch(Exception e) {
        e.printStackTrace();
      }
    }

    /**
    * Liest die aktuelle Systemzeit aus und rundet diese auf das nächste Zeitinterval (hier 1/4h) auf
    * @return dateSting
    */
    public static String getTime(){

      Date sysDate = new Date();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(sysDate);

      int unroundedMinutes = calendar.get(Calendar.MINUTE);
      int time = ((15 - unroundedMinutes % 15) + unroundedMinutes); //auf nächste 1/4h runden

      if(time==60){
        int unroundedHour = calendar.get(Calendar.HOUR)+1;
        calendar.set(calendar.MINUTE, 0);
        calendar.set(calendar.HOUR, unroundedHour);}

      else{calendar.set(calendar.MINUTE, time);}

      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      String dateString = calendar.getTime().toString();

      return dateString;
    }

    /**
    * Funktion die Aktuelle Systemzeit ungerundet zurückgibt.
    * Das Runden für die MAC übernimmt später die Verifizierungskomponete
    */
    public static String getRealTime(){

      Date sysDate = new Date();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(sysDate);

      String dateString = " "+calendar.getTime().toString();

      return dateString;
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

     for(int i = 0;i<inputValueArray.length; i++)
     {
       if(i % 2 == 0 && i != 0)sb.append(" ").append(inputValueArray[i]); // i!=0 weil keine führenes Space erwünscht ist
       else sb.append(inputValueArray[i]);
     }

     return sb.toString().toUpperCase();
   }

}
