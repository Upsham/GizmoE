package gizmoe.TaskDagResolver;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import gizmoe.taskdag.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ResolveDag {

	/**
	 * @param args
	 */
	private static MyDag taskdag = new MyDag();
	private static HashMap<Integer, Capability> idMap = new HashMap<Integer, Capability>();
	private static HashMap<Integer, HashMap<String, ArrayList<Integer>>> resolveMap = new HashMap<Integer, HashMap<String, ArrayList<Integer>>>();
	private static int ioidcounter = 2;
	
	/**********************************************************************************************************
	 * Only for BASIC TESTING (during developmeny). Please use the unit test method in devtest instead
	 **********************************************************************************************************/
	public static void main(String[] args) {
		try {

			//xpathsearch();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false); // never forget this!
			Input[] userInputList = new Input[1];
			Output[] userOutputList = new Output[1];
			userInputList[0] = new Input("user",1,"user");
			userOutputList[0] = new Output("user",0,"user");
			taskdag.addCapability("userDummyCapability", 0, userInputList, userOutputList);
			HashMap<String, ArrayList<Integer>> start_end =  resolve("NewCombo", factory);//Map the overall outputs/inputs using hashmap returned
			System.out.println("Start IDs:");
			for(int startid : start_end.get("-1")){
				System.out.println(startid);
			}
			System.out.println("End IDs:");
			for(int startid : start_end.get("-2")){
				System.out.println(startid);
			}
			printNextCap(16,taskdag);
			printNextCap(17,taskdag);
			printNextCap(10,taskdag);
			printNextCap(21,taskdag);
			printNextCap(12,taskdag);
			printNextCap(25,taskdag);
			printNextCap(26,taskdag);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	
	private static void printNextCap(int id, MyDag testdag){
		ArrayList<Integer> nextCap = testdag.nextCapabilities(id);
		ArrayList<Integer> joinCap = testdag.nextCapabilities(id);
		System.out.println("The nextcapability for "+id+" is:");
		for(int i : nextCap){
			System.out.println(i);
		}
		if(testdag.isJoin(id)){
			System.out.println("This is also a joining point! The following capabilities join at this point:");
			joinCap = testdag.joinToBecome(id);
			for(int i : joinCap){
				System.out.println(i);
			}
		}
		System.out.println();
	}
	
	public static MyDag TaskDagResolver(String MainTaskName){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false); // never forget this!
		
		/******************************
		 * Create dummy for user
		 *****************************/
		Input[] userInputList = new Input[1];
		Output[] userOutputList = new Output[1];
		userInputList[0] = new Input("user",1,"user");
		userOutputList[0] = new Output("user",0,"user");
		taskdag.addCapability("userDummyCapability", 0, userInputList, userOutputList);
		
		/******************************
		 * Recursively solve!
		 ******************************/
		HashMap<String, ArrayList<Integer>> start_end =  resolve(MainTaskName, factory);//Map the overall outputs/inputs using hashmap returned
		/*****************************
		 * Set start IDs
		 *****************************/
		//System.out.println("Start IDs:");
		for(int startid : start_end.get("-1")){
			//System.out.println(startid);
			taskdag.setStartCapability(startid);
		}
//		System.out.println("End IDs:");
//		for(int startid : start_end.get("-2")){
//			System.out.println(startid);
//		}
		
		return taskdag;
		
	}
	
	
	private static HashMap<String, ArrayList<Integer>> resolve(String filename, DocumentBuilderFactory factory){
		HashMap<String, ArrayList<Integer>> mappedID = new HashMap<String, ArrayList<Integer>>();
		try {
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream taskLoc = ResolveDag.class.getResourceAsStream("/gizmoe/devtest/TaskDagResolver/"+filename+".xml");
			Document doc = builder.parse(taskLoc);
			//System.out.println("Root element :" + doc.getDocumentElement().getAttribute("name"));

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
					//
				}*/
				if(isCapability(name)){
					idMap.put(id, createCapability(name));
					taskdag.addCapability(name, id, idMap.get(id).inputArr(), idMap.get(id).outputArr());
				}else{
					resolveMap.put(id, resolve(name, factory));//Recursive step, careful here
				}
			}
			/******************************
			 * Control Resolution
			 *****************************/
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
									if(!mappedID.containsKey("-1")){
										ArrayList<Integer> startID = new ArrayList<Integer>();
										startID.add(id);
										mappedID.put("-1", startID);
									}else{
										ArrayList<Integer> startID = mappedID.get("-1");
										startID.add(id);
										mappedID.put("-1", startID);
									}
									// Begin node of control?
								}else{
									for(int oldID : previous){
										if(!resolveMap.containsKey(oldID)){
											taskdag.connect(oldID, id);
										}else{
											for(int oldinternalID : resolveMap.get(oldID).get("-2")){
												taskdag.connect(oldinternalID, id);
											}
										}
									}
								}
								previous.clear();
								previous.add(id);
							}else{
								if(previous.isEmpty()){
									if(!mappedID.containsKey("-1")){
										ArrayList<Integer> startID = new ArrayList<Integer>();
										startID.addAll(resolveMap.get(id).get("-1"));
										mappedID.put("-1", startID);
									}else{
										ArrayList<Integer> startID = mappedID.get("-1");
										startID.addAll(resolveMap.get(id).get("-1"));
										mappedID.put("-1", startID);
									}
									// Begin node of control?
									previous.addAll(resolveMap.get(id).get("-2"));
								}else{
									if(previous.size() > 1 && resolveMap.get(id).get("-1").size()>1){
										System.err.println("two parallels in indirect sequence with no sequence block in between!");
									}else{
										for(int newID : resolveMap.get(id).get("-1")){
											for(int older : previous){
												taskdag.connect(older, newID);
											}
										}
									}
								}
								previous.clear();
								previous.addAll(resolveMap.get(id).get("-2"));
							}
						}else if(sequence.item(i).getAttributes().getNamedItem("type")!=null){
							NodeList parallel = sequence.item(i).getChildNodes();
							ArrayList<Integer> replacement = new ArrayList<Integer>();
							for(int k = 0; k<parallel.getLength(); k++){
								if(parallel.item(k).getNodeType() == Node.ELEMENT_NODE){
									//System.out.println(parallel.item(k).getAttributes().getNamedItem("id").getNodeValue());
									int id = Integer.parseInt(parallel.item(k).getAttributes().getNamedItem("id").getNodeValue());
									if(previous.isEmpty()){
										if(!resolveMap.containsKey(id)){
											if(!mappedID.containsKey("-1")){
												ArrayList<Integer> startID = new ArrayList<Integer>();
												startID.add(id);
												mappedID.put("-1", startID);
											}else{
												ArrayList<Integer> startID = mappedID.get("-1");
												startID.add(id);
												mappedID.put("-1", startID);
											}
										}else{
											if(!mappedID.containsKey("-1")){
												ArrayList<Integer> startID = new ArrayList<Integer>();
												startID.addAll(resolveMap.get(id).get("-1"));
												mappedID.put("-1", startID);
											}else{
												ArrayList<Integer> startID = mappedID.get("-1");
												startID.addAll(resolveMap.get(id).get("-1"));
												mappedID.put("-1", startID);
											}
										}
									}else{
										if(previous.size() > 1){
											System.err.println("two parallels in direct/indirect sequence with no sequence block in between!");
										}else if(!resolveMap.containsKey(id)){
											taskdag.connect(previous.get(0),id);
										}else{
											for(int newid : resolveMap.get(id).get("-1")){
												taskdag.connect(previous.get(0),newid);
											}
										}
									}
									if(!resolveMap.containsKey(id)){
										replacement.add(id);
									}else{
										replacement.addAll(resolveMap.get(id).get("-2"));
									}
								}
							}
							previous = replacement;
						}
					}
				}
				if(!mappedID.containsKey("-2")){
					mappedID.put("-2", previous);
				}else{
					ArrayList<Integer> startID = mappedID.get("-2");
					startID.addAll(previous);
					mappedID.put("-2", startID);
				}
			}else if(control.item(0).getChildNodes().item(1).getAttributes().getNamedItem("type").getNodeValue().equals("parallel")){
				NodeList parallel = control.item(0).getChildNodes().item(1).getChildNodes();
				for(int i = 0; i< parallel.getLength(); i++){
					if(parallel.item(i).getNodeType() == Node.ELEMENT_NODE){
						if(parallel.item(i).getAttributes().getNamedItem("id")!=null){
							int id = Integer.parseInt(parallel.item(i).getAttributes().getNamedItem("id").getNodeValue());
							if(!resolveMap.containsKey(id)){
								if(!mappedID.containsKey("-1")){
									ArrayList<Integer> startID = new ArrayList<Integer>();
									startID.add(id);
									mappedID.put("-1", startID);
								}else{
									ArrayList<Integer> startID = mappedID.get("-1");
									startID.add(id);
									mappedID.put("-1", startID);
								}
								if(!mappedID.containsKey("-2")){
									ArrayList<Integer> startID = new ArrayList<Integer>();
									startID.add(id);
									mappedID.put("-2", startID);
								}else{
									ArrayList<Integer> startID = mappedID.get("-2");
									startID.add(id);
									mappedID.put("-2", startID);
								}
							//Begin node of control
							}else{
								if(!mappedID.containsKey("-1")){
									ArrayList<Integer> startID = new ArrayList<Integer>();
									startID.addAll(resolveMap.get(id).get("-1"));
									mappedID.put("-1", startID);
								}else{
									ArrayList<Integer> startID = mappedID.get("-1");
									startID.addAll(resolveMap.get(id).get("-1"));
									mappedID.put("-1", startID);
								}
								if(!mappedID.containsKey("-2")){
									ArrayList<Integer> startID = new ArrayList<Integer>();
									startID.addAll(resolveMap.get(id).get("-2"));
									mappedID.put("-2", startID);
								}else{
									ArrayList<Integer> startID = mappedID.get("-2");
									startID.addAll(resolveMap.get(id).get("-2"));
									mappedID.put("-2", startID);
								}
							}
						}else if(parallel.item(i).getAttributes().getNamedItem("type")!=null){
							NodeList sequence = parallel.item(i).getChildNodes();
							for(int k = 0; k<sequence.getLength(); k++){
								if(sequence.item(k).getNodeType() == Node.ELEMENT_NODE){
									int id = Integer.parseInt(sequence.item(k).getAttributes().getNamedItem("id").getNodeValue());
									if(!resolveMap.containsKey(id)){
										if(previous.isEmpty()){
											if(!mappedID.containsKey("-1")){
												ArrayList<Integer> startID = new ArrayList<Integer>();
												startID.add(id);
												mappedID.put("-1", startID);
											}else{
												ArrayList<Integer> startID = mappedID.get("-1");
												startID.add(id);
												mappedID.put("-1", startID);
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
											if(!mappedID.containsKey("-1")){
												ArrayList<Integer> startID = new ArrayList<Integer>();
												startID.addAll(resolveMap.get(id).get("-1"));
												mappedID.put("-1", startID);
											}else{
												ArrayList<Integer> startID = mappedID.get("-1");
												startID.addAll(resolveMap.get(id).get("-1"));
												mappedID.put("-1", startID);
											}
											previous.addAll(resolveMap.get(id).get("-2"));
											// begin node of control!
										}else{
											if(previous.size()>1 && resolveMap.get(id).get("-1").size() > 1){
												System.err.println("In parallels, trying to connect many-to-many");
											}else{
												for(int newID : resolveMap.get(id).get("-1")){
													for(int older : previous){
														taskdag.connect(older, newID);
													}
												}
											}
												
											previous.clear();
											previous.addAll(resolveMap.get(id).get("-2"));
										}
									}
								}
							}
							if(!mappedID.containsKey("-2")){
								mappedID.put("-2", previous);
							}else{
								ArrayList<Integer> startID = mappedID.get("-2");
								startID.addAll(previous);
								mappedID.put("-2", startID);
							}
						}
					}
				}
			}else{
				System.err.println("Neither sequence nor parallel in control block!");
			}
			
			/**************************
			 * IO (dataflow) Resolution
			 *************************/
			NodeList data = doc.getElementsByTagName("mapping");
			for(int i = 0; i<data.getLength(); i++){
				String mode = data.item(i).getAttributes().getNamedItem("mode").getNodeValue();
				
//				System.out.println("Mode: "+mode);
				if(mode.equalsIgnoreCase("pipe")){
					NodeList mapping = data.item(i).getChildNodes();
					int fromCapID = Integer.parseInt(mapping.item(1).getAttributes().getNamedItem("ref").getNodeValue());
					String fromIOName = mapping.item(1).getAttributes().getNamedItem("name").getNodeValue();
					int toCapID = Integer.parseInt(mapping.item(3).getAttributes().getNamedItem("ref").getNodeValue());
					String toIOName = mapping.item(3).getAttributes().getNamedItem("name").getNodeValue();
					int fromIOID;
					ArrayList<Integer> toIOID = new ArrayList<Integer>();
					if(idMap.containsKey(fromCapID)){
						fromIOID = idMap.get(fromCapID).ioLookup.get(fromIOName);
					}else{
						if(resolveMap.get(fromCapID).get(fromIOName).size()>1){
							throw new Exception("IO Mapping has many to one mapping (doesn't make sense)");
						}else{
							fromIOID = resolveMap.get(fromCapID).get(fromIOName).get(0);
						}
					}
					if(idMap.containsKey(toCapID)){
						toIOID.add(idMap.get(toCapID).ioLookup.get(toIOName));
					}else{
						toIOID.addAll(resolveMap.get(toCapID).get(toIOName));
					}
					for(int to : toIOID){
						taskdag.mapIO(fromIOID, to, mode);
					}	
				}else if(mode.equalsIgnoreCase("copy")){
					NodeList mapping = data.item(i).getChildNodes();

					if(mapping.item(1).getAttributes().getNamedItem("ref")!=null){
						int fromCapID = Integer.parseInt(mapping.item(1).getAttributes().getNamedItem("ref").getNodeValue());
						String fromIOName = mapping.item(1).getAttributes().getNamedItem("name").getNodeValue();
						String toIOName = mapping.item(3).getAttributes().getNamedItem("name").getNodeValue();
						ArrayList<Integer> idArray = new ArrayList<Integer>();
						
						if(idMap.containsKey(fromCapID)){
							idArray.add(idMap.get(fromCapID).ioLookup.get(fromIOName));
						}else{
							if(resolveMap.get(fromCapID).get(fromIOName).size()>1){
								throw new Exception("Trying to map more than one outputs to one task output");
							}else{
								idArray.add(resolveMap.get(fromCapID).get(fromIOName).get(0));
							}
						}
						if(mappedID.containsKey(toIOName)){
							throw new Exception ("An output is being remapped, not valid. A task output can only have one connection");
						}else{
							mappedID.put(toIOName, idArray);
						}
					}else if (mapping.item(3).getAttributes().getNamedItem("ref")!=null){
						String fromIOName = mapping.item(1).getAttributes().getNamedItem("name").getNodeValue();
						String toIOName = mapping.item(3).getAttributes().getNamedItem("name").getNodeValue();
						int toCapID = Integer.parseInt(mapping.item(3).getAttributes().getNamedItem("ref").getNodeValue());
						ArrayList<Integer> idArray;

						if(mappedID.containsKey(fromIOName)){
							idArray = mappedID.get(fromIOName);
						}else{
							idArray = new ArrayList<Integer>();
						}
						
						if(idMap.containsKey(toCapID)){
							idArray.add(idMap.get(toCapID).ioLookup.get(toIOName));
						}else{
								idArray.addAll(resolveMap.get(toCapID).get(toIOName));
						}	
						mappedID.put(fromIOName, idArray);

					}else{
						throw new Exception("The mapping block has an invalid entry, with type 'copy'.");
					}
				}else if(mode.equalsIgnoreCase("user")){
					//TODO what to do with user??
					NodeList mapping = data.item(i).getChildNodes();
					String prompt = mapping.item(1).getAttributes().getNamedItem("string").getNodeValue();
					String toIOName = mapping.item(3).getAttributes().getNamedItem("name").getNodeValue();
					int toCapID = Integer.parseInt(mapping.item(3).getAttributes().getNamedItem("ref").getNodeValue());
					if(idMap.containsKey(toCapID)){
						taskdag.mapIO(0, idMap.get(toCapID).ioLookup.get(toIOName), mode+"::"+prompt);
					}else{
						for(int ioid : resolveMap.get(toCapID).get(toIOName)){
							taskdag.mapIO(0, ioid, mode+"::"+prompt);
						}
					}
				}else{
					NodeList mapping = data.item(i).getChildNodes();
					if(mapping.item(3).getAttributes().getNamedItem("ref")==null){
						int fromCapID = Integer.parseInt(mapping.item(1).getAttributes().getNamedItem("ref").getNodeValue());
						String fromIOName = mapping.item(1).getAttributes().getNamedItem("name").getNodeValue();
						String toIOName = mapping.item(3).getAttributes().getNamedItem("name").getNodeValue();
						ArrayList<Integer> idArray = new ArrayList<Integer>();
						
						if(idMap.containsKey(fromCapID)){
							idArray.add(idMap.get(fromCapID).ioLookup.get(fromIOName));
						}else{
							if(resolveMap.get(fromCapID).get(fromIOName).size()>1){
								throw new Exception("Trying to map more than one outputs to one task output");
							}else{
								idArray.add(resolveMap.get(fromCapID).get(fromIOName).get(0));
							}
						}
						if(mappedID.containsKey(toIOName)){
							throw new Exception ("An output is being remapped, not valid. A task output can only have one connection");
						}else{
							mappedID.put(toIOName, idArray);
						}
					}else if (mapping.item(1).getAttributes().getNamedItem("ref")==null){
						String fromIOName = mapping.item(1).getAttributes().getNamedItem("name").getNodeValue();
						String toIOName = mapping.item(3).getAttributes().getNamedItem("name").getNodeValue();
						int toCapID = Integer.parseInt(mapping.item(3).getAttributes().getNamedItem("ref").getNodeValue());
						ArrayList<Integer> idArray;

						if(mappedID.containsKey(fromIOName)){
							idArray = mappedID.get(fromIOName);
						}else{
							idArray = new ArrayList<Integer>();
						}
						
						if(idMap.containsKey(toCapID)){
							idArray.add(idMap.get(toCapID).ioLookup.get(toIOName));
						}else{
								idArray.addAll(resolveMap.get(toCapID).get(toIOName));
						}	
						mappedID.put(fromIOName, idArray);

					}else{
						int fromCapID = Integer.parseInt(mapping.item(1).getAttributes().getNamedItem("ref").getNodeValue());
						String fromIOName = mapping.item(1).getAttributes().getNamedItem("name").getNodeValue();
						int toCapID = Integer.parseInt(mapping.item(3).getAttributes().getNamedItem("ref").getNodeValue());
						String toIOName = mapping.item(3).getAttributes().getNamedItem("name").getNodeValue();
						int fromIOID;
						ArrayList<Integer> toIOID = new ArrayList<Integer>();
						if(idMap.containsKey(fromCapID)){
							fromIOID = idMap.get(fromCapID).ioLookup.get(fromIOName);
						}else{
							if(resolveMap.get(fromCapID).get(fromIOName).size()>1){
								throw new Exception("IO Mapping has many to one mapping (doesn't make sense)");
							}else{
								fromIOID = resolveMap.get(fromCapID).get(fromIOName).get(0);
							}
						}
						if(idMap.containsKey(toCapID)){
							toIOID.add(idMap.get(toCapID).ioLookup.get(toIOName));
						}else{
							toIOID.addAll(resolveMap.get(toCapID).get(toIOName));
						}
						for(int to : toIOID){
							taskdag.mapIO(fromIOID, to, mode);
						}	
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mappedID;


	}
	
	private static Capability createCapability(String name){
		//Convert to lookup in capability database later
		ArrayList<Output> outputs =  new ArrayList<Output>();
		ArrayList<Input> inputs =  new ArrayList<Input>();

		InputStream input = ResolveDag.class.getResourceAsStream("/gizmoe/devtest/TaskDagResolver/"+name);
		Scanner in = new Scanner(input);
		while(in.hasNext()){
			String line = in.nextLine();
			String[] word = line.split(";");
			if(word[0].equals("Input")){
				inputs.add(new Input(word[1],ioidcounter++, word[2]));
			}else if(word[0].equals("Output")){
				outputs.add(new Output(word[1], ioidcounter++, word[2]));
			}
		}
		return new Capability(name, inputs, outputs);
		
	}
	private static boolean isCapability(String candidate){
		if(candidate.startsWith("Task")){
			return false;
		}else{
			return true;
		}
	}
	
	@SuppressWarnings("unused")
	private static void xpathsearch(){
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false); // never forget this!
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream taskLoc = ResolveDag.class.getResourceAsStream("/gizmoe/devtest/TaskDagResolver/"+"NewCombo"+".xml");
			Document doc = builder.parse(taskLoc);

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
			e.printStackTrace();
		}
	}
	
	private static class Capability{
		@SuppressWarnings("unused")
		String name;
		ArrayList<Input> inputs;
		ArrayList<Output> outputs;
		HashMap <String, Integer> ioLookup = new HashMap<String, Integer>();
		//HashMap <String, Integer> outputLookup = new HashMap<String, Integer>();

		public Capability(String name, ArrayList<Input> inputs, ArrayList<Output> outputs){
			this.name = name;
			this.inputs = inputs;
			for(Input in : inputs){
				ioLookup.put(in.name, in.id);
			}
			for(Output out  : outputs){
				ioLookup.put(out.name, out.id);
			}
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
}
