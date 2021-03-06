/*
 * $Id$
 */

import RouterLayer.AgentClient.*;
import Abstract.*;
import KQMLLayer.*;
import java.io.*;


/**
 * Class Cliente
 *
 * Muestra la hora actual al agente que se lo pida
 *
 * @author  $Author$
 * @version $Revision$
 */

public class Cliente extends RouterClientAction {
    public Cliente (Address myAddress, Address routerAddress, Address registerAddress,
		     int durationTime, boolean registerRequest) {
	super (myAddress, routerAddress, registerAddress, durationTime);
	try {
	    createServerThread (myAddress.getID (), Thread.NORM_PRIORITY);
	    if (registerRequest)
		register (registerAddress, myAddress);
	    connect (myAddress);
	} catch (Exception ex) {
	    System.out.println ("Clase Cliente (primera excepcion)");
	    System.out.println ("==================================");
	    ex.printStackTrace ();
	    System.exit (1);
	}
    }

    public boolean Act (Object obj) {
	String stampMsg = (String) obj;
	try {
	    KQMLmail mail = new KQMLmail (stampMsg, 1);
	    _mailQueue.addElement (mail);
	    KQMLmessage kqml = mail.getKQMLmessage ();
	    String perf = kqml.getValue ("performative");
	    String content = kqml.getValue ("content");
	    if (content == null) {
		System.out.println (">> Contenido vacio");
		return false;
	    }
	    if (perf.equals ("tell")) {
		System.out.println ("La hora en el servidor es: " + content);
	    } else {
		System.out.println ("CLI>> No entiendo el mensaje: " + perf);
		return false;
	    }
	    unregister ();
	} catch (Exception ex) {
	    System.out.println ("Clase Cliente (segunda excepcion)");
	    System.out.println ("==================================");
	    ex.printStackTrace ();
	    System.exit (1);
	}
	return true;
    }

    protected void obtenerHora () {
	String sendMsg = "(achieve :sender Cliente :receiver Servidor :language Java :content TIME?)";
	try {
	    sendMessage (sendMsg);
	    System.out.println ("CLI>> Pregunto la hora");
	} catch (Exception ex) {
	    System.out.println ("Clase Cliente (tercera excepcion)");
	    System.out.println ("=================================");
	    ex.printStackTrace ();
	    System.exit (1);
	}
    }

    protected void sendErrorMessage (KQMLmessage kqml) {
	String receiver = kqml.getValue ("sender");
	String sendMsg = "(error :sender ";
	sendMsg = sendMsg + getName () + " :receiver " + receiver;
	sendMsg = sendMsg + " :language Java :content (" + kqml.getSendString () + "))";
	try {
	    sendMessage (sendMsg);
	} catch (Exception ex) {
	    System.out.println ("Clase Cliente (cuarta excepcion)");
	    System.out.println ("================================");
	    ex.printStackTrace ();
	    System.exit (1);
	}
    }

    public void processMessage (String s, Object obj) {}
    
    public static void main(String args[]) {
	if (args.length != 1) {
	    System.out.println ("Uso: java Cliente <direccion>");
	} else {
	    Address myAddress = null;
	    Address routerAddress = null;
	    Address registerAddress = null;
	    boolean registerRequest = false;
	    int idleTime = 1000;
	    try {
		DataInputStream in = new DataInputStream (new FileInputStream (new File (args[0])));
		while (true) {
		    String line = in.readLine ();
		    if (line == null)
			break;
		    String next;
		    if (line.startsWith ("MyAddress")) {
			next = in.readLine ();
			myAddress = new Address (next);
		    } else if (line.startsWith ("RouterAddress")) {
			next = in.readLine ();
			routerAddress = new Address (next);
		    } else if (line.startsWith ("RegisterRequest")) {
			next = in.readLine ();
			if (next.startsWith ("y")) {
			    registerRequest = true;
			} else {
			    registerRequest = false;
			}
		    } else if (line.startsWith ("MaxIdleTime")) {
			next = in.readLine ();
			idleTime = (Integer.valueOf (next)).intValue ();
		    }
		}
		in.close ();
		Cliente cliente = new Cliente (myAddress, routerAddress, registerAddress, idleTime, registerRequest);
		cliente.obtenerHora ();
		cliente.start ();
	    } catch (Exception ex) {
		System.out.println ("Clase Cliente (quinta excepcion)");
		System.out.println ("================================");
		ex.printStackTrace ();
		System.exit (1);
	    }
	}
    }
}
