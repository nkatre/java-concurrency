package concurrency.ch4;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class ReaderWriterTest {
	private String mutableData = "Initial Value";
	private int readers = 0;
	private final Semaphore mutex = new Semaphore(1);
	private final Semaphore roomEmpty = new Semaphore(1);
	private final CountDownLatch latch = new CountDownLatch(31);
	
	private Executor executor = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	@Test
	public void test() throws InterruptedException {
		for(int i = 0; i < 20; i++){
			final int index = i;
			executor.execute(new Reader());
			if(i%2 == 0) executor.execute(new Writer(index));
		}
		
		latch.countDown();
		latch.await(5, TimeUnit.SECONDS);
	}
	
	private class Reader implements Runnable {

		public void run() {
			mutex.acquireUninterruptibly();
			++readers;
			if(readers == 1) roomEmpty.acquireUninterruptibly(); // first reader locks
			mutex.release();
			
			System.out.println(mutableData);
			
			mutex.acquireUninterruptibly();
			--readers;
			if(readers == 0) roomEmpty.release(); // last reader unlocks
			mutex.release();
			
			latch.countDown();
		}
		
	}
	
	private class Writer implements Runnable{
		private final int id;
		
		Writer(final int id){
			this.id = id;
		}
		
		public void run() {
			
			roomEmpty.acquireUninterruptibly();
			mutableData = "Writer " + id;
			roomEmpty.release();
			
			latch.countDown();
		}
	}

}
