/** SecBeacon: Sender/Empfänger Beacon Klasse
*
*   Über diese Klasse werden die SecBeacons(gekürzter UUID und Mac) angelegt und gesteuert.
*   Unterschied von Sender und Empfänger BlueBeacon.java -> veränderter Konstuktor für Klasse.
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


//SecureBeacon

class SecBeacon
{
        private String[] advdata = new String[47];
        public String advertising=null;
        public String uuid="";
        public String key="SecBeacon";
        public String mac=null;
        public String tx;
        public String rssi;
        public String major="";
        public String minor="";
        public String adv="";
        public String err="12";


        Runtime r = Runtime.getRuntime();



  public static void main(String[] args) {
    try{}

    catch(Exception e){e.printStackTrace();}
  }




  /**
  * Konstruktor SecBeacon
  * Flags werden festgelegt und nicht übergeben
  * @param builder uuid wird übergeben
  */
  public SecBeacon (String builder) {

    this.advertising = "0x08 0x0008 1E 02 01 1A 1A FF 4C 8B 02 15 "+ builder;

    this.rssi = " C8";  //Baustelle -> Byte to String
    this.tx = " 00";
    this.uuid = builder;

  }



  /**
  * Konsolenausgabe des aktuellen SecBeacons
  * Ausgabe auch ohne berechnete Mac möglich
  * Ausgabe der aktuellen Zeit wo gesendet wird
  * Ausgabe der berechneten Zeitinterval Rundung
  */
  public void printBeacon() {

    System.out.println("\n"+this.getClass().getName());
    System.out.println("\nUUID:           "+this.uuid);
    if(this.mac != null) {
      System.out.println("MAC:           "+this.mac);
    }
    else {
      System.out.println("MAC:            -not set-");
    }
    System.out.println("Timestamp:      "+this.getTime());
    System.out.println("Realtime:       "+this.getRealTime()+"\n");
  }



  /**
  * Sendet die für ein SecBeacon Objekt festgelegten Parameter
  * Berechnet zunächste die Mac (uuid+time, key, Verschlüsselungsverfahren), daraus entsteht das advertising
  * Übergabe und Ausführung via exec
  */
  public void sendAdv() throws Exception {

    this.mac = getMac(this.uuid+this.getTime(), this.key, "HmacMD5");
    this.advertising = advertising+ " "+this.mac+this.rssi+this.tx;

    //System.out.println(this.advertising);
    try {
      r.exec("sudo hciconfig hci0 noleadv");
      r.exec("sudo hciconfig hci0 leadv");
      r.exec("sudo hcitool -i hci0 cmd "+ advertising);
    }

    catch(Exception e){e.printStackTrace();}

  }


  /**
  * Berechnet die Mac für die übergebene UUID+Time, den Schlüseel und des Verschlüsselungsverfahren
  * @param messgae = uuid+Time
  * @param secKey = String geheimer Schlüssel (nur Sender und Verifizierungskomponete bekannt) evt String zu unsicher(KeyValue?!)
  * @param algo = String des Verschlüsselungsverfahren
  * @throws UnsupportedEncodingException
  * @throws InvalidKeyException
  * @throws NoSuchAlgorithmException
  * @return output = berechnete Mac
  */
  public static String getMac (String message, String secKey, String algo){
    String output=null;
    try {

      SecretKeySpec key = new SecretKeySpec((secKey).getBytes("UTF-8"), algo);
      Mac mac = Mac.getInstance(algo);
      mac.init(key);

      byte[] bytes = mac.doFinal(message.getBytes("ASCII"));
      StringBuffer hash = new StringBuffer();

      for(int i = 0;i<bytes.length;i++) {
        String hex = Integer.toHexString(0xFF & bytes[i]);
        if(hex.length()==1){
          hash.append('0');
        }
        hash.append(hex);
      }

    output = addSpace(hash.toString());

    }

    catch (UnsupportedEncodingException e){e.printStackTrace();}
    catch (InvalidKeyException e){e.printStackTrace();}
    catch (NoSuchAlgorithmException e){e.printStackTrace();}

	  return output;
    //return output;

  }


  /**
  * Liest die aktuelle Systemzeit aus und rundet diese auf das nächste Zeitinterval (hier 1/4h) auf
  * @return dateSting
  */
  public static String getTime() {

    Date curDate = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(curDate);

    int unroundedMinutes = calendar.get(Calendar.MINUTE);
    int time = ((15-unroundedMinutes%15)+unroundedMinutes); //auf nächste 1/4h runden

    if(time==60){
      int unroundedHour = calendar.get(Calendar.HOUR)+1;
      calendar.set(calendar.MINUTE, 0);
      calendar.set(calendar.HOUR, unroundedHour);
    }

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
    public static String getRealTime() {

      Date sysDate = new Date();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(sysDate);

      String dateString = calendar.getTime().toString();

      return dateString;
    }


    /**
    * getAdvertising(): Scannt nach LE Beacon Data und erstellt ein neues SecBeacon Objekt
    * Aufruf des hcitool/hcidump über exec-Befehl
    * Hauptbestandteil in BleScanner Klasse
    * Liest die Konsolenausgabe des hcidump aus und splittet diese in die einzelnen Komponenten auf
    * Dient nur noch zu Testzwekcen in dieser Klasse
    */

    public void getAdvertising() throws Exception{

      this.uuid="";
      this.major="";
      this.minor="";
      this.mac="";

      ArrayList<String> advinput = new ArrayList<String>();

      try{      //sudo hcitool lescan --duplicates
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
       for(i=2;i<advinput.size();i+=3)
       {this.advertising = advinput.get(i)+advinput.get(i+1)+advinput.get(i+2);}

       //Split Space and ">" -> Zuweisung AD-Bystes üebr ArrayString
       advdata = this.advertising.split("\\s+");

       for(i=24;i<28;i++) //UUID 24.-28. Byte
       {this.uuid=this.uuid+advdata[i];}

       for(i=28;i<44;i++)//MAC
       {this.mac=this.mac+advdata[i];}

       //Abspeichern der gescannten Daten
       bw.append(this.uuid+" "+this.mac+" "+getRealTime()+"\n");
       bw.close();


     }

     catch(Exception e)
     {
       e.printStackTrace();
       this.err="RuntimeException getAdvertising()";
     }
   }

    /**
    * Testklasse zum Vergleichen der Mac's. Nur auf dem Empfänger Beacon relevant
    * Verifizierung erfolg im nächsten Schritt über externe Komponente
    * Berechnet aus den empfangen Daten die MAC und vergöleicht diese miteinander. Liefer Boolean Wert zurück.
    * @return boolean
    */
    public boolean verifyMac(){

      System.out.println(this.uuid+" "+this.getTime()+" "+this.key);

      if(this.mac==getMac(this.uuid+this.getTime(), this.key, "HmacMD5"))
      {
        System.out.println(this.mac);
        System.out.println(getMac(this.uuid+this.getTime(), this.key, "HmacMD5"));
        System.out.println("Authentifizierung erfolgreich!");
        return true;}

      else {
        System.out.println(this.mac);
        System.out.println(getMac(this.uuid+this.getTime(), this.key, "HmacMD5"));
        System.out.println("Authentifizierung fehlgeschlagen!");
        return false;}
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
        if(i % 2 == 0)sb.append(" ").append(inputValueArray[i]);
        else sb.append(inputValueArray[i]);
      }

      return sb.toString().toUpperCase();
    }
}
