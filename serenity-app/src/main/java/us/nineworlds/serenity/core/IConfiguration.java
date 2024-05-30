package us.nineworlds.serenity.core;

/**
 * @author dcarver
 *
 */
public interface IConfiguration {

	/**
	 * @return the host
	 */
	public abstract String getHost();

	/**
	 * @param host the host to set
	 */
	public abstract void setHost(String host);

	/**
	 * @return the port
	 */
	public abstract String getPort();

	/**
	 * @param port the port to set
	 */
	public abstract void setPort(String port);

	public abstract String getUsername();

	public abstract void setUsername(String username);


	public abstract String getPassword();

	public abstract void setPassword(String password);

}
