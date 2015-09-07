package org.zalando.testmailserver.util;


import static org.zalando.testmailserver.util.FormattingUtils.format;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

	private final static Logger LOG = LoggerFactory.getLogger(Utils.class);
	
	public static String getDefaultHostName(){
		String hostName;
		try {
			hostName = InetAddress.getLocalHost().getCanonicalHostName();
		}
		catch (final UnknownHostException e){
			final String defaultHostName = "localhost";
			LOG.warn(format("Could not obtain hostname. Using {}.", defaultHostName), e);
			hostName = defaultHostName;
		}
		return hostName;
	}

}
