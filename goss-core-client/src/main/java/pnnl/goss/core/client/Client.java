package pnnl.goss.core.client;

import java.io.Serializable;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import pnnl.goss.core.Request;
import pnnl.goss.core.Request.RESPONSE_FORMAT;

public interface Client {

	/**
	 * Sends request and gets response for synchronous communication.
	 * @param request instance of pnnl.goss.core.Request or any of its subclass.
	 * @return return an Object which could be a  pnnl.goss.core.DataResponse,  pnnl.goss.core.UploadResponse or  pnnl.goss.core.DataError.
	 * @throws IllegalStateException when GossCLient is initialized with an GossResponseEvent. Cannot synchronously receive a message when a MessageListener is set.
	 * @throws JMSException
	 */
	public abstract Object getResponse(Request request)
			throws IllegalStateException, JMSException;

	/**
	 * Sends request and gets response for synchronous communication.
	 * @param request instance of pnnl.goss.core.Request or any of its subclass.
	 * @return return an Object which could be a  pnnl.goss.core.DataResponse,  pnnl.goss.core.UploadResponse or  pnnl.goss.core.DataError.
	 * @throws IllegalStateException when GossCLient is initialized with an GossResponseEvent. Cannot synchronously receive a message when a MessageListener is set.
	 * @throws JMSException
	 */
	public abstract Object getResponse(Request request,
			RESPONSE_FORMAT responseFormat);

	/**
	 * Sends request and initializes listener for asynchronous communication
	 * To get data, request object should extend gov.pnnl.goss.communication.RequestData. 
	 * To upload data, request object should extend gov.pnnl.goss.communication.RequestUpload.
	 * @param request gov.pnnl.goss.communication.Request.
	 * @param event of GossResponseEvent
	 * @return the replyDestination topic
	 */
	public abstract String sendRequest(Request request, GossResponseEvent event,
			RESPONSE_FORMAT responseFormat) throws NullPointerException;

	/**
	 * Lets the client subscribe to a Topic of the given name for event based communication.
	 * @param topicName
	 * throws IllegalStateException if GossCLient is not initialized with an GossResponseEvent. Cannot asynchronously receive a message when a MessageListener is not set.
	 * throws JMSException
	 */
	public abstract void subscribeTo(String topicName, GossResponseEvent event)
			throws NullPointerException;

	public abstract void publish(String topicName, Serializable data,
			RESPONSE_FORMAT responseFormat) throws NullPointerException;

	public abstract void publish(String topicName, Serializable data)
			throws NullPointerException;
	
	public abstract void publishString(String topicName, String data)
			throws NullPointerException;

	/**
	 * Closes the GossClient connection with server.
	 */
	public abstract void close();

}