//package team18.java;

//Student name: jid18,spw20

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import oracle.sql.*;
import java.sql.DriverManager;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class team18 {
	private Connection connection;
	private String username, password;
	private Scanner input;
	
	private static final String HR = "--------------------------------------";
	
	public team18() {
		
			
			String username = "jid18";
			String password = "3885219";
			
			
			try {
				
				// register oracle driver and connect to db
				DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
				connection = DriverManager.getConnection("jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass", username, password);
				input = new Scanner(System.in);
				
				System.out.println("Welcome to MyAuction!");
				//go to main menu 
				promptMenu(0);
			} catch(SQLException e)  {
				System.err.println("Error connecting to database: " + e.toString());
			}
		
	}
	
	
	//main menu
	public void promptMenu(int menu) {
		
		// print choices
		List<String> choices = null;
		int choice = 0;
		switch(menu) {
			case 3:
				choices = Arrays.asList(
					"Highest volume leaf categories",
					"Highest volume root categories",
					"Most active bidders",
					"Most active buyers",
					"Return to admin menu"
				);
				choice = getUserChoice("Administrator statistics", choices);
				break;
			case 2:
				choices = Arrays.asList(
					"New customer registration",
					"Update system date",
					"Product statistics",
					"Statistics",
					"Logout"
				);
				choice = getUserChoice("Administrator menu", choices);
				break;
			case 1:
				choices = Arrays.asList(
					"Browse products",
					"Search products",
					"Auction product",
					"Bid on product",
					"Sell product",
					"Show suggestions",
					"Logout"
				);
				choice = getUserChoice("User menu", choices);
				break;
			default:
				choices = Arrays.asList(
					"Administrator login",
					"User login",
					"Exit"
				);
				choice = getUserChoice("Main menu", choices);
				break;
		}
		
		// handle user's choice
		System.out.println("\n" + choices.get(choice - 1));
		if (menu == 3) {
			// deal with admin statistics menu
			int months = 0, limit = 0;
			if (choice < 5) {
				months = getUserNumericInput("Number of previous months to include");
				limit = getUserNumericInput("Please enter a limit");
			}
			switch(choice) {
				case 1:
					// Highest volume leaf categories
					topLeafCategories(months, limit);
					promptMenu(3);
					break;
				case 2:
					// Highest volume root categories
					topRootCategories(months, limit);
					promptMenu(3);
					break;
				case 3:
					// Most active bidders
					topActiveBidders(months, limit);
					promptMenu(3);
					break;
				case 4:
					// Most active buyers
					topActiveBuyers(months, limit);
					promptMenu(3);
					break;
				default:
					promptMenu(2);
					break;
			}
		} else if (menu == 2) {
			// deal with admin menu choices
			switch(choice) {
				case 1:
					// New customer registration
					registerCustomer();
					promptMenu(2);
					break;
				case 2:
					// Update system date
					updateDate();
					promptMenu(2);
					break;
				case 3:
					// Product statistics
					String customer = getUserInput("Enter customer username or leave blank to display all products", -1, false);
					productStatistics(customer);
					promptMenu(2);
					break;
				case 4:
					// Statistics
					promptMenu(3);
					break;
				default:
					promptMenu(0);
					break;
			}
		} else if (menu == 1) {
			// deal with user menu choices
			switch(choice) {
				case 1:
					// Browse products
					browse();
					promptMenu(1);
					break;
				case 2:
					// Search products
					System.out.println("You can search for up to two keywords.");
					String[] keywords = getUserInput("Please enter your keywords separated by a space").split("\\s");
					String second;
					if (keywords.length == 1) {
						second = null;
					} else {
						second = keywords[1];
						if (keywords.length > 2) {
							System.out.println("Sorry, you can't search for more than two keywords, but here are the results for '" + keywords[0] + "' and '" + keywords[1] + "':");
						}
					}
					search(keywords[0], second);
					promptMenu(1);
					break;
				case 3:
					// Auction product
					System.out.println("Please enter the following product information:");
					String name = getUserInput("Product name", 20);
					String description = getUserInput("Description (optional)", 30, false);
					
					// add valid categories
					String temp = getUserInput("Categories (separated by a comma)", -1);
					temp = temp.substring(0, 1).toUpperCase() + temp.substring(1) ;
					List<String> cats = Arrays.asList(temp.trim().split("\\s*,\\s*"));
					List<String> categories = new ArrayList<String>(cats);
					List<String> errors = new ArrayList<String>();
					for (String cat : cats) {
					    if (!isLeafCategory(cat)) {
					    	categories.remove(cat);
					    	errors.add(cat);
					    }
					}
					int errorCount = errors.size();
					if (errorCount == 1) {
						System.out.println("Sorry, the category '" + errors.get(0) + "' is invalid");
					} else if (errorCount > 1) {
						System.out.println("Sorry, the " + errorCount + " categories (" + formatList(errors, '\0', ", ") + ") are invalid.");
					}
					
					int days = getUserNumericInput("Number of days for auction");
					int price = getUserNumericInput("Minimum starting price (optional)", false); // default is 0 
					
					int id = auctionProduct(name, description, categories.toArray(), days, price);
					System.out.println("\nAn auction for '" + name + "' has been created with Auction ID = " + id + "!");
					
					promptMenu(1);
					break;
				case 4:
					// Bid on product
					System.out.println("\nPlease provide the following bid information:");
					int a_id = getUserNumericInput("Auction ID");
					int bid = getUserNumericInput("Bid Amount");
					placeBid(a_id, bid);
					promptMenu(1);
					break;
				case 5:
					// Sell product
					try {
						ResultSet myProducts = query("select auction_id, name from product where seller = '" + username + "' and status = 'closed'");
						if (myProducts != null) {
							List<Integer> ids = new ArrayList<Integer>();
							List<String> products = new ArrayList<String>();
							while (myProducts.next()) {
								ids.add(myProducts.getInt(1));
								products.add(myProducts.getString(2) + " (#" + myProducts.getInt(1) + ")");
							}
							int userChoice = getUserChoice("Your closed auctions", products, "Please enter the product you would like to sell");
							sellProduct(ids.get(userChoice - 1));
						} else {
							System.out.println("You currently do not have any closed auctions.");
						}
					} catch (SQLException e) {
						handleSQLException(e);
					}
					promptMenu(1);
					break;
				case 6:
					// Show suggestions
					suggest();
					promptMenu(1);
					break;
				default:
					promptMenu(0);
					break;
			}
		} else {
			// deal with main menu choices
			switch(choice) {
				case 1:
					// administrator login
					if (login(1)) {
						System.out.println("\nWelcome, " + username + "!");
						promptMenu(2);
					} else {
						System.out.println("\nError! Invalid username/password!");
						promptMenu(0);
					}
					break;
				case 2:	
					// user login
					if (login(2)) {
						System.out.println("\nWelcome, " + username + "!");
						promptMenu(1);
					} else {
						System.out.println("\nError! Invalid username/password!");
						promptMenu(0);
					}
					break;
				default:
					System.out.println("Goodbye!");
					break;
			}
		}
	}
	
	

	//User interface method 
	//======================================================================================================================================================
	//======================================================================================================================================================
	//======================================================================================================================================================
	//======================================================================================================================================================
	

	/*
	 * Browse through products of a specific category
	 */
	public void browse() {
		try {
			// traverse through hierarchical categories
			List<String> cats = null;
			String chosenCat = null;	//parent category 
			int choice = 0;
			do {
				cats = getCategories(chosenCat);
				// get user choice and set new parent category
				if (cats != null) {
					String title = (chosenCat == null ? "Categories" : chosenCat);
					choice = getUserChoice(title, cats, "Which category would you like to browse?");
					chosenCat = cats.get(choice - 1);
				}
			} while(cats != null);
			
			// After reach the leaf category, prompt user for sort method
			int sort = getUserChoice("\nHow do you want your products sorted by?", Arrays.asList(
				"Highest bid first",
				"Lowest bid first",
				"Alphabetically by product name"
			), "Choose a sort option");
			
			// construct query string
			String query = "select auction_id, name, description, amount from product where status = 'underauction' and auction_id in (select auction_id from belongsto where category = '" + chosenCat + "') order by ";
			if (sort == 1) {
				query += "amount desc";
			} else if (sort == 2) {
				query += "amount asc";
			} else {
				query += "name asc";
			}
			
			// run query
			ResultSet products = query(query);
			
			if (products != null) {
				// print table heading
				String[] titles = {"id", "name", "description", "highest bid"};
				int[] widths = {5, 20, 30, 10};
				System.out.println(createTableHeading(titles, widths));
				
				// print results
				while (products.next()) {
					System.out.printf("%5d %-20s %-30s %10d\n", products.getInt(1), products.getString(2), products.getString(3), products.getInt(4));
				}
			} else {
				System.out.println("\nNo products found.");
			}
		} catch(SQLException e) {
			handleSQLException(e);
		}
	}
	



	/*
	 * @param input1 first keyword
	 * @param input2 second optional keyword
	 */
	public void search(String input1, String input2) {
		try {
			String temp = "";
			if (input2 != null) {
				temp = " and upper(description) like upper('%" + input2 + "%')";
			}
			ResultSet resultSet = query("select auction_id, name, description from product where upper(description) like upper('%" + input1 + "%')" + temp);
			
			if (resultSet != null) {
				// print table heading
				String[] titles = {"id", "name", "description"};
				int[] widths = {5, 20, 30};
				System.out.println(createTableHeading(titles, widths));
				
				// print results
				while(resultSet.next()) {
					System.out.printf("%5d %-20s %-30s\n", resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3));
				}	
			} else {
				System.out.println("\nNo products found.");
			}
		} catch(SQLException e) {
			handleSQLException(e);
		}
	}
	
	

	
	/*
	 * @param name name of product
	 * @param description product description
	 * @param categories array of categories
	 * @param days number of days of auction
	 * @param price minimum price of product
	 * @return auction id of newly created product, or -1 on error
	 */
	public int auctionProduct(String name, String description, Object[] categories, int days, int price) {
		try {
			CallableStatement cs = connection.prepareCall("begin put_product(?, ?, ?, ?, ?, ?, ?); end;");
			cs.registerOutParameter(7, Types.INTEGER);
			cs.setString(1, name);
			cs.setString(2, description);
			
			// create an array of valid categories
			ArrayDescriptor desc = ArrayDescriptor.createDescriptor("VCARRAY", connection);
			cs.setArray(3, new ARRAY(desc, connection, categories));
			
			cs.setInt(4, days);
			cs.setString(5, username);
			cs.setInt(6, price);
			cs.execute();
			
			return cs.getInt(7);
		} catch (SQLException e) {
			handleSQLException(e);
			return -1;
		}
	}
	
	
	/*
	 * Places a bid on a particular product if the bid amount is valid
	 * @param a_id auction id
	 * @param bid bid amount
	 */
	public void placeBid(int a_id, int bid) {
		try {
			// turn of auto commit and lock table for inserts
			connection.setAutoCommit(false);
			Statement locking = connection.createStatement();
			locking.execute("lock table bidlog in share row exclusive mode");
			
			// check if our bid is valid
			CallableStatement cs = connection.prepareCall("{? = call validate_bid(?, ?)}") ;
			cs.registerOutParameter(1, Types.INTEGER) ;
			cs.setInt(2, a_id) ;
			cs.setInt(3, bid) ;
			cs.execute();
			int output = cs.getInt(1) ;
			
			// place bid
			if (output == 1) {
				PreparedStatement s = getPreparedQuery("insert into bidlog values(1, ?, ?, (select c_date from ourSysDate), ?)");
				s.setInt(1, a_id);
				s.setString(2, username);
				s.setInt(3, bid);
				s.executeQuery();
				System.out.println("\nBid successful!") ;
			} else {
				System.out.println("\nError: Bid is invalid.") ;
			}
			
			// commit inserts and unlock table
			connection.commit();
			connection.setAutoCommit(true) ;
		} catch (SQLException e) {
			handleSQLException(e);
		}
	}
	
	
	/*
	 * Sells or withdraws a closed product given that there were bids on it.
	 * @param auctionID auction ID for product
	 */
	public void sellProduct(int auctionID) {
		try {
			//ResultSet resultSet = null, resultSet2 = null, resultSet3 = null ;
			ResultSet result = query("select count(bidsn) as bids from bidlog where auction_id = " + auctionID);
			result.next();
			int bids = result.getInt(1);
			if (bids == 0) {
				// withdraw auction if there are no bids
				query("update product set status = 'withdrawn' where auction_id = " + auctionID);
				System.out.println("\nSorry, no bids were placed on your product with Auction ID of " + auctionID + ". This auction has now been withdrawn.");
			} else {
				// get second highest bidding price (or highest if only one bidder)
				int num = (bids == 1 ? 1 : 2);
				result = query("select amount from (select amount, rownum as rn from (select amount from bidlog where auction_id = " + auctionID + " " +
					"order by bid_time desc) where rownum <= 2) where rn = " + num);
				result.next();
				int price = result.getInt(1);
				
				int answer = getUserChoice("Do you want to sell your product for $" + price + "?", Arrays.asList(
					"Sell product",
					"Withdraw product"
				), "Choose an option");
				
				if (answer == 1) {
					// sell product
					query("update product set status = 'sold', buyer = (select * from (select bidder from bidlog where auction_id = " + auctionID + " " +
						"order by bid_time desc) where rownum <= 1), sell_date = (select c_date from ourSysDate), amount = " + price + " where auction_id = " + auctionID);
					System.out.println("\nSold product " + auctionID + " for $" + price + "!");
				} else {
					// withdraw
					query("update product set status = 'withdrawn' where auction_id = " + auctionID);
					System.out.println("\nWithdrew product " + auctionID + ".");
				}
			}
		} catch(SQLException e) {
			handleSQLException(e);
		}
	}
	
	
	/*
	 * Prints a list of suggested products to the user if any exist.
	 */
	public void suggest() {
		try {
/*
			ResultSet suggestions = query("select product.auction_id, product.name, product.description, product.amount from (" +
				"select friends.bidder, bids.auction_id from (select distinct bidder from bidlog b1 where not exists (" +
				"select distinct auction_id from bidlog b2 where bidder = 'user0' and not exists (select distinct bidder, auction_id " +
				"from bidlog b3 where b1.bidder = b3.bidder and b2.auction_id = b3.auction_id)) and bidder <> 'user0' and (" +
				"select count(auction_id) from bidlog where bidder = 'user0') > 0) friends join bidlog bids on friends.bidder = bids.bidder " +
				"join product p on bids.auction_id = p.auction_id where bids.auction_id not in (select distinct auction_id from bidlog " +
				"where bidder = 'user0') and p.status = 'underauction') t1 join product on t1.auction_id = product.auction_id " +
				"group by product.auction_id, product.name, product.description, product.amount order by count(bidder) desc");
*/


			String tmpQuery = 	"select distinct product.auction_id as suggested_auction, product.name, product.description, product.amount, count(distinct bidlog.bidder) as num_bid_friends "+
							  	"from product join bidlog" +
							  	"on product.auction_id = bidlog.auction_id" +
							  	"where status = 'underauction' and bidder in (" +
							  		"select distinct bidder"+ 
							  		"from bidlog bl1" +
							  		"where not exists (" +
							  			"select distinct auction_id" +
							  			"where bidder =" + username + "and not exists (" +
							  				"select distinct bidder" +
							  				"from bidlog bl2" +
							  				"where bl1.bidder = bl2.bidder and bl2.auction_id = cust_bidlog.auction_id" +
							  			")" +
									")" +
								") and product.auction_id not in (" +
									"select distinct auction_id" +
									"from bidlog" +
									"where bidder =" + username +
								")" +
								"group by product.auction_id, product.name, product.description, product.amount" +
								"order by count(distinct bidlog.bidder) desc";

			ResultSet suggestions = query(tmpQuery); 	


			if(suggestions != null) {
				// print table heading
				String[] titles = {"id", "name", "description", "highest bid", "num_bid_friends"};
				int[] widths = {5, 20, 30, 10,10};
				System.out.println(createTableHeading(titles, widths));
				
				// print results
				while (suggestions.next()) {
					System.out.printf("%5d %-20s %-30s %10d %10d\n", suggestions.getInt(1), suggestions.getString(2), suggestions.getString(3), suggestions.getInt(4), suggestions.getInt(5));
				}
			} else {
				System.out.println("No suggestions found.");
			}
		} catch(SQLException e) {
			handleSQLException(e);
		}
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	/*
	 * Registers an admin or customer.
	 */
	public void registerCustomer() {
		String name, address, email, login, password, admin ;
		System.out.println("Please provide the following information for the new user:");
		name = getUserInput("Name", 10) ;
		address = getUserInput("Address", 30) ;
		email = getUserInput("Email Address", 20) ;
		
		// make sure username doesn't already exist
		login = "";
		PreparedStatement statement = getPreparedQuery("select count(login) from customer where login = ?");
		ResultSet result = null;
		boolean firstAttempt = true;
		String prompt = "Username";
		try {
			do {
				if (!firstAttempt) {
					prompt = "Username already exists! Please enter another";
				}
				
				login = getUserInput(prompt, 10);
				result = query(statement, login);
				// sanity check (result should always be returning a count)
				if (result != null) {
					result.next();
				}
				
				firstAttempt = false;
			} while(result.getInt(1) > 0);
		} catch (SQLException e) {
			handleSQLException(e);
		}
		
		password = getUserInput("Password", 10) ;
		admin = getUserInput("Is this user an admin? (yes/no)").toLowerCase();
		
		// insert user into database
		String type = (admin.equals("yes") ? "administrator" : "customer");
		statement = getPreparedQuery("insert into " + type + " values (?, ?, ?, ?, ?)");
		result = query(statement, Arrays.asList(login, password, name, address, email));
		System.out.println("\nUser successfully added!\n") ;
	}
	
	
	/*
	 * Update our system date.
	 */
	public void updateDate() {
		String date;
		boolean exit = false ;
		do {
			date = getUserInput("Please enter a date or 'exit' to exit (must match dd-mm-yyyy/hh:mi:ssam)").toUpperCase();
			if(date.equals("EXIT")) {
				exit = true ;
				break ;
			}
		} while(!isDateValid(date));
		
		if(!exit) {
			queryUpdate(getPreparedQuery("update ourSysDate set c_date = to_date(?, 'dd-mm-yyyy/hh:mi:ssam')"), date);
			System.out.println("\nDate successfully changed!\n") ;
		}
	}
	
	
	/*
	 * Check validity of date string
	 * @param date date string
	 * @param format date format
	 * @return validity of date string
	 */
	private static boolean isDateValid(String date, String format) {
        try {
            DateFormat df = new SimpleDateFormat(format);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
	}
	
	
	/*
	 * Prints a summary table of product statistics
	 * @param customer option seller filter
	 */
	public void productStatistics(String customer) {
		try {
			String query = "select name, status, amount as highest_bid, login, seller from (" +
				"select p.name, p.status, p.amount, b.bidder as login, p.seller from product p left join bidlog b on p.auction_id = b.auction_id and p.amount = b.amount where p.status = 'underauction' " +
				"union select name, status, amount, buyer as login, seller from product where status = 'sold')";
			ResultSet result;
			if (customer.isEmpty()) {
				result = query(query);
			} else {
				query += " where seller = ?";
				PreparedStatement s = getPreparedQuery(query);
				s.setString(1, customer);
				result = s.executeQuery();
			}
			
			// print out results
			String[] titles = {"Name", "Status", "Highest Bid", "Bidder/Buyer"};
			int[] widths = {20, 20, 15, 15};
			System.out.println(createTableHeading(titles, widths));
			while(result.next()) {
				String bidder = result.getString(4);
				if (result.wasNull()) {
					bidder = ""; // just leave bidder blank if there were no bids
				}
				System.out.printf("%-20s %-20s %15d %-15s\n", result.getString(1), result.getString(2), result.getInt(3), bidder);
			}
		} catch (SQLException e) {
			handleSQLException(e);
		}
	}
	
	
	
	/*
	 * @param months the number of months to include in query
	 * @param k number of categories to print
	 * @return the top k highest volume leaf categories
	 */
	public void topLeafCategories(int months, int k) {
		try {
			PreparedStatement s = getPreparedQuery("select c1.name, product_count(?, c1.name) as count from category c1 " +
				"where not exists (select name from category c2 where c2.parent_category = c1.name) and product_count(?, c1.name) > 0 " +
				"order by product_count(?, c1.name) desc");
			s.setInt(1, months);
			s.setInt(2, months);
			s.setInt(3, months);
			
			ResultSet resultSet = s.executeQuery();
			
			if (resultSet != null) {
				// print table heading
				String[] titles = {"count", "category"};
				int[] widths = {5, 20};
				System.out.println(createTableHeading(titles, widths));
				
				// print results
				while(resultSet.next()) {
					System.out.printf("%5d %-20s\n", resultSet.getInt(2), resultSet.getString(1));
				}
			} else {
				System.out.println("Sorry, no products are categorized.");
			}
		} catch (SQLException e) {
			handleSQLException(e);
		}
	}
	
	
	
	
	/*
	 * @param months the number of months to include in query
	 * @param k number of categories to print
	 * @return the top k highest volume root categories
	 */
	public void topRootCategories(int months, int k) {
		
		try {
			Map<String, Integer> map = new HashMap<String, Integer>();
			List<String> rootCats = getCategories(null);
			
			for (String root : rootCats) {
				List<String> underRoot = new ArrayList<String>(Arrays.asList(root));
				underRoot.addAll(getChildCategories(root));
				
				System.out.println(formatList(underRoot, '\'', ", "));

				// count products sold under root category
				PreparedStatement s = getPreparedQuery("select count(auction_id) from (" +
					"select distinct p.auction_id from product p join belongsto b on p.auction_id = b.auction_id " +
					"where p.status = 'sold' and p.sell_date >= add_months((select c_date from ourSysDate), -1 * ?) " +
					"and b.category in (" + formatList(underRoot, '\'', ", ") + "))");
				s.setInt(1, months);
				ResultSet result = s.executeQuery();
				result.next();
				map.put(root, result.getInt(1));
			}
			
			if (!map.isEmpty()) {
				// sort map
				ValueComparator vc = new ValueComparator(map);
				Map<String, Integer> sorted = new TreeMap<String, Integer>(vc);
				sorted.putAll(map);
				
				// print table heading
				String[] titles = {"count", "category"};
				int[] widths = {5, 20};
				System.out.println(createTableHeading(titles, widths));
				
				// print results
				int count = 0;
				for (Map.Entry<String, Integer> cat : sorted.entrySet()) {
					if (cat.getValue() == 0) break;
					System.out.printf("%5d %-20s\n", cat.getValue(), cat.getKey());
					count++;
					if (count == k) break;
				}
			} else {
				System.out.println("Sorry, no products are categorized.");
			}
		} catch (SQLException e) {
			handleSQLException(e);
		}
	}
	
	
	
	
	/*
	 * @param parent parent category or null for root categories
	 * @return list of categories with specified parent
	 */
	public List<String> getCategories(String parent) {
		ResultSet r;
		if (parent == null) {
			r = query("select name from category where parent_category is null");
		} else {
			r = query("select name from category where parent_category = '" + parent + "'");
		}
		
		List<String> cats = new ArrayList<String>();
		if (r != null) {
			try {
				while (r.next()) {
					cats.add(r.getString(1));
				}
				return cats;
			} catch (SQLException e) {
				handleSQLException(e);
				return null;
			}
		} else {
			// return null if result set is empty
			return null;
		}
	}
	
	/*
	 * @return list of leaf categories of parent
	 */
	public List<String> getLeafCategories() {
		return getLeafCategories(null);
	}
	
	/*
	 * @param parent parent category or null for root categories
	 * @return list of leaf categories of parent
	 */
	private List<String> getLeafCategories(String parent) {
		List<String> children = getCategories(parent);
		if (children == null) {
			return Arrays.asList(parent);
		} else {
			List<String> leaves = new ArrayList<String>();
			for (String child : children) {
				leaves.addAll(getLeafCategories(child));
			}
			return leaves;
		}
	}
	
	
	
	/*
	 * @param parent parent category
	 * @return list of all categories under a parent node
	 */
	public List<String> getChildCategories(String parent) {
		List<String> children = getCategories(parent);
		if (children == null) {
			return Arrays.asList(parent);
		} else {
			List<String> result = new ArrayList<String>();
			for (String cat : children) {
				result.addAll(getChildCategories(cat));
			}
			return result;
		}
	}
	
	/*
	 * @param list list of strings
	 * @param del delimiter that will separated formated list
	 * @return formated list of strings
	 */
	public String formatList(List<String> list, char surround, String del) {
		String result = "";
		for (int i = 0; i < list.size(); i++) {
			result += surround + list.get(i) + surround;
			if (i < list.size() - 1) {
				result += del;
			}
		}
		return result;
	}
	
	
	
	/*
	 * Class used to sort results of various statistic queries. Sorts
	 * descending and uses keys as a tiebraker.
	 */
	private static class ValueComparator implements Comparator<String> {
        private Map<String, Integer> base;
        
        ValueComparator(Map<String, Integer> base) {
            this.base = base;
        }
        
        public int compare(String a, String b) {
            Integer x = base.get(a);
            Integer y = base.get(b);
            if (x.equals(y)) {
                return a.compareTo(b);
            }
            return y.compareTo(x);
        }
    }
	
	
	/*
	 * @param months the number of months to include in query
	 * @param k number of bidders to print
	 * @return the k most active bidders
	 */
	public void topActiveBidders(int months, int k) {
		try {
			PreparedStatement s = getPreparedQuery("select * from (" +
				"select login, bid_count(login, ?) as amount from customer where bid_count(login, ?) > 0 order by amount desc) where rownum <= ?");
			s.setInt(1, months);
			s.setInt(2, months);
			s.setInt(3, k);
			ResultSet bidders = s.executeQuery();
			
			// print out results
			System.out.println(HR);
			while (bidders.next()) {
				System.out.println(bidders.getInt(2) + "\t" + bidders.getString(1));
			}
		} catch (SQLException e) {
			handleSQLException(e);
		}
	}
	
	
	
	/*
	 * @param months the number of months to include in query
	 * @param k number of bidders to print
	 * @return the k most active bidders
	 */
	public void topActiveBuyers(int months, int k) {
		try {
			PreparedStatement s = getPreparedQuery("select * from ( select login, buying_amount(login, ?) as amount" +
				" from customer where buying_amount(login, ?) is not null order by amount desc) where rownum <= ?");
			s.setInt(1, months);
			s.setInt(2, months);
			s.setInt(3, k);
			ResultSet buyers = s.executeQuery();
			
			// print out results
			System.out.println(HR);
			while (buyers.next()) {
				System.out.println(buyers.getInt(2) + "\t" + buyers.getString(1));
			}
		} catch (SQLException e) {
			handleSQLException(e);
		}
	}
	
	
	
	/*
	 * Check validity of date string on default format
	 * @param date date string
	 * @return validity of date string
	 */
	private static boolean isDateValid(String date) {
		return isDateValid(date, "dd-MM-yyyy/hh:mm:ssa");
	}
	
	
	/*
	 * @param e exception
	 */
	public void handleSQLException(Exception e) {
		System.err.println("Error running database query: " + e.toString());
		e.printStackTrace();
		System.exit(1);
	}
	
	
	
	
	public String createTableHeading(String[] titles, int[] widths) {
		String result = "\n";
		for (int i = 0; i < titles.length; i++) {
			result += String.format("%-" + widths[i] + "s ", titles[i].toUpperCase());
		}
		result += "\n";
	
		// construct hr string
		for (int i = 0; i < widths.length; i++) {
			for (int j = 0; j < widths[i]; j++) {
				result += '-';
			}
			result += ' ';
		}
		
		return result;
	}









/*
	 *	Check whether the login information is valid 
	 *	@param login type(2 represent customer, 1 represent administrator)
	 *	@return true or false 
	 */
	public boolean login(int type) {
		try {

			Console console = System.console();
			System.out.println("\nPlease enter your login information.");
			username = getUserInput("Username");
			//password = new String(console.readPassword("Password: "));
			password = getUserInput("Password");
			//checking to make sure the usn/pwd match something in the database
			ResultSet resultSet;
			if(type == 2) //Which database to check for the login info
				resultSet = query("select login, password from customer");
			else
				resultSet = query("select login, password from administrator");
			while(resultSet.next())
			{
				if(username.equals(resultSet.getString(1)) && password.equals(resultSet.getString(2)))	//password/username combination is found
					return true ; 
			}
			
			return false ; //If there was no match for the username/password, return false

		} catch(SQLException e) {
			handleSQLException(e);
			return false;
		}
	}
	
	
	/*
	 * @param query SQL select query
	 * @return result set of query or null if empty
	 */
	public ResultSet query(String query) {
		try {
			Statement s = connection.createStatement();
			ResultSet result = s.executeQuery(query);
			// check to see if result is empty
			if (result.isBeforeFirst()) {
				return result;
			} else {
				return null;
			}
		} catch (SQLException e) {
			handleSQLException(e);
			return null;
		}
	}
	
	
	/*
	 * @param query SQL select query
	 * @param parameters list of string parameters to replace in query
	 * @return result set of query
	 */
	public ResultSet query(PreparedStatement ps, List<String> parameters) {
		try {
			for (int i = 1; i <= parameters.size(); i++) {
				ps.setString(i, parameters.get(i - 1));
			}
			ResultSet result = ps.executeQuery();
			
			// check to see if result is empty
			if (result.isBeforeFirst()) {
				return result;
			} else {
				return null;
			}
		} catch (SQLException e) {
			handleSQLException(e);
			return null;
		}
	}
	
	/*
	 * @param query SQL select query
	 * @param str one parameter to replace in query
	 * @return result set of query
	 */
	public ResultSet query(PreparedStatement ps, String str) {
		return query(ps, Arrays.asList(str));
	}
	
	/*
	 * @param query SQL update query
	 * @param parameters list of string parameters to replace in query
	 * @return result of update
	 */
	public int queryUpdate(PreparedStatement ps, List<String> parameters) {
		try {
			for (int i = 1; i <= parameters.size(); i++) {
				ps.setString(i, parameters.get(i - 1));
			}
			return ps.executeUpdate();
		} catch (SQLException e) {
			handleSQLException(e);
			return -1;
		}
	}
	
	/*
	 * @param query SQL update query
	 * @param str one parameter to replace in query
	 * @return result of update
	 */
	public int queryUpdate(PreparedStatement ps, String str) {
		return queryUpdate(ps, Arrays.asList(str));
	}
	
	
	
	/*
	 * @param query SQL query with some question marks
	 * @return prepared statement ready for input
	 */
	public PreparedStatement getPreparedQuery(String query) {
		try {
			return connection.prepareStatement(query);
		} catch (SQLException e) {
			handleSQLException(e);
			return null;
		}
	}
	
	/*
	 * @param prompt descriptive prompt of input
	 * @return numeric input
	 */
	public int getUserNumericInput(String prompt) {
		while (true) {
			try {
				return Integer.parseInt(getUserInput(prompt, -1));
			} catch (NumberFormatException e) {
				continue;
			}	
		}
	}
	
	/*
	 * @param prompt descriptive prompt of input
	 * @param whether or not input is required
	 * @return numeric input
	 */
	public int getUserNumericInput(String prompt, boolean required) {
		while (true) {
			try {
				String input = getUserInput(prompt, -1, required);
				if (!required && input.isEmpty()) {
					return 0;
				} else {
					return Integer.parseInt(input);
				}
			} catch (NumberFormatException e) {
				continue;
			}	
		}
	}
	
	/*
	 * @param prompt descriptive prompt of input
	 * @return trimmed line of required user input
	 */
	public String getUserInput(String prompt) {
		return getUserInput(prompt, -1, true);
	}
	
	/*
	 * @param prompt descriptive prompt of input
	 * @param length maximum length of input
	 * @return trimmed line of required user input
	 */
	public String getUserInput(String prompt, int length) {
		return getUserInput(prompt, length, true);
	}
	
	/*
	 * @param prompt descriptive prompt of input
	 * @param required whether input is optional or required
	 * @return trimmed line of optional or required user input
	 */
	public String getUserInput(String prompt, int length, boolean required) {
		boolean lengthCheck;
		String str;
		do {
			System.out.print(prompt + ": ");
			str = input.nextLine().trim();
			
			// check length if necessary
			if (length > 0 && str.length() > length) {
				System.out.println("Sorry, input can only be " + length + " characters (yours was " + str.length() + ").");
				lengthCheck = true;
			} else {
				lengthCheck = false;
			}
		} while(lengthCheck || required && str.isEmpty());
		return str;
	}
	
	/*
	 * Prints a formatted list of choices and prompts the user for input
	 * @param title title of choices
	 * @param choices list of choices
	 * @param prompt descriptive prompt for user input
	 * @return user's choice
	 */
	public int getUserChoice(String title, List<String> choices, String prompt) {
		// print choices
		System.out.println("\n" + title + "\n" + HR);
		for (int i = 1; i <= choices.size(); i++) {
			System.out.println("  " + i + ") " + choices.get(i - 1));
		}
		System.out.println(HR);
		
		// prompt user for input
		int choice;
		do {
			choice = getUserNumericInput(prompt);
		} while (choice <= 0 || choice > choices.size());
		return choice;
	}
	
	/*
	 * Prints a formatted list of choices and prompts the user for input
	 * with a default descriptive prompt.
	 * @param title title of choices
	 * @param choices list of choices
	 * @return user's choice
	 */
	public int getUserChoice(String title, List<String> choices) {
		return getUserChoice(title, choices, "Choose a menu item");
	}


	/*
	 * @param category name of category
	 * @return whether or not category exists
	 */
	public boolean isLeafCategory(String category) {
		try {
			category = category.substring(0, 1).toUpperCase() + category.substring(1) ;
			PreparedStatement s = getPreparedQuery("select count(name) from category where name = ? or parent_category = ?");
			ResultSet r = query(s, Arrays.asList(category, category));
			r.next();
			
			if (r.getInt(1) == 1) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			handleSQLException(e);
			return false;
		}
	}


	public static void main(String args[]) {
		team18 test = new team18();
	}
}
