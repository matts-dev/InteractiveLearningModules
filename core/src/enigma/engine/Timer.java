package enigma.engine;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;

public class Timer {
	private HashMap<String, Long> clocks = new HashMap<String, Long>();
	
	/**
	 * Set a timer for a given name. 
	 * 
	 * @param timerName
	 * @param millisFromNow
	 */
	public void setTimer(String timerName, long millisFromNow) {
		long expiration = millisFromNow + TimeUtils.millis();
		clocks.put(timerName, expiration);
	}
	
	/**
	 * Returns true if there currently is a timer with the given name.
	 * 
	 * @param timerName
	 * @return
	 */
	public boolean hasTimer(String timerName) {
		return clocks.containsKey(timerName);
	}
	
	/**
	 * Returns true if the given timer has expired. 
	 * 
	 * @param timerName
	 * @return
	 */
	public boolean timerUp(String timerName) {
		Long expiration = clocks.get(timerName);
		if(expiration == null) {
			Gdx.app.log("timer", "No timer with name:" + timerName);
			return false;
		}
		return expiration < TimeUtils.millis();
	}
	
	/**
	 * Remove the timer from the tracker.
	 * @param timerName
	 */
	public void removeTimer(String timerName) {
		clocks.remove(timerName);
	}
}