/** Main.Java: Empfänger Beacon
*
*   Über diese Klasse wird der BleScanner ausgeführt, der nach LE Data scannt.
*
*   @author David Juraske
*   @version final Build 17.11.16
*/

class Main
{

      //Scanner BLE
      public static void main (String args[]) {

	      try {

           System.out.println("BLE Scanner");
           System.out.println("Bluetooth-Low-Energy scan starts..");

           while(true) {
             BleScanner bleScan1 = new BleScanner();
             bleScan1.getAdvertising();
             }


	      }


	      catch(Exception e){e.printStackTrace();}

     }

}
