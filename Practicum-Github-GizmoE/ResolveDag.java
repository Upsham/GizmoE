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
/**
 * Class ResolveDag resolve the Task XML into single units of execution, i.e. into capabilities. 
 * A task however maybe composed of other pre-defined tasks. Hence, the task resolver 
 * resolves/flattens a task recursively until it finds only the capabilities and stores 
 * them in the Task DAG that can be used by the Task Execution Module
 * @author ukd (implementation)
 * @author sindhusatish (documentation)
 * @see TaskDAGResolver
 */
public class ResolveDag {

  /**
	 * Creating an instance of MyDag
	 */
	private static MyDag taskdag = new MyDag();
	/**
	 * idMap has been designed to store the values of the type "capability", each 
	 * corresponding to a unique key, an internal capability id of type "Integer"
	 */
	private static HashMap<Integer, Capability> idMap = new HashMap<Integer, Capability>();
	/**
	 * resolveMap is a complex data structure that has been designed to store data 
	 * that would help go up the recursion of a task XML. In other words the inner 
	 * hashmap stores the strings of capabilities/tasks and the Task DAG Resolver 
	 * checks the data in this structure while performing recursive steps one level 
	 * above, i.e. when the task DAG Resolver goes down until it reaches a capability, 
	 * it will then go back one level up to find/make connections to this capability. 
	 * This is where resolveMap is used
	 */
	private static HashMap<Integer, HashMap<String, ArrayList<Integer>>> resolveMap = new HashMap<Integer, HashMap<String, ArrayList<Integer>>>();
	
	private static int ioidcounter = 2;
	/**
	 * overallIOMap has been designed to store the values of the type "integer", 
	 * each corresponding to a unique key, task of type "String"
	 */
	private static HashMap<String, Integer> overallIOMap = new HashMap<String, Integer>();
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
			//asigned to only one user...
			userInputList[0] = new Input("user",1,"user");
			//not required
			userOutputList[0] = new Output("user",0,"user");
			//capability id 0 is reserved for user dummpy capability
			taskdag.addCapability("userDummyCapability", 0, userInputList, userOutputList);
			HashMap<String, ArrayList<Integer>> start_end =  resolve("NewCombo", factory, true);//Map the overall outputs/inputs using hashmap returned
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
		HashMap<String, ArrayList<Integer>> start_end =  resolve(MainTaskName, factory, true);//Map the overall outputs/inputs using hashmap returned

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


	/**
	 * @param filename
	 * @param factory
	 * @param isRoot
	 * @return
	 */
	private static HashMap<String, ArrayList<Integer>> resolve(String filename, DocumentBuilderFactory factory, boolean isRoot){
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
					resolveMap.put(id, resolve(name, factory, false));//Recursive step, careful here
				}
			}
			/******************************
			 * Control Resolution
			 *****************************/
			if(isRoot){
				/**
				 * inputs and and the outputs for the overall task is stored in the overallIOMap
				 * as well as added into the Task DAG. This is done by parsing the inputs and 
				 * outputGroup blocks in the Task XML.
				 */
				NodeList data = doc.getElementsByTagName("inputs").item(0).getChildNodes();
				ArrayList<Input> in = new ArrayList<Input>();
				ArrayList<Output> out = new ArrayList<Output>();
				for(int i = 0; i<data.getLength(); i++){
					if(data.item(i).getNodeType()==Node.ELEMENT_NODE){
						String name = data.item(i).getAttributes().getNamedItem("name").getNodeValue();
						String type = data.item(i).getAttributes().getNamedItem("type").getNodeValue();
						if(data.item(i).getAttributes().getNamedItem("default")!=null){
							String defaul = data.item(i).getAttributes().getNamedItem("default").getNodeValue();
							overallIOMap.put(name, ioidcounter);
							in.add(new Input(name, ioidcounter++, type, defaul));
						}else{
							overallIOMap.put(name, ioidcounter);
							in.add(new Input(name, ioidcounter++, type));
						}
					}
				}
				/**
				 * the outputGroup of the task xml is read to retrieve output information
				 */
				data = doc.getElementsByTagName("outputGroup");
				for(int i = 0; i<data.getLength(); i++){
					String outgroup = data.item(i).getAttributes().getNamedItem("status").getNodeValue();
					NodeList internals = data.item(i).getChildNodes();
					for(int j = 0; j< internals.getLength(); j++){
						if(internals.item(j).getNodeType()==Node.ELEMENT_NODE){
							String name = internals.item(j).getAttributes().getNamedItem("name").getNodeValue();
							String type = internals.item(j).getAttributes().getNamedItem("type").getNodeValue();
							overallIOMap.put(name, ioidcounter);
							out.add(new Output(name, ioidcounter++, type, outgroup));
						}
					}
				}

				Input[] inputArr = new Input[in.size()];
				Output[] outputArr = new Output[out.size()];
				for(int i = 0; i < in.size(); i++){
					inputArr[i] = in.get(i);
				}
				for(int i = 0; i < out.size(); i++){
					outputArr[i] = out.get(i);
				}
				/**
				 * addOverallIO of MyDag interface is called for adding overall IO
				 */
				taskdag.addOverallIO(inputArr, outputArr);

			}
			
			/******************************
			 * Control Resolution
			 *****************************/
			xPathExpression = xpath.compile("//control");
			NodeList control = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);
			ArrayList<Integer> previous = new ArrayList<Integer>();
			/***************************************
			 * parallel within a sequential task
			 ***************************************/
			/**
			 * Checks whether the highest level task is a sequential one (parallel within a sequential task)
			 */
			if(control.item(0).getChildNodes().item(1).getAttributes().getNamedItem("type").getNodeValue().equals("sequence")){
				NodeList sequence = control.item(0).getChildNodes().item(1).getChildNodes();
				/**
				 * gets the child nodes (tasks or capabilities) and their ids
				 */
				for(int i = 0; i< sequence.getLength(); i++){
					if(sequence.item(i).getNodeType() == Node.ELEMENT_NODE){
						if(sequence.item(i).getAttributes().getNamedItem("id")!=null){
							int id = Integer.parseInt(sequence.item(i).getAttributes().getNamedItem("id").getNodeValue());
							/**
							 * if resolveMap doesnt contain the id read (capability/task id is not yet resolved)
							 */
							if(!resolveMap.containsKey(id)){
								if(previous.isEmpty()){
									/**
									 * mappedID doesnt contain a mapping for key -1 (overall inputs)
									 */
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
									/**
									 * check previous for capability ids that were mapped previously
									 */
									for(int oldID : previous){
										if(!resolveMap.containsKey(oldID)){
											taskdag.connect(oldID, id);
										}else{
											/**
											 * if resolveMap also contains ids of previous then connect all, since they
											 * are all capabilities
											 */
											for(int oldinternalID : resolveMap.get(oldID).get("-2")){
												taskdag.connect(oldinternalID, id);
											}
										}
									}
								}
								previous.clear();
								previous.add(id);
							}
							/**
							 * resolveMap contains id read
							 */
							else{
								/**
								 * previous is empty
								 */
								if(previous.isEmpty()){
									/**
									 * same as above
									 */
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
									/**
									 * two parallel capabilities in indirect sequence with 
									 * no sequence task/capability in between cannot be connect
									 * (many-to-many mapping doesn’t make sense)
									 */
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
						}
						/**
						 * checks if there is a parallel task/capability within the sequential task
						 */
						
						else if(sequence.item(i).getAttributes().getNamedItem("type")!=null){
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
										/**
										 * if previous contains more than one capability ids and the read capability ids are 
										 * also in parallel, then this connection is not possible.
										 * if the previous contains more than 1 capability ids and the resolveMap also has more 
										 * than 1 capability id’s at key “-1”, the resolver throws a system error message. 
										 * This is because; two parallel capabilities in indirect sequence with no sequence 
										 * task/capability in between cannot be connect (many-to-many mapping doesn’t make sense)
										 */
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
							/**
							 * updating previous
							 */
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
			
				
			}
			/***************************************
			 * sequence within a parallel task
			 ***************************************/
			/**
			 * checks whether the highest level task is a parallel one (sequential within a parallel task)
			 * similar to a seuqntial task
			 */
			else if(control.item(0).getChildNodes().item(1).getAttributes().getNamedItem("type").getNodeValue().equals("parallel")){
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
								//if there is a resolution...
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
						}
						/**
						 * check if there is a sequential task within the parallel task
						 */
						else if(parallel.item(i).getAttributes().getNamedItem("type")!=null){
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
				/**************************
				 * mode is pipe
				 *************************/
				if(mode.equalsIgnoreCase("pipe")){
					/**
					 * gets the fromCapID, toCapID, and fromIOName from the attributes “ref”, 
					 * “target”, “name” respectively
					 */
					NodeList mapping = data.item(i).getChildNodes();
					int fromCapID = Integer.parseInt(mapping.item(1).getAttributes().getNamedItem("ref").getNodeValue());
					String fromIOName = mapping.item(1).getAttributes().getNamedItem("name").getNodeValue();
					int toCapID = Integer.parseInt(mapping.item(3).getAttributes().getNamedItem("ref").getNodeValue());
					String toIOName = mapping.item(3).getAttributes().getNamedItem("name").getNodeValue();
					int fromIOID;
					ArrayList<Integer> toIOID = new ArrayList<Integer>();
					/**
					 * checks if the idMap has the fromCapID as a key. If it does it retrieves the fromIOName 
					 * from the idMap (idMap has data regarding the capability ids and the corresponding IO)
					 */
					if(idMap.containsKey(fromCapID)){
						fromIOID = idMap.get(fromCapID).ioLookup.get(fromIOName);
					}else{
						/**
						 * checks if the resolveMap has many from capabilities for same fromIOName and throws an 
						 * exception if it does. This is because; IO cannot have many to one mapping, as it doesn’t make any sense
						 */
						if(resolveMap.get(fromCapID).get(fromIOName).size()>1){
							throw new Exception("IO Mapping has many to one mapping (doesn't make sense)");
						}else{
							fromIOID = resolveMap.get(fromCapID).get(fromIOName).get(0);
						}
					}
					/**
					 * then checks if the idMap has toCapID as key. 
					 */
					if(idMap.containsKey(toCapID)){
						toIOID.add(idMap.get(toCapID).ioLookup.get(toIOName));
					}else{
						toIOID.addAll(resolveMap.get(toCapID).get(toIOName));
					}
					/**If it does, it maps the the IO to the to 
					 * and from capability ids by calling the mapIO() abstract method of MyDagInterface
					 */
					for(int to : toIOID){
						taskdag.mapIO(fromIOID, to, mode);
					}	
				}
				/**************************
				 * mode is copy
				 *************************/
				else if(mode.equalsIgnoreCase("copy")){
					NodeList mapping = data.item(i).getChildNodes();
                    /**
                     * if reference equal to null
                     */
					if(mapping.item(1).getAttributes().getNamedItem("ref")!=null){
						int fromCapID = Integer.parseInt(mapping.item(1).getAttributes().getNamedItem("ref").getNodeValue());
						String fromIOName = mapping.item(1).getAttributes().getNamedItem("name").getNodeValue();
						String toIOName = mapping.item(3).getAttributes().getNamedItem("name").getNodeValue();
						ArrayList<Integer> idArray = new ArrayList<Integer>();
                        /**
                         * checks if the fromCapID is a key in idMap
                         */
						if(idMap.containsKey(fromCapID)){
							/**
							 * adds to a data structure called idArray
							 */
							idArray.add(idMap.get(fromCapID).ioLookup.get(fromIOName));
						}else{
							/**
							 * checks if the resolveMap contains more than one capability id for a single fromIOName 
							 * and throws an exception if it is. This is because, more than one output cannot be 
							 * copied to a single task output
							 */
							if(resolveMap.get(fromCapID).get(fromIOName).size()>1){
								throw new Exception("Trying to map more than one outputs to one task output");
							}else{
								idArray.add(resolveMap.get(fromCapID).get(fromIOName).get(0));
							}
						}
						/**
						 * checks if toCapID is in idMap. If it is present then the resolver throws an exception. 
						 * This is because; an output is being remapped which is not semantically correct. 
						 * A task output can only have a single connection
						 */
						if(mappedID.containsKey(toIOName)){
							throw new Exception ("An output is being remapped, not valid. A task output can only have one connection");
						}else if(isRoot){
							if(idArray.size()==1){
								taskdag.mapIO(idArray.get(0), overallIOMap.get(toIOName), mode);
							}else{
								throw new Exception("Overall IO cannout be mapped correctly as one to many mapping is being tried");
							}
						}else{
							mappedID.put(toIOName, idArray);
						}
					}
					/**
					 * if reference not equal to null
					 */
					else if (mapping.item(3).getAttributes().getNamedItem("ref")!=null){
						String fromIOName = mapping.item(1).getAttributes().getNamedItem("name").getNodeValue();
						String toIOName = mapping.item(3).getAttributes().getNamedItem("name").getNodeValue();
						int toCapID = Integer.parseInt(mapping.item(3).getAttributes().getNamedItem("ref").getNodeValue());
						ArrayList<Integer> idArray;
                        /**
                         * checks if if the fromCapID and toCapID are present in the idMap as keys or not. 
                         * If fromIOCap is present, then the fromIOName is retrieved from mappedID and if it is 
                         * not then idArray is created. If toCapID is present in idMap then the resolver adds 
                         * it to idArray and if it is not then all the ids in resolveMap are added to idArray. 
                         * At this point the resolver checks if the the capability id is a root, if it is then 
                         * it maps the IO by calling mapIO() where the overall inputs are mapped to the root
                         */
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

						if(!isRoot){
							mappedID.put(fromIOName, idArray);
						}else{
							if(idArray.size()==1){
								taskdag.mapIO(overallIOMap.get(fromIOName), idArray.get(0), mode);
							}else{
								throw new Exception("Overall IO cannout be mapped correctly as one to many mapping is being tried");
							}
						}

					}else{
						throw new Exception("The mapping block has an invalid entry, with type 'copy'.");
					}
				}
				/**************************
				 * mode is user
				 *************************/
				else if(mode.equalsIgnoreCase("user")){
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
								throw new Exception("Trying to map more than one output to one task output");
							}else{
								idArray.add(resolveMap.get(fromCapID).get(fromIOName).get(0));
							}
						}
						if(mappedID.containsKey(toIOName)){
							throw new Exception ("An output is being remapped, not valid. A task output can only have one connection");
						}else if(isRoot){
							if(idArray.size()==1){
								taskdag.mapIO(idArray.get(0), overallIOMap.get(toIOName), mode);
							}else{
								throw new Exception("Overall IO cannout be mapped correctly as one to many mapping is being tried");
							}
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
						if(!isRoot){
							mappedID.put(fromIOName, idArray);
						}else{
							if(idArray.size()==1){
								taskdag.mapIO(overallIOMap.get(fromIOName), idArray.get(0), mode);
							}else{
								throw new Exception("Overall IO cannout be mapped correctly as one to many mapping is being tried");
							}
						}
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
	/**
	 * Checks if the string being passed is a capability name or not
	 * @param candidate
	 * @return
	 */
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
