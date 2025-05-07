 package parser;
 
 import java.io.PrintStream;
 
 
 
 public class GenerateAckCommand
 {
   public String createAckCommand(String message)
   {
     String ackoutdata = null;
     String data = null;
     String command_Numerator = null;
     String SystemCode = null;
     String messageType = null;
     String UNIT_ID = null;
     String packatecntrlfield = null;
     String lengthofmodulw = null;
     String Authentication_Code_Field = null;
     String Action_code = null;
     String Main_Acknowledge_number = null;
     String notUsingBytes = null;
     String checksum = null;
     String modulename = null;
     String lengthofmodule = null;
     String spare = null;
     String msg = message;
     
     String checksumin = msg;
     
     String msgtype = message.substring(8, 10);
     System.out.println("msgtype::" + msgtype);
     int convtmsgtype = HexToDec(msgtype);
     System.out.println("convtmsgtype::" + convtmsgtype);
     String inttostrmsgtype = Integer.toString(convtmsgtype);
     System.out.println("inttostrmsgtype::" + inttostrmsgtype);
     if (inttostrmsgtype.equalsIgnoreCase("0")) {
       System.out.println("In msg type 0");
       SystemCode = message.substring(0, 8);
       System.out.println("SystemCode::" + SystemCode);
       messageType = "04";
       System.out.println("messageType::" + messageType);
       UNIT_ID = message.substring(10, 18);
       System.out.println("UNIT_ID::" + UNIT_ID);
       
       command_Numerator = "00";
       
       System.out.println("command_Numerator calculated::" + command_Numerator);
       
       if (command_Numerator.length() == 1) {
         command_Numerator = "0" + command_Numerator;
         System.out.println("command_Numerator if::" + command_Numerator);
       }
       Authentication_Code_Field = "00000000";
       System.out.println("Authentication_Code_Field::" + Authentication_Code_Field);
       Action_code = "00";
       System.out.println("Action_code::" + Action_code);
       Main_Acknowledge_number = message.substring(22, 24);
       System.out.println("Message numerator Main_Acknowledge_number::" + Main_Acknowledge_number);
       notUsingBytes = "0000000000000000000000";
       System.out.println("unusednotUsingBytes::" + notUsingBytes);
       data = String.valueOf(messageType) + UNIT_ID + command_Numerator + Authentication_Code_Field + Action_code + Main_Acknowledge_number + notUsingBytes;
       System.out.println("data before checksum::" + data);
       checksum = cal_checksum(data);
       System.out.println("checksum::" + checksum);
       ackoutdata = String.valueOf(SystemCode) + messageType + UNIT_ID + command_Numerator + Authentication_Code_Field + Action_code + Main_Acknowledge_number + notUsingBytes + checksum;
       System.out.println("data after ckecksum ::" + ackoutdata);
     }
     
    return ackoutdata;
   }
   
   public String cal_checksum(String data) {
     System.out.println("data into  ckecksum fucntion::" + data);
     String checksum = null;
     int i_command_Numerator = 0;
     for (int i = 0; i < data.length(); i += 2) {
       i_command_Numerator += Integer.parseInt(data.substring(i, i + 2), 16);
     }
     System.out.println("i_command_Numerator ckecksum fucntion::" + i_command_Numerator);
     checksum = Integer.toHexString(i_command_Numerator);
     System.out.println("checksum ckecksum fucntion::" + checksum);
     checksum = checksum.substring(checksum.length() - 2);
     System.out.println("checksum222::" + checksum);
     return checksum;
   }
   
   public boolean validate_checksum(String data) {
     String checksum = null;
     int i_command_Numerator = 0;
     boolean flag = false;
     String checksum_data = null;
     checksum_data = data.substring(8, data.length() - 2);
     for (int i = 0; i < checksum_data.length(); i += 2) {
       i_command_Numerator += Integer.parseInt(checksum_data.substring(i, i + 2), 16);
     }
     checksum = Integer.toHexString(i_command_Numerator);
     checksum = checksum.substring(checksum.length() - 2);
     if (checksum.equalsIgnoreCase(data.substring(data.length() - 2))) {
       flag = true;
     }
     return flag;
   }
   
   private static int HexToDec(String hex) {
     int decimal = Integer.parseInt(hex, 16);
     return decimal;
   }
 }


