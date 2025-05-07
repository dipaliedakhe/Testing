 package parser;
 
 import java.io.PrintStream;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.spi.SelectorProvider;
 import java.sql.Connection;
 import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 public class PointerDataReceiver
   extends Thread
 {
   public static Connection connnew = null;
   public static Statement stnew; 
            public static Statement stnew1; 
            static Connection conn = null;
   static Connection conn1 = null;
   static Statement st;
   static Statement st1;
   static Statement st2; static Statement st3;
   public PointerDataReceiver(int Port) throws Exception { 
	          this.strTempMsg = "";
     this.tflag = false;
     this.fInsert = 0;
     this.FileName1 = "";
     this.readBuffer = ByteBuffer.allocate(8192);
     this.port = Port;
     try {
       this.selector = initSelector();
     }
     catch (Exception e) {
       e.printStackTrace(); } }
   
   String strTempMsg;
   boolean tflag;
   int fInsert; public String day;
   private static String IntelHex(String str) { String s1 = "";
     for (int i = str.length() - 1; i >= 0; i -= 2) {
       s1 = String.valueOf(s1) + str.charAt(i - 1) + str.charAt(i);
     }
     return s1; }
   
   public String month;
   public String year; public String FileName1;
   private static int HexToDec(String hex) { int decimal = Integer.parseInt(hex, 16);
     return decimal;
   }
   
   private InetAddress hostAddress;
   
   public void run() { 
	          System.out.println("Pointer start for " + this.port);
     newSocketList = new ArrayList();
     socketList = new HashMap();
     try {
       for (;;) {
         this.selector.selectNow();
         Iterator selectedKeys = this.selector.selectedKeys().iterator();
         while (selectedKeys.hasNext()) {
           SelectionKey key = (SelectionKey)selectedKeys.next();
           selectedKeys.remove();
           if (key.isValid()) {
             if (key.isAcceptable()) {
               accept(key);
 
             }
             else if (key.isReadable())
             {
 
               read(key);
             }
           }
         }
         Thread.sleep(1000L);
       }
     }
     catch (Exception e) {
       e.printStackTrace();
     } }
   
   private int port;
   
   private void accept(SelectionKey key) throws Exception { SocketChannel socketChannel = null;
     try {
       ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
       socketChannel = serverSocketChannel.accept();
       socketChannel.configureBlocking(false);
       socketChannel.register(this.selector, 1);
       if (!newSocketList.contains(socketChannel)) {
         newSocketList.add(socketChannel);
       }
     }
     catch (Exception e) {
       e.printStackTrace();
     } }
   
   int portNum;
   
   private Selector initSelector() throws Exception { Selector socketSelector = null;
     try {
       socketSelector = SelectorProvider.provider().openSelector();
       (this.serverChannel = ServerSocketChannel.open()).configureBlocking(false);
       InetSocketAddress isa = new InetSocketAddress("82.180.147.11", this.port);
       this.serverChannel.socket().bind(isa);
       this.hostAddress = InetAddress.getLocalHost();
       System.out.println(this.hostAddress);
       this.serverChannel.register(socketSelector, 16);
     }
     catch (Exception e) {
       e.printStackTrace();
       try {
         this.serverChannel.close();
       }
       catch (Exception ee) {
         ee.printStackTrace();
       }
     }
     return socketSelector; }
   
   private ServerSocketChannel serverChannel;
   
   private void read(SelectionKey key) throws Exception { 
					System.out.println("IN read");
     try {
       SocketChannel socketChannel = (SocketChannel)key.channel();
       this.portNum = socketChannel.socket().getLocalPort();
       this.readBuffer.clear();
       int numRead = 0;
       try {
         numRead = socketChannel.read(this.readBuffer);
       }
       catch (Exception e1) {
         socketChannel.socket().setSoLinger(true, 0);
         socketChannel.close();
         key.cancel();
         newSocketList.remove(socketChannel);
         return;
       }
       if (numRead == -1) {
         try {
           if (newSocketList.contains(socketChannel)) {
             socketChannel.socket().setSoLinger(true, 0);
             socketChannel.close();
             newSocketList.remove(socketChannel);
           }
           key.cancel();
         }
         catch (Exception ee) {
           ee.printStackTrace();
         }
       }
       else if (this.readBuffer != null) {
         this.readBuffer.flip();
         byte[] array = new byte['✐'];
         while (this.readBuffer.hasRemaining()) {
           String stOutput = "";
           String rawOutputData = "";
           String strSimNo = "";
           try {
             int n = this.readBuffer.remaining();
             this.readBuffer.get(array, 0, n);
             for (int i = 0; i < n; i++) {
               String hex = Integer.toHexString(array[i]);
               byte rawDataa = array[i];
               if (hex.length() > 2) {
                 hex = hex.substring(6, hex.length());
               }
               if (hex.length() < 2) {
                 hex = "0" + hex;
               }
               stOutput = String.valueOf(stOutput) + hex.toUpperCase();
               rawOutputData = String.valueOf(rawOutputData) + rawDataa;
             }
           }
           catch (Exception localException1) {}
           stOutput = stOutput.trim();
           String[] splitstr = null;
           String deviceid = stOutput.substring(10, 18);
           String DevId = IntelHex(deviceid);
           int DeviceId = HexToDec(DevId);
           String concat = "";
           
           System.out.println("Packet received: " + stOutput);
           if (stOutput.length() > 1) {
             splitstr = stOutput.split("4D434750");
             for (int j = 1; j < splitstr.length; j++) {
               System.out.println("after received: 4D434750" + splitstr[j]);
               concat = "4D434750" + splitstr[j];
               GenerateAckCommand ack = new GenerateAckCommand();
               
 
               String commandACK = ack.createAckCommand(concat);
               
               System.out.println("Ack gen: " + commandACK);
               System.out.println("commandACK" + commandACK.length());
               savetodatabase(concat, DeviceId);
               byte[] da = getData(commandACK);
               System.out.println("Ack gen b da: " + da.length);
               ByteBuffer buf = ByteBuffer.wrap(da);
               System.out.println("\nbytebuffer capacity: " + buf.capacity());
               System.out.println("\nbytebuffer limit: " + buf.limit());
               socketChannel.write(buf);
               System.out.println("after sent buf::" + buf);
             }
           }
         }
       } } catch (Exception localException2) {} }
   
   private Selector selector;
   private ByteBuffer readBuffer;
   public static ArrayList<SocketChannel> newSocketList;
   public static HashMap<String, SocketChannel> socketList;
   public static byte[] getData(String s) { System.out.println("hex ack : " + s);
     int len = s.length();
     byte[] data = new byte[len / 2];
     for (int i = 0; i < len; i += 2) {
       data[(i / 2)] = ((byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16)));
     }
     System.out.println("hex data : " + data);
     return data;
   }
   
   public static void getConnection() {
     try {
       System.out.println("[CONNECTING TO DATABASE]  ....");
       Class.forName("com.mysql.cj.jdbc.Driver");
       conn = DriverManager.getConnection("jdbc:mysql://localhost/EL_avlalldata", "java", "J@5a<I86");
       System.out.println("[CONNECTED SUCCESSFULLY]*");
       st = conn.createStatement();
       st1 = conn.createStatement();
       st2 = conn.createStatement();
       st3 = conn.createStatement();
     }
     catch (Exception e) {
       System.out.print("Connection Exception--- > " + e);
     }
   }
   
   public static void closeConnection() {
     try {
       if (!conn.isClosed()) {
         st2.close();
         st3.close();
         st.close();
         st1.close();
         conn.close();
       }
     }
     catch (Exception localException) {}
   }
   
   public static String GetTable() {
     Date d = new Date();
     String tb = new SimpleDateFormat("yyyy-MM-dd").format(d);
     tb = tb.replace("-", "_");
     String tablename1 = "t_pointer" + tb;
     try {
       String sql1 = "CREATE TABLE IF NOT EXISTS " + tablename1 + "  (RID  double NOT NULL auto_increment,MailDate date default NULL,MailTime time default NULL, StoredDate  date default NULL,StoredTime  time default NULL,MailFrom varchar(100) default '-',MailTo varchar(100) default '-',Subject varchar(100) default '-',Header longtext,Body  longtext,UnitID  varchar(50) default '-',TransmissionReason  varchar(50) default '-',VehRegNo  varchar(50) default '-',Transporter  varchar(100) default '-',VehID  varchar(50) default '-',Status  varchar(50) default 'Pending',UnProcessedStamp  longtext,ProcessStatus  varchar(50) default 'Pending',Server varchar(50) default '-',primary key (RID))";
       st.executeUpdate(sql1);
     }
     catch (Exception ex) {
       closeConnection();
       getConnection();
       try {
         String sql2 = "CREATE TABLE IF NOT EXISTS " + tablename1 + "  (RID  double NOT NULL auto_increment,MailDate date default NULL,MailTime time default NULL, StoredDate  date default NULL,StoredTime  time default NULL,MailFrom varchar(100) default '-',MailTo varchar(100) default '-',Subject varchar(100) default '-',Header longtext,Body  longtext,UnitID  varchar(50) default '-',TransmissionReason  varchar(50) default '-',VehRegNo  varchar(50) default '-',Transporter  varchar(100) default '-',VehID  varchar(50) default '-',Status  varchar(50) default 'Pending',UnProcessedStamp  longtext,ProcessStatus  varchar(50) default 'Pending',Server varchar(50) default '-',primary key (RID))";
         st3.executeUpdate(sql2);
       }
       catch (Exception localException1) {}
     }
     return tablename1;
   }
   
   public void savetodatabase(String stOutput, int DeviceId) {
     System.out.println("[SAVING INTO DATABASE]*   ");
     String storeddate = "-";
     String storedtime = "-";
     String unitid = "-";
     String MailDate = "";
     String MailTime = "";
     String MailFrom = "Pointer";
     String Server = "S1";
     String unitID = "";
			  String imeiNo="";
     String header = "-";
     String UnProcessedStamp = "-";
     String TransmissionReason = "";
     String UnitId1 = stOutput.substring(10, 18);
     String UnitId2 = IntelHex(UnitId1);
     int DevId = HexToDec(UnitId2);
     //unitID = String.valueOf(DevId);
				imeiNo = String.valueOf(DevId);
     String TransmissionReason2 = stOutput.substring(36, 38);
     System.out.println("TransmissionReason1::   " + TransmissionReason2);
     int TransmissionReason1id = HexToDec(TransmissionReason2);
     TransmissionReason = String.valueOf(TransmissionReason1id);
     System.out.println("TransmissionReason::   " + TransmissionReason);

int i = Integer.parseInt(imeiNo);

String DevIdNew = String.format("%015d", new Object[] { Integer.valueOf(i) });

System.out.println("formated device id  is " + DevIdNew);

try {

String sqlunitid = "select * from db_gps.t_imeidetails where imei='" + DevIdNew + "'";

System.out.println("imei details>>>>>>>>>>>>>>>>>>>>>>" + sqlunitid);
ResultSet rsunitid= st2.executeQuery(sqlunitid);

if (rsunitid.next()) {
	System.out.println("ifffffffffffff************* UnitID:"+unitID);
	unitID = rsunitid.getString("unitid");
} else {
	unitID = imeiNo;
} 


System.out.println("**outside***************** UnitID:"+unitID);

}catch(SQLException s) {
	s.printStackTrace();
	unitID = imeiNo;
}



     try {
       storeddate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
       storedtime = new SimpleDateFormat("HH:mm:ss").format(new Date());
       MailDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
       MailTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
       String tablename = GetTable();
       //String sql1 = "insert into " + tablename + " (MailDate,MailTime,StoredDate,StoredTime,MailFrom,Subject,Body,UnitID,Server,Header,UnProcessedStamp,TransmissionReason) values('" + MailDate + "','" + MailTime + "','" + storeddate + "','" + storedtime + "','" + "Pointer" + "','" + unitID + "','" + stOutput + "','" + unitID + "','" + "S1" + "','" + "-" + "','" + "-" + "','" + TransmissionReason + "')";
       String sql1 = "insert into " + tablename + " (MailDate,MailTime,StoredDate,StoredTime,MailFrom,Subject,Body,UnitID,Server,Header,UnProcessedStamp,TransmissionReason) values('" + MailDate + "','" + MailTime + "','" + storeddate + "','" + storedtime + "','" + "Pointer" + "','" + imeiNo + "','" + stOutput + "','" + unitID + "','" + "S1" + "','" + "-" + "','" + "-" + "','" + TransmissionReason + "')";
				System.out.println("[DATA INSERTED]  : " + sql1);
       st1.executeUpdate(sql1);
       System.out.println("[EXECUTION SUCCESSFUL]*");
     }
     catch (Exception localException) {}
   }
   
   public void parseData() {
     throw new Error("Unresolved compilation problems: \n\tType0 cannot be resolved to a type\n\tType0 cannot be resolved to a type\n");
   }
 }


