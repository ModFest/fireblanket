package net.modfest.fireblanket;

import net.modfest.fireblanket.util.LinkedBlocQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestBlocQueue {
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 10; i++) {
			testThreaded();
		}

		System.out.println("==========");

		for (int i = 0; i < 10; i++) {
			testThreadedOld();
		}

		System.out.println("==========");

		for (int i = 0; i < 10; i++) {
			testThreadedConc();
		}
	}

	public static void testBasic() {
		System.out.println("Starting!");
		LinkedBlocQueue<Integer> q = new LinkedBlocQueue<>();
		q.put(0);
		q.put(1);
		q.put(2);
		q.put(3);
		q.put(4);
		System.out.println(q.pull());
	}

	public static void testThreaded() throws Exception {
		LinkedBlocQueue<Integer> q = new LinkedBlocQueue<>();

		Thread t1 = new Thread(() -> {
			for (int i = 0; i < 1000000; i++) {
				q.put(i);
			}
		});

		Thread t2 = new Thread(() -> {
			for (int i = 2000000; i < 3000000; i++) {
				q.put(i);
			}
		});

		AtomicInteger v = new AtomicInteger();
		AtomicBoolean end = new AtomicBoolean(false);
		Thread p = new Thread(() -> {
			while (true) {
				if (end.get() || v.get() == 2000000) {
					return;
				}
				LinkedBlocQueue.Bloc<Integer> pull = q.pull();
				if (pull == null) {
					Thread.yield();
				} else {
					v.addAndGet(pull.size());
//					System.out.println(pull.size());
				}
			}
		});

		t1.start();
		t2.start();
		p.start();

		long t = System.currentTimeMillis();

		System.out.println("Started! Waiting 10 seconds...");

//		Thread.sleep(2_000);
//		end.set(true);

		p.join();

		System.out.println("End:");
		System.out.println(v);

		System.out.println(">>> " + (System.currentTimeMillis() - t));
	}

	public static void testThreadedOld() throws Exception {
		LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>();

		Thread t1 = new Thread(() -> {
			for (int i = 0; i < 1000000; i++) {
				q.offer(i);
			}
		});

		Thread t2 = new Thread(() -> {
			for (int i = 2000000; i < 3000000; i++) {
				q.offer(i);
			}
		});

		AtomicInteger v = new AtomicInteger();
		AtomicBoolean end = new AtomicBoolean(false);
		Thread p = new Thread(() -> {
			while (true) {
				if (end.get() || v.get() == 2000000) {
					return;
				}
				try {
					Integer take = q.take();
					v.incrementAndGet();
				} catch (Exception e) {
					e.printStackTrace();
				}
//				LinkedBlocQueue.Bloc<Integer> pull = q.pull();
//				if (pull == null) {
//					Thread.yield();
//				} else {
//					v.addAndGet(pull.size());
////					System.out.println(pull.size());
//				}
			}
		});

		t1.start();
		t2.start();
		p.start();

		long t = System.currentTimeMillis();

		System.out.println("Started! Waiting 10 seconds...");

//		Thread.sleep(2_000);
//		end.set(true);

		p.join();

		System.out.println("End:");
		System.out.println(v);

		System.out.println(">>> " + (System.currentTimeMillis() - t));
	}

	public static void testThreadedConc() throws Exception {
		ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>();

		Thread t1 = new Thread(() -> {
			for (int i = 0; i < 1000000; i++) {
				q.offer(i);
			}
		});

		Thread t2 = new Thread(() -> {
			for (int i = 2000000; i < 3000000; i++) {
				q.offer(i);
			}
		});

		AtomicInteger v = new AtomicInteger();
		AtomicBoolean end = new AtomicBoolean(false);
		Thread p = new Thread(() -> {
			while (true) {
				if (end.get() || v.get() == 2000000) {
					return;
				}
				try {
					Integer take = q.poll();
					if (take == null) {
						Thread.yield();
						continue;
					}
					v.incrementAndGet();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		t1.start();
		t2.start();
		p.start();

		long t = System.currentTimeMillis();

		System.out.println("Started! Waiting 10 seconds...");

//		Thread.sleep(2_000);
//		end.set(true);

		p.join();

		System.out.println("End:");
		System.out.println(v);

		System.out.println(">>> " + (System.currentTimeMillis() - t));
	}
}
