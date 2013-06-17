package gizmoe.TaskDagResolver;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import gizmoe.taskdag.*;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ResolveDag {

	/**
	 * @param args
	 */
	static MyDag taskdag = new MyDag();
	static HashMap<Integer, Capability> idMap = new HashMap<Integer, Capability>();
	static HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> resolveMap = new HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>();
	private static int capID = 0;
	public static void main(String[] args) {
		try {

			File file = new File("/Users/upsham/NewCombo.xml");

			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();

			Document doc = dBuilder.parse(file);

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			if (doc.hasChildNodes()) {

				//printNote(doc.getChildNodes());
				//xpathsearch();
				resolve("/Users/upsham/NewCombo.xml");//Map the overall outputs/inputs using hashmap returned
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	private static void xpathsearch(){
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false); // never forget this!
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.parse("/Users/upsham/NewCombo.xml");

			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xpath = xPathFactory.newXPath();

			XPathExpression xPathExpression = xpath.compile("//mapping");

			NodeList nodes = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);

			System.out.println(nodes.getLength());
			for (int i = 0; i < nodes.getLength(); i++) {
				System.out.println(nodes.item(i).getAttributes().getNamedItem("mode").getNodeValue()); 
				NodeList mappingchildren = nodes.item(i).getChildNodes();
				for(int j = 0; j< mappingchildren.getLength(); j++){
					if(mappingchildren.item(j).getNodeType() == Node.ELEMENT_NODE){
						//System.out.println(mappingchildren.item(j).getNodeValue());
						if(mappingchildren.item(j).getAttributes().getNamedItem("id")!=null){
							System.out.println(mappingchildren.item(j).getAttributes().getNamedItem("id").getNodeValue());
							
						}else if (mappingchildren.item(j).getAttributes().getNamedItem("string")!=null){
							System.out.println(mappingchildren.item(j).getAttributes().getNamedItem("string").getNodeValue());
						}else{
							System.out.println(mappingchildren.item(j).getAttributes().getNamedItem("ref").getNodeValue());
							System.out.println(mappingchildren.item(j).getAttributes().getNamedItem("name").getNodeValue());
						}
						//System.out.println(mappingchildren.item(j).getAttributes().getNamedItem("ref").getNodeValue());
						//System.out.println(mappingchildren.item(j).getAttributes().getNamedItem("name").getNodeValue());
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static HashMap<Integer, ArrayList<Integer>> resolve(String filename){
		HashMap<Integer, ArrayList<Integer>> mappedID = new HashMap<Integer, ArrayList<Integer>>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false); // never forget this!
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.parse(filename);

			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xpath = xPathFactory.newXPath();
			
			XPathExpression xPathExpression = xpath.compile("//step");
			NodeList steps = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);
			for(int i = 0; i< steps.getLength(); i++){
				String name = steps.item(i).getAttributes().getNamedItem("name").getNodeValue();
				int id = Integer.parseInt(steps.item(i).getAttributes().getNamedItem("id").getNodeValue());
				
				//CapTaskID captask = new CapTaskID(steps.item(i).getAttributes().getNamedItem("name").getNodeValue(), steps.item(i).getAttributes().getNamedItem("id").getNodeValue());
				//idMap.put(id, name);
				/*if(!isCapability(name)){
					//TODO
				}*/
				if(isCapability(name)){
					idMap.put(id, createCapability(name));
					taskdag.addCapability(name, id, idMap.get(id).inputArr(), idMap.get(id).outputArr());
				}else{
					resolveMap.put(id, resolve(name));//Recursive step, careful here
				}
			}
			
			xPathExpression = xpath.compile("//control");
			NodeList control = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);
			ArrayList<Integer> previous = new ArrayList<Integer>();
			if(control.item(0).getChildNodes().item(1).getAttributes().getNamedItem("type").getNodeValue().equals("sequence")){
				NodeList sequence = control.item(0).getChildNodes().item(1).getChildNodes();
				for(int i = 0; i< sequence.getLength(); i++){
					if(sequence.item(i).getNodeType() == Node.ELEMENT_NODE){
						if(sequence.item(i).getAttributes().getNamedItem("id")!=null){
							int id = Integer.parseInt(sequence.item(i).getAttributes().getNamedItem("id").getNodeValue());
							if(!resolveMap.containsKey(id)){
								if(previous.isEmpty()){
									if(!mappedID.containsKey(-1)){
										ArrayList<Integer> startID = new ArrayList<Integer>();
										startID.add(id);
										mappedID.put(-1, startID);
									}else{
										ArrayList<Integer> startID = mappedID.get(-1);
										startID.add(id);
										mappedID.put(-1, startID);
									}
									// Begin node of control?
								}else{
									for(int oldID : previous){
										if(!resolveMap.containsKey(oldID)){
											taskdag.connect(oldID, id);
										}else{
											for(int oldinternalID : resolveMap.get(oldID).get(-2)){
												taskdag.connect(oldinternalID, id);
											}
										}
									}
								}
								previous.clear();
								previous.add(id);
							}else{
								if(previous.isEmpty()){
									if(!mappedID.containsKey(-1)){
										ArrayList<Integer> startID = new ArrayList<Integer>();
										startID.addAll(resolveMap.get(id).get(-1));
										mappedID.put(-1, startID);
									}else{
										ArrayList<Integer> startID = mappedID.get(-1);
										startID.addAll(resolveMap.get(id).get(-1));
										mappedID.put(-1, startID);
									}
									// Begin node of control?
									previous.addAll(resolveMap.get(id).get(-2));
								}else{
									if(previous.size() > 1 && resolveMap.get(id).get(-1).size()>1){
										System.err.println("two parallels in indirect sequence with no sequence block in between!");
									}else{
										for(int newID : resolveMap.get(id).get(-1)){
											for(int older : previous){
												taskdag.connect(older, newID);
											}
										}
									}
								}
								previous.clear();
								previous.addAll(resolveMap.get(id).get(-2));
							}
						}else if(sequence.item(i).getAttributes().getNamedItem("type")!=null){
							NodeList parallel = sequence.item(i).getChildNodes();
							ArrayList<Integer> replacement = new ArrayList<Integer>();
							for(int k = 0; k<parallel.getLength(); k++){
								if(parallel.item(k).getNodeType() == Node.ELEMENT_NODE){
									System.out.println(parallel.item(k).getAttributes().getNamedItem("id").getNodeValue());
									int id = Integer.parseInt(parallel.item(k).getAttributes().getNamedItem("id").getNodeValue());
									if(previous.isEmpty()){
										if(!resolveMap.containsKey(id)){
											if(!mappedID.containsKey(-1)){
												ArrayList<Integer> startID = new ArrayList<Integer>();
												startID.add(id);
												mappedID.put(-1, startID);
											}else{
												ArrayList<Integer> startID = mappedID.get(-1);
												startID.add(id);
												mappedID.put(-1, startID);
											}
										}else{
											if(!mappedID.containsKey(-1)){
												ArrayList<Integer> startID = new ArrayList<Integer>();
												startID.addAll(resolveMap.get(id).get(-1));
												mappedID.put(-1, startID);
											}else{
												ArrayList<Integer> startID = mappedID.get(-1);
												startID.addAll(resolveMap.get(id).get(-1));
												mappedID.put(-1, startID);
											}
										}
									}else{
										if(previous.size() > 1){
											System.err.println("two parallels in direct/indirect sequence with no sequence block in between!");
										}else if(!resolveMap.containsKey(id)){
											taskdag.connect(previous.get(0),id);
										}else{
											for(int newid : resolveMap.get(id).get(-1)){
												taskdag.connect(previous.get(0),newid);
											}
										}
									}
									if(!resolveMap.containsKey(id)){
										replacement.add(id);
									}else{
										replacement.addAll(resolveMap.get(id).get(-2));
									}
								}
							}
							previous = replacement;
						}
					}
				}
				if(!mappedID.containsKey(-2)){
					mappedID.put(-2, previous);
				}else{
					ArrayList<Integer> startID = mappedID.get(-2);
					startID.addAll(previous);
					mappedID.put(-2, startID);
				}
			}else if(control.item(0).getChildNodes().item(1).getAttributes().getNamedItem("type").getNodeValue().equals("parallel")){
				NodeList parallel = control.item(0).getChildNodes().item(1).getChildNodes();
				for(int i = 0; i< parallel.getLength(); i++){
					if(parallel.item(i).getNodeType() == Node.ELEMENT_NODE){
						if(parallel.item(i).getAttributes().getNamedItem("id")!=null){
							int id = Integer.parseInt(parallel.item(i).getAttributes().getNamedItem("id").getNodeValue());
							if(!resolveMap.containsKey(id)){
								if(!mappedID.containsKey(-1)){
									ArrayList<Integer> startID = new ArrayList<Integer>();
									startID.add(id);
									mappedID.put(-1, startID);
								}else{
									ArrayList<Integer> startID = mappedID.get(-1);
									startID.add(id);
									mappedID.put(-1, startID);
								}
								if(!mappedID.containsKey(-2)){
									ArrayList<Integer> startID = new ArrayList<Integer>();
									startID.add(id);
									mappedID.put(-2, startID);
								}else{
									ArrayList<Integer> startID = mappedID.get(-2);
									startID.add(id);
									mappedID.put(-2, startID);
								}
							//Begin node of control
							}else{
								if(!mappedID.containsKey(-1)){
									ArrayList<Integer> startID = new ArrayList<Integer>();
									startID.addAll(resolveMap.get(id).get(-1));
									mappedID.put(-1, startID);
								}else{
									ArrayList<Integer> startID = mappedID.get(-1);
									startID.addAll(resolveMap.get(id).get(-1));
									mappedID.put(-1, startID);
								}
								if(!mappedID.containsKey(-2)){
									ArrayList<Integer> startID = new ArrayList<Integer>();
									startID.addAll(resolveMap.get(id).get(-2));
									mappedID.put(-2, startID);
								}else{
									ArrayList<Integer> startID = mappedID.get(-2);
									startID.addAll(resolveMap.get(id).get(-2));
									mappedID.put(-2, startID);
								}
							}
						}else if(parallel.item(i).getAttributes().getNamedItem("type")!=null){
							NodeList sequence = parallel.item(i).getChildNodes();
							for(int k = 0; k<sequence.getLength(); k++){
								if(sequence.item(k).getNodeType() == Node.ELEMENT_NODE){
									int id = Integer.parseInt(sequence.item(k).getAttributes().getNamedItem("id").getNodeValue());
									if(!resolveMap.containsKey(id)){
										if(previous.isEmpty()){
											if(!mappedID.containsKey(-1)){
												ArrayList<Integer> startID = new ArrayList<Integer>();
												startID.add(id);
												mappedID.put(-1, startID);
											}else{
												ArrayList<Integer> startID = mappedID.get(-1);
												startID.add(id);
												mappedID.put(-1, startID);
											}
											// begin node of control!
										}else{
											for(int previd : previous){
												taskdag.connect(previd,id);
											}
										}
										previous.clear();
										previous.add(id);
									}else{
										if(previous.isEmpty()){
											if(!mappedID.containsKey(-1)){
												ArrayList<Integer> startID = new ArrayList<Integer>();
												startID.addAll(resolveMap.get(id).get(-1));
												mappedID.put(-1, startID);
											}else{
												ArrayList<Integer> startID = mappedID.get(-1);
												startID.addAll(resolveMap.get(id).get(-1));
												mappedID.put(-1, startID);
											}
											previous.addAll(resolveMap.get(id).get(-2));
											// begin node of control!
										}else{
											if(previous.size()>1 && resolveMap.get(id).get(-1).size() > 1){
												System.err.println("In parallels, trying to connect many-to-many");
											}else{
												for(int newID : resolveMap.get(id).get(-1)){
													for(int older : previous){
														taskdag.connect(older, newID);
													}
												}
											}
												
											previous.clear();
											previous.addAll(resolveMap.get(id).get(-2));
										}
									}
								}
							}
							if(!mappedID.containsKey(-2)){
								mappedID.put(-2, previous);
							}else{
								ArrayList<Integer> startID = mappedID.get(-2);
								startID.addAll(previous);
								mappedID.put(-2, startID);
							}
						}
					}
				}
			}else{
				System.err.println("Neither sequence nor parallel in control block!");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mappedID;


	}
	
	private static Capability createCapability(String name){
		//Convert to lookup in capability database later
		ArrayList<Output> outputs =  new ArrayList<Output>();
		ArrayList<Input> inputs =  new ArrayList<Input>();

		try {
			Scanner in = new Scanner(new File("/Users/upsham/capabilities/"+name));
			while(in.hasNext()){
				String line = in.nextLine();
				String[] word = line.split(";");
				if(word[0].equals("Input")){
					inputs.add(new Input(word[1], capID++, word[2]));
				}else if(word[0].equals("Output")){
					outputs.add(new Output(word[1], capID++, word[2]));
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Capability(name, inputs, outputs);
		
	}
	private static boolean isCapability(String candidate){
		return true;
	}
	private static void printNote(NodeList nodeList) {

		for (int count = 0; count < nodeList.getLength(); count++) {

			Node tempNode = nodeList.item(count);

			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

				// get node name and value
				System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
				System.out.println("Node Value =" + tempNode.getTextContent());

				if (tempNode.hasAttributes()) {

					// get attributes names and values
					NamedNodeMap nodeMap = tempNode.getAttributes();

					for (int i = 0; i < nodeMap.getLength(); i++) {

						Node node = nodeMap.item(i);
						System.out.println("attr name : " + node.getNodeName());
						System.out.println("attr value : " + node.getNodeValue());

					}

				}

				if (tempNode.hasChildNodes()) {

					// loop again if has child nodes
					printNote(tempNode.getChildNodes());

				}

				System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");

			}

		}

	}

	private static class Capability{
		String name;
		ArrayList<Input> inputs;
		ArrayList<Output> outputs;
		
		public Capability(String name, ArrayList<Input> inputs, ArrayList<Output> outputs){
			this.name = name;
			this.inputs = inputs;
			this.outputs = outputs;
		}
		
		public Input[] inputArr(){
			Input [] arr = new Input[inputs.size()];
			for(int i = 0; i< inputs.size(); i++){
				arr[i] = inputs.get(i);
			}
			return arr;
		}
		
		public Output[] outputArr(){
			Output [] arr = new Output[outputs.size()];
			for(int i = 0; i< outputs.size(); i++){
				arr[i] = outputs.get(i);
			}
			return arr;
		}
	}
	private static class CapTaskID{
		public String name;
		public int id;
		
		public CapTaskID(String name, String  string){
			this.name = name;
			this.id = Integer.parseInt(string);
		}
	}
}
