import java.util.ArrayList;

/**
 * Created by zxzhang on 2017/6/15.
 */
public class TestCase {
    public ArrayList<Step> steps = new ArrayList<Step>();
    Har har;
    public TestcaseContext context = new TestcaseContext();
    public String ID;
    public String Name;
    public String Description;
    public ArrayList<Market> UnSupportMarkets;
    public ArrayList<Market> SupportMarkets;
    public String testEnvironment;
}
