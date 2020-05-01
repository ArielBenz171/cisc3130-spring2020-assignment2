import java.io.*;

public class Main {
	public static void main(String[] args) throws NumberFormatException, IOException
	{
		File folder = new File("../charts");
		File[] charts = folder.listFiles();

		// 200 tracks, each track may be by a different artist
		int NUMBER_TRACKS = 200; 
		int RELEVANT_DATA_FIELDS = 2;
		String[][] myList = new String[NUMBER_TRACKS][RELEVANT_DATA_FIELDS];
		// myList[i][0] = track title
		// myList[i][1] = artist name

		TrackList[] weeks = new TrackList[charts.length];
		// each element is a list that represents one week of data

		// reads each week into a sorted list and prints to screen
		for (int i=0; i < charts.length; i++) {
			
			weeks[i] = readOneWeek(charts[i], myList);	
			
			System.out.println("------------------------------------------------------------");
			System.out.println("Week " + (i+1) + "\n");
			System.out.println(weeks[i].displayList());
		}
		
		TrackList playlist = mergeAll(weeks, weeks.length/2);
		System.out.println("------------------------------------------------------------");
		System.out.println("The playlist after merging all weeks and omitting duplicates\n");
		System.out.println(playlist.displayList());
		
		TrackStack listenHistory = new TrackStack();
		listenToPlaylist(playlist, listenHistory);
		
		System.out.println("---------------------------------");
		System.out.println("The playlist after listening to each song\n");
		System.out.println(playlist.displayList());
		
		System.out.println("---------------------------------");
		System.out.println("The listen history after listening to each song\n");
		System.out.println(listenHistory.displayList());
	}

	// reads in one file from the directory and returns a TrackList object for that week
	public static TrackList readOneWeek(File in, String[][] myList) throws NumberFormatException, IOException
	{
		
		BufferedReader reader = new BufferedReader(new FileReader(in));
		// variables to be used in processing each track
		String[] track = new String[5];
		int firstEmptyRow = 0; // index of the first empty row in myList [x][]

		// clears notes and column titles from buffer
		reader.readLine();
		reader.readLine();

		// loop to process each track. Saves relevant data; omits chart position, streams, and url
		while (reader.ready()) {

			String str = reader.readLine();
			track = str.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
			// splits a line from the CSV file, ignores commas that aren't delimiter
			
			track[2] = track[2].replace("\"", ""); // remove " from artist name
			track[1] = track[1].replace("\"", ""); // remove " from track title

			myList[firstEmptyRow][0] = track[1];
			myList[firstEmptyRow][1] = track[2];

			firstEmptyRow++;
		}
		reader.close();
		
		TrackList top = new TrackList();
		// inserts each track from myList[][] into a TrackList data structure,
		for (int i=0; i < firstEmptyRow; i++) {
			top.insert(myList[i][0], myList[i][1]);
		}
		return top;
	}	

	// initial call of this recursive method should be TrackList.length/2
	public static TrackList mergeAll(TrackList[] weeks, int n) 
	{
		if (n == 1) 
			return weeks[0];
		
		// merges elements in the list according to the following concept
		// [1][2][3][2][1] -> [1][2][1] -> [1][1] -> [1]
		for (int i=0; i < n/2; i++) {
			
			if (i != n/2) // avoids case where a list will merge with itself
				weeks[i].merge(weeks[n-1-i]);
		}
		return mergeAll(weeks, n/2);
	}

	public static void listenToPlaylist(TrackList playlist, TrackStack history)
	{
		while (!playlist.isEmpty())
			history.insert(playlist.listen());
		
	}
}
// sorted list of Track nodes
// nodes are always inserted in proper order in relation to nodes already in the list
// for processing, this list functions as a queue; elements are ONLY removed from the front
public class TrackList {

	Track first;		
	Track last;		
	
	public class Track implements Comparable<Track>{
		
		String title;
		String artist;
		Track next;
		
		public Track(String name, String artist) {
			this.title = name;
			this.artist = artist;
			next = null;
		}
		
		public String getName() {
			return title;
		}
		public String getArtist() {
			return artist;
		}

		public String displayTrack() {
			
			return "Title:  " + title + "\n" + "Artist: " + artist;
		}

		// uses String.compareTo, natural order of Track is alphabetized by title field
		// negative return -> this Track precedes the argument
		// positive return -> this Track follows the argument
		public int compareTo(Track other) {
			
			return this.title.compareToIgnoreCase(other.getName());
		}
	}
	
	public TrackList() {
		
		first = null;
		last = null;
	}
	
	public boolean isEmpty() {
		return first == null;
	}
	
	// performs a sorted insert of a new element
	public void insert(String title, String artist) {
		
		Track track = new Track(title, artist);
		
		if (first == null || first.compareTo(track) > 0) {
			
			track.next = first;
			first = track;
			
			return;
		}		
		
		Track current = first;
		
		while (current.next != null && current.next.compareTo(track) < 0) 
			current = current.next;
		
		track.next = current.next;
		current.next = track;
	}
	
	public Track remove() {
		
		Track temp = first;
		first = first.next;
		return temp;

	}
	
	public int size() {
		
		int count = 0;
		Track current = first;
		
		while(current != null) {
			count += 1;
			current = current.next;
		}
		
		return count;
	}
	
	// "listens" to the first song in the list
	public Track listen() {
		return remove();
	}
	
	// merges this list with another list
	// this method omits duplicate elements
	// this method sets the argument list to null after processing
	public void merge(TrackList aList) {
		
		Track result = new Track("", ""); // result.next will be the new head of the merged lists
		Track tail = result; // last element in the resultant list
		
		Track a = this.first;
		Track b = aList.first;
		
		// case for when both lists are not empty
		while (a != null && b != null) {
			
			if (a.compareTo(b) > 0) {
				tail.next = b;
				tail = tail.next;
				b = b.next;
			}
			else if (a.compareTo(b) < 0) {
				tail.next = a;
				tail = tail.next;
				a = a.next;
			}
			else { // case where both elements are equal. One is skipped, other is added to new list
				b = b.next;
				tail.next = a;
				tail = tail.next;
				a = a.next;
			}
		}
		
		// case for when list a is empty; pulls nodes from list b into resultant list
		while (a == null && b!= null) {
			tail.next = b;
			tail = tail.next;
			b = b.next;
		}
		// case for when list b is empty; pulls nodes from list a into resultant list
		while (b == null && a != null) {
			tail.next = a;
			tail = tail.next;
			a = a.next;
		}
		
		first = result.next; // this list is now the entire resultant merged list
		aList.first = null; // clear up memory taken up by argument list
	}
	
	public String displayList() {
		
		String str = "";
		
		Track current = first;
		
		while (current != null) {
			str += current.displayTrack() + "\n\n";
			current = current.next;
		}
		
		return str;
	}
}

// retains same overall structure as TrackList, but is not sorted
// treats first as the top of the stack; inserts and removes at first
public class TrackStack extends TrackList{

	public TrackStack() { super(); }
	
	public void insert(String title, String artist) {
		
		Track track = new Track(title, artist);
		track.next = this.first;
		first = track;
	}
	public void insert(Track track) {
		
		track.next = this.first;
		first = track;
	}
}
