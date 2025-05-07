 package parser;
 
 
 public class MainRunner
 {
   public static void main(String[] args)
     throws Exception
   {
     try
     {
       Thread thread = new PointerDataReceiver(4222);
       thread.start();
     }
     catch (Exception e) {
       e.printStackTrace();
     }
   }
 }


