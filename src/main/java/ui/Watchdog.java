package ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.util.Duration;

import java.util.Observable;

/**
 * Watchdog class that is fed by any user action and notifies (using observer -- it's simple and effective, I guess)
 * the UI controller that it should switch back to the main screen. I confess that I don't entirely understand how this
 * jfx code works, so some documentation may be lacking
 *
 * Resource used: https://stackoverflow.com/questions/27162374/javafx-2-user-idle-detection
 */
public class Watchdog extends Observable
{
	private Timeline idleTime;
	private EventHandler userEventHandler;

	public Watchdog(Duration timeout, Runnable notifier)
	{
		idleTime = new Timeline(new KeyFrame(timeout, e->notifier.run()));
		idleTime.setCycleCount(Animation.INDEFINITE);
		userEventHandler = e->notIdle();
		idleTime.playFromStart(); //Start monitoring immediately
	}

	/**
	 * Register a scene as being able to feed the watchdog.
	 * @param scene Scene to register
	 * @param eventType Type of event that can feed the watchdog. Event.ANY will capture all events.
	 */
	public void registerScene(Scene scene, EventType<? extends Event> eventType)
	{
		scene.addEventFilter(eventType, userEventHandler);
	}

	/**
	 * Unregister a scene as being able to feed this watchdog
	 * @param scene Scene to unregister
	 * @param eventType Type of event that feeds the watchdog.
	 */
	public void unregisterScene(Scene scene, EventType<? extends Event> eventType)
	{
		scene.removeEventFilter(eventType, userEventHandler);
	}

	/**
	 * Feed the watchdog
	 */
	public void notIdle()
	{
		if (idleTime.getStatus() == Animation.Status.RUNNING)
			idleTime.playFromStart();
	}


}
