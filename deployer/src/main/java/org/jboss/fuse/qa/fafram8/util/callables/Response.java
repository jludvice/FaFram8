package org.jboss.fuse.qa.fafram8.util.callables;

import lombok.Getter;

/**
 * Response wrapper for generic data response. <br/>
 * Designated usage: {@code return java.util.concurrent.Callable&lt;Response&lt;T&gt;&gt;}
 *
 * @author Josef Ludvicek
 */
public class Response<T> {
	@Getter
	private Boolean success = false;

	@Getter
	private T data = null;

	public Response(Boolean success, T response) {
		this.success = success;
		this.data = response;
	}

	/**
	 * Suggests that operation timed out and data is null.
	 *
	 * @return data = null
	 */
	public static <T> Response<T> timeOut() {
		return timeOut(null);
	}

	/**
	 * Suggests that operation timed out, but there are data with requested type to return.
	 *
	 * @param data error/debug message - what failed
	 * @param <T> type of requested data
	 * @return success=false, data=data
	 */
	public static <T> Response<T> timeOut(T data) {
		return new Response<>(false, data);
	}

	/**
	 * Suggests operation passed and there is data to return. <br/>
	 * Use null for "no data".
	 *
	 * @param data data to return
	 * @param <T> type of requested data
	 * @return success=true, data=data
	 */
	public static <T> Response<T> success(T data) {
		return new Response<>(true, data);
	}
}
