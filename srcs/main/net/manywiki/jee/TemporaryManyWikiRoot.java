package net.manywiki.jee;

//FIXME-PP

public class TemporaryManyWikiRoot
{
	public static boolean isLoggingAllHitsToServletLogs()
	{
		//Todo XD
		return true;
	}
	
	public static boolean isDebug()
	{
		//Todo XD
		return true;
	}
	
	public static boolean isForceSecure()
	{
		//Todo XD
		return false;
	}
	
	
	
	
	/**
	 * Gets the domain name of the server we're running on (for http://).
	 */
	public static String getDomainForHTTPInsecure()
	{
		return "localhost";
	}
	
	/**
	 * Gets the domain name of the server we're running on (for https://).
	 */
	public static String getDomainForHTTPSecure()
	{
		return "localhost";
	}
	
	
	/**
	 * Gets the domain name of the server we're running on (for http://).
	 */
	public static int getPortForHTTPInsecure()
	{
		return 61832;
	}
	
	/**
	 * Gets the domain name of the server we're running on (for https://).
	 */
	public static int getPortForHTTPSecure()
	{
		return 61833;
	}
}
