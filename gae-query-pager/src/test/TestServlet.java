package test;

import java.io.IOException;
import javax.servlet.http.*;

import com.insose.gae.pager.GaeQueryPager;

@SuppressWarnings("serial")
public class TestServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		String func = req.getParameter("func");
		
		if("create-data".equals(func)) {
			new GaeQueryPagerTests().createTestData();
		}
		else if("check-unique".equals(func)) {
			new GaeQueryPagerTests().checkDateFieldIsUnique();
		}
		else if("test-paging".equals(func)) {
			new GaeQueryPagerTests().displayPagedData();
		}
		else if("clean-data".equals(func)) {
			new GaeQueryPagerTests().cleanTheDB();
		}
		
		resp.setContentType("text/plain");
		resp.getWriter().println("done.");
	}
	
	public static void main(String args[])
			throws Throwable {
		
		GaeQueryPager.basicTest();
	}
}
