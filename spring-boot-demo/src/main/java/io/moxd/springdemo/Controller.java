/** Controller.java Spring-Boot Webserver
*   In der Klasse Controller.java werden unter Spring-Boot die verschiedenen Request-Mappings konfiguriert
*   und Methoden für die Weiterverarbeitung implementiert
*
*   @author David Juraske
*   @version final 11.04.17
*/

package io.moxd.springdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.time.*;
import java.lang.Math;
import java.lang.String.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class Controller {

    //Log4J Logger Objekt erstellen
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /** Standardmapping GET: 192.168.2.119:8080 Gibt alle implementierten Funktionen
    *   mit dazugehörigem Mapping zurück
    */
    @RequestMapping(method = RequestMethod.GET, path = "/")
    public String getRandomQuote() {
        return "-Signature Beacon Web-Authentification- \nMethods:  \n192.168.2.119:8080/getAuth -> returns Authentificationcode";
    }


    // getAuth Post-Request --> SignatureBeacon Mac Berechnung und Gegenauthentifizierung
    // Return(UUID, MAC, Time, MATCH/MISSMATCH )
    @RequestMapping(value = "/getAuth", method=RequestMethod.POST)
    @ResponseBody
    public String getAuthentification(@RequestParam(value = "payload") String payload){
      ArrayList<String> fileInput = new ArrayList<String>();
      String[] fileArray = new String[47];
      String uuid;
      String mac;
      String time;
      String key = "SecBeacon";

      // Aufteilung der Beacon Komponenten für die Berechnung
      fileInput.add(payload);
      fileArray = fileInput.get(fileInput.size() - 1).split("  ");

      uuid = fileArray[0];
      mac = fileArray[1];
      time = fileArray[2];

      //MAC auf Server berechnen
      String calcMac=getMac(uuid+changeTimeZoneUtc(getRoundTime(time)), key, "HmacMD5");

      System.out.println("---------------------------------------------------------");
      System.out.println("UUID    = "+uuid+" TIME = "+time);
      System.out.println("MAC     = "+mac);
      System.out.println("CALCMAC = "+calcMac);
      System.out.println("SERVERTIME   = "+changeTimeZoneUtc(getRoundTime(time)));

      //Vergleich der Mac's un Rückgabe des Ergebnisses
      if(mac.equals(calcMac)){
        System.out.println("...Verified...");
        logger.info("Verified: "+payload);
        return ""+uuid+"  "+mac+"  "+time+" verified!";
      }
      else{
        System.out.println("...Missmatch...");
        logger.error("Missmatch:"+payload);
        return ""+uuid+"  "+mac+"  "+time+" missmatch!";}


    }


    /** getMac(Message, Key, Hash-Algorithmus): Berechnet für die Übergeben Daten
    *   den Message Authentification Code
    *   Return MAC(Server)
    */
    public static String getMac (String message, String secKey, String algo) {

      String output=null;

        try {
          SecretKeySpec key = new SecretKeySpec((secKey).getBytes("UTF-8"), algo);
          Mac mac = Mac.getInstance(algo);
          mac.init(key);

          byte[] bytes = mac.doFinal(message.getBytes("ASCII"));

          StringBuffer hash = new StringBuffer();
          for(int i = 0;i<bytes.length;i++){
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if(hex.length()==1){
              hash.append('0');}

              hash.append(hex);}

            output = hash.toString();
          }

          catch (UnsupportedEncodingException e){e.printStackTrace();}
          catch (InvalidKeyException e){e.printStackTrace();}
          catch (NoSuchAlgorithmException e){e.printStackTrace();}

          return addSpace(output); //MAC in StandartisierteForm bringen
      }


      /** getRoundTime(String Date): Berechnet für die übergebene Zeit, an denen die BLE Daten auf dem Client empfangen wurden,
      *   das zugehörige Zeitintrevall
      *   Je nach Betriebsystem oder der Zeitzoneneinstellung des Webservers unterscheiden diese sich zu den Zeitzone auf dem Client und SenderBeacon (UTC)
      *   -->Falls diese sich unterscheiden wird changeTimeZoneUtc aufgerufen um die Zeitzone zu ändern
      */
      public static String getRoundTime(String roundDate){

        TimeZone tZ = TimeZone.getTimeZone("UTC");
        Date sysDate = new Date(roundDate);
        Calendar calendar = Calendar.getInstance(tZ);
        calendar.setTime(sysDate);

        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int time = ((15 - unroundedMinutes % 15)+unroundedMinutes); //auf nächste 1/4h runden
        int actHour = calendar.get(calendar.HOUR)-2;              //Differenz von Windows zu linux beachten

        if(time==60){
          int unroundedHour = calendar.get(Calendar.HOUR)+1;
          calendar.set(calendar.MINUTE, 0);
          calendar.set(calendar.HOUR, unroundedHour);}

          else{calendar.set(calendar.MINUTE, time);}

          calendar.set(calendar.HOUR, actHour);
          calendar.set(Calendar.SECOND, 0);
          calendar.set(Calendar.MILLISECOND, 0);

          String dateString = calendar.getTime().toString();

          return dateString;
        }

        /** Bringt die MAC in die Adertrining Package Form (Zwischen jedem Byte ein Leerzeichen)
        *   Return Strng
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

        /** changeTimeZoneUtc(Sting Date): Bring das Date Obejekt von n-Format in UTC Format
        *   Impelemntiert: CET / CEST / EET
        */
        public static String changeTimeZoneUtc(String timestmp){

          if(timestmp.contains("CET"))
            {
              int start = timestmp.indexOf("CET");

              while(start != -1){
                timestmp = timestmp.substring(0,20) + "UTC"+ timestmp.substring(23, timestmp.length());
                start = timestmp.indexOf("CET", start + 3);
              }
            }

          if(timestmp.contains("CEST"))
              {
              int start = timestmp.indexOf("CEST");

              while(start != -1){
                timestmp = timestmp.substring(0,20) + "UTC"+ timestmp.substring(24, timestmp.length());
                start = timestmp.indexOf("CEST", start + 4);
              }
            }

          if(timestmp.contains("EET"))
              {
                int start = timestmp.indexOf("EET");

                while(start != -1){
                  timestmp = timestmp.substring(0,20) + "UTC"+ timestmp.substring(23, timestmp.length());
                  start = timestmp.indexOf("EET", start + 3);
                }
              }


        return timestmp;
        }

}
