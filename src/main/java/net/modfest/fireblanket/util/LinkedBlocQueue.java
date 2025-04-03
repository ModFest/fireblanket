package net.modfest.fireblanket.util;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.StampedLock;

/**
 * A 'Bloc' Queue, not a {@link BlockingQueue}. This is a batched queue designed to support a single consumer and a (relatively) large
 * amount of producers. It is not a full implementation of {@link Queue}. This "Queue" supports 2 operations, {@link #put(T)}
 * and {@link #pull()}. The put operation adds a new doubly linked node onto the queue, and the pull operation retrieves
 * <b>all</b> of the current elements on the queue. The design goal is to reduce the number of mutex operations by retrieving
 * all nodes rather than just the first element. This queue is an ideal option when locking is a more expensive operation
 * than processing elements. This datastructure has a number of considerations before use. The consumer is responsible for
 * iterating the linked node and processing elements. It has no rate limiting mechanism and is extremely susceptible to
 * resource starvation and positive feedback loops of consumer latency. External care must be taken to ensure that the
 * consumer always removes elements faster than producers insert elements. Use at your own mortal peril.
 */
public final class LinkedBlocQueue<T> {
	private final StampedLock stamp = new StampedLock();
	private Node<T> head;
	private Node<T> tail;
	private int size;

	public static class Node<T> {
		public T data;
		public Node<T> next;
		public Node<T> last;

		private String strLast() {
			String s = "";
			if (last != null) {
				s = last.strLast() + " -> ";
			}

			return s + "(" + data + ")";
		}

		private String strNext() {
			String s = "";
			if (next != null) {
				s = " -> " + next.strNext();
			}

			return "(" + data + ")" + s;
		}

		@Override
		public String toString() {
			String l = last == null ? "" : last.strLast() + " -> ";
			String n = next == null ? "" : " -> " + next.strNext();
			return "{" + l + "(" + data + ")" + n + "}";
		}
	}

	public record Bloc<T>(Node<T> node, int size) {}

	public void put(T t) {
		Node<T> node = new Node<>();
		node.data = t;

		long writeLock = stamp.writeLock();

		if (size == 0) {
			head = node;
			tail = node;
		} else {
			Node<T> tail = this.tail;
			tail.next = node;
			node.last = tail;
			this.tail = node;
		}
		size++;

		stamp.unlockWrite(writeLock);
	}

	public Bloc<T> pull() {
		long optoRead = stamp.tryOptimisticRead();

		if (size == 0) {
			return null;
		}

		long read = 0;
		long convertToWrite = optoRead;
		if (!stamp.validate(optoRead)) {
			read = stamp.readLock();
			convertToWrite = read;

			if (size == 0) {
				stamp.unlockRead(read);
				return null;
			}
		}

		long write = stamp.tryConvertToWriteLock(convertToWrite);
		if (write == 0) {
			if (read != 0) {
				stamp.unlockRead(read);
			}

			write = stamp.writeLock();
		}

		if (size == 0) {
			stamp.unlockWrite(write);
			return null;
		} else {
			Node<T> aHead = head;
			int aSize = size;

			head = null;
			tail = null;
			size = 0;
			stamp.unlockWrite(write);

			return new Bloc<>(aHead, aSize);
		}
	}
}
