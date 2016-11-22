package org.xson.web.async;

import java.util.LinkedList;

import org.apache.log4j.Logger;

/**
 * 异步任务线程
 */
public class AsyncTaskThread {

	private Logger					log			= Logger.getLogger(AsyncTaskThread.class);

	// private static Log log = LogFactory.getLog(AsyncTaskThread.class);

	private LinkedList<AsyncTask>	taskQueue	= null;

	private volatile boolean		running		= false;

	private InnerTaskThread			thread		= null;

	private class InnerTaskThread extends Thread {

		private int	count	= 0;

		public InnerTaskThread() {
			super("AsyncTaskThread");
			setDaemon(true);
		}

		@Override
		public void run() {
			while (running) {
				AsyncTask task = null;
				synchronized (taskQueue) {
					task = taskQueue.poll();
				}
				if (null != task) {
					try {
						task.run();
					} catch (Exception e) {
						log.error("AsyncTask Error", e);
					}
					count++;
				} else {
					try {
						sleep(100L);
					} catch (InterruptedException e) {
						log.error(null, e);
						return;
					}
				}
				if (count > 100) {
					try {
						sleep(100L);
						count = 0;
					} catch (InterruptedException e) {
						log.error(null, e);
						return;
					}
				}
			}
		}
	}

	public void addTask(AsyncTask task) {
		if (running) {
			synchronized (taskQueue) {
				taskQueue.add(task);
			}
		} else {
			log.error("Thread has not been started");
		}
	}

	public void start() {
		if (!running) {
			running = true;
			taskQueue = new LinkedList<AsyncTask>();
			thread = new InnerTaskThread();
			thread.start();
			log.info("AsyncTaskThread start...");
		}
	}

	public void stop() {
		if (running) {
			running = false;
			log.info("AsyncTaskThread stop...");
		}
	}
}
