package it.polimi.distsys.dcd.worker;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

public class CustomClassLoader extends ClassLoader {
	
	private CommunicationHandler handler;
	private ObjectMessage msg;
	
	
	public CustomClassLoader(CommunicationHandler handler, ObjectMessage msg, ClassLoader classLoader) {
		super(CustomClassLoader.class.getClassLoader());

		this.handler = handler;
		this.msg = msg;
	}
	
	@Override
	public synchronized Class<?> loadClass(String className, boolean resolveIt) throws ClassNotFoundException {
		
		Class<?> result = loadClass(className);
		
		/* Load reference classes as well if required */
		if (resolveIt) {
			resolveClass(result);
		}
		
		return result;
	}
	
	@Override
	public synchronized Class<?> findClass(String className) throws ClassNotFoundException {
		return loadClass(className);
	}
	
	// Main function called by client to resolve class
	@Override
	public synchronized Class<?> loadClass(String className) throws ClassNotFoundException {

		Class<?> result;
		byte classData[];
		
		/* Call super class loader */
		try {
			result = super.findSystemClass(className); 
			return result;
		} catch (ClassNotFoundException e) {
			System.err.println("WARNING: Not a class parent classloader can load.");  
		}

		System.err.println("Attempting to load class "+className+" from client...");
		
		/* Try to load it from our repository */
		classData = getClassFromClient(className);
		if (classData == null) {
			throw new ClassNotFoundException();
		}
		
		System.err.println("Class "+className+" loaded.");
		
		/* Define the class */
		result = defineClass(className, classData, 0, classData.length, null);
		if (result == null) {
			System.err.println("Format Error");
			throw new ClassFormatError();
		}
		
		return result;  
	}

	private byte[] getClassFromClient(String className) {
		byte[] classData = null;
		
		try {
			classData = handler.lookupClass(className, msg.getJMSReplyTo());
		}
		catch (JMSException e) {
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Error looking for class: " + e.getMessage());
		}
		
		return classData;
	}
}
