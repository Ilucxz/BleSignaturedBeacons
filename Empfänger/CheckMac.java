/** CheckMac: Provisorische Klasse zum überprüfen der Mac-Verifizierung
*
*   Diese Klasse liest die Datei "AdvertisingScanData.txt" aus, berechnet aus den eingelesenen Daten das Zeitintervall und anschließend die MAC.
*   Stimmt die berechnete Mac mit der übertragenden Mac überein, ist die Verifizierung gewährleistet.
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

class CheckMac {

  /**
  * Die Main-Methode liest die Datei aus (Mittels BufferedReader), speichert die Komponenten in einem Array um mit diesen anschließend
  * die Mac zu berechnen und zu vergleichen
  * Dieser Vorgang wird füe jede Zeile in der Datei wiederholt und in der Konsole ausgegeben.
  */
  public static void main (String args[]) throws IOException {

     String uuid="";
     String mac="";
     String time="";
     String key="SecBeacon";

     ArrayList<String> fileInput = new ArrayList<String>();
     String[] fileArray = new String[47];

     try {

       FileReader fr = new FileReader("AdvertisingScanData.txt");
       BufferedReader br = new BufferedReader(fr);
       String line;

       line = br.readLine();
       while((line = br.readLine()) != null){

         fileInput.add(line);
         fileArray = fileInput.get(fileInput.size() - 1).split("  ");

         uuid = fileArray[0];
         mac = fileArray[1];
         time = fileArray[2];

         String calcMac = getMac(uuid+getRoundTime(time), key, "HmacMD5");

         if(mac.equals(getMac(uuid+getRoundTime(time), key, "HmacMD5"))){
           System.out.println(uuid+"  "+mac+"  "+time+"  verified!");}
         else{System.out.println(uuid+"  "+mac+"  "+time+"  missmatch!");}


        }
      }

      catch(Exception e){e.printStackTrace();}

    }



    /**
    * Die in der Datei/Db gespeicherte Zeit ist die Empfangszeit des Packages.
    * Da die Mac immer mit dem gerundeten Zeitinterval berechnet wird muss dieses nachträglich
    * berechnet werden.
    * @return dateSting
    */
    public static String getRoundTime(String roundDate){

      Date sysDate = new Date(roundDate);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(sysDate);

      int unroundedMinutes = calendar.get(Calendar.MINUTE);
      int time = ((15 - unroundedMinutes % 15)+unroundedMinutes); //auf nächste 1/4h runden

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
    * Berechnet die Mac für die übergebene UUID+Time, den Schlüseel und des Verschlüsselungsverfahren
    * @param messgae = uuid+Time
    * @param secKey = String geheimer Schlüssel (nur Sender und Verifizierungskomponete bekannt) evt String zu unsicher(KeyValue?!)
    * @param algo = String des Verschlüsselungsverfahren
    * @throws UnsupportedEncodingException
    * @throws InvalidKeyException
    * @throws NoSuchAlgorithmException
    * @return output = berechnete Mac
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
