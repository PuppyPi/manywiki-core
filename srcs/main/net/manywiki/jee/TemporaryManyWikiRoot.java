package net.manywiki.jee;

import java.util.Properties;
import javax.annotation.Nullable;
import org.apache.wiki.filters.SpamFilter;
import org.apache.wiki.filters.SpamFilterInsertions;

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
	
	
	
	
	public static SpamFilter getSpamFilter()
	{
		//Todo XD'
		return null;
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

	public static @Nullable SpamFilterInsertions active = null;

	
	public static void sendMessage(Properties wikiProperties, String email, String format, String mailMessage)
	{
		//Todo-PP replace this with more a configurable alerts system!
	}
}
