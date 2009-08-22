package test;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import test.data.User;

import com.insose.gae.pager.GaeQueryPager;
import com.insose.gae.pager.PageDirection;


public class GaeQueryPagerTests {

	private static final PersistenceManagerFactory factory = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");
	
	public void generateTestUserRecords(int count, int intervalGroupA, int intervalGroupB) {
		
		PersistenceManager pm = factory.getPersistenceManager();
		try {
			p("Creating " + count + " users...");
			
			int counterGroupA = 0, counterGroupB = 0;
			
			List<User> users = new LinkedList<User>();
			
			for(int i = 0; i < count; i++) {
				
				User user = new User();
				user.setRecordNumber(i + 1);
				user.setEmail(generateString(7) + "@" + generateString(10) + ".com");
				user.setFirstName("GroupA" + pad(counterGroupA) + counterGroupA);
				user.setLastName("GroupB" + pad(counterGroupB) + counterGroupB);
				
				// enough to make dateCreated unique (can be enforced in prod
				// the same way, if tx's are used!)
				Thread.sleep(10L);
				
				user.setDateCreated(new Date());
				users.add(user);
				
				counterGroupA++;
				if(counterGroupA == intervalGroupA)
					counterGroupA = 0;
				
				counterGroupB++;
				if(counterGroupB == intervalGroupB)
					counterGroupB = 0;
			}
			
			pm.makePersistentAll(users);
			
			p("Users were created.");
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			pm.close();
		}
		
		checkDateFieldIsUnique();
	}
	
	private static final String zeroes = "0000000000000000000000";
	
	private static String pad(int number) {
		String ns = Integer.toString(number);
		return zeroes.substring(zeroes.length() - ns.length());
	}
	
	private static String generateString(int size) {
		Random r = new Random();
		char c[] = new char[size];
		for(int i = 0; i < size; i++) {
			c[i] = (char) (r.nextInt('z' - 'a')  + 'a');
		}
		return new String(c);
	}

	public void createTestData() {
		
		generateTestUserRecords(50, 7, 5);
	}

	public void cleanTheDB() {

		PersistenceManager pm = factory.getPersistenceManager();
		try {
			p("Deleting all users in the DB.");
			
			List<User> users = new LinkedList<User>();
			
			Extent<User> extent = pm.getExtent(User.class);
			for(Iterator<User> it = extent.iterator(); it.hasNext(); ) {
				User user = it.next();
				users.add(user);
			}
			
			pm.deletePersistentAll(users);
			
			p(users.size() + " users deleted.");
		}
		finally {
			pm.close();
		}
	}
	
	public void checkDateFieldIsUnique() {

		PersistenceManager pm = factory.getPersistenceManager();
		try {
			p("Checking whether User.dateCreated is unique.");
			
			Set<Long> check = new HashSet<Long>();
			int nonUnique = 0;
			
			Extent<User> extent = pm.getExtent(User.class);
			for(Iterator<User> it = extent.iterator(); it.hasNext(); ) {
				User user = it.next();
				long dateCreated = user.getDateCreated().getTime();
				
				if(!check.add(dateCreated)) {
					nonUnique++;
				}
			}

			if(nonUnique > 0) {
				p("No, the dateCreated is not unique. (" + nonUnique + " duplicate values)");
			}
			else {
				p("Yes, the dateCreated is unique.");
			}
		}
		finally {
			pm.close();
		}

	}
	
	public void validateJdoQuerySyntax() {
		
		PersistenceManager pm = factory.getPersistenceManager();
		try {
			
			Map<String, Object> params = new LinkedHashMap<String, Object>();
			
			//String queryString = "SELECT FROM User ORDER BY ID ASC";
			
			String queryString = "select order by dateCreated";
			p("Trying: " + queryString);
			
			Query query = pm.newQuery(User.class, queryString);
			
			//query.setClass(User.class);
			
			List<User> users = (List<User>) query.executeWithMap(params);
			
			for(User user : users) {
				p("User: number(" + user.getRecordNumber() + ") " +
						"fname(" + user.getFirstName() + ") lname(" + user.getLastName() + ") " +
						"email(" + user.getEmail() + ") created(" + user.getDateCreated() + ")"); 
			}
		}
		finally {
			pm.close();
		}
	}
	
	public void displayPagedData() {
		
		//StringBuilder sb = new StringBuilder();

		PersistenceManager pm = factory.getPersistenceManager();
		try {
			p("Paging through all Users in the DB.");

			Map<String, Object> params = new LinkedHashMap<String, Object>();
			Map<String, Object> bookmark = new LinkedHashMap<String, Object>();

			//String query = "";
			//String query = "order by email asc";
			//String query = "order by email desc";
			
			String query = "where recordNumber > param1 parameters int param1 order by firstName asc, lastName desc";
			params.put("param1", 20);
			
			//String query = "";
			
			/*String query = "where recordNumber > param1 && recordNumber < param2 " +
					"parameters int param1, int param2";
					//" order by firstName asc, lastName desc";
			params.put("param1", 10);
			params.put("param2", 30);*/

			
			PageDirection direction = PageDirection.forward;

			p("Query: " + query + " " + params + "\nDirection: " + direction + "\n\n");
			

			int page = 1;
			while(true) {
				
				p("\nPage #" + page + ": -----------------------------------------\n");

				GaeQueryPager pager = new GaeQueryPager(
						pm, User.class,
						"ID", 
						9,
						direction,
						query, 
						params,
						bookmark);
				
				List<User> users = pager.performQueries();
				
				for(User user : users) {
					p("User: ID(" + user.getID() + ") " +
							"number(" + user.getRecordNumber() + ") " +
							"fname(" + user.getFirstName() + ") lname(" + user.getLastName() + ") " +
							"email(" + user.getEmail() + ") created(" + user.getDateCreated().getTime() + ")"); 
				}
				
				long count = pager.performCount();
				p("records: " + users.size() + "; all records: " + count
						+ "; hasNextPage: " + pager.hasNextPage()
						+ "\nbookmark: " + bookmark);
				
				if(users.isEmpty()) {
					p("no more records");
					break;
				}
				
				page++;
			}
			
		}
		finally {
			pm.close();
		}
	}

    public void p(Object o) {
    	System.out.println(String.valueOf(o));
    }

    public void e(Object o, Throwable t) {
    	System.err.println(String.valueOf(o));
    	t.printStackTrace();
    }
}
