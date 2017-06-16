import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class TestcaseFileParser {
    public static ArrayList<TestCase> testCases = new ArrayList<TestCase>();

    public static void main(String[] args) throws XPathExpressionException {
        parse("");
    }

    public static void parse(String fileName) throws XPathExpressionException {
        try {
            URL filePath = TestcaseFileParser.class.getResource("TestCase_IN_Domain-Others.xml");
            File fXmlFile = new File(filePath.getFile());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

            XPath xpath = XPathFactory.newInstance().newXPath();
            // XPath Query for showing all nodes value
            XPathExpression expr = xpath.compile("//Testcase[@ID='Facebook']");
            NodeList nList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nList.getLength(); i++) {
                TestCase testCase = new TestCase();
                NodeList stepList = (NodeList) xpath.compile("Step").evaluate(nList.item(i), XPathConstants.NODESET);
                for (int j = 0; j < stepList.getLength(); j++) {
                    Step step = new Step();
                    Node nNode = stepList.item(j);

                    System.out.println("\nCurrent Element :" + nNode.getNodeName());

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        step.id = eElement.getAttribute("ID");
                        step.action = eElement.getAttribute("Action");
                        step.url = eElement.getAttribute("URL");
                        step.xPath = eElement.getAttribute("XPATH");
                        step.value = eElement.getAttribute("Value");
                        String temp = eElement.getAttribute("DelayAfterAction");
                        if (temp.length() > 0) {
                            step.waitSecondsAfterAction = Integer.valueOf(temp);
                        } else {
                            step.waitSecondsAfterAction = 0;
                        }
                        step.unSupportMarkets = eElement.getAttribute("UnSupportMarkets");
                        step.supportMarkets = eElement.getAttribute("SupportMarkets");
                        testCase.steps.add(step);
                    }
                }
                testCases.add(testCase);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
