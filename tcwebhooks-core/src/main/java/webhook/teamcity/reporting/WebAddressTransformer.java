package webhook.teamcity.reporting;

import java.net.URL;

public interface WebAddressTransformer {
	
	/**
	 * Extracts the hostname from a URL, generalises it (eg, convert web.api.blah.google.com => google.com)
	 * and returns the generalised version as a string;
	 * Special cases like hostnames without a domain, ipv4 addresses, ipv6Addresses 
	 * @param uri
	 * @return {@link GeneralisedWebAddress}
	 */
	public GeneralisedWebAddress getGeneralisedHostName(URL uri);
	
}
