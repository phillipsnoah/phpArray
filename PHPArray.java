/** 
 *	PHPArray class for Professor Sherif Khattab's Assignment 2.
 *  Using a variety of methods from Lab 4 & Lab 5, along with original methods.
 *	@author Noah Phillips
 */
import java.util.Iterator;
import java.util.*;
/**
* A generic PHPArray using an underlying linear probing hash table with 
* a linked list of nodes. Stores nodes in the hash table for 0(1) access and keeps 
* a linked list for iterative and storage purposes. 
*/
public class PHPArray<V> implements Iterable<V>
{
	private static final int INIT_CAPACITY = 4;
	private int N;           // number of key-value pairs in the symbol table
	private int M;           // size of linear probing table
	private Node<V>[] entries;  // the table
	private Node<V> head;       // head of the linked list
	private Node<V> tail;       // tail of the linked list	
	private MyIterator eachIterator;
	
	/*PHPArray Constructors*/
	
	/**
	* Default constructor override, making the PHPArray with the default capacity (4).
	*/
	public PHPArray()
	{
		this(INIT_CAPACITY);
	}
	
	/**
	* Initializes a new PHPArray object.
	* @param capacity The capacity of the PHPArray.
	*/
	public PHPArray(int capacity) 
	{
		M = capacity;
		@SuppressWarnings("unchecked")
		Node<V>[] temp = (Node<V>[]) new Node[M];
		entries = temp;
		head = tail = null;
		N = 0;
	}
	/**
	* Initalizes and returns a new MyIterator iterator. This iterator will traverse the
	* PHPArray using the LinkedList node traversal.
	* @return A new MyIterator Iterator.
	*/
	public Iterator<V> iterator() 
	{
		return new MyIterator();
	}
	
	/*PHPArray Methods*/
	/**
	* Calls the full put method using an int as a key. Added for convenience.
	* @param key An int key to be passed to the put method as a string.
	* @param val A generic value that will be used as the value in the key-value pair.
	*/
	public void put(int key, V val)
	{
		put(Integer.toString(key), val);
	}
	/**
	* Inserts a new key-value pair into the PHPArray, utilizing 
	* a linear probing hash table containing nodes.
	* If the value for the key is null, removes the key.
	* If the key already exists in the table, update the value associate with it. 
	* If the table is at 50% capacity, call the resize method. 
	* @param key A string to be used as the key in the key-value pair.
	* @param val A generic value to be used as the value in the key-value pair. 
	*/
	public void put(String key, V val) 
	{
		if (val == null) unset(key);
		// double table size if 50% full
		if (N >= M/2) resize(2*M);

		// linear probing
		int i;
		for (i = hash(key); entries[i] != null; i = (i + 1) % M) 
		{
			// update the value if key already exists
			if (entries[i].key.equals(key)) 
			{
				entries[i].value = val; return;
			}
		}
		// found an empty entry
		entries[i] = new Node<V>(key, val);
		//insert the node into the linked list
		// TODO: DONE Insert the node into the doubly linked list in O(1) time
		if (head == null) 
		{
			head = tail = entries[i];
		}
		else
		{
			tail.next = entries[i];
			entries[i].prev = tail;
			tail = entries[i];
		}
		N++;
	}
	/**
	* Calls the full get method using an int as a key. Added for convenience.
	* @param key An int key that will be passed to the get method as a string.
	* @return The value associated with key.
	*/
	public V get(int key)
	{
		return get(Integer.toString(key));
	}
	/**
	* Using the hashed value of the key, search the hash table. 
	* If the hashed key is in the table, return the value associated with it. Else return false.
	* @param key A String key to search the hash table for.
	* @return The value associated with the key parameter, if the key is valid.
	* @return null If the key doesn't have a value associated with it in the table.
	*/
	public V get(String key) 
	{
		for (int i = hash(key); entries[i] != null; i = (i + 1) % M)
			if (entries[i].key.equals(key))
				return entries[i].value;
		return null;
	}
	/**
	* Calls the full unset method using an int as a key. Added for convenience.
	* @param key An int key to be passed to the unset method as a string.
	*/
	public void unset(int key)
	{
		unset(Integer.toString(key));
	}
	/**
	* Unsets (deletes) the hash table entry that has the parameter key as its key. 
	* If the key doesn't exist within the table, do nothing. 
	* Hashes the key, then searches the hashtable with that value. Once found, make the hash table 
	* entry null. 
	* @param key A string key to be removed from the table. 
	*/	
	public void unset(String key) 
	{
		if (get(key) == null) return;

		// find position i of key
		int i = hash(key);
		while (!key.equals(entries[i].key)) 
		{
			i = (i + 1) % M;
		}
		// delete node from hash table
		Node<V> toDelete = entries[i];
		entries[i] = null;
		// TODO: delete the node from the linked list in O(1)
		if (N > 2)
		{
			if ( toDelete == head ) head = head.next;
			else if ( toDelete == tail ) tail = tail.prev;
			else
			{
				toDelete.prev.next = toDelete.next;// the node after toDelete's previous 
				toDelete.next.prev = toDelete.prev;// the node before toDelete's next
			}
			toDelete = null;
		}
		else
		{
			toDelete = null;
		}
		// rehash all keys in same cluster
		i = (i + 1) % M;
		while (entries[i] != null) 
		{
			// delete and reinsert
			Node<V> nodeToRehash = entries[i];
			entries[i] = null;
			rehash(nodeToRehash);
			i = (i + 1) % M;
		}
		N--;
    // halves size of array if it's 12.5% full or less
		if (N > 0 && N <= M/8) resize(M/2);
	}
	/**
	* Resize the hash table to the given capacity.
	* Creates a new hash table with the new capacity. After iterating through
	* and adding the old PHPArray elements, sets the old hash table's variables equal to the 
	* new hash table's variables. 
	* @param capacity The new capacity of the hash table.
	*/
	private void resize(int capacity) 
	{
		PHPArray<V> temp = new PHPArray<V>(capacity);

		//rehash the entries in the order of insertion
		Node<V> current = head;
		while(current != null)
		{
			temp.put(current.key, current.value);
			current = current.next;
		}
		entries = temp.entries;
		head    = temp.head;
		tail    = temp.tail;
		M       = temp.M;
	}
	/**
	* Hashes the given key.
	* @param key The string key that will be hashed. 
	* @return The string's hashed integer representation. 
	*/
	private int hash(String key) 
	{
		return (key.hashCode() & 0x7fffffff) % M;
	}
	/**
	* Rehashes the node that is passed as a parameter while keeping it in place. 
	* @param node The node that will be rehashed in place. 
	*/
	private void rehash(Node<V> node)
	{
		Node<V> temp = node;

	
		unset(node.key);
		int i;
		for (i = hash(temp.key); entries[i] != null; i = (i + 1) % M) 
		{
			if (entries[i].key.equals(temp.key)) 
			{
				return; // don't update the val, just return. We can't rehash if it's already there. 
			}
		}
		entries[i] = temp;	// we're in an empty hash position (from put method) 
		// we've made the hash entry at [i] (the new hash table spot) equal to the copy of node
		// thus we've rehashed but kept the same spot as original node
	}
	/**
	* Returns the number of key-value pairs in the PHPArray. 
	* @return The number of key-value pairs in the PHPArray. 
	*/
	public int length()
	{
		return N;
	}
	/**
	* Returns a new ArrayList of type string containing all of the keys in the hash table. 
	* Uses an iterator to iterate through each node that is contained in the table, adding 
	* the keys to an ArrayList as it goes. 
	* @return An ArrayList of Strings that contains every key in the table. 
	*/
	public ArrayList<String> keys()
	{
		ArrayList<String> result = new ArrayList<String>(N - 1);
		MyIterator keysIterator = new MyIterator();
		while(keysIterator.hasNext())
		{
			result.add(keysIterator.current.getKey());
			keysIterator.next();
		}
		return result;
	}
	/**
	* Returns a new ArrayList of type V containing all of the values in the hash table. 
	* Uses an iterator to iterate through each node that is contained in the table, adding 
	* the values to an ArrayList as it goes. 
	* @return An ArrayList of V that contains every value in the table. 
	*/
	public ArrayList<V> values()
	{
		ArrayList<V> result = new ArrayList<V>(N - 1);
		MyIterator valuesIterator = new MyIterator();
		while(valuesIterator.hasNext())
		{
			result.add(valuesIterator.current.getValue());
			valuesIterator.next();
		}
		return result;
	}
	/**
	* Iterates through the entire hash table, printing the data at each entry.
	* If there is no key-value pair at an entry, simply print null.
	* Else, print the key and its associated value. 
	*/	
	public void showTable()
	{
		// print values in order of insertion
		System.out.println("\tRaw Hash Table Contents:");
		for (int i = 0; i < entries.length; i++) 
		{
			if (entries[i] == null) System.out.println(i + ": null");
			else System.out.println(i + ": Key: " + entries[i].getKey() + " Value: " + entries[i].getValue());
		}
	}
	/**
	* Flips the key-value pairs (to value-key pairs) in the PHPArray. 
	* Will work on any PHPArray, even if they're empty (will return an empty table). 
	* @throws ClassCastException if the values in the hashtable aren't strings (or ints) 
	* and thus cannot be used as keys. 
	* @return A new PHPArray of Strings that is identical to the original PHPArray, but 
	* the key-value pairs are flipped (value-key pairs now). 
	*/
	public PHPArray<String> array_flip() throws ClassCastException
	{
		PHPArray<String> result = new PHPArray<String>(N);
		Object[] values = values().toArray();
		Object[] keys = keys().toArray();
		
		for ( int i = 0; i < N; i++)
		{
			//try 
			
				result.put( (String)values[i], (String)keys[i]);
			
		}
		return result;
	}
	/**
	* Sorts the PHPArray using the Arrays.sort() method on an array of values in the table.
	* Then, adds the values to a new PHPArray in sorted order. Removes the key values 
	* and uses natural order numbering ( 0 - the amount of values in the table ). 
	*/
	public void sort()
	{
		Object[] values = values().toArray();
		if (values.length == 0) return;
		if (values[0] instanceof Comparable)
		{			
			Arrays.sort(values);
			PHPArray<V> temp = new PHPArray<V>(N);
		
			for (int i = 0; i < values.length; i++)
			{
				temp.put(i, (V)values[i]);
			}
			entries = temp.entries;
			head    = temp.head;
			tail    = temp.tail;
			M       = temp.M;		
		}
		else 
		{
			throw new ClassCastException();
		}		
		//else throw new Exception("Objects in PHPArray must be Comparable");
	}	
	/**
	* Sorts the PHPArray by value but retains key-value pairing. Same idea as the sort() method,
	* but uses a treeMap to sort by value and retain pairing. 
	* The casting in this method is not dangerous!
	*/
	public void asort()
	{
		Object[] values = values().toArray();
		Object[] keys = keys().toArray();
		
		if (values.length == 0 || keys.length == 0) return;
		if (values[0] instanceof Comparable)
		{			
			TreeMap<Object, String> sortMap = new TreeMap<Object, String>();			
			PHPArray<V> temp = new PHPArray<V>(N);
			
			for ( int i = 0; i < values.length && i < keys.length; i++)
			{
				sortMap.put(values[i],(String)keys[i]); 
			}
			Set mapKeys = sortMap.keySet();
			for (Object o: mapKeys)
			{
				temp.put(sortMap.get(o), (V)o);
			}
			entries = temp.entries;
			head    = temp.head;
			tail    = temp.tail;
			M       = temp.M;		
		}
		else 
		{
			throw new ClassCastException();
		}
	}
	/**
	* Iterates through the nodes in the hash table to gather each key-value pair and return 
	* them as a Pair object. 
	* Will remember its spot until reset() is called.
	* @return A pair object that contains the next key-value pair in the hash table. 
	* @return null if there are no more key-value pairs in the table.
	*/
	public Pair<V> each()
	{
		if ( eachIterator == null )
		{
			eachIterator = new MyIterator();
		}
		if (eachIterator.current == null) return null;
		Pair<V> result = new Pair<V>(eachIterator.current.getKey(), eachIterator.current.getValue() );
		eachIterator.next();
		return result;
	}
	/**
	* Resets the iterator used in the each() method by making iterator used equal to a 
	* new MyIterator object (which will start at the beginning of the linkedList). 
	*/
	public void reset()
	{
		eachIterator = new MyIterator();
	}
	/**
	* Returns a PHPArray that contains the intersecting elements of this array and the parameter PHPArray other. 
	* This method will not work at compile time if the two PHPArrays are not the same type.
	* USES VALUES FOR COMPARISON
	* @param other A PHPArray of the same type as this PHPArray
	* @return A PHPArray of type V that will contain the intersecting elements of the two PHPArrays. 
	*/
	public PHPArray<V> intersect(PHPArray<V> other)
	{
		if (other == null) return null;
		if (this.length() == 0 || other.length() == 0) return null;
		PHPArray<V> result = new PHPArray<V>(N);
		
		ArrayList<String> thisKeys = keys();
		ArrayList<String> otherKeys = other.keys();
		
		for (String a : otherKeys)
		{
			if ( thisKeys.contains(a) ) // if the keys are both present 
			{
				if ( other.get(a).equals(this.get(a)) ) // if they both have the same value 
				{
					result.put(a, this.get(a));
				}
			}
		}
		return result;
	}
	/**
	* Returns a PHPArray that contains the differing elements of this array and the parameter PHPArray other. 
	* This method will not work at compile time if the two PHPArrays are not the same type.
	* USES VALUES FOR COMPARISON
	* @param other A PHPArray of the same type as this PHPArray
	* @return A PHPArray of type V that will contain the differing elements of the two PHPArrays. 
	*/
	public PHPArray<V> difference(PHPArray<V> other)
	{
		if (other == null) return null;
		if (this.length() == 0 || other.length() == 0) return null;
		PHPArray<V> result = new PHPArray<V>(N);
		
		ArrayList<String> thisKeys = keys();
		ArrayList<String> otherKeys = other.keys();
		
		for (String a : otherKeys)
		{
			if ( thisKeys.contains(a) ) // if the keys are both present 
			{
				if ( !other.get(a).equals(this.get(a)) ) // if they don't have the same value, add it 
				{
					result.put(a, this.get(a));
				} // if they have the same value they aren't differing 
			}
			else 
			{
				result.put(a, other.get(a)); // this is in the other PHPArray but not THIS PHPArray. 
			}
		}
		for (String b : thisKeys) // catching the stragglers from this.keys(). These weren't in otherKeys at all. 
		{
			if ( !otherKeys.contains(b) )
			{
				result.put(b, this.get(b));
			}
		}
		return result;
	}	
/**
* An inner static class that encapsulates a single key-value. 
*/
public static class Pair<V>
{
	String key;
	V value;	
	/**
	* A constructor that allows the user to pass in a key and value for the Pair upon creation. 
	* @param key A String key that will be used as the key for this Pair. 
	* @param value A generic value that will be used as this Pair's value. 
	*/
	public Pair(String key, V value)
	{
		this.key = key;
		this.value = value;
	}
} // end pair class
	/*Inner Node Class*/
/** 
* An private inner node class of generic typing.
*/
private class Node<V> 
{
    private String key;
    private V value;
    private Node<V> next;
    private Node<V> prev;

	/**
	* Initalizes a node with the given key and value, but with null next and prev references. 
	* @param key The (String) key of the node to be created. 
	* @param value The value of the node to be created. 
	*/
    Node(String key, V value)
	{
		this(key, value, null, null);
	}
	/**
	* Initializes a node with a key, value, next node, and previous node. 
	* @param key The (String) key of the node to be created.  
	* @param value The value of the node to be created. 
	* @param next The node that will be this node's next reference. 
	* @param prev The node that will be this node's previous reference.
	*/
    Node(String key, V value, Node<V> next, Node<V> prev)
	{
		this.key = key;
		this.value = value;
		this.next = next;
		this.prev = prev;
	}
	/**
	* Getter for the node's key. 
	* @return The node's key. 
	*/
	String getKey()
	{
		return this.key;
	}
	/**
	* Getter for the node's value. 
	* @return The node's value. 
	*/	
	V getValue()
	{
		return this.value;
	}
}	/*End of Inner Node Class*/
	/*Inner Iterator Class*/
/**
* An inner class that creates an iterator for the PHPArray.
*/
public class MyIterator implements Iterator<V> 
{
	private Node<V> current;
	
	/**
	* Default constructor that initializes the iteration node as the head of the linked list. 
	*/
    public MyIterator() 
	{
		current = head;
    }
	/**
	* Checks to see if the iteration node can move forward. 
	* @return true if the iteration node's next reference is not null.
	* @return false if the iteration node's next reference is null. 
	*/
    public boolean hasNext() 
	{
		return current != null;
    }
	/**
	* Returns the value of the node the iterator is currently on
	* and moves the iterator node forward one. 
	* @return The generic value of the iterator node. 
	*/
    public V next() 
	{
		V result = current.value;
		current = current.next;
		return result;
    }
} // end MyIterator

} // end PHPArray