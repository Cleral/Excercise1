package ndm.service;

import static org.junit.Assert.*;

import org.junit.Test;

@RunWith(SpringJUnit4ClassRunner.class)//基于JUnit4的Spring测试框架
@ContextConfiguration(locations={"/applicationContext.xml"})//启动Spring容器
public class TestManageTimeService {
	@Autowired
	private ManageTimeService manageTimeService;
	
	@Test
	public void getResultSet() {
		boolean b=manageTimeService.getResultSet();
		assertTrue(b);
	}

}
